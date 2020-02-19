package org.egov.swService.validator;

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
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.repository.ServiceRequestRepository;
import org.egov.swService.util.SewerageServicesUtil;
import org.egov.swService.util.SWConstants;
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
		Map<String, String> errorMap = new HashMap<>();

		String jsonPath = SWConstants.JSONPATH_ROOT;
		String taxjsonPath = SWConstants.TAX_JSONPATH_ROOT;
		String tenantId = request.getRequestInfo().getUserInfo().getTenantId();
		String[] masterNames = {SWConstants.MDMS_SW_Connection_Type };
		List<String> names = new ArrayList<>(Arrays.asList(masterNames));
		List<String> taxModelnames = new ArrayList<>(Arrays.asList(SWConstants.SC_ROADTYPE_MASTER));
		Map<String, List<String>> codes = getAttributeValues(tenantId, SWConstants.MDMS_SW_MOD_NAME, names, "$.*.code",
				jsonPath, request.getRequestInfo());
		Map<String, List<String>> codeFromCalculatorMaster = getAttributeValues(tenantId, SWConstants.SW_TAX_MODULE,
				taxModelnames, "$.*.code", taxjsonPath, request.getRequestInfo());
		// merge codes
		String[] finalmasterNames = { SWConstants.MDMS_SW_Connection_Type, SWConstants.SC_ROADTYPE_MASTER };
		Map<String, List<String>> finalcodes = Stream.of(codes, codeFromCalculatorMaster).map(Map::entrySet)
				.flatMap(Collection::stream).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		validateMDMSData(finalmasterNames, finalcodes);
		validateCodes(request.getSewerageConnection(), finalcodes, errorMap);
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
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

	private static Map<String, String> validateCodes(SewerageConnection sewerageConnection,
			Map<String, List<String>> codes, Map<String, String> errorMap) {
		StringBuilder messageBuilder = new StringBuilder();
		if (!codes.get(SWConstants.MDMS_SW_Connection_Type).contains(sewerageConnection.getConnectionType())
				&& sewerageConnection.getConnectionType() != null) {
			messageBuilder = new StringBuilder();
			messageBuilder.append("The SewerageConnection connection type ")
					.append(sewerageConnection.getConnectionType()).append(" does not exists");
			errorMap.put("INVALID SEWERAGE CONNECTION TYPE", "The SewerageConnection connection type '"
					+ sewerageConnection.getConnectionType() + "' does not exists");
		}
		if (sewerageConnection.getRoadType() != null
				&& !codes.get(SWConstants.SC_ROADTYPE_MASTER).contains(sewerageConnection.getRoadType())) {
			messageBuilder = new StringBuilder();
			messageBuilder.append("The SewerageConnection road type ").append(sewerageConnection.getRoadType())
					.append(" does not exists");
			errorMap.put("INVALID_WATER_ROAD_TYPE", messageBuilder.toString());
		}
		return errorMap;
	}

}
