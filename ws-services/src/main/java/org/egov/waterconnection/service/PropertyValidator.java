package org.egov.waterconnection.service;

import java.util.HashMap;
import java.util.Map;

import org.egov.waterconnection.model.ValidatorResult;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PropertyValidator implements WaterActionValidator {

	@Override
	public ValidatorResult validate(WaterConnectionRequest waterConnectionRequest, boolean isUpdate) {
		Map<String, String> errorMap = new HashMap<>();
		if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getProcessInstance())
				|| StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getProcessInstance().getAction())) {
			errorMap.put("INVALID_ACTION", "Workflow obj can not be null or action can not be empty!!");
			return new ValidatorResult(false, errorMap);
		}
		if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getProperty())) {
			errorMap.put("INVALID_PROPERTY", "Property should not be empty");
		}
		if (!StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getProperty()) && (StringUtils
				.isEmpty(waterConnectionRequest.getWaterConnection().getProperty().getUsageCategory()))) {
			errorMap.put("INVALID_WATER_CONNECTION_PROPERTY_USAGE_TYPE", "Property usage type should not be empty");
		}
		if (!errorMap.isEmpty())
			return new ValidatorResult(false, errorMap);
		return new ValidatorResult(true, errorMap);
	}

}
