package org.egov.bpa.calculator.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.egov.bpa.calculator.config.BPACalculatorConfig;
import org.egov.bpa.calculator.repository.ServiceRequestRepository;
import org.egov.bpa.calculator.web.models.RequestInfoWrapper;
import org.egov.bpa.calculator.web.models.bpa.BPA;
import org.egov.bpa.calculator.web.models.bpa.BPAResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

@Service
public class EDCRService {

	@Autowired
	 private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private BPACalculatorConfig config;

	@Autowired
	private ObjectMapper mapper;
	
	@SuppressWarnings("rawtypes")
	public LinkedHashMap getEDCRDetails(RequestInfo requestInfo, BPA bpa) {

		String edcrNo = bpa.getEdcrNumber();
		StringBuilder uri = new StringBuilder(config.getEdcrHost());

		uri.append(config.getGetPlanEndPoint());
		uri.append("?").append("tenantId=").append(bpa.getTenantId());
		uri.append("&").append("edcrNumber=").append(edcrNo);
		RequestInfo edcrRequestInfo = new RequestInfo();
		BeanUtils.copyProperties(requestInfo, edcrRequestInfo);
		edcrRequestInfo.setUserInfo(null); // since EDCR service is not
											// accepting userInfo
		LinkedHashMap responseMap = null;
		try {
			responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
					new RequestInfoWrapper(edcrRequestInfo));
		} catch (ServiceCallException se) {
			throw new CustomException("EDCR ERROR", " EDCR Number is Invalid");
		}

		if (CollectionUtils.isEmpty(responseMap))
			throw new CustomException("EDCR ERROR", "The response from EDCR service is empty or null");

		return responseMap;
	}
	
	public BPA getBuildingPlan(RequestInfo requestInfo, String approvalNo, String tenantId) {
		StringBuilder url = getBPASearchURL();
		url.append("tenantId=");
		url.append(tenantId);
		url.append("&");
		url.append("approvalNo=");
		url.append(approvalNo);
		LinkedHashMap responseMap = null;
		responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(url, new RequestInfoWrapper(requestInfo));

		BPA bpaResponse = null;

		String jsonString = new JSONObject(responseMap).toString();
		DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
		ArrayList<BPA> bpa = context.read("Bpa");
		if (CollectionUtils.isEmpty(bpa))
			return null;
		try {
			bpaResponse = mapper.convertValue(bpa.get(0), BPA.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Error while parsing response of TradeLicense Search");
		}

		return bpaResponse;
	}

	private StringBuilder getBPASearchURL() {
		// TODO Auto-generated method stub
		StringBuilder url = new StringBuilder(config.getBpaHost());
	      url.append(config.getBpaContextPath());
	      url.append(config.getBpaSearchEndpoint());
	      url.append("?");
	      return url;
	}
}
