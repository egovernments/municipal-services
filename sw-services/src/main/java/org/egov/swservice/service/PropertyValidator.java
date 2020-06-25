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
		
		if(StringUtils.isEmpty(sewerageConnectionRequest.getSewerageConnection().getPropertyId())) {
			errorMap.put("INVALID_PROPERTY_UUID", "Property uuid should not be empty");
		}
		if (!errorMap.isEmpty())
			return new ValidatorResult(false, errorMap);
		return new ValidatorResult(true, errorMap);
	}

}
