package org.egov.pt.service;

import java.util.*;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.Difference;
import org.egov.pt.models.OwnerInfo;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.enums.Status;
import org.egov.pt.models.user.User;
import org.egov.pt.models.user.UserDetailResponse;
import org.egov.pt.models.user.UserSearchRequest;
import org.egov.pt.models.workflow.ProcessInstanceRequest;
import org.egov.pt.producer.Producer;
import org.egov.pt.repository.PropertyRepository;
import org.egov.pt.util.PropertyUtil;
import org.egov.pt.validator.PropertyValidator;
import org.egov.pt.web.contracts.PropertyRequest;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.ListCompareAlgorithm;
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
    private Workflowservice wfService;
    
    @Autowired
    private PropertyUtil util;


    
	/**
	 * Assign Ids through enrichment and pushes to Kafka
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
		//userService.createUser(request);
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
		Set<String> ownerIds = new HashSet<String>();
		
		if(!CollectionUtils.isEmpty(criteria.getOwnerIds()))
			ownerIds.addAll(criteria.getOwnerIds());
		criteria.setOwnerIds(null);

		propertyValidator.validatePropertyCriteria(criteria, requestInfo);

		if (criteria.getMobileNumber() != null || criteria.getName() != null || criteria.getOwnerIds() != null) {
			
			String userTenant = criteria.getTenantId();
			if(criteria.getTenantId() == null)
				userTenant = requestInfo.getUserInfo().getTenantId();

			UserSearchRequest userSearchRequest = userService.getBaseUserSearchRequest(userTenant, requestInfo);
			userSearchRequest.setMobileNumber(criteria.getMobileNumber());
			userSearchRequest.setName(criteria.getName());
			userSearchRequest.setUuid(criteria.getOwnerIds());

			UserDetailResponse userDetailResponse = userService.getUser(userSearchRequest);
			if (CollectionUtils.isEmpty(userDetailResponse.getUser()))
				return Collections.emptyList();

			// fetching property id from owner-id and enriching criteria
			ownerIds.addAll(userDetailResponse.getUser().stream().map(User::getUuid).collect(Collectors.toSet()));
			criteria.setUuids(new HashSet<>(repository.getPropertyIds(ownerIds)));

			properties = getPropertiesWithOwnerInfo(criteria, requestInfo);
		} else {
			properties = getPropertiesWithOwnerInfo(criteria, requestInfo);
		}
		return properties;
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

}