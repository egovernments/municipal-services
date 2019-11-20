package org.egov.swService.service;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.PropertyCriteria;
import org.egov.waterConnection.model.PropertyRequest;
import org.springframework.stereotype.Component;


public interface PropertyService {
	public List<Property> createProperty(PropertyRequest propertyRequest);
	public List<Property> searchProperty(PropertyCriteria propertyCriteria,RequestInfo requestInfo);

}
