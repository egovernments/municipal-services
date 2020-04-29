package org.egov.swservice.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.model.AuditDetails;
import org.egov.swservice.model.Property;
import org.egov.swservice.model.PropertyCriteria;
import org.egov.swservice.model.PropertyRequest;
import org.egov.swservice.model.PropertyResponse;
import org.egov.swservice.model.RequestInfoWrapper;
import org.egov.swservice.model.SearchCriteria;
import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.model.workflow.BusinessService;
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
	ObjectMapper mapper;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	public SewerageServicesUtil(ServiceRequestRepository serviceRequestRepository) {
		this.serviceRequestRepository = serviceRequestRepository;

	}
	private String URL = "url";

	/**
	 * 
	 * @param sewarageConnectionRequest
	 *            SewarageConnectionRequest containing property
	 * @return List of Property
	 */

	public List<Property> propertySearch(SewerageConnectionRequest sewerageConnectionRequest) {
		Set<String> propertyIds = new HashSet<>();
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		HashMap<String, Object> propertyRequestObj = new HashMap<>();
		propertyIds.add(sewerageConnectionRequest.getSewerageConnection().getProperty().getPropertyId());
		propertyCriteria.setPropertyIds(propertyIds);
		propertyRequestObj.put("RequestInfoWrapper", getPropertyRequestInfoWrapperSearch(new RequestInfoWrapper(),
				sewerageConnectionRequest.getRequestInfo()));
		propertyRequestObj.put("PropertyCriteria", propertyCriteria);
		Object result = serviceRequestRepository.fetchResult(
				getPropURLForCreate(sewerageConnectionRequest.getSewerageConnection().getProperty().getTenantId(),
						sewerageConnectionRequest.getSewerageConnection().getProperty().getPropertyId()),
				RequestInfoWrapper.builder().requestInfo(sewerageConnectionRequest.getRequestInfo()).build());
		List<Property> propertyList = getPropertyDetails(result);
		if (CollectionUtils.isEmpty(propertyList)) {
			throw new CustomException("INCORRECT PROPERTY ID", "SEWERAGE CONNECTION CAN NOT BE CREATED");
		}
		return propertyList;
	}

	private RequestInfoWrapper getPropertyRequestInfoWrapperSearch(RequestInfoWrapper requestInfoWrapper,
			RequestInfo requestInfo) {
		return RequestInfoWrapper.builder().requestInfo(requestInfo).build();
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
			throw new CustomException("PARSING ERROR", "The property json cannot be parsed");
		}
	}

	/*
	 * @param sewerageConnectionRequest
	 * 
	 * @return Created property list
	 */
	public List<Property> createPropertyRequest(SewerageConnectionRequest sewerageConnectionRequest) {
		List<Property> propertyList = new ArrayList<>();
		propertyList.add(sewerageConnectionRequest.getSewerageConnection().getProperty());
		PropertyRequest propertyReq = getPropertyRequest(sewerageConnectionRequest.getRequestInfo(),
				sewerageConnectionRequest.getSewerageConnection().getProperty());
		return getPropertyDetails(serviceRequestRepository.fetchResult(getPropertyCreateURL(), propertyReq));
	}

	private PropertyRequest getPropertyRequest(RequestInfo requestInfo, Property propertyList) {
		return PropertyRequest.builder().requestInfo(requestInfo).property(propertyList).build();
	}

	/**
	 * 
	 * @param sewerageConnectionSearchCriteria
	 * @param requestInfo
	 * @return
	 */

	public List<Property> propertySearchOnCriteria(SearchCriteria sewerageConnectionSearchCriteria,
			RequestInfo requestInfo) {
		if (StringUtils.isEmpty(sewerageConnectionSearchCriteria.getMobileNumber())) {
			return Collections.emptyList();
		}
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		propertyCriteria.setMobileNumber(sewerageConnectionSearchCriteria.getMobileNumber());

		if (!StringUtils.isEmpty(sewerageConnectionSearchCriteria.getTenantId())) {
			propertyCriteria.setTenantId(sewerageConnectionSearchCriteria.getTenantId());
		}

		Object result = serviceRequestRepository.fetchResult(
				getPropURL(sewerageConnectionSearchCriteria.getTenantId(),
						sewerageConnectionSearchCriteria.getMobileNumber()),
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
	 * @return search url for property search
	 */
	private String getpropertySearchURLForMobileSearch() {
		StringBuilder url = new StringBuilder(getPropertyURL());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("mobileNumber=");
		url.append("{2}");
		return url.toString();
	}

	/**
	 * 
	 * @return search url for property search
	 */
	private String getpropertySearchURLForMobileSearchCitizen() {
		StringBuilder url = new StringBuilder(getPropertyURL());
		url.append("?");
		url.append("mobileNumber=");
		url.append("{2}");
		return url.toString();
	}

	private StringBuilder getPropURL(String tenantId, String mobileNumber) {
		String url = getpropertySearchURLForMobileSearchCitizen();
		if (tenantId != null)
			url = getpropertySearchURLForMobileSearch();
		if (url.indexOf("{1}") > 0)
			url = url.replace("{1}", tenantId);
		if (url.indexOf("{2}") > 0)
			url = url.replace("{2}", mobileNumber);
		return new StringBuilder(url);
	}

	/**
	 * 
	 * @return search url for property search employee
	 */
	private String getPropertySearchURLForEmployee() {
		StringBuilder url = new StringBuilder(getPropertyURL());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("propertyIds=");
		url.append("{2}");
		return url.toString();
	}

	/**
	 * 
	 * @return search url for property search citizen
	 */
	private String getPropertySearchURLForCitizen() {
		StringBuilder url = new StringBuilder(getPropertyURL());
		url.append("?");
		url.append("propertyIds=");
		url.append("{2}");
		return url.toString();
	}

	private StringBuilder getPropURLForCreate(String tenantId, String propertyIds) {
		String url = getPropertySearchURLForCitizen();
		if (tenantId != null)
			url = getPropertySearchURLForEmployee();
		if (url.indexOf("{1}") > 0)
			url = url.replace("{1}", tenantId);
		if (url.indexOf("{2}") > 0)
			url = url.replace("{2}", propertyIds);
		return new StringBuilder(url);
	}

	/**
	 * 
	 * @param tenantId
	 * @param propertyId
	 * @param requestInfo
	 * @return List of Property
	 */
	public List<Property> searchPropertyOnId(String tenantId, String propertyIds, RequestInfo requestInfo) {

		Object result = serviceRequestRepository.fetchResult(getPropURLForCreate(tenantId, propertyIds),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());
		return getPropertyDetails(result);
	}

	/**
	 * Method to return auditDetails for create/update flows
	 *
	 * @param by
	 * @param isCreate
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

	public boolean getStatusForUpdate(BusinessService businessService, SewerageConnection searchresult) {
		return workflowService.isStateUpdatable(searchresult.getApplicationStatus().name(), businessService);
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

	public String getShortnerURL(String actualURL) {
		JSONObject obj = new JSONObject();
		obj.put(URL, actualURL);
		String url = config.getNotificationUrl() + config.getShortenerURL();

		Object response = serviceRequestRepository.getShortningURL(new StringBuilder(url), obj);
		return response.toString();
	}

}
