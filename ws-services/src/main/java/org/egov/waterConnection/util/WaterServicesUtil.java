package org.egov.waterConnection.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.config.WSConfiguration;
import org.egov.waterConnection.model.AuditDetails;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.PropertyCriteria;
import org.egov.waterConnection.model.PropertyRequest;
import org.egov.waterConnection.model.PropertyResponse;
import org.egov.waterConnection.model.RequestInfoWrapper;
import org.egov.waterConnection.model.SearchCriteria;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.workflow.BusinessService;
import org.egov.waterConnection.repository.ServiceRequestRepository;
import org.egov.waterConnection.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WaterServicesUtil {

	@Autowired
	ObjectMapper objectMapper;
	
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
		Set<String> propertyIds = new HashSet<>();
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		HashMap<String, Object> propertyRequestObj = new HashMap<>();
		propertyIds.add(waterConnectionRequest.getWaterConnection().getProperty().getPropertyId());
		propertyCriteria.setPropertyIds(propertyIds);
		propertyRequestObj.put("RequestInfoWrapper",
				getPropertyRequestInfoWrapperSearch(new RequestInfoWrapper(), waterConnectionRequest.getRequestInfo()));
		propertyRequestObj.put("PropertyCriteria", propertyCriteria);
		Object result = serviceRequestRepository.fetchResult(
				getPropURLForCreate(waterConnectionRequest.getWaterConnection().getProperty().getTenantId(),
						waterConnectionRequest.getWaterConnection().getProperty().getPropertyId()),
				RequestInfoWrapper.builder().requestInfo(waterConnectionRequest.getRequestInfo()).build());
		List<Property> propertyList = getPropertyDetails(result);
		if (CollectionUtils.isEmpty(propertyList)) {
			throw new CustomException("INCORRECT PROPERTY ID", "WATER CONNECTION CAN NOT BE CREATED");
		}
		return propertyList;
	}
	
	/**
	 * 
	 * @param waterConnectionRequest
	 * @return Created property list
	 */
	public List<Property> createPropertyRequest(WaterConnectionRequest waterConnectionRequest) {
		List<Property> propertyList = new ArrayList<>();
		propertyList.add(waterConnectionRequest.getWaterConnection().getProperty());
		PropertyRequest propertyReq = getPropertyRequest(waterConnectionRequest.getRequestInfo(),
				waterConnectionRequest.getWaterConnection().getProperty());
		return getPropertyDetails(serviceRequestRepository.fetchResult(getPropertyCreateURL(), propertyReq));
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
		// if ((waterConnectionSearchCriteria.getTenantId() == null
		// || waterConnectionSearchCriteria.getTenantId().isEmpty())) {
		// throw new CustomException("INVALID SEARCH", "TENANT ID NOT PRESENT");
		// }
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
		return getPropertyDetails(serviceRequestRepository.fetchResult(
				getPropURL(waterConnectionSearchCriteria.getTenantId(),
						waterConnectionSearchCriteria.getMobileNumber()),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build()));
	}
	
	/**
	 * 
	 * @param tenantId
	 * @param propertyId
	 * @param requestInfo
	 * @return List of Property
	 */
	public List<Property> searchPropertyOnId(String tenantId, String propertyIds, RequestInfo requestInfo) {
		return getPropertyDetails(serviceRequestRepository.fetchResult(getPropURLForCreate(tenantId, propertyIds),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build()));
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
			PropertyResponse propertyResponse = objectMapper.convertValue(result, PropertyResponse.class);
			return propertyResponse.getProperties();
		} catch (Exception ex) {
			throw new CustomException("PARSING ERROR", "The property json cannot be parsed");
		}
	}

	private PropertyRequest getPropertyRequest(RequestInfo requestInfo, Property propertyList) {
		return PropertyRequest.builder().requestInfo(requestInfo).property(propertyList).build();
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
	 * @return search url for property search
	 */
	private String getpropertySearchURLForMobileSearch() {
		StringBuilder url = new StringBuilder(getPropertyURL());
		url.append("?");
		url.append(tenantId);
		url.append("{1}");
		url.append("&");
		url.append(mobileNumber);
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
		url.append(mobileNumber);
		url.append("{2}");
		return url.toString();
	}

	private StringBuilder getPropURL(String tenantId, String mobileNumber) {
		String url = getpropertySearchURLForMobileSearchCitizen();
		if(tenantId != null)
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
		url.append(tenantId);
		url.append("{1}");
		url.append("&");
		url.append(propertyIds);
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
		url.append(propertyIds);
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
	 * @param businessService
	 * @param searchresult
	 * @return true if state updatable is true else false
	 */
	public boolean getStatusForUpdate(BusinessService businessService, WaterConnection searchresult) {
		return workflowService.isStateUpdatable(searchresult.getApplicationStatus().name(), businessService);
	}
	/**
	 * 
	 * @return URL of calculator service
	 */
	public StringBuilder getCalculatorURL() {
		StringBuilder builder = new StringBuilder();
		return builder.append(config.getCalculatorHost()).append(config.getCalculateEndpoint());
	}
}
