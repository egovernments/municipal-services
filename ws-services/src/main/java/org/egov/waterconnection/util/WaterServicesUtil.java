package org.egov.waterconnection.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.egov.waterconnection.config.WSConfiguration;
import org.egov.waterconnection.model.AuditDetails;
import org.egov.waterconnection.model.Property;
import org.egov.waterconnection.model.PropertyCriteria;
import org.egov.waterconnection.model.PropertyResponse;
import org.egov.waterconnection.model.RequestInfoWrapper;
import org.egov.waterconnection.model.SearchCriteria;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.egov.waterconnection.model.workflow.BusinessService;
import org.egov.waterconnection.repository.ServiceRequestRepository;
import org.egov.waterconnection.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONObject;

@Component
public class WaterServicesUtil {

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private WSConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	@Value("${egov.property.service.host}")
	private String propertyHost;

	@Value("${egov.property.createendpoint}")
	private String createPropertyEndPoint;

	@Value("${egov.property.searchendpoint}")
	private String searchPropertyEndPoint;
	

	@Autowired
	public WaterServicesUtil(ServiceRequestRepository serviceRequestRepository) {
		this.serviceRequestRepository = serviceRequestRepository;

	}
	@Autowired
	private WorkflowService workflowService;
	
	private String tenantId = "tenantId=";
	private String mobileNumber = "mobileNumber=";
	private String propertyIds = "propertyIds=";
	private String uuids = "uuids=";
	private String URL = "url";
	

	/**
     * Method to return auditDetails for create/update flows
     *
     * @param by
     * @param isCreate
     * @return AuditDetails
     */
    public AuditDetails getAuditDetails(String by, Boolean isCreate) {
        Long time = System.currentTimeMillis();
        if(isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
    }
    
	/**
	 * 
	 * @param waterConnectionRequest
	 *            WaterConnectionRequest containing property
	 * @return List of Property
	 */
	public List<Property> propertySearch(WaterConnectionRequest waterConnectionRequest) {
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		HashSet<String> propertyUUID = new HashSet<>();
		propertyUUID.add(waterConnectionRequest.getWaterConnection().getPropertyId());
		propertyCriteria.setUuids(propertyUUID);
		if (waterConnectionRequest.getRequestInfo().getUserInfo() != null
				&& "EMPLOYEE".equalsIgnoreCase(waterConnectionRequest.getRequestInfo().getUserInfo().getType())) {
			propertyCriteria.setTenantId(waterConnectionRequest.getWaterConnection().getTenantId());
		}
		Object result = serviceRequestRepository.fetchResult(
				getPropertyURL(propertyCriteria),
				RequestInfoWrapper.builder().requestInfo(waterConnectionRequest.getRequestInfo()).build());
		List<Property> propertyList = getPropertyDetails(result);
		if (CollectionUtils.isEmpty(propertyList)) {
			throw new CustomException("INCORRECT PROPERTY ID", "WATER CONNECTION CAN NOT BE CREATED");
		}
		return propertyList;
	}
	
	/**
	 * 
	 * @param waterConnectionSearchCriteria
	 *            WaterConnectionSearchCriteria containing search criteria on
	 *            water connection
	 * @param requestInfo
	 * @return List of property matching on given criteria
	 */
	public List<Property> propertySearchOnCriteria(SearchCriteria waterConnectionSearchCriteria,
			RequestInfo requestInfo) {
		if (StringUtils.isEmpty(waterConnectionSearchCriteria.getMobileNumber())) {
			return Collections.emptyList();
		}
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		if (!StringUtils.isEmpty(waterConnectionSearchCriteria.getTenantId())) {
			propertyCriteria.setTenantId(waterConnectionSearchCriteria.getTenantId());
		}
		if (!StringUtils.isEmpty(waterConnectionSearchCriteria.getMobileNumber())) {
			propertyCriteria.setMobileNumber(waterConnectionSearchCriteria.getMobileNumber());
		}
		if (!StringUtils.isEmpty(waterConnectionSearchCriteria.getPropertyId())) {
			HashSet<String> propertyIds = new HashSet<>();
			propertyIds.add(waterConnectionSearchCriteria.getPropertyId());
			propertyCriteria.setPropertyIds(propertyIds);
		}
		return getPropertyDetails(serviceRequestRepository.fetchResult(getPropertyURL(propertyCriteria),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build()));
	}
	
	/**
	 * 
	 * @param tenantId
	 * @param propertyId
	 * @param requestInfo
	 * @return List of Property
	 */
	public List<Property> searchPropertyOnId(PropertyCriteria criteria, RequestInfo requestInfo) {
		return getPropertyDetails(serviceRequestRepository.fetchResult(getPropertyURL(criteria),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build()));
	}
	
	/**
	 * 
	 * @param result
	 *            Response object from property service call
	 * @return List of property
	 */
	private List<Property> getPropertyDetails(Object result) {

		try {
			PropertyResponse propertyResponse = objectMapper.convertValue(result, PropertyResponse.class);
			return propertyResponse.getProperties();
		} catch (Exception ex) {
			throw new CustomException("PARSING ERROR", "The property json cannot be parsed");
		}
	}

	public StringBuilder getPropertyCreateURL() {
		return new StringBuilder().append(propertyHost).append(createPropertyEndPoint);
	}

	public StringBuilder getPropertyURL() {
		return new StringBuilder().append(propertyHost).append(searchPropertyEndPoint);
	}

	public MdmsCriteriaReq prepareMdMsRequest(String tenantId, String moduleName, List<String> names, String filter,
			RequestInfo requestInfo) {
		List<MasterDetail> masterDetails = new ArrayList<>();
		names.forEach(name -> {
			masterDetails.add(MasterDetail.builder().name(name).filter(filter).build());
		});
		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(moduleName).masterDetails(masterDetails).build();
		List<ModuleDetail> moduleDetails = new ArrayList<>();
		moduleDetails.add(moduleDetail);
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	/**
	 * 
	 * @param criteria
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
	 * @param businessService
	 * @param searchresult
	 * @return true if state updatable is true else false
	 */
	public boolean getStatusForUpdate(BusinessService businessService, String applicationStatus) {
		return workflowService.isStateUpdatable(applicationStatus, businessService);
	}
	/**
	 * 
	 * @return URL of calculator service
	 */
	public StringBuilder getCalculatorURL() {
		StringBuilder builder = new StringBuilder();
		return builder.append(config.getCalculatorHost()).append(config.getCalculateEndpoint());
	}
	
	/**
	 * 
	 * @return URL of estimation service
	 */
	public StringBuilder getEstimationURL() {
		StringBuilder builder = new StringBuilder();
		return builder.append(config.getCalculatorHost()).append(config.getEstimationEndpoint());
	}
	
	
	/**
	 * 
	 * @return URL for create meterreading
	 */
	public StringBuilder getMeterReadingCreateURL() {
		StringBuilder builder = new StringBuilder();
		return builder.append(config.getCalculatorHost()).append(config.getCreateMeterReadingEndpoint());
	}
	
	public String getShortnerURL(String actualURL) {
		JSONObject obj = new JSONObject();
		obj.put(URL, actualURL);
		String url = config.getNotificationUrl() + config.getShortenerURL();
		
		Object response = serviceRequestRepository.getShortningURL(new StringBuilder(url), obj);
		return response.toString();
	}
}
