package org.egov.pt.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.Assessment;
import org.egov.pt.models.Assessment.Source;
import org.egov.pt.models.Demand;
import org.egov.pt.models.DemandDetail;
import org.egov.pt.models.DemandSearchCriteria;
import org.egov.pt.models.OwnerInfo;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.enums.Channel;
import org.egov.pt.models.enums.CreationReason;
import org.egov.pt.models.enums.Status;
import org.egov.pt.models.user.UserDetailResponse;
import org.egov.pt.models.user.UserSearchRequest;
import org.egov.pt.models.workflow.State;
import org.egov.pt.producer.Producer;
import org.egov.pt.repository.PropertyRepository;
import org.egov.pt.repository.ServiceRequestRepository;
import org.egov.pt.util.CommonUtils;
import org.egov.pt.util.ErrorConstants;
import org.egov.pt.util.PTConstants;
import org.egov.pt.util.PropertyUtil;
import org.egov.pt.validator.PropertyValidator;
import org.egov.pt.web.contracts.AssessmentRequest;
import org.egov.pt.web.contracts.DemandResponse;
import org.egov.pt.web.contracts.PropertyRequest;
import org.egov.pt.web.contracts.RequestInfoWrapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.parser.JSONParser;

@Service
public class PropertyService {

    @Autowired
    private Producer producer;

    @Autowired
    private PropertyConfiguration config;

    @Autowired
    private PropertyRepository repository;

    @Autowired
    private EnrichmentService enrichmentService;

    @Autowired
    private PropertyValidator propertyValidator;

    @Autowired
    private UserService userService;

    @Autowired
	private WorkflowService wfService;
    
    @Autowired
    private PropertyUtil util;
    
    @Autowired
    private ObjectMapper mapper;
    
    @Autowired
	private CalculationService calculatorService;
    
    @Autowired
    private ServiceRequestRepository serviceRepository;
    
    @Autowired
    private AssessmentService assessmentService;
    
    
	/**
	 * Enriches the Request and pushes to the Queue
	 *
	 * @param request PropertyRequest containing list of properties to be created
	 * @return List of properties successfully created
	 */
	public Property createProperty(PropertyRequest request) {

		propertyValidator.validateCreateRequest(request);
		enrichmentService.enrichCreateRequest(request);
		userService.createUser(request);
		if (config.getIsWorkflowEnabled()
				&& !request.getProperty().getCreationReason().equals(CreationReason.DATA_UPLOAD)
				&& !"LEGACY_RECORD".equals(request.getProperty().getSource().toString())) {
			wfService.updateWorkflow(request, request.getProperty().getCreationReason());

		} else {

			request.getProperty().setStatus(Status.ACTIVE);
		}

		producer.push(config.getSavePropertyTopic(), request);
		request.getProperty().setWorkflow(null);
		return request.getProperty();
	}
	
	/**
	 * Updates the property
	 * 
	 * handles multiple processes 
	 * 
	 * Update
	 * 
	 * Mutation
	 *
	 * @param request PropertyRequest containing list of properties to be update
	 * @return List of updated properties
	 */
	public Property updateProperty(PropertyRequest request) {

		Property propertyFromSearch = propertyValidator.validateCommonUpdateInformation(request);
		
		boolean isRequestForOwnerMutation = CreationReason.MUTATION.equals(request.getProperty().getCreationReason());
		
		if (isRequestForOwnerMutation)
			processOwnerMutation(request, propertyFromSearch);
		else
			processPropertyUpdate(request, propertyFromSearch);

		request.getProperty().setWorkflow(null);
		return request.getProperty();
	}

