package org.egov.swService.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swService.model.Address;
import org.egov.swService.model.Property;
import org.egov.swService.model.PropertyCriteria;
import org.egov.swService.model.PropertyRequest;
import org.springframework.stereotype.Component;

@Component
public class PropertyServiceImpl implements PropertyService {

	@Override
	public List<Property> createProperty(PropertyRequest propertyRequest) {
		List<Property> propertyList = new ArrayList<>();
		Property property = new Property();
		for (int i = 0; i < 2; i++) {
			property = new Property();
			property.setAccountId("AccountId " + i);
			property.setAcknowldgementNumber("AcknowldgementNumber " + i);
			property.setId("PropertyId " + i);
			property.setAddress(new Address());
			property.setChannel(Property.ChannelEnum.CITIZEN);
			propertyList.add(property);
		}
		return propertyList;

	}

	@Override
	public List<Property> searchProperty(PropertyCriteria propertyCriteria, RequestInfo requestInfo) {
		List<Property> propertyList = new ArrayList<>();
		Property property = new Property();
		for (int i = 0; i < 2; i++) {
			property = new Property();
			property.setAccountId("AccountId " + i);
			property.setAcknowldgementNumber("AcknowldgementNumber " + i);
			property.setId(String.valueOf(i));
			property.setAddress(new Address());
			property.setChannel(Property.ChannelEnum.CITIZEN);
			propertyList.add(property);
		}
		return propertyList;
	}

}
