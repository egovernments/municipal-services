package org.egov.waterconnection.service;

import org.egov.tracer.model.CustomException;
import org.egov.waterconnection.model.Property;
import org.egov.waterconnection.model.ValidatorResult;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Component
public class PropertyValidator implements WaterActionValidator {

	@Override
	public ValidatorResult validate(WaterConnectionRequest waterConnectionRequest, boolean isUpdate) {
		Map<String, String> errorMap = new HashMap<>();
		if(StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getPropertyId())) {
			errorMap.put("INVALID_PROPERTY_UUID", "Property uuid should not be empty");
		}
		if (!errorMap.isEmpty())
			return new ValidatorResult(false, errorMap);
		return new ValidatorResult(true, errorMap);
	}
}
