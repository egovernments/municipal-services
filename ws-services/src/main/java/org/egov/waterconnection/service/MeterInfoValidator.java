package org.egov.waterconnection.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.model.ValidatorResult;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MeterInfoValidator implements WaterActionValidator {

	@Autowired
	private ObjectMapper mapper;

	@Override
	@SuppressWarnings("unchecked")
	public ValidatorResult validate(WaterConnectionRequest waterConnectionRequest, boolean isUpdate) {
		Map<String, String> errorMap = new HashMap<>();
		if (WCConstants.ACTIVATE_CONNECTION_CONST
				.equalsIgnoreCase(waterConnectionRequest.getWaterConnection().getProcessInstance().getAction())) {
			if (!StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionType())
					&& WCConstants.METERED_CONNECTION
							.equalsIgnoreCase(waterConnectionRequest.getWaterConnection().getConnectionType())) {
				if (waterConnectionRequest.getWaterConnection().getMeterId() == null) {
					errorMap.put("INVALID_METER_ID", "Meter Id cannot be empty");
				}
				if (waterConnectionRequest.getWaterConnection().getMeterInstallationDate() == null
						|| waterConnectionRequest.getWaterConnection().getMeterInstallationDate() < 0
						|| waterConnectionRequest.getWaterConnection().getMeterInstallationDate() == 0) {
					errorMap.put("INVALID_METER_INSTALLATION_DATE",
							"Meter Installation date cannot be null or negative");
				}
				HashMap<String, Object> addDetail = mapper.convertValue(
						waterConnectionRequest.getWaterConnection().getAdditionalDetails(), HashMap.class);
				if (StringUtils.isEmpty(addDetail)
						|| addDetail.getOrDefault(WCConstants.INITIAL_METER_READING_CONST, null) == null) {
					errorMap.put("INVALID_INITIAL_METER_READING", "Initial meter reading can not be null");
				} else {
					BigDecimal initialMeterReading = BigDecimal.ZERO;
					initialMeterReading = new BigDecimal(
							String.valueOf(addDetail.get(WCConstants.INITIAL_METER_READING_CONST)));
					if (initialMeterReading.compareTo(BigDecimal.ZERO) == 0) {
						errorMap.put("INVALID_INITIAL_METER_READING", "Initial meter reading can not be zero");
					}
				}

			}
		}
		if (!errorMap.isEmpty())
			return new ValidatorResult(false, errorMap);
		return new ValidatorResult(true, errorMap);
	}

}
