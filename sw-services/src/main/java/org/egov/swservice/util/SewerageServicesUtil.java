package org.egov.swservice.util;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.web.models.AuditDetails;
import org.egov.swservice.web.models.Property;
import org.egov.swservice.web.models.PropertyCriteria;
import org.egov.swservice.web.models.PropertyResponse;
import org.egov.swservice.web.models.RequestInfoWrapper;
import org.egov.swservice.web.models.SearchCriteria;
import org.egov.swservice.web.models.SewerageConnectionRequest;
import org.egov.swservice.web.models.workflow.BusinessService;
import org.egov.swservice.repository.ServiceRequestRepository;
import org.egov.swservice.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONObject;

@Component
public class SewerageServicesUtil {

	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private SWConfiguration config;

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
	public SewerageServicesUtil(ServiceRequestRepository serviceRequestRepository) {
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
	 * @param sewerageConnectionRequest
	 *            SewerageConnectionRequest containing property
	 * @return List of Property
	 */

	public List<Property> propertySearch(SewerageConnectionRequest sewerageConnectionRequest) {
		HashSet<String> propertyUUID = new HashSet<>();
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		propertyUUID.add(sewerageConnectionRequest.getSewerageConnection().getPropertyId());
		propertyCriteria.setUuids(propertyUUID);
		if (sewerageConnectionRequest.getRequestInfo().getUserInfo() != null
				&& "EMPLOYEE".equalsIgnoreCase(sewerageConnectionRequest.getRequestInfo().getUserInfo().getType())) {
			propertyCriteria.setTenantId(sewerageConnectionRequest.getSewerageConnection().getTenantId());
		}
		if (sewerageConnectionRequest.getRequestInfo().getUserInfo() != null
				&& "SYSTEM".equalsIgnoreCase(sewerageConnectionRequest.getRequestInfo().getUserInfo().getType())) {
			sewerageConnectionRequest.getRequestInfo().getUserInfo().setType("EMPLOYEE");
			List<Role> oldRoles = sewerageConnectionRequest.getRequestInfo().getUserInfo().getRoles();
			List<Role>  newRoles = new ArrayList<>();
			for(Role role:oldRoles){
				if(!role.getCode().equalsIgnoreCase("ANONYMOUS"))
					newRoles.add(role);
			}
			sewerageConnectionRequest.getRequestInfo().getUserInfo().setRoles(newRoles);
			HashMap<String, Object> addDetail = mapper
					.convertValue(sewerageConnectionRequest.getSewerageConnection().getAdditionalDetails(), HashMap.class);
			propertyCriteria.setTenantId(sewerageConnectionRequest.getSewerageConnection().getTenantId());
			propertyCriteria.setLocality(addDetail.get(localityCode).toString());
		}
		Object result = serviceRequestRepository.fetchResult(
				getPropertyURL(propertyCriteria),
				RequestInfoWrapper.builder().requestInfo(sewerageConnectionRequest.getRequestInfo()).build());
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
	private List<Property> getPropertyDetails(Object result) {
		try {
			return mapper.convertValue(result, PropertyResponse.class).getProperties();
		} catch (Exception ex) {
			throw new CustomException("PARSING_ERROR", "The property json cannot be parsed");
		}
	}
 
	/**
	 * 
	 * @param sewerageConnectionSearchCriteria - Sewerage Connection Search Criteria
	 * @param requestInfo - Request Info
	 * @return - Returns list of Property
	 */

	public List<Property> propertySearchOnCriteria(SearchCriteria sewerageConnectionSearchCriteria,
			RequestInfo requestInfo) {
		if (StringUtils.isEmpty(sewerageConnectionSearchCriteria.getMobileNumber())
				&& StringUtils.isEmpty(sewerageConnectionSearchCriteria.getPropertyId())) {
			return Collections.emptyList();
		}
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		propertyCriteria.setMobileNumber(sewerageConnectionSearchCriteria.getMobileNumber());

		if (!StringUtils.isEmpty(sewerageConnectionSearchCriteria.getTenantId())) {
			propertyCriteria.setTenantId(sewerageConnectionSearchCriteria.getTenantId());
		}
		if (!StringUtils.isEmpty(sewerageConnectionSearchCriteria.getPropertyId())) {
			HashSet<String> propertyIds = new HashSet<>();
			propertyIds.add(sewerageConnectionSearchCriteria.getPropertyId());
			propertyCriteria.setPropertyIds(propertyIds);
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
	private StringBuilder getPropertyURL(PropertyCriteria criteria) {
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
	 * @param sewerageConnectionRequest
	 * @return
	 */
	public boolean isModifyConnectionRequest(SewerageConnectionRequest sewerageConnectionRequest) {
		return !org.springframework.util.StringUtils.isEmpty(sewerageConnectionRequest.getSewerageConnection().getConnectionNo());
	}

	public StringBuilder getcollectionURL() {
		StringBuilder builder = new StringBuilder();
		return builder.append(config.getCollectionHost()).append(config.getPaymentSearch());
	}

}
