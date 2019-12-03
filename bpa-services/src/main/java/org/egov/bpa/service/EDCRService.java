package org.egov.bpa.service;

import java.util.LinkedHashMap;
import java.util.List;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.edcr.RequestInfoWrapper;
import org.egov.bpa.web.models.edcr.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@Service
public class EDCRService {

	private ServiceRequestRepository serviceRequestRepository;

	private ObjectMapper mapper;

	private BPAConfiguration config;
	
	private String edcrTenantId="jupiter";

	@Autowired
	public EDCRService(ServiceRequestRepository serviceRequestRepository,
			ObjectMapper mapper, BPAConfiguration config) {
		this.serviceRequestRepository = serviceRequestRepository;
		this.mapper = mapper;
		this.config = config;
	}

	/**
	 * Validates the EDCR Plan based on the edcr Number
	 * 
	 * @param request
	 *            BPARequest for create
	 * 
	 */
	public Boolean validateEdcrPlan(BPARequest request) {
		
		String edcrNo = request.getBPA().getEdcrNumber();
		StringBuilder uri = new StringBuilder(config.getEdcrHost());
		uri.append(config.getGetPlanEndPoint());
		uri.append("?").append("tenantId=").append(this.edcrTenantId);
		uri.append("&").append("edcrNumber=").append(edcrNo);
		RequestInfo edcrRequestInfo = new RequestInfo();
		BeanUtils.copyProperties(request.getRequestInfo(), edcrRequestInfo);
		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository
				.fetchResult(uri, new RequestInfoWrapper(edcrRequestInfo));
		if (CollectionUtils.isEmpty(responseMap))
			throw new CustomException("EDCR ERROR",
					"The response from EDCR service is empty or null");
		
		String jsonString = new JSONObject(responseMap).toString();

		DocumentContext context = JsonPath.parse(jsonString);
		List<String> edcrStatus = context.read("edcrDetail.*.status");
		
		if(CollectionUtils.isEmpty(edcrStatus) && !edcrStatus.get(0).equalsIgnoreCase("Accepted") ) {
			return Boolean.FALSE;
		}else {
			return Boolean.TRUE;
		}
		
		

		

		

	}
	
	
	
}
