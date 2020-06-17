package org.egov.waterconnection.validator;

import java.util.Optional;

import org.egov.tracer.model.CustomException;
import org.egov.waterconnection.model.Property;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.egov.waterconnection.util.WaterServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ValidateProperty {

	@Autowired
	private WaterServicesUtil waterServiceUtil;
	
	/**
	 * 
	 * @param waterConnectionRequest WaterConnectionRequest is request to be validated against property
	 */
	public void validatePropertyCriteria(Property property) {
		if (StringUtils.isEmpty(property.getPropertyId())) {
			throw new CustomException("INVALID PROPERTY", "WaterConnection cannot be updated without propertyId");
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