	/**
	 * Method to process Property update 
	 * 
	 * @param request
	 * @param propertyFromSearch
	 */
	private void processPropertyUpdate(PropertyRequest request, Property propertyFromSearch) {
		
		propertyValidator.validateRequestForUpdate(request, propertyFromSearch);
		if (CreationReason.CREATE.equals(request.getProperty().getCreationReason())) {	
			userService.createUser(request);	
		} else {	
			request.getProperty().setOwners(util.getCopyOfOwners(propertyFromSearch.getOwners()));	
		}
		enrichmentService.enrichAssignes(request.getProperty());
		enrichmentService.enrichUpdateRequest(request, propertyFromSearch);
		
		PropertyRequest OldPropertyRequest = PropertyRequest.builder()
				.requestInfo(request.getRequestInfo())
				.property(propertyFromSearch)
				.build();
		
		util.mergeAdditionalDetails(request, propertyFromSearch);
		
		if(config.getIsWorkflowEnabled() && ! "LEGACY_RECORD".equals(request.getProperty().getSource().toString())) {
			
			State state = wfService.updateWorkflow(request, CreationReason.UPDATE);

			if (state.getIsStartState() == true
					&& state.getApplicationStatus().equalsIgnoreCase(Status.INWORKFLOW.toString())
					&& !propertyFromSearch.getStatus().equals(Status.INWORKFLOW)) {

				propertyFromSearch.setStatus(Status.INACTIVE);
				producer.push(config.getUpdatePropertyTopic(), OldPropertyRequest);
				util.saveOldUuidToRequest(request, propertyFromSearch.getId());
				producer.push(config.getSavePropertyTopic(), request);

			} else if (state.getIsTerminateState()
					&& !state.getApplicationStatus().equalsIgnoreCase(Status.ACTIVE.toString())) {

				terminateWorkflowAndReInstatePreviousRecord(request, propertyFromSearch);
			}else {
				/*
				 * If property is In Workflow then continue
				 */
				producer.push(config.getUpdatePropertyTopic(), request);
			}

		} else {

			/*
			 * If no workflow then update property directly with mutation information
			 */
			producer.push(config.getUpdatePropertyTopic(), request);
		}
	}

	/**
	 * method to process owner mutation
	 * 
	 * @param request
	 * @param propertyFromSearch
	 */
	private void processOwnerMutation(PropertyRequest request, Property propertyFromSearch) {
		
		propertyValidator.validateMutation(request, propertyFromSearch);
		userService.createUserForMutation(request, !propertyFromSearch.getStatus().equals(Status.INWORKFLOW));		enrichmentService.enrichAssignes(request.getProperty());
		enrichmentService.enrichMutationRequest(request, propertyFromSearch);
		calculatorService.calculateMutationFee(request.getRequestInfo(), request.getProperty());
		
		// TODO FIX ME block property changes FIXME
		util.mergeAdditionalDetails(request, propertyFromSearch);
		PropertyRequest oldPropertyRequest = PropertyRequest.builder()
				.requestInfo(request.getRequestInfo())
				.property(propertyFromSearch)
				.build();
		
		if (config.getIsMutationWorkflowEnabled()) {

			State state = wfService.updateWorkflow(request, CreationReason.MUTATION);
      
			/*
			 * updating property from search to INACTIVE status
			 * 
			 * to create new entry for new Mutation
			 */
			if (state.getIsStartState() == true
					&& state.getApplicationStatus().equalsIgnoreCase(Status.INWORKFLOW.toString())
					&& !propertyFromSearch.getStatus().equals(Status.INWORKFLOW)) {
				
				propertyFromSearch.setStatus(Status.INACTIVE);
				producer.push(config.getUpdatePropertyTopic(), oldPropertyRequest);

				util.saveOldUuidToRequest(request, propertyFromSearch.getId());
				/* save new record */
				producer.push(config.getSavePropertyTopic(), request);

			} else if (state.getIsTerminateState()
					&& !state.getApplicationStatus().equalsIgnoreCase(Status.ACTIVE.toString())) {

				terminateWorkflowAndReInstatePreviousRecord(request, propertyFromSearch);
			} else {
				/*
				 * If property is In Workflow then continue
				 */
				producer.push(config.getUpdatePropertyTopic(), request);
			}

		} else {

			/*
			 * If no workflow then update property directly with mutation information
			 */
			producer.push(config.getUpdatePropertyTopic(), request);
		}
	}

	private void terminateWorkflowAndReInstatePreviousRecord(PropertyRequest request, Property propertyFromSearch) {
		
		/* current record being rejected */
		producer.push(config.getUpdatePropertyTopic(), request);
		
		/* Previous record set to ACTIVE */
		@SuppressWarnings("unchecked")
		Map<String, Object> additionalDetails = mapper.convertValue(propertyFromSearch.getAdditionalDetails(), Map.class);
		if(null == additionalDetails) 
			return;
		
		String propertyUuId = (String) additionalDetails.get(PTConstants.PREVIOUS_PROPERTY_PREVIOUD_UUID);
		if(StringUtils.isEmpty(propertyUuId)) 
			return;
		
		PropertyCriteria criteria = PropertyCriteria.builder().uuids(Sets.newHashSet(propertyUuId))
				.tenantId(propertyFromSearch.getTenantId()).build();
		Property previousPropertyToBeReInstated = searchProperty(criteria, request.getRequestInfo()).get(0);
		previousPropertyToBeReInstated.setAuditDetails(util.getAuditDetails(request.getRequestInfo().getUserInfo().getUuid().toString(), true));
		previousPropertyToBeReInstated.setStatus(Status.ACTIVE);
		request.setProperty(previousPropertyToBeReInstated);
		
		producer.push(config.getUpdatePropertyTopic(), request);
	}

    /**
     * Search property with given PropertyCriteria
     *
     * @param criteria PropertyCriteria containing fields on which search is based
     * @return list of properties satisfying the containing fields in criteria
     */
	public List<Property> searchProperty(PropertyCriteria criteria, RequestInfo requestInfo) {

		List<Property> properties;

		/*
		 * throw error if audit request is with no proeprty id or multiple propertyids
		 */
		if (criteria.isAudit() && (CollectionUtils.isEmpty(criteria.getPropertyIds())
				|| (!CollectionUtils.isEmpty(criteria.getPropertyIds()) && criteria.getPropertyIds().size() > 1))) {

			throw new CustomException("EG_PT_PROPERTY_AUDIT_ERROR", "Audit can only be provided for a single propertyId");
		}

		if (criteria.getMobileNumber() != null || criteria.getName() != null || criteria.getOwnerIds() != null) {

			/* converts owner information to associated property ids */
			Boolean shouldReturnEmptyList = repository.enrichCriteriaFromUser(criteria, requestInfo);

			if (shouldReturnEmptyList)
				return Collections.emptyList();

			properties = repository.getPropertiesWithOwnerInfo(criteria, requestInfo, false);
		} else {
			properties = repository.getPropertiesWithOwnerInfo(criteria, requestInfo, false);
		}

		properties.forEach(property -> {
			enrichmentService.enrichBoundary(property, requestInfo);
		});
		
		return properties;
	}

	public List<Property> searchPropertyPlainSearch(PropertyCriteria criteria, RequestInfo requestInfo) {
		List<Property> properties = getPropertiesPlainSearch(criteria, requestInfo);
		for(Property property:properties)
			enrichmentService.enrichBoundary(property,requestInfo);
		return properties;
	}


	List<Property> getPropertiesPlainSearch(PropertyCriteria criteria, RequestInfo requestInfo) {
		if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit())
			criteria.setLimit(config.getMaxSearchLimit());
		if(criteria.getLimit()==null)
			criteria.setLimit(config.getDefaultLimit());
		if(criteria.getOffset()==null)
			criteria.setOffset(config.getDefaultOffset());
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		if (criteria.getUuids() != null || criteria.getPropertyIds() != null) {
			if (criteria.getUuids() != null)
				propertyCriteria.setUuids(criteria.getUuids());
			if (criteria.getPropertyIds() != null)
				propertyCriteria.setPropertyIds(criteria.getPropertyIds());

		} else {
			List<String> uuids = repository.fetchIds(criteria);
			if (uuids.isEmpty())
				return Collections.emptyList();
			propertyCriteria.setUuids(new HashSet<>(uuids));
		}
		propertyCriteria.setLimit(criteria.getLimit());
		List<Property> properties = repository.getPropertiesForBulkSearch(propertyCriteria);
		if(properties.isEmpty())
			return Collections.emptyList();
		Set<String> ownerIds = properties.stream().map(Property::getOwners).flatMap(List::stream)
				.map(OwnerInfo::getUuid).collect(Collectors.toSet());

