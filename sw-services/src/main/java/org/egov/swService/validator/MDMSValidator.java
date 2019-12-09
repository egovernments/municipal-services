package org.egov.swService.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	;

	public void validateMasterData(SewerageConnectionRequest request) {
		Map<String, String> errorMap = new HashMap<>();

		String jsonPath = SWConstants.JSONPATH_ROOT;
		String tenantId = request.getRequestInfo().getUserInfo().getTenantId();

		String[] masterNames = { SWConstants.MDMS_SW_Connection_Type };
		List<String> names = new ArrayList<>(Arrays.asList(masterNames));
		Map<String, List<String>> codes = getAttributeValues(tenantId, SWConstants.MDMS_SW_MOD_NAME, names, "$.*.code",
				jsonPath, request.getRequestInfo());
		validateMDMSData(masterNames, codes);
		validateCodes(request.getSewerageConnection(), codes, errorMap);
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	private Map<String, List<String>> getAttributeValues(String tenantId, String moduleName, List<String> names,
			String filter, String jsonpath, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder(mdmsHost).append(mdmsEndpoint);
		// String module=moduleName;
		// String master=names.get(0);
		// String tenantId_new=requestInfo.getUserInfo().getTenantId();
		//
		MdmsCriteriaReq criteriaReq = sewerageServicesUtil.prepareMdMsRequest(tenantId, moduleName, names, filter,
				requestInfo);
		// Object
		// abc=criteriaReq.getMdmsCriteria().getModuleDetails().get(0).getMasterDetails().get(0).getClass();

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
		if (!codes.get(SWConstants.MDMS_SW_Connection_Type).contains(sewerageConnection.getConnectionType())
				&& sewerageConnection.getConnectionType() != null) {
			errorMap.put("INVALID SEWERAGE CONNECTION TYPE", "The SewerageConnection connection type '"
					+ sewerageConnection.getConnectionType() + "' does not exists");
		}
		return errorMap;
	}

}
