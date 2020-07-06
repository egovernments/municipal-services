package org.egov.waterconnection.validator;

import org.egov.tracer.model.CustomException;
import org.egov.waterconnection.model.Property;
import org.egov.waterconnection.model.Status;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.egov.waterconnection.util.WaterServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
public class ValidateProperty {

	@Autowired
	private WaterServicesUtil waterServiceUtil;
	
	/**
	 * 
	 * @param property validate property fields
	 */
	public void validatePropertyFields(Property property) {
		if (StringUtils.isEmpty(property.getPropertyId())) {
			throw new CustomException("INVALID_PROPERTY", "WaterConnection cannot be updated without property Id");
		}
		if (StringUtils.isEmpty(property.getStatus()) || (!property.getStatus().equals(Status.ACTIVE))) {
			throw new CustomException("INVALID_PROPERTY_STATUS", " Please enter a valid property ID");
		}
	}

	/**
	 * 
	 * @param waterConnectionRequest WaterConnectionRequest
	 */
	public Property getOrValidateProperty(WaterConnectionRequest waterConnectionRequest) {
		Optional<Property> propertyList = waterServiceUtil.propertySearch(waterConnectionRequest).stream().findFirst();
		if (!propertyList.isPresent()) {
			throw new CustomException("INVALID WATER CONNECTION PROPERTY",
					"Water connection cannot be enriched without property");
		}
		Property property = propertyList.get();
		if (StringUtils.isEmpty(property.getUsageCategory())) {
			throw new CustomException("INVALID WATER CONNECTION PROPERTY USAGE TYPE",
					"Water connection cannot be enriched without property usage type");
		}
		return property;
	}
	
}
