package org.egov.bpa.calculator.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.bpa.calculator.config.BPACalculatorConfig;
import org.egov.bpa.calculator.repository.ServiceRequestRepository;
import org.egov.bpa.calculator.utils.BPACalculatorConstants;
import org.egov.bpa.calculator.web.models.RequestInfoWrapper;
import org.egov.bpa.calculator.web.models.bpa.BPA;
import org.egov.bpa.calculator.web.models.bpa.BPA.RiskTypeEnum;
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
import com.jayway.jsonpath.TypeRef;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EDCRService {

	private ServiceRequestRepository serviceRequestRepository;

	private ObjectMapper mapper;

	private BPACalculatorConfig config;
	
	

	@Autowired
	public EDCRService(ServiceRequestRepository serviceRequestRepository,
			ObjectMapper mapper, BPACalculatorConfig config) {
		this.serviceRequestRepository = serviceRequestRepository;
		this.mapper = mapper;
		this.config = config;
	}
	
	/**
	 * fetch EDCR Data
	 * 
	 * @param EDCRNumber
	 * 
	 */
	public Map getEDCRPlanData(BPA bpa) {

		HashMap calculationData = new HashMap();
		
		StringBuilder uri = new StringBuilder(config.getEdcrHost());
		
		uri.append(config.getPlanEndPoint());
		uri.append("?").append("tenantId=").append(bpa.getTenantId());
		uri.append("&").append("edcrNumber=").append(bpa.getEdcrNumber());
		RequestInfo edcrRequestInfo = new RequestInfo();
		
		Object responseMap = null;
		Object bpaAdditionalDetails = bpa.getAdditionalDetails();
		
		
		
		try {
			 responseMap = serviceRequestRepository.fetchResult(uri,
					new RequestInfoWrapper(edcrRequestInfo));
		}catch( ServiceCallException se) {
			throw new CustomException("EDCR ERROR", " EDCR Number is Invalid");
		}
//		
//		if (CollectionUtils.isEmpty(responseMap))
//			throw new CustomException("EDCR ERROR", "The response from EDCR service is empty or null");

//		String jsonString = new JSONObject(responseMap).toString();
//		DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
//		List<String> edcrStatus = context.read("edcrDetail.*.status");
//		List<String> OccupancyTypes = context.read("edcrDetail.*.planDetail.virtualBuilding.occupancyTypes.*.type.code");
		TypeRef<List<Double>> typeRef = new TypeRef<List<Double>>() {};
		TypeRef<List<List>> blockstypeRef = new TypeRef<List<List>>() {};
		
		
//		List<Double> plotAreas = context.read("edcrDetail.*.planDetail.plot.area",typeRef);
		List<Double> plotAreas =JsonPath.read(responseMap, "$.edcrDetail.*.planDetail.plot.area");
		
//		List<List> blocks = context.read("edcrDetail.*.planDetail.blocks",blockstypeRef);
		
		List<List> blocks =JsonPath.read(responseMap, "$.edcrDetail.*.planDetail.blocks");
		
//		List<Double> buildingHeights = context.read("edcrDetail.*.planDetail.blocks.*.building.buildingHeight",typeRef);
		
		List<Double> buildingHeights =JsonPath.read(responseMap, "$.edcrDetail.*.planDetail.blocks.*.building.buildingHeight");
		
//		List<Double> totalBuiltUpArea = context.read("edcrDetail.*.planDetail.virtualBuilding.totalBuitUpArea",typeRef);
		
		List<Double> totalBuiltupAreas =JsonPath.read(responseMap, "$.edcrDetail.*.planDetail.virtualBuilding.totalBuitUpArea");
		
		calculationData.put(BPACalculatorConstants.BUILT_UP_AREA, totalBuiltupAreas.get(0));
		calculationData.put(BPACalculatorConstants.BUILDING_HEIGHT, buildingHeights.get(0));
		calculationData.put("BLOCKS", blocks.get(0));
		return calculationData;
		
		
	}
}
