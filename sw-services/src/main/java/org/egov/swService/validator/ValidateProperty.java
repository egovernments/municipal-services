package org.egov.swService.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.tracer.model.CustomException;
import org.egov.swService.model.Property;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.util.SewerageServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ValidateProperty {

	@Autowired
	SewerageServicesUtil sewerageServiceUtil;

	/**
	 * 
	 * @param sewerageConnectionRequest
	 *            SewerageConnectionRequest is request to be validated against
	 *            property
	 */

	public void validatePropertyCriteriaForCreateSewerage(SewerageConnectionRequest sewerageConnectionRequest) {
		Map<String, String> errorMap = new HashMap<>();
		Property property = sewerageConnectionRequest.getSewerageConnection().getProperty();
		if (property.getTenantId() == null || property.getTenantId().isEmpty()) {
			errorMap.put("INVALID PROPERTY", "SewerageConnection cannot be updated without tenantId");
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

	}

	/**
	 * 
	 * @param sewarageConnectionRequest
	 *            SewarageConnectionRequest is request to be validated against
	 *            property ID
	 * @return true if property id is present otherwise return false
	 */

	public boolean isPropertyIdPresentForSewerage(SewerageConnectionRequest sewerageConnectionRequest) {
		Property property = sewerageConnectionRequest.getSewerageConnection().getProperty();
		if (property.getPropertyId() == null || property.getPropertyId().isEmpty()) {
			return false;
		}
		return true;
	}

	public void enrichPropertyForSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest) {
		List<Property> propertyList;
		if (isPropertyIdPresentForSewerage(sewerageConnectionRequest)) {
			propertyList = sewerageServiceUtil.propertySearch(sewerageConnectionRequest);
		} else {
			propertyList = sewerageServiceUtil.createPropertyRequest(sewerageConnectionRequest);
		}
		if (propertyList != null && !propertyList.isEmpty())
			sewerageConnectionRequest.getSewerageConnection().setProperty(propertyList.get(0));
	}

	public void validatePropertyForConnection(List<SewerageConnection> sewerageConnectionList) {
		sewerageConnectionList.forEach(sewerageConnection -> {
			List<Property> propertyList;
			if (sewerageConnection.getProperty().getPropertyId() == null
					|| sewerageConnection.getProperty().getPropertyId().isEmpty()) {
				throw new CustomException("INVALID SEARCH",
						"PROPERTY ID NOT FOUND FOR " + sewerageConnection.getId() + " SEWERAGE CONNECTION ID");
			}
		});
	}
}
