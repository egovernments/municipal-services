package org.egov.pt.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.*;
import org.egov.pt.models.Address;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.enums.*;
import org.egov.pt.models.oldProperty.*;
import org.egov.pt.models.user.User;
import org.egov.pt.models.user.UserDetailResponse;
import org.egov.pt.models.user.UserSearchRequest;
import org.egov.pt.models.workflow.ProcessInstanceRequest;
import org.egov.pt.producer.Producer;
import org.egov.pt.repository.PropertyRepository;
import org.egov.pt.util.PropertyUtil;
import org.egov.pt.validator.PropertyValidator;
import org.egov.pt.web.contracts.PropertyRequest;
import org.egov.pt.web.contracts.PropertyResponse;
import org.javers.common.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

	private ObjectMapper mapper;


    
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
		if (config.getIsWorkflowEnabled())
			updateWorkflow(request, true);
		producer.push(config.getSavePropertyTopic(), request);
		return request.getProperty();
	}
	
	/**
	 * Updates the property
	 *
	 * @param request PropertyRequest containing list of properties to be update
	 * @return List of updated properties
	 */
	public Property updateProperty(PropertyRequest request) {

		Property propertyFromSearch = propertyValidator.validateUpdateRequest(request);
		enrichmentService.enrichUpdateRequest(request, propertyFromSearch);
		userService.createUser(request);
		if (config.getIsWorkflowEnabled()){
		}
		else
			producer.push(config.getUpdatePropertyTopic(), request);
		return request.getProperty();
	}
	



	/**
	 * method to prepare process instance request 
	 * and assign status back to property
	 * 
	 * @param request
	 */
	private void updateWorkflow(PropertyRequest request, Boolean isCreate) {

		Property property = request.getProperty();

		ProcessInstanceRequest workflowReq = util.getWfForPropertyRegistry(request, isCreate);
		String status = wfService.callWorkFlow(workflowReq);
		if (status.equalsIgnoreCase(config.getWfStatusActive()) && property.getPropertyId() == null) {
			
			String pId = enrichmentService.getIdList(request.getRequestInfo(), property.getTenantId(), config.getPropertyIdGenName(), config.getPropertyIdGenFormat(), 1).get(0);
			request.getProperty().setPropertyId(pId);
		}
		request.getProperty().setStatus(Status.fromValue(status));
	}
	
    /**
     * Search property with given PropertyCriteria
     *
     * @param criteria PropertyCriteria containing fields on which search is based
     * @return list of properties satisfying the containing fields in criteria
     */
	public List<Property> searchProperty(PropertyCriteria criteria, RequestInfo requestInfo) {

		List<Property> properties;
		propertyValidator.validatePropertyCriteria(criteria, requestInfo);

		if (criteria.getMobileNumber() != null || criteria.getName() != null || criteria.getOwnerIds() != null) {
			
			Boolean shouldReturnEmptyList = enrichCriteriaFromUser(criteria, requestInfo);

			if (shouldReturnEmptyList)
				return Collections.emptyList();

			properties = getPropertiesWithOwnerInfo(criteria, requestInfo);
		} else {
			properties = getPropertiesWithOwnerInfo(criteria, requestInfo);
		}

		properties.forEach(property -> {
			enrichmentService.enrichBoundary(property, requestInfo);
		});
		return properties;
	}
	
	/**
	 * 
	 * Method to enrich property search criteria with user based criteria info
	 * 
	 * If no info found based on user criteria boolean true will be returned so that empty list can be returned 
	 * 
	 * else returns false to continue the normal flow
	 * 
	 * The enrichment of object is done this way(instead of directly applying in the search query) to fetch multiple owners related to property at once
	 * 
	 * @param criteria
	 * @param requestInfo
	 * @return
	 */
	private Boolean enrichCriteriaFromUser(PropertyCriteria criteria, RequestInfo requestInfo) {
		
		Set<String> ownerIds = new HashSet<String>();
		
		if(!CollectionUtils.isEmpty(criteria.getOwnerIds()))
			ownerIds.addAll(criteria.getOwnerIds());
		criteria.setOwnerIds(null);
		
		String userTenant = criteria.getTenantId();
		if(criteria.getTenantId() == null)
			userTenant = requestInfo.getUserInfo().getTenantId();

		UserSearchRequest userSearchRequest = userService.getBaseUserSearchRequest(userTenant, requestInfo);
		userSearchRequest.setMobileNumber(criteria.getMobileNumber());
		userSearchRequest.setName(criteria.getName());
		userSearchRequest.setUuid(criteria.getOwnerIds());

		UserDetailResponse userDetailResponse = userService.getUser(userSearchRequest);
		if (CollectionUtils.isEmpty(userDetailResponse.getUser()))
			return true;

		// fetching property id from owner table and enriching criteria
		ownerIds.addAll(userDetailResponse.getUser().stream().map(User::getUuid).collect(Collectors.toSet()));
		List<String> propertyIds = repository.getPropertyIds(ownerIds);

		// returning empty list if no property id found for user criteria
		if (CollectionUtils.isEmpty(propertyIds)) {

			return true;
		} else if (!CollectionUtils.isEmpty(criteria.getPropertyIds())) {

			// eliminating property Ids not matching with Ids found using user data

			Set<String> givenIds = criteria.getPropertyIds();

			givenIds.forEach(id -> {

				if (!propertyIds.contains(id))
					givenIds.remove(id);
			});

			if (CollectionUtils.isEmpty(givenIds))
				return true;
		} else {

			criteria.setPropertyIds(Sets.asSet(propertyIds));
		}
		
		return false;
	}

	/**
	 * Returns list of properties based on the given propertyCriteria with owner
	 * fields populated from user service
	 *
	 * @param criteria    PropertyCriteria on which to search properties
	 * @param requestInfo RequestInfo object of the request
	 * @return properties with owner information added from user service
	 */
	List<Property> getPropertiesWithOwnerInfo(PropertyCriteria criteria, RequestInfo requestInfo) {

		List<Property> properties = repository.getProperties(criteria);
		if (CollectionUtils.isEmpty(properties))
			return Collections.emptyList();

		Set<String> ownerIds = properties.stream().map(Property::getOwners).flatMap(List::stream)
				.map(OwnerInfo::getUuid).collect(Collectors.toSet());

		UserSearchRequest userSearchRequest = userService.getBaseUserSearchRequest(criteria.getTenantId(), requestInfo);
		userSearchRequest.setUuid(ownerIds);

		UserDetailResponse userDetailResponse = userService.getUser(userSearchRequest);
		enrichmentService.enrichOwner(userDetailResponse, properties);
		return properties;
	}


	public List<Property> migrateProperty(RequestInfo requestInfo, List<OldProperty> oldProperties) {

		List<Property> properties = new ArrayList<>();
		for(OldProperty oldProperty : oldProperties){
			Property property = new Property();
			property.setId(UUID.randomUUID().toString());
			property.setPropertyId(oldProperty.getPropertyId());
			//property.setSurveyId();
			property.setLinkedProperties(null);
			property.setTenantId(oldProperty.getTenantId());
			property.setAccountId(requestInfo.getUserInfo().getUuid());
			property.setOldPropertyId(oldProperty.getOldPropertyId());
			property.setStatus(Status.fromValue(oldProperty.getStatus().toString()));

			if(oldProperty.getAddress() == null)
				property.setAddress(null);
			else
				property.setAddress(migrateAddress(oldProperty.getAddress()));

			property.setAcknowldgementNumber(oldProperty.getAcknowldgementNumber());
			property.setPropertyType(oldProperty.getPropertyDetails().get(0).getPropertyType());
			property.setOwnershipCategory(migrateOwnwershipCategory(oldProperty));
			property.setOwners(migrateOwnerInfo(oldProperty.getPropertyDetails().get(0).getOwners()));
			if(oldProperty.getPropertyDetails().get(0).getInstitution() == null)
				property.setInstitution(null);
			else
				property.setInstitution(migrateInstitution(oldProperty.getPropertyDetails().get(0).getInstitution()));
			property.setCreationReason(CreationReason.fromValue(String.valueOf(oldProperty.getCreationReason())));
			property.setUsageCategory(migrateUsageCategory(oldProperty));
			property.setNoOfFloors(oldProperty.getPropertyDetails().get(0).getNoOfFloors());
			property.setLandArea(Double.valueOf(oldProperty.getPropertyDetails().get(0).getLandArea()));
			property.setSuperBuiltUpArea(BigDecimal.valueOf(oldProperty.getPropertyDetails().get(0).getBuildUpArea()));
			property.setSource(Source.fromValue(String.valueOf(oldProperty.getPropertyDetails().get(0).getSource())));
			property.setChannel(Channel.fromValue(String.valueOf(oldProperty.getPropertyDetails().get(0).getChannel())));

			if(oldProperty.getPropertyDetails().get(0).getDocuments() == null)
				property.setDocuments(null);
			else
				property.setDocuments(migrateDocument(oldProperty.getPropertyDetails().get(0).getDocuments()));

			property.setUnits(migrateUnit(oldProperty.getPropertyDetails().get(0).getUnits()));
			property.setAdditionalDetails(oldProperty.getPropertyDetails().get(0).getAdditionalDetails());
			property.setAuditDetails(migrateAuditDetails(oldProperty.getOldAuditDetails()));
			properties.add(property);

			PropertyRequest request = PropertyRequest.builder().requestInfo(requestInfo).property(property).build();
			//createProperty(request);

			propertyValidator.validateCreateRequest(request);
			if (config.getIsWorkflowEnabled())
				updateWorkflow(request, true);
			producer.push(config.getSavePropertyTopic(), request);
		}

		return properties;
	}

	public Address migrateAddress(org.egov.pt.models.oldProperty.Address oldAddress){
		Address address = new Address();
		address.setTenantId(oldAddress.getTenantId());
		address.setDoorNo(oldAddress.getDoorNo());
		//address.setPlotNo();
		address.setId(oldAddress.getId());
		address.setLandmark(oldAddress.getLandmark());
		address.setCity(oldAddress.getCity());
		//address.setDistrict();
		//address.setRegion();
		//address.setState();
		//address.setCountry();
		address.setPincode(oldAddress.getPincode());
		address.setBuildingName(oldAddress.getBuildingName());
		address.setStreet(oldAddress.getStreet());
		address.setLocality(migrateLocality(oldAddress.getLocality()));
		address.setAdditionalDetails(oldAddress.getAdditionalDetails());
		address.setGeoLocation(migrateGeoLocation(oldAddress));


		return  address;
	}

	public Locality migrateLocality(Boundary oldLocality){
		Locality locality = new Locality();
		locality.setCode(oldLocality.getCode());
		locality.setName(oldLocality.getName());
		locality.setLabel(oldLocality.getLabel());
		locality.setLatitude(oldLocality.getLatitude());
		locality.setLongitude(oldLocality.getLongitude());
		locality.setArea(oldLocality.getArea());
		locality.setMaterializedPath(oldLocality.getMaterializedPath());
		locality.setChildren(setmigrateLocalityList(oldLocality.getChildren()));
		return  locality;
	}

	public List<Locality> setmigrateLocalityList(List<Boundary> oldchildrenList){
		List<Locality> childrenList = new ArrayList<>();
		for(Boundary oldChildren : oldchildrenList ){
			childrenList.add(migrateLocality(oldChildren));
		}
		return childrenList;
	}

	public GeoLocation migrateGeoLocation(org.egov.pt.models.oldProperty.Address oldAddress){
		GeoLocation geoLocation = new GeoLocation();
		if(oldAddress.getLatitude() == null)
			geoLocation.setLongitude(null);
		else
			geoLocation.setLatitude(Double.valueOf(oldAddress.getLatitude()));

		if(oldAddress.getLongitude() == null)
			geoLocation.setLongitude(null);
		else
			geoLocation.setLongitude(Double.valueOf(oldAddress.getLongitude()));
		return  geoLocation;
	}

	public List<OwnerInfo> migrateOwnerInfo(Set<OldOwnerInfo> oldOwnerInfosSet){
		List<OwnerInfo> ownerInfolist = new ArrayList<>();
		for(OldOwnerInfo oldOwnerInfo : oldOwnerInfosSet){
			OwnerInfo ownerInfo = new OwnerInfo();
			ownerInfo.setId(oldOwnerInfo.getId());
			ownerInfo.setUuid(oldOwnerInfo.getUuid());
			ownerInfo.setUserName(oldOwnerInfo.getUserName());
			ownerInfo.setPassword(oldOwnerInfo.getPassword());
			ownerInfo.setSalutation(oldOwnerInfo.getSalutation());
			ownerInfo.setName(oldOwnerInfo.getName());
			ownerInfo.setEmailId(oldOwnerInfo.getEmailId());
			ownerInfo.setAltContactNumber(oldOwnerInfo.getAltContactNumber());
			ownerInfo.setPan(oldOwnerInfo.getPan());
			ownerInfo.setAadhaarNumber(oldOwnerInfo.getAadhaarNumber());
			ownerInfo.setPermanentAddress(oldOwnerInfo.getPermanentAddress());
			ownerInfo.setPermanentCity(oldOwnerInfo.getPermanentCity());
			ownerInfo.setPermanentPincode(oldOwnerInfo.getPermanentPincode());
			ownerInfo.setCorrespondenceAddress(oldOwnerInfo.getCorrespondenceAddress());
			ownerInfo.setCorrespondenceCity(oldOwnerInfo.getCorrespondenceCity());
			ownerInfo.setCorrespondencePincode(oldOwnerInfo.getCorrespondencePincode());
			ownerInfo.setActive(oldOwnerInfo.getActive());
			ownerInfo.setDob(oldOwnerInfo.getDob());
			ownerInfo.setPwdExpiryDate(oldOwnerInfo.getPwdExpiryDate());
			ownerInfo.setLocale(oldOwnerInfo.getLocale());
			ownerInfo.setType(oldOwnerInfo.getType());
			ownerInfo.setSignature(oldOwnerInfo.getSignature());
			ownerInfo.setAccountLocked(oldOwnerInfo.getAccountLocked());
			ownerInfo.setRoles(oldOwnerInfo.getRoles());
			ownerInfo.setBloodGroup(oldOwnerInfo.getBloodGroup());
			ownerInfo.setIdentificationMark(oldOwnerInfo.getIdentificationMark());
			ownerInfo.setPhoto(oldOwnerInfo.getPhoto());
			ownerInfo.setCreatedBy(oldOwnerInfo.getCreatedBy());
			ownerInfo.setCreatedDate(oldOwnerInfo.getCreatedDate());
			ownerInfo.setLastModifiedBy(oldOwnerInfo.getLastModifiedBy());
			ownerInfo.setLastModifiedDate(oldOwnerInfo.getLastModifiedDate());
			ownerInfo.setTenantId(oldOwnerInfo.getTenantId());
			ownerInfo.setOwnerInfoUuid(UUID.randomUUID().toString());
			ownerInfo.setMobileNumber(oldOwnerInfo.getMobileNumber());
			ownerInfo.setGender(oldOwnerInfo.getGender());
			ownerInfo.setFatherOrHusbandName(oldOwnerInfo.getFatherOrHusbandName());
			ownerInfo.setCorrespondenceAddress(oldOwnerInfo.getCorrespondenceAddress());
			ownerInfo.setIsPrimaryOwner(oldOwnerInfo.getIsPrimaryOwner());
			ownerInfo.setOwnerShipPercentage(oldOwnerInfo.getOwnerShipPercentage());
			ownerInfo.setOwnerType(oldOwnerInfo.getOwnerType());
			ownerInfo.setInstitutionId(oldOwnerInfo.getInstitutionId());
			ownerInfo.setStatus(Status.ACTIVE);
			if(oldOwnerInfo.getOldDocuments() == null)
				ownerInfo.setDocuments(null);
			else
				ownerInfo.setDocuments(migrateDocument(oldOwnerInfo.getOldDocuments()));

			ownerInfo.setRelationship(Relationship.fromValue(String.valueOf(oldOwnerInfo.getRelationship())));

			ownerInfolist.add(ownerInfo);
		}
		return ownerInfolist;
	}

	public Institution migrateInstitution(OldInstitution oldInstitution){
		Institution newInstitution = new Institution();
		newInstitution.setId(oldInstitution.getId());
		newInstitution.setTenantId(oldInstitution.getTenantId());
		newInstitution.setName(oldInstitution.getName());
		newInstitution.setType(oldInstitution.getType());
		newInstitution.setDesignation(oldInstitution.getDesignation());
		//newInstitution.setNameOfAuthorizedPerson();
		newInstitution.setAdditionalDetails(oldInstitution.getAdditionalDetails());

		return newInstitution;

	}

	public String migrateUsageCategory(OldProperty oldProperty){
		StringBuilder usageCategory = new StringBuilder(oldProperty.getPropertyDetails().get(0).getUsageCategoryMajor());
		if(oldProperty.getPropertyDetails().get(0).getUsageCategoryMinor() != null)
			usageCategory.append(".").append(oldProperty.getPropertyDetails().get(0).getUsageCategoryMinor());

		return usageCategory.toString();
	}
	public String migrateOwnwershipCategory(OldProperty oldProperty){
		StringBuilder ownershipCategory = new StringBuilder(oldProperty.getPropertyDetails().get(0).getOwnershipCategory());
		if(oldProperty.getPropertyDetails().get(0).getSubOwnershipCategory() != null)
			ownershipCategory.append(".").append(oldProperty.getPropertyDetails().get(0).getSubOwnershipCategory());

		return ownershipCategory.toString();
	}

	public List<Unit> migrateUnit(List<OldUnit> oldUnits){
		List<Unit> units = new ArrayList<>();
		for(OldUnit oldUnit : oldUnits){
			Unit unit = new Unit();
			unit.setId(oldUnit.getId());
			unit.setTenantId(oldUnit.getTenantId());
			unit.setFloorNo(Integer.valueOf(oldUnit.getFloorNo()));
			unit.setUnitType(oldUnit.getUnitType());
			unit.setUsageCategory(migrateUnitUsageCategory(oldUnit));
			unit.setOccupancyType(OccupancyType.fromValue(oldUnit.getOccupancyType()));
			unit.setOccupancyDate(oldUnit.getOccupancyDate());
			unit.setActive(Boolean.TRUE);
			unit.setConstructionDetail(migrateConstructionDetail(oldUnit));
			unit.setAdditionalDetails(oldUnit.getAdditionalDetails());
			//unit.setAuditDetails();
			unit.setArv(oldUnit.getArv());
			units.add(unit);
		}

		return  units;
	}

	public String migrateUnitUsageCategory(OldUnit oldUnit){
		StringBuilder usageCategory = new StringBuilder(oldUnit.getUsageCategoryMajor());
		if(oldUnit.getUsageCategoryMinor() != null)
			usageCategory.append(".").append(oldUnit.getUsageCategoryMinor());
		if(oldUnit.getUsageCategorySubMinor() != null)
			usageCategory.append(".").append(oldUnit.getUsageCategorySubMinor());
		if(oldUnit.getUsageCategoryDetail() != null)
			usageCategory.append(".").append(oldUnit.getUsageCategoryDetail());

		return usageCategory.toString();
	}

	public ConstructionDetail migrateConstructionDetail(OldUnit oldUnit){
		ConstructionDetail constructionDetail = new ConstructionDetail();
		constructionDetail.setBuiltUpArea(BigDecimal.valueOf(oldUnit.getUnitArea()));

		if (oldUnit.getConstructionType() == null){
			constructionDetail.setConstructionType(null);
			return constructionDetail;
		}

		StringBuilder constructionType = new StringBuilder(oldUnit.getConstructionType());
		if(oldUnit.getConstructionSubType() != null)
			constructionType.append(".").append(oldUnit.getConstructionSubType());
		constructionDetail.setConstructionType(constructionType.toString());

		return constructionDetail;
	}

	public List<Document> migrateDocument(Set<OldDocument> oldDocumentList){
		List<Document> documentList = new ArrayList<>();
		for(OldDocument oldDocument: oldDocumentList){
			Document doc = new Document();
			doc.setId(oldDocument.getId());
			doc.setDocumentType(oldDocument.getDocumentType());
			doc.setFileStoreId(oldDocument.getFileStore());
			doc.setDocumentUid(oldDocument.getDocumentUid());
			documentList.add(doc);
		}
		return  documentList;
	}

	public AuditDetails migrateAuditDetails(OldAuditDetails oldAuditDetails){
		AuditDetails details = new AuditDetails();
		details.setCreatedBy(oldAuditDetails.getCreatedBy());
		details.setCreatedTime(oldAuditDetails.getCreatedTime());
		details.setLastModifiedBy(oldAuditDetails.getLastModifiedBy());
		details.setLastModifiedTime(oldAuditDetails.getLastModifiedTime());
		return  details;
	}


}