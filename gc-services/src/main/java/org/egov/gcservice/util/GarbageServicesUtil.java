package org.egov.gcservice.util;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.gcservice.config.GCConfiguration;
import org.egov.gcservice.web.models.AuditDetails;
import org.egov.gcservice.web.models.Property;
import org.egov.gcservice.web.models.PropertyCriteria;
import org.egov.gcservice.web.models.PropertyResponse;
import org.egov.gcservice.web.models.RequestInfoWrapper;
import org.egov.gcservice.web.models.SearchCriteria;
import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.gcservice.web.models.workflow.BusinessService;
import org.egov.gcservice.repository.ServiceRequestRepository;
import org.egov.gcservice.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONObject;

@Component
public class GarbageServicesUtil {

	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private GCConfiguration config;

	@Value("${egov.property.service.host}")
	private String propertyHost;

	@Value("${egov.property.createendpoint}")
	private String createPropertyEndPoint;

	@Value("${egov.property.searchendpoint}")
	private String searchPropertyEndPoint;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	public GarbageServicesUtil(ServiceRequestRepository serviceRequestRepository) {
		this.serviceRequestRepository = serviceRequestRepository;

	}
	private String tenantId = "tenantId=";
	private String mobileNumber = "mobileNumber=";
	private String propertyIds = "propertyIds=";
	private String uuids = "uuids=";
	private String URL = "url";
	private String locality = "locality=";
	private String localityCode = "locality";

	/**
	 * 
	 * @param garbageConnectionRequest
	 *            GarbageConnectionRequest containing property
	 * @return List of Property
	 */

	public List<Property> propertySearch(GarbageConnectionRequest garbageConnectionRequest) {
		HashSet<String> propertyIds = new HashSet<>();
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		propertyIds.add(garbageConnectionRequest.getGarbageConnection().getPropertyId());
		propertyCriteria.setPropertyIds(propertyIds);
		if (garbageConnectionRequest.getRequestInfo().getUserInfo() != null
				&& "EMPLOYEE".equalsIgnoreCase(garbageConnectionRequest.getRequestInfo().getUserInfo().getType())) {
			propertyCriteria.setTenantId(garbageConnectionRequest.getGarbageConnection().getTenantId());
		}
		if (garbageConnectionRequest.getRequestInfo().getUserInfo() != null
				&& "SYSTEM".equalsIgnoreCase(garbageConnectionRequest.getRequestInfo().getUserInfo().getType())) {
			garbageConnectionRequest.getRequestInfo().getUserInfo().setType("EMPLOYEE");
			List<Role> oldRoles = garbageConnectionRequest.getRequestInfo().getUserInfo().getRoles();
			List<Role>  newRoles = new ArrayList<>();
			for(Role role:oldRoles){
				if(!role.getCode().equalsIgnoreCase("ANONYMOUS"))
					newRoles.add(role);
			}
			garbageConnectionRequest.getRequestInfo().getUserInfo().setRoles(newRoles);
			HashMap<String, Object> addDetail = mapper
					.convertValue(garbageConnectionRequest.getGarbageConnection().getAdditionalDetails(), HashMap.class);
			propertyCriteria.setTenantId(garbageConnectionRequest.getGarbageConnection().getTenantId());
			propertyCriteria.setLocality(addDetail.get(localityCode).toString());
		}
		Object result = serviceRequestRepository.fetchResult(
				getPropertyURL(propertyCriteria),
				RequestInfoWrapper.builder().requestInfo(garbageConnectionRequest.getRequestInfo()).build());
		List<Property> propertyList = getPropertyDetails(result);
		if (CollectionUtils.isEmpty(propertyList)) {
			throw new CustomException("INCORRECT_PROPERTY_ID", "Failed to find Property for the given Id.");
		}
		return propertyList;
	}

	/**
	 * 
	 * @param result
	 *            Response object from property service call
	 * @return List of property
	 */
	public List<Property> getPropertyDetails(Object result) {
		try {
			return mapper.convertValue(result, PropertyResponse.class).getProperties();
		} catch (Exception ex) {
			throw new CustomException("PARSING_ERROR", "The property json cannot be parsed");
		}
	}
 
	/**
	 * 
	 * @param GarbageConnectionSearchCriteria - Sewerage GarbageConnection Search Criteria
	 * @param requestInfo - Request Info
	 * @return - Returns list of Property
	 */

	public List<Property> propertySearchOnCriteria(SearchCriteria GarbageConnectionSearchCriteria,
			RequestInfo requestInfo) {
		if (StringUtils.isEmpty(GarbageConnectionSearchCriteria.getMobileNumber())
				&& StringUtils.isEmpty(GarbageConnectionSearchCriteria.getPropertyId())) {
			return Collections.emptyList();
		}
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		propertyCriteria.setMobileNumber(GarbageConnectionSearchCriteria.getMobileNumber());

		if (!StringUtils.isEmpty(GarbageConnectionSearchCriteria.getTenantId())) {
			propertyCriteria.setTenantId(GarbageConnectionSearchCriteria.getTenantId());
		}
		if (!StringUtils.isEmpty(GarbageConnectionSearchCriteria.getPropertyId())) {
			HashSet<String> propertyIds = new HashSet<>();
			propertyIds.add(GarbageConnectionSearchCriteria.getPropertyId());
			propertyCriteria.setPropertyIds(propertyIds);
		}
		if (!StringUtils.isEmpty(GarbageConnectionSearchCriteria.getLocality())) {
			propertyCriteria.setLocality(GarbageConnectionSearchCriteria.getLocality());
		}

		Object result = serviceRequestRepository.fetchResult(
				getPropertyURL(propertyCriteria),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());
		return getPropertyDetails(result);
	}

	public MdmsCriteriaReq prepareMdMsRequest(String tenantId, String moduleName, List<String> names, String filter,
			RequestInfo requestInfo) {
		List<MasterDetail> masterDetails = new ArrayList<>();
		names.forEach(name -> {
			masterDetails.add(MasterDetail.builder().name(name).filter(filter).build());
		});
		List<ModuleDetail> moduleDetails = new ArrayList<>();
		moduleDetails.add(ModuleDetail.builder().moduleName(moduleName).masterDetails(masterDetails).build());
		return MdmsCriteriaReq.builder().requestInfo(requestInfo)
				.mdmsCriteria(MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build()).build();
	}

	public StringBuilder getPropertyCreateURL() {
		return new StringBuilder().append(propertyHost).append(createPropertyEndPoint);
	}

	public StringBuilder getPropertyURL() {
		return new StringBuilder().append(propertyHost).append(searchPropertyEndPoint);
	}

	/**
	 *
	 * @param criteria Property Search Criteria
	 * @param requestInfo - Request Info Object
	 * @return List of Property
	 */
	public List<Property> searchPropertyOnId(PropertyCriteria criteria, RequestInfo requestInfo) {

		Object result = serviceRequestRepository.fetchResult(getPropertyURL(criteria),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());
		return getPropertyDetails(result);
	}

	/**
	 * Method to return auditDetails for create/update flows
	 *
	 * @param by - Updated By user detail
	 * @param isCreate - Boolean flag to identify create request
	 * @return AuditDetails
	 */
	public AuditDetails getAuditDetails(String by, Boolean isCreate) {
		Long time = System.currentTimeMillis();
		if (isCreate)
			return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time)
					.build();
		else
			return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
	}

	public boolean getStatusForUpdate(BusinessService businessService, String applicationStatus) {
		return workflowService.isStateUpdatable(applicationStatus, businessService);
	}

	/**
	 * 
	 * @return URL of calculator service
	 */
	public StringBuilder getCalculatorURL() {
		return new StringBuilder(config.getCalculatorHost()).append(config.getCalculateEndpoint());
	}

	/**
	 * 
	 * @return URL of estimation service
	 */
	public StringBuilder getEstimationURL() {
		StringBuilder builder = new StringBuilder();
		return builder.append(config.getCalculatorHost()).append(config.getEstimationEndpoint());
	}

	public String getShortenedURL(String actualURL) {
		JSONObject obj = new JSONObject();
		obj.put(URL, actualURL);
		String url = config.getNotificationUrl() + config.getShortenerURL();

		Object response = serviceRequestRepository.getShortningURL(new StringBuilder(url), obj);
		return response.toString();
	}
 
	/**
	 * 
	 * search url for property search
	 * @param criteria - Property Search Criteria
	 * @return property URL
	 */
	public StringBuilder getPropertyURL(PropertyCriteria criteria) {
		StringBuilder url = new StringBuilder(getPropertyURL());
		boolean isanyparametermatch = false;
		url.append("?");
		if (!StringUtils.isEmpty(criteria.getTenantId())) {
			isanyparametermatch = true;
			url.append(tenantId).append(criteria.getTenantId());
		}
		if (!CollectionUtils.isEmpty(criteria.getPropertyIds())) {
			if (isanyparametermatch)url.append("&");
			isanyparametermatch = true;
			String propertyIdsString = criteria.getPropertyIds().stream().map(propertyId -> propertyId)
					.collect(Collectors.toSet()).stream().collect(Collectors.joining(","));
			url.append(propertyIds).append(propertyIdsString);
		}
		if (!StringUtils.isEmpty(criteria.getMobileNumber())) {
			if (isanyparametermatch)url.append("&");
			isanyparametermatch = true;
			url.append(mobileNumber).append(criteria.getMobileNumber());
		}
		if (!org.springframework.util.StringUtils.isEmpty(criteria.getLocality())) {
			if (isanyparametermatch)url.append("&");
			isanyparametermatch = true;
			url.append(locality).append(criteria.getLocality());
		}
		if (!CollectionUtils.isEmpty(criteria.getUuids())) {
			if (isanyparametermatch)url.append("&");
			String uuidString = criteria.getUuids().stream().map(uuid -> uuid).collect(Collectors.toSet()).stream()
					.collect(Collectors.joining(","));
			url.append(uuids).append(uuidString);
		}
		return url;
	}
 
	/**
	 *
	 * @param garbageConnectionRequest
	 * @return
	 */
	public boolean isModifyConnectionRequest(GarbageConnectionRequest garbageConnectionRequest) {
		return !org.springframework.util.StringUtils.isEmpty(garbageConnectionRequest.getGarbageConnection().getConnectionNo());
	}

	public StringBuilder getcollectionURL() {
		StringBuilder builder = new StringBuilder();
		return builder.append(config.getCollectionHost()).append(config.getPaymentSearch());
	}

}
