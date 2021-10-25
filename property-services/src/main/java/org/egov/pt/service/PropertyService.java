package org.egov.pt.service;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.Owner;
import org.egov.pt.models.OwnerInfo;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.enums.CreationReason;
import org.egov.pt.models.enums.Status;
import org.egov.pt.models.user.UserDetailResponse;
import org.egov.pt.models.user.UserSearchRequest;
import org.egov.pt.models.workflow.ProcessInstance;
import org.egov.pt.models.workflow.ProcessInstanceRequest;
import org.egov.pt.models.workflow.State;
import org.egov.pt.producer.Producer;
import org.egov.pt.repository.PropertyRepository;
import org.egov.pt.util.PTConstants;
import org.egov.pt.util.PropertyUtil;
import org.egov.pt.validator.PropertyValidator;
import org.egov.pt.web.contracts.PropertyRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

@Service
public class PropertyService {

    @Autowired
    private Producer producer;

    @Autowired
    private NotificationService notifService;

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
	private FuzzySearchService fuzzySearchService;


    
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
		System.out.println("isRequestForOwnerMutation -------- "+isRequestForOwnerMutation);

		boolean isRequestForUpdateMobileNumber = false;
		if(null != request.getProperty().getAdditionalDetails()) {
			HashMap<String, Boolean> additionalDetails = mapper.convertValue(request.getProperty().getAdditionalDetails(),HashMap.class);
			isRequestForUpdateMobileNumber = additionalDetails.getOrDefault("isMobileNumberUpdate", false);
		}
		
		boolean isNumberDifferent=false;
		
		if(isRequestForUpdateMobileNumber)
			isNumberDifferent = checkIsRequestForMobileNumberUpdate(request, propertyFromSearch);
		// Map <String, String> uuidToMobileNumber = new HashMap <String, String>();
		// List <OwnerInfo> owners = propertyFromSearch.getOwners();

		// for(OwnerInfo owner : owners) {
		// 	uuidToMobileNumber.put(owner.getUuid(), owner.getMobileNumber());
		// }

		// List <OwnerInfo> ownersFromRequest = request.getProperty().getOwners();

		// Boolean isNumberDifferent = false;

		// for(OwnerInfo owner : ownersFromRequest) {
		// 	if(!uuidToMobileNumber.get(owner.getUuid()).equals(owner.getMobileNumber())) {
		// 		isNumberDifferent = true;
		// 		break;
		// 	}
		// }
		
		if (isRequestForOwnerMutation)
			processOwnerMutation(request, propertyFromSearch);
		else if(isNumberDifferent)
			processMobileNumberUpdate(request, propertyFromSearch);
		else
			processPropertyUpdate(request, propertyFromSearch);

		request.getProperty().setWorkflow(null);
		return request.getProperty();
	}
	/*
		Method to check if the update request is for updating owner mobile numbers
	*/
	private boolean checkIsRequestForMobileNumberUpdate(PropertyRequest request, Property propertyFromSearch) {
		Map <String, String> uuidToMobileNumber = new HashMap <String, String>();
		List <OwnerInfo> owners = propertyFromSearch.getOwners();

		for(OwnerInfo owner : owners) {
			uuidToMobileNumber.put(owner.getUuid(), owner.getMobileNumber());
		}

		List <OwnerInfo> ownersFromRequest = request.getProperty().getOwners();

		Boolean isNumberDifferent = false;

		for(OwnerInfo owner : ownersFromRequest) {
			if(!uuidToMobileNumber.get(owner.getUuid()).equals(owner.getMobileNumber())) {
				isNumberDifferent = true;
				break;
			}
		}

		return isNumberDifferent;
	}
	/*
		Method to process owner mobile number update
	*/
	private void processMobileNumberUpdate(PropertyRequest request, Property propertyFromSearch) {

				if (CreationReason.CREATE.equals(request.getProperty().getCreationReason())) {
					userService.createUser(request);
				} else {			
					updateOwnerMobileNumbers(request,propertyFromSearch);
				}

				enrichmentService.enrichUpdateRequest(request, propertyFromSearch);
				util.mergeAdditionalDetails(request, propertyFromSearch);
				producer.push(config.getUpdatePropertyTopic(), request);		
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
			if ("LEGACY_RECORD".equals(request.getProperty().getSource().toString())) {
				userService.createUser(request);

				for (OwnerInfo info : propertyFromSearch.getOwners()) {
					info.setStatus(Status.INACTIVE);
				}
				
				for (OwnerInfo info : request.getProperty().getOwners()) {
					info.setStatus(Status.ACTIVE);
				}
				
				List<OwnerInfo> collectedOwners = new ArrayList<OwnerInfo>();
				collectedOwners.addAll(propertyFromSearch.getOwners());
				collectedOwners.addAll(request.getProperty().getOwners());

				request.getProperty().setOwners(util.getCopyOfOwners(collectedOwners));
			} else{
				request.getProperty().setOwners(util.getCopyOfOwners(propertyFromSearch.getOwners()));
			}
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
	/*
		Method to update owners mobile number
	*/
	private void updateOwnerMobileNumbers(PropertyRequest request, Property propertyFromSearch) {


		Map <String, String> uuidToMobileNumber = new HashMap <String, String>();
		List <OwnerInfo> owners = propertyFromSearch.getOwners();

		for(OwnerInfo owner : owners) {
			uuidToMobileNumber.put(owner.getUuid(), owner.getMobileNumber());
		}

		userService.updateUserMobileNumber(request, uuidToMobileNumber);
		notifService.sendNotificationForMobileNumberUpdate(request, propertyFromSearch);						
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
		util.mergeAdditionalDetails(request, propertyFromSearch);
		System.out.println("--------- merge additionaldetails before calculate ---------- ");
		calculatorService.calculateMutationFee(request.getRequestInfo(), request.getProperty());
		//String feesPresent = calculatorService.checkApplicableFees(request.getRequestInfo(), request.getProperty());
		//System.out.println("--------- feesPresent = "+feesPresent);
		
		// TODO FIX ME block property changes FIXME
		//util.mergeAdditionalDetails(request, propertyFromSearch);
		PropertyRequest oldPropertyRequest = PropertyRequest.builder()
				.requestInfo(request.getRequestInfo())
				.property(propertyFromSearch)
				.build();
		
		if (config.getIsMutationWorkflowEnabled()) {

			State state = wfService.updateWorkflow(request, CreationReason.MUTATION);
      
			System.out.println("Request State ~~~~~~~~~~~~~~~~~~~~"+request.getProperty().getWorkflow().getState().getState());
			if (Arrays.asList(config.getSkipPaymentStatuses().split(","))
					.contains(state.getState())) {
				skipPayment(request, state.getState());
			}
			/*
			 * updating property from search to INACTIVE status
			 * 
			 * to create new entry for new Mutation
			 */
			if (state.getIsStartState() == true
					&& state.getApplicationStatus().equalsIgnoreCase(Status.INWORKFLOW.toString())
					&& !propertyFromSearch.getStatus().equals(Status.INWORKFLOW)) {
				
				/*if (Arrays.asList(config.getSkipPaymentStatuses().split(","))
						.contains(state.getState())) {
					skipPayment(request, state.getApplicationStatus(), state);
				}*/

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
				System.out.println("~~~~~~~~~~~~ Updating property in else ~~~~~~~~~~~ ");
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


		if(criteria.getDoorNo()!=null || criteria.getName()!=null || criteria.getOldPropertyId()!=null){
			return fuzzySearchService.getProperties(requestInfo, criteria);
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
		List<Property> properties = repository.getPropertiesForBulkSearch(propertyCriteria, true);
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
	
	public void skipPayment(PropertyRequest propertyRequest, String currentState){
    	System.out.println("~~~~~~~~~~ Current state = "+currentState);
    	Property property = propertyRequest.getProperty();
		BigDecimal balanceAmount = util.getBalanceAmount(propertyRequest);
		if (!(balanceAmount.compareTo(BigDecimal.ZERO) > 0)) {
			System.out.println("~~~~~~~~~~ Enabling skip payment for balance = "+balanceAmount);
			String action = EMPTY;
			if("APPLICATION_FEE_PAYMENT".equalsIgnoreCase(currentState))
				action = PTConstants.ACTION_SKIP_PAY;
			else if("APPROVED".equalsIgnoreCase(currentState)) 
				action = PTConstants.ACTION_FINAL_SKIP_PAY;
			
			ProcessInstance workflow = ProcessInstance.builder().action(action).build();
			workflow.setBusinessId(property.getAcknowldgementNumber());
			workflow.setTenantId(property.getTenantId());
			workflow.setBusinessService(config.getMutationWfName());
			workflow.setModuleName(config.getPropertyModuleName());
			property.setWorkflow(workflow);
			ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(propertyRequest.getRequestInfo(),
					Collections.singletonList(workflow));
			wfService.callWorkFlow(workflowRequest);
			System.out.println("~~~~~~~~~ Payment skipped ~~~~~~~~~~");
			//Activate the property while skipping payment after approval
			if("APPROVED".equalsIgnoreCase(currentState)) 
				propertyRequest.getProperty().setStatus(Status.ACTIVE);
		}
	}
    
    
}
