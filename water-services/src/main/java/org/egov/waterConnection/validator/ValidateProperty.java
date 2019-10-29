package org.egov.waterConnection.validator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.WaterConnectionSearchCriteria;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ValidateProperty {

	@Autowired
	WaterServicesUtil waterServiceUtil;

	public void validatePropertyCriteria(WaterConnectionRequest waterConnectionRequest) {
		Map<String, String> errorMap = new HashMap<>();
		Property property = waterConnectionRequest.getWaterConnection().getProperty();
		if (property.getId() == null || property.getId().isEmpty()) {
			errorMap.put("INVALID PROPERTY", "WaterConnection cannot be updated without propertyId");
		}
		if (property.getTenantId() == null || property.getTenantId().isEmpty()) {
			errorMap.put("INVALID PROPERTY", "WaterConnection cannot be updated without tenantId");
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	
	public void validatePropertyCriteriaForCreate(WaterConnectionRequest waterConnectionRequest) {
		Map<String, String> errorMap = new HashMap<>();
		Property property = waterConnectionRequest.getWaterConnection().getProperty();
		if (property.getTenantId() == null || property.getTenantId().isEmpty()) {
			errorMap.put("INVALID PROPERTY", "WaterConnection cannot be updated without tenantId");
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

	}

	public boolean isPropertyIdPresent(WaterConnectionRequest waterConnectionRequest) {
		Property property = waterConnectionRequest.getWaterConnection().getProperty();
		if (property.getId() == null || property.getId().isEmpty()) {
			return false;
		}
		return true;
	}
}