		UserSearchRequest userSearchRequest = userService.getBaseUserSearchRequest(criteria.getTenantId(), requestInfo);
		userSearchRequest.setUuid(ownerIds);
		UserDetailResponse userDetailResponse = userService.getUser(userSearchRequest);
		util.enrichOwner(userDetailResponse, properties, false);
		return properties;
	}

	public void intiateRollOver(String tenantid, List<Property> properties, RequestInfo requestInfo) {

		List<Map<String, Object>> propsForRollOver = repository.fetchPropertiesForRollOver(tenantid);

		properties = getPropertiesForRollOver(propsForRollOver, properties);
		
		List<Map<String, Object>> finYears = getMasterFinancialYearData(tenantid,requestInfo);
		
		if (!CollectionUtils.isEmpty(properties)) {
			for (Property property : properties) {

				DemandSearchCriteria criteria = new DemandSearchCriteria();
				criteria.setTenantId(property.getTenantId());
				criteria.setPropertyId(property.getPropertyId());

				List<Demand> demands = new ArrayList<Demand>();
				DemandResponse res = mapper.convertValue(serviceRepository.fetchResult(
						util.getDemandSearchUrl(criteria), new RequestInfoWrapper(requestInfo)).get(), DemandResponse.class);

				if (!CollectionUtils.isEmpty(res.getDemands())) {
					demands.addAll(res.getDemands());

					Long maxFromDate = demands.stream().max(Comparator.comparing((Demand::getTaxPeriodFrom))).get()
							.getTaxPeriodFrom();
					Long maxToDate = demands.stream().max(Comparator.comparing((Demand::getTaxPeriodTo))).get()
							.getTaxPeriodTo();
					
					if ( maxFromDate.equals(finYears.stream().filter(finYear -> finYear.get("code").equals("2020-21")).collect(Collectors.toList()).get(0).get("startingDate"))
							&& maxToDate.equals(finYears.stream().filter(finYear -> finYear.get("code").equals("2020-21")).collect(Collectors.toList()).get(0).get("endingDate"))) {
						Demand demand = demands.stream()
								.filter(dmnd -> dmnd.getTaxPeriodFrom().equals(finYears.stream().filter(finYear -> finYear.get("code").equals("2020-21")).collect(Collectors.toList()).get(0).get("startingDate"))
										&& dmnd.getTaxPeriodTo().equals(finYears.stream().filter(finYear -> finYear.get("code").equals("2020-21")).collect(Collectors.toList()).get(0).get("endingDate"))).collect(Collectors.toList()).get(0);
						List<Demand> newDemands = prepareDemandRequest(demand,finYears);
						Assessment assessment = createAssessmentForRollOver(newDemands, requestInfo, property);
						if (!Objects.isNull(assessment))
							repository.saveRollOver(property, "2021-22", "SUCCESS", "Roll Over is Successfully Done");
						else
							repository.saveRollOver(property, "2021-22", "FAILED",
									"Assessment or Demand Creation Failed");
					} else {
						repository.saveRollOver(property, "2021-22", "NOTINITIATED",
								"No Demand found for the Current Financial Year");
					}
				}
			}
		} else {
			throw new CustomException("EG_PT_PROPERTY_ROLLOVER_ERROR",
					"No Properties found for roll over in given criteria");
		}
	}

	private List<Map<String, Object>> getMasterFinancialYearData(String tenantid, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder(config.getMdmsHost()).append(config.getMdmsEndpoint());
        List<MasterDetail> masterDetails = new ArrayList<>();
        masterDetails.add(MasterDetail.builder().name("FinancialYear").build());
        List<ModuleDetail> moduleDetails = new ArrayList<>();
        moduleDetails.add(ModuleDetail.builder().moduleName("egf-master").masterDetails(masterDetails).build());
        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId("uk").moduleDetails(moduleDetails).build();
        MdmsCriteriaReq req = MdmsCriteriaReq.builder().requestInfo(new RequestInfo()).mdmsCriteria(mdmsCriteria).build();
        
        try {
            Optional<Object> result = serviceRepository.fetchResult(uri, req);
            return JsonPath.read(result.get(),"$.MdmsRes.egf-master.FinancialYear");
        } catch (Exception e) {
            throw new CustomException(ErrorConstants.INVALID_TENANT_ID_MDMS_KEY,
                    ErrorConstants.INVALID_TENANT_ID_MDMS_MSG);
        }
		
	}

	private List<Property> getPropertiesForRollOver(List<Map<String, Object>> propsForRollOver,
			List<Property> properties) {
			if(!CollectionUtils.isEmpty(propsForRollOver)){
				propsForRollOver.stream().filter(prop -> prop.get("status") != "SUCCESS").collect(Collectors.toList());
				properties.stream().filter(property -> propsForRollOver.contains(property.getPropertyId())).collect(Collectors.toList());
				return properties;
			}
			else
				return properties;
	}

	private Assessment createAssessmentForRollOver(List<Demand> newDemands, RequestInfo requestInfo, Property property) {
		Assessment assessment = Assessment.builder().assessmentDate(ZonedDateTime.now().toInstant().toEpochMilli()).financialYear("2021-22").channel(Channel.CFC_COUNTER).source(Source.LEGACY_RECORD).tenantId(property.getTenantId()).propertyId(property.getPropertyId()).build();
		ObjectNode additionalDetails = new ObjectMapper().createObjectNode();
		assessment.setAdditionalDetails(additionalDetails.set("Demands", new ObjectMapper().valueToTree(newDemands)));
		assessment.setAdditionalDetails(additionalDetails.set("RequestInfo", new ObjectMapper().valueToTree(requestInfo)));
		assessment.setAdditionalDetails(additionalDetails.set("isRollOver", new ObjectMapper().valueToTree(true)));
		AssessmentRequest request = AssessmentRequest.builder().requestInfo(requestInfo).assessment(assessment).build();
		
		return assessmentService.createLegacyAssessments(request);
		
	}

	private List<Demand> prepareDemandRequest(Demand demand, List<Map<String, Object>> finYears) {
		List<Demand> newDemands = new ArrayList<Demand>();
		List<DemandDetail> details = demand.getDemandDetails().stream().filter(dmnddtls -> dmnddtls.getTaxHeadMasterCode().equals("PT_TAX") || dmnddtls.getTaxHeadMasterCode().equals("SWATCHATHA_TAX")).collect(Collectors.toList());
		List<DemandDetail> newDetails = new ArrayList<DemandDetail>();
		Map<String, Object> financialYear = finYears.stream().filter(finYear -> finYear.get("code").equals("2021-22")).collect(Collectors.toList()).get(0);		
		details.forEach( detail -> {
			DemandDetail newDetail = new DemandDetail();
			newDetail.setTaxHeadMasterCode(detail.getTaxHeadMasterCode());
			newDetail.setTaxAmount(detail.getTaxAmount());
			newDetail.setCollectionAmount(BigDecimal.ZERO);
			newDetail.setTenantId(demand.getTenantId());
			
			newDetails.add(newDetail);
		});
		demand.setDemandDetails(newDetails);
		demand.setId(null);
		demand.setTaxPeriodFrom(Long.valueOf(financialYear.get("startingDate").toString()));
		demand.setTaxPeriodTo(Long.valueOf(financialYear.get("endingDate").toString()));
		newDemands.add(demand);
		return newDemands;		
	}
}