package org.egov.wsCalculation.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.repository.ServiceRequestRepository;
import org.egov.wsCalculation.util.MRConstants;
import org.egov.wsCalculation.util.MeterReadingUtil;
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
	private MeterReadingUtil meterReadingUtil;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndpoint;

	public void validateMasterData(MeterConnectionRequest request) {
		Map<String, String> errorMap = new HashMap<>();

		String jsonPath = MRConstants.JSONPATH_ROOT;
		String tenantId = request.getRequestInfo().getUserInfo().getTenantId();

		String[] masterNames = { MRConstants.MDMS_MS_BILLING_PERIOD };
		List<String> names = new ArrayList<>(Arrays.asList(masterNames));
		Map<String, List<String>> codes = getAttributeValues(tenantId, MRConstants.MDMS_WC_MOD_NAME, names, "$.*.code",
				jsonPath, request.getRequestInfo());
		validateMDMSData(masterNames, codes);
		validateCodes(request.getMeterReading(), codes, errorMap);
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	private Map<String, List<String>> getAttributeValues(String tenantId, String moduleName, List<String> names,
			String filter, String jsonpath, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder(mdmsHost).append(mdmsEndpoint);
		MdmsCriteriaReq criteriaReq = meterReadingUtil.prepareMdMsRequest(tenantId, moduleName, names, filter,
				requestInfo);
		try {
			Object result = serviceRequestRepository.fetchResult(uri, criteriaReq);
			return JsonPath.read(result, jsonpath);
		} catch (Exception e) {
			log.error("Error while fetching MDMS data", e);
			throw new CustomException(MRConstants.INVALID_BILLING_PERIOD, MRConstants.INVALID_BILLING_PERIOD_MSG);
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

	private static Map<String, String> validateCodes(MeterReading meterReading, Map<String, List<String>> codes,
			Map<String, String> errorMap) {
		if (!codes.get(MRConstants.MDMS_MS_BILLING_PERIOD).contains(meterReading.getBillingPeriod())
				&& meterReading.getBillingPeriod() != null) {
			errorMap.put("INVALID BILLING PERIOD",
					"The Billing period" + meterReading.getBillingPeriod() + " does not exist");
		}

		return errorMap;
	}
}
