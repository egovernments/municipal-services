package org.egov.gcservice.service;

import java.util.HashMap;
import java.util.Map;

import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.gcservice.web.models.ValidatorResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PropertyValidator implements GarbageActionValidator {

	@Override
	public ValidatorResult validate(GarbageConnectionRequest garbageConnectionRequest, int reqType) {
		Map<String, String> errorMap = new HashMap<>();
		
		if(StringUtils.isEmpty(garbageConnectionRequest.getGarbageConnection().getPropertyId())) {
			errorMap.put("INVALID_PROPERTY_UNIQUE_ID", "Property Unique Id should not be empty");
		}
		if (!errorMap.isEmpty())
			return new ValidatorResult(false, errorMap);
		return new ValidatorResult(true, errorMap);
	}

}
