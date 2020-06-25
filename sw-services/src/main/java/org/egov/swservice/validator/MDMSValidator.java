package org.egov.swservice.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.tracer.model.CustomException;
import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.repository.ServiceRequestRepository;
import org.egov.swservice.util.SWConstants;
import org.egov.swservice.util.SewerageServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MDMSValidator {
	@Autowired
	private SewerageServicesUtil sewerageServicesUtil;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndpoint;

	public void validateMasterData(SewerageConnectionRequest request) {
		if (request.getSewerageConnection().getProcessInstance().getAction().equalsIgnoreCase(SWConstants.ACTIVATE_CONNECTION_CONST)){
		Map<String, String> errorMap = new HashMap<>();
		List<String> names = new ArrayList<>(Arrays.asList(SWConstants.MDMS_SW_Connection_Type));
		List<String> taxModelnames = new ArrayList<>(Arrays.asList(SWConstants.SC_ROADTYPE_MASTER));
		Map<String, List<String>> codes = getAttributeValues(request.getSewerageConnection().getTenantId(), 
				SWConstants.MDMS_SW_MOD_NAME, names, "$.*.code",
				SWConstants.JSONPATH_ROOT, request.getRequestInfo());
		Map<String, List<String>> codeFromCalculatorMaster = getAttributeValues(request.getSewerageConnection().getTenantId(), 
				SWConstants.SW_TAX_MODULE, taxModelnames, "$.*.code", 
				SWConstants.TAX_JSONPATH_ROOT, request.getRequestInfo());
		// merge codes
		
		Map<String, List<String>> finalcodes = Stream.of(codes, codeFromCalculatorMaster).map(Map::entrySet)
				.flatMap(Collection::stream).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		validateMDMSData(finalcodes);
		validateCodes(request.getSewerageConnection(), finalcodes, errorMap);
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	     }
	}


	private Map<String, List<String>> getAttributeValues(String tenantId, String moduleName, List<String> names,
			String filter, String jsonpath, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder(mdmsHost).append(mdmsEndpoint);
		MdmsCriteriaReq criteriaReq = sewerageServicesUtil.prepareMdMsRequest(tenantId, moduleName, names, filter,
				requestInfo);
		try {

			Object result = serviceRequestRepository.fetchResult(uri, criteriaReq);
			return JsonPath.read(result, jsonpath);
		} catch (Exception e) {
			log.error("Error while fetching MDMS data", e);
			throw new CustomException(SWConstants.INVALID_CONNECTION_TYPE, SWConstants.INVALID_CONNECTION_TYPE);
		}
	}

	private void validateMDMSData(Map<String, List<String>> codes) {
		String[] masterNames = { SWConstants.MDMS_SW_Connection_Type, SWConstants.SC_ROADTYPE_MASTER };
		Map<String, String> errorMap = new HashMap<>();
		for (String masterName : masterNames) {
			if (CollectionUtils.isEmpty(codes.get(masterName))) {
				errorMap.put("MDMS DATA ERROR ", "Unable to fetch " + masterName + " codes from MDMS");
			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	private static Map<String, String> validateCodes(SewerageConnection sewerageConnection,
			Map<String, List<String>> codes, Map<String, String> errorMap) {
		StringBuilder messageBuilder = null;
		if (sewerageConnection.getConnectionType() != null 
				&& !codes.get(SWConstants.MDMS_SW_Connection_Type).contains(sewerageConnection.getConnectionType())) {
			messageBuilder = new StringBuilder();
			messageBuilder.append("Connection type value is invalid, please enter proper value! ");
			errorMap.put("INVALID SEWERAGE CONNECTION TYPE", messageBuilder.toString());
		}
		if (sewerageConnection.getRoadType() != null
				&& !codes.get(SWConstants.SC_ROADTYPE_MASTER).contains(sewerageConnection.getRoadType())) {
			messageBuilder = new StringBuilder();
			messageBuilder.append("Road type value is invalid, please enter proper value! ");
			errorMap.put("INVALID_WATER_ROAD_TYPE", messageBuilder.toString());
		}
		return errorMap;
	}

}
