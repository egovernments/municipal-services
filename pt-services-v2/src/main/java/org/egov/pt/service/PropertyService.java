package org.egov.pt.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.OwnerInfo;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.user.User;
import org.egov.pt.models.user.UserDetailResponse;
import org.egov.pt.models.user.UserSearchRequest;
import org.egov.pt.producer.Producer;
import org.egov.pt.repository.PropertyRepository;
import org.egov.pt.validator.PropertyValidator;
import org.egov.pt.web.contracts.PropertyRequest;
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

	/**
	 * Assign ids through enrichment and pushes to kafka
	 *
	 * @param request PropertyRequest containing list of properties to be created
	 * @return List of properties successfully created
	 */
	public Property createProperty(PropertyRequest request) {

		propertyValidator.validateCreateRequest(request);
		enrichmentService.enrichCreateRequest(request);
		userService.createUser(request);
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

		propertyValidator.validateUpdateRequest(request);
		userService.createUser(request);
		producer.push(config.getUpdatePropertyTopic(), request);
		return request.getProperty();
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

		if (criteria.getMobileNumber() != null || criteria.getName() != null) {

			UserSearchRequest userSearchRequest = userService.getBaseUserSearchRequest(criteria.getTenantId(),
					requestInfo);
			userSearchRequest.setMobileNumber(criteria.getMobileNumber());
			userSearchRequest.setName(criteria.getName());

			UserDetailResponse userDetailResponse = userService.getUser(userSearchRequest);
			if (CollectionUtils.isEmpty(userDetailResponse.getUser()))
				return Collections.emptyList();

			// fetching property id from owner-id and enriching criteria
			ownerIds.addAll(userDetailResponse.getUser().stream().map(User::getUuid).collect(Collectors.toSet()));
			criteria.setIds(new HashSet<>(repository.getPropertyIds(ownerIds)));

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

		List<String> ownerIds = properties.stream().map(Property::getOwners).flatMap(List::stream)
				.map(OwnerInfo::getUuid).collect(Collectors.toList());

		UserSearchRequest userSearchRequest = userService.getBaseUserSearchRequest(criteria.getTenantId(), requestInfo);
		userSearchRequest.setUuid(ownerIds);

		UserDetailResponse userDetailResponse = userService.getUser(userSearchRequest);
		enrichmentService.enrichOwner(userDetailResponse, properties);
		return properties;
	}

}