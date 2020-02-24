package org.egov.waterConnection.validator;

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
import org.egov.waterConnection.constants.WCConstants;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.repository.ServiceRequestRepository;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MDMSValidator {
	@Autowired
	private WaterServicesUtil waterServicesUtil;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndpoint;
    
	/**
	 * Validate Master data for given request
	 * 
	 * @param request
	 */
	public void validateMasterData(WaterConnectionRequest request) {
		if (request.getWaterConnection().getAction().equalsIgnoreCase(WCConstants.APPROVE_CONNECTION_CONST)) {
			String jsonPath = WCConstants.JSONPATH_ROOT;
			String taxjsonPath = WCConstants.TAX_JSONPATH_ROOT;
			String tenantId = request.getRequestInfo().getUserInfo().getTenantId();
			List<String> names = new ArrayList<>(Arrays.asList(WCConstants.MDMS_WC_Connection_Type, WCConstants.MDMS_WC_Connection_Category,
					WCConstants.MDMS_WC_Water_Source));
			List<String> taxModelnames = new ArrayList<>(Arrays.asList(WCConstants.WC_ROADTYPE_MASTER));
			Map<String, List<String>> codes = getAttributeValues(tenantId, WCConstants.MDMS_WC_MOD_NAME, names,
					"$.*.code", jsonPath, request.getRequestInfo());
			Map<String, List<String>> codeFromCalculatorMaster = getAttributeValues(tenantId, WCConstants.WS_TAX_MODULE,
					taxModelnames, "$.*.code", taxjsonPath, request.getRequestInfo());
			
			//merge codes
			String[] finalmasterNames = { WCConstants.MDMS_WC_Connection_Type, WCConstants.MDMS_WC_Connection_Category,
					WCConstants.MDMS_WC_Water_Source, WCConstants.WC_ROADTYPE_MASTER };
			Map<String, List<String>> finalcodes = Stream.of(codes, codeFromCalculatorMaster).map(Map::entrySet)
					.flatMap(Collection::stream).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			validateMDMSData(finalmasterNames, finalcodes);
			validateCodes(request.getWaterConnection(), finalcodes);
		}
	}

	private Map<String, List<String>> getAttributeValues(String tenantId, String moduleName, List<String> names,
			String filter, String jsonpath, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder(mdmsHost).append(mdmsEndpoint);
		MdmsCriteriaReq criteriaReq = waterServicesUtil.prepareMdMsRequest(tenantId, moduleName, names, filter,
				requestInfo);
		try {

			Object result = serviceRequestRepository.fetchResult(uri, criteriaReq);
			return JsonPath.read(result, jsonpath);
		} catch (Exception e) {
			log.error("Error while fetching MDMS data", e);
			throw new CustomException(WCConstants.INVALID_CONNECTION_TYPE, WCConstants.INVALID_CONNECTION_TYPE);
		}
	}
	


	private void validateMDMSData(String[] masterNames, Map<String, List<String>> codes) {
		Map<String, String> errorMap = new HashMap<>();
		for (String masterName : masterNames) {
			if (CollectionUtils.isEmpty(codes.get(masterName))) {
				errorMap.put("MDMS DATA ERROR ", "Unable to fetch " + masterName + " codes from MDMS");
			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
	
	/**
	 * validateCodes will validate for given fields and return error map if codes are not matching
	 * 
	 * @param waterConnection
	 * @param codes
	 * @param errorMap
	 * @return error map for given fields
	 */
	private void validateCodes(WaterConnection waterConnection, Map<String, List<String>> codes) {
		Map<String, String> errorMap = new HashMap<>();
		StringBuilder messageBuilder = new StringBuilder();
		if (!StringUtils.isEmpty(waterConnection.getConnectionType())
				&& !codes.get(WCConstants.MDMS_WC_Connection_Type).contains(waterConnection.getConnectionType())) {
			messageBuilder = new StringBuilder();
			messageBuilder.append("The WaterConnection connection type ").append(waterConnection.getConnectionType())
					.append(" does not exists");
			errorMap.put("INVALID_WATER_CONNECTION_TYPE", messageBuilder.toString());
		}
		if (!StringUtils.isEmpty(waterConnection.getConnectionCategory()) && !codes
				.get(WCConstants.MDMS_WC_Connection_Category).contains(waterConnection.getConnectionCategory())) {
			messageBuilder = new StringBuilder();
			messageBuilder.append("The WaterConnection connection category ")
					.append(waterConnection.getConnectionCategory()).append(" does not exists");
			errorMap.put("INVALID_WATER_CONNECTION_CATEGORY",
					"The WaterConnection connection category" + messageBuilder.toString());
		}
		if (!StringUtils.isEmpty(waterConnection.getWaterSource())
				&& !codes.get(WCConstants.MDMS_WC_Water_Source).contains(waterConnection.getWaterSource())) {
			messageBuilder = new StringBuilder();
			messageBuilder.append("The WaterConnection connection source ").append(waterConnection.getWaterSource())
					.append(" does not exists");
			errorMap.put("INVALID_WATER_CONNECTION_SOURCE", messageBuilder.toString());
		}
		if (!StringUtils.isEmpty(waterConnection.getRoadType())
				&& !codes.get(WCConstants.WC_ROADTYPE_MASTER).contains(waterConnection.getRoadType())) {
			messageBuilder = new StringBuilder();
			messageBuilder.append("The WaterConnection road type ").append(waterConnection.getRoadType())
					.append(" does not exists");
			errorMap.put("INVALID_WATER_ROAD_TYPE", messageBuilder.toString());
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
}
