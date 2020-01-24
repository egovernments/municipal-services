package org.egov.waterConnection.validator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	public void validateMasterData(WaterConnectionRequest request) {
		Map<String, String> errorMap = new HashMap<>();

		String jsonPath = WCConstants.JSONPATH_ROOT;
		String tenantId = request.getRequestInfo().getUserInfo().getTenantId();

		String[] masterNames = { WCConstants.MDMS_WC_Connection_Type, WCConstants.MDMS_WC_Connection_Category,
				WCConstants.MDMS_WC_Water_Source};
		List<String> names = new ArrayList<>(Arrays.asList(masterNames));
		Map<String, List<String>> codes = getAttributeValues(tenantId, WCConstants.MDMS_WC_MOD_NAME, names, "$.*.code",
				jsonPath, request.getRequestInfo());
		validateMDMSData(masterNames, codes);
		validateCodes(request.getWaterConnection(),codes,errorMap);
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	private Map<String, List<String>> getAttributeValues(String tenantId, String moduleName, List<String> names,
			String filter, String jsonpath, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder(mdmsHost).append(mdmsEndpoint);
//		String module=moduleName;
//		String master=names.get(0);
//		String tenantId_new=requestInfo.getUserInfo().getTenantId();
//		
		MdmsCriteriaReq criteriaReq = waterServicesUtil.prepareMdMsRequest(tenantId, moduleName, names, filter,
				requestInfo);
		//Object abc=criteriaReq.getMdmsCriteria().getModuleDetails().get(0).getMasterDetails().get(0).getClass();
		
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
	
	private static Map<String, String> validateCodes(WaterConnection waterConnection, Map<String, List<String>> codes,
			Map<String, String> errorMap) {
		if(!codes.get(WCConstants.MDMS_WC_Connection_Type).contains(waterConnection.getConnectionType()) && waterConnection.getConnectionType() != null) {
			errorMap.put("INVALID WATER CONNECTION TYPE",
					"The WaterConnection connection type '" + waterConnection.getConnectionType() + "' does not exists");
		}
		if(!codes.get(WCConstants.MDMS_WC_Connection_Category).contains(waterConnection.getConnectionCategory()) && waterConnection.getConnectionCategory() != null) {
			errorMap.put("INVALID WATER CONNECTION CATEGORY",
					"The WaterConnection connection category'" + waterConnection.getConnectionCategory() + "' does not exists");
		}
		if(!codes.get(WCConstants.MDMS_WC_Water_Source).contains(waterConnection.getWaterSource()) && waterConnection.getWaterSource() != null) {
			errorMap.put("INVALID WATER CONNECTION SOURCE",
					"The WaterConnection connection source'" + waterConnection.getWaterSource() + "' does not exists");
		}
		return errorMap;
	}
}
