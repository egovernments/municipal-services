package org.egov.swservice.service;

import java.util.HashMap;
import java.util.Map;

import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.model.ValidatorResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PropertyValidator implements SewerageActionValidator {

	@Override
	public ValidatorResult validate(SewerageConnectionRequest sewerageConnectionRequest, boolean isUpdate) {
		Map<String, String> errorMap = new HashMap<>();
		if (StringUtils.isEmpty(sewerageConnectionRequest.getSewerageConnection().getProperty())) {
			errorMap.put("INVALID_PROPERTY", "Property should not be empty");
		}
		if (!StringUtils.isEmpty(sewerageConnectionRequest.getSewerageConnection().getProperty()) && (StringUtils
				.isEmpty(sewerageConnectionRequest.getSewerageConnection().getProperty().getUsageCategory()))) {
			errorMap.put("INVALID SEWERAGE CONNECTION PROPERTY USAGE TYPE",
					"SewerageConnection cannot be created without property usage type");
		}
		if (!errorMap.isEmpty())
			return new ValidatorResult(false, errorMap);
		return new ValidatorResult(true, errorMap);
	}

}
