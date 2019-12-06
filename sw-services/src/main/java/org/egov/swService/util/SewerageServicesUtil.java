package org.egov.swService.util;

import java.util.ArrayList;
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

import org.egov.swService.model.Property;
import org.egov.swService.model.PropertyCriteria;
import org.egov.swService.model.PropertyRequest;
import org.egov.swService.model.PropertyResponse;
import org.egov.swService.model.RequestInfoWrapper;
import org.egov.swService.model.SearchCriteria;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SewerageServicesUtil {

	private ServiceRequestRepository serviceRequestRepository;

	@Value("${egov.property.service.host}")
	private String propertyHost;

	@Value("${egov.property.createendpoint}")
	private String createPropertyEndPoint;

	@Value("${egov.property.searchendpoint}")
	private String searchPropertyEndPoint;

	@Autowired
	public SewerageServicesUtil(ServiceRequestRepository serviceRequestRepository) {
		this.serviceRequestRepository = serviceRequestRepository;

	}

	/**
	 * 
	 * @param sewarageConnectionRequest
	 *            SewarageConnectionRequest containing property
	 * @return List of Property
	 */
	
	public List<Property> propertySearch(SewerageConnectionRequest sewerageConnectionRequest) {
		Set<String> propertyIds = new HashSet<>();
		List<Property> propertyList = new ArrayList<>();
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		HashMap<String, Object> propertyRequestObj = new HashMap<>();
		propertyIds.add(sewerageConnectionRequest.getSewerageConnection().getProperty().getPropertyId());
		propertyCriteria.setPropertyIds(propertyIds);
		propertyRequestObj.put("RequestInfoWrapper",
				getPropertyRequestInfoWrapperSearch(new RequestInfoWrapper(), sewerageConnectionRequest.getRequestInfo()));
		propertyRequestObj.put("PropertyCriteria", propertyCriteria);
		Object result = serviceRequestRepository.fetchResult(
				getPropURLForCreate(sewerageConnectionRequest.getSewerageConnection().getProperty().getTenantId(),
						sewerageConnectionRequest.getSewerageConnection().getProperty().getPropertyId()),
				RequestInfoWrapper.builder().requestInfo(sewerageConnectionRequest.getRequestInfo()).build());
		propertyList = getPropertyDetails(result);
		if (propertyList == null || propertyList.isEmpty()) {
			throw new CustomException("INCORRECT PROPERTY ID", "WATER CONNECTION CAN NOT BE CREATED");
		}
		return propertyList;
	}

	private RequestInfoWrapper getPropertyRequestInfoWrapperSearch(RequestInfoWrapper requestInfoWrapper,
			RequestInfo requestInfo) {
		RequestInfoWrapper requestInfoWrapper_new = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		return requestInfoWrapper_new;
	}

	public StringBuilder getPropertySearchURL() {
		return new StringBuilder().append(propertyHost).append(searchPropertyEndPoint);
	}

	/**
	 * 
	 * @param result
	 *            Response object from property service call
	 * @return List of property
	 */
	private List<Property> getPropertyDetails(Object result) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			PropertyResponse propertyResponse = mapper.convertValue(result, PropertyResponse.class);
			return propertyResponse.getProperties();
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
		Object result = serviceRequestRepository.fetchResult(getPropertyCreateURL(), propertyReq);
		return getPropertyDetails(result);
	}
	
	private PropertyRequest getPropertyRequest(RequestInfo requestInfo, Property propertyList) {
		PropertyRequest propertyReq = PropertyRequest.builder().requestInfo(requestInfo).property(propertyList).build();
		return propertyReq;
	}

	public StringBuilder getPropertyCreateURL() {
		return new StringBuilder().append(propertyHost).append(createPropertyEndPoint);
	}

	private PropertyRequest getPropertyRequest(RequestInfo requestInfo, List<Property> propertyList) {
		PropertyRequest propertyReq = PropertyRequest.builder().requestInfo(requestInfo).properties(propertyList)
				.build();
		return propertyReq;
	}

	/**
	 * 
	 * @param sewerageConnectionSearchCriteria
	 * @param requestInfo
	 * @return
	 */
	public List<Property> propertySearchOnCriteria(SearchCriteria sewerageConnectionSearchCriteria,
			RequestInfo requestInfo) {
		if ((sewerageConnectionSearchCriteria.getTenantId() == null
				|| sewerageConnectionSearchCriteria.getTenantId().isEmpty())) {
			throw new CustomException("INVALID SEARCH", "TENANT ID NOT PRESENT");
		}
		HashMap<String, Object> propertyRequestObj = new HashMap<>();
		RequestInfoWrapper requestInfoWrapper = new RequestInfoWrapper();
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		if (sewerageConnectionSearchCriteria.getTenantId() != null
				&& !sewerageConnectionSearchCriteria.getTenantId().isEmpty()) {
			propertyCriteria.setTenantId(sewerageConnectionSearchCriteria.getTenantId());
		}
		if (sewerageConnectionSearchCriteria.getMobileNumber() != null
				&& !sewerageConnectionSearchCriteria.getMobileNumber().isEmpty()) {
			propertyCriteria.setMobileNumber(sewerageConnectionSearchCriteria.getMobileNumber());
		}
		// if (!waterConnectionSearchCriteria.getIds().isEmpty()) {
		// propertyCriteria.setIds(waterConnectionSearchCriteria.getIds());
		// }
		requestInfoWrapper.setRequestInfo(requestInfo);
		propertyRequestObj.put("RequestInfoWrapper",
				getPropertyRequestInfoWrapperSearch(new RequestInfoWrapper(), requestInfo));
		propertyRequestObj.put("PropertyCriteria", propertyCriteria);
		Object result = serviceRequestRepository.fetchResult(getPropertySearchURL(), propertyRequestObj);
		return getPropertyDetails(result);
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
	private String getPropertySearchURL() {
		StringBuilder url = new StringBuilder(getPropertyURL());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("mobileNumber=");
		url.append("{2}");
		return url.toString();
	}

	private StringBuilder getPropURL(String tenantId, String mobileNumber) {
		String url = getPropertySearchURL();
		url = url.replace("{1}", tenantId).replace("{2}", mobileNumber);
		return new StringBuilder(url);
	}
	
	/**
	 * 
	 * @return search url for property search
	 */
	private String getPropertySearchURLForCreate() {
		StringBuilder url = new StringBuilder(getPropertyURL());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("propertyIds=");
		url.append("{2}");
		return url.toString();
	}
	
	
	private StringBuilder getPropURLForCreate(String tenantId, String propertyIds) {
		String url = getPropertySearchURLForCreate();
		url = url.replace("{1}", tenantId).replace("{2}", propertyIds);
		return new StringBuilder(url);
	}

}
