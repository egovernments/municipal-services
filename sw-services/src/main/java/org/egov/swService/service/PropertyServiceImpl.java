package org.egov.swService.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swService.model.Address;
import org.egov.swService.model.GeoLocation;
import org.egov.swService.model.OwnerInfo;
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
			List<OwnerInfo> ownerInfoList = new ArrayList<>();
			OwnerInfo owner = new OwnerInfo();
			Address address = new Address();
		address.setCity("Bangalore");
		address.setCountry("India");
		address.setBuildingName("Umiya Emporium");
		address.setDistrict("Bangalore");
		GeoLocation geoLocation= new GeoLocation();
		geoLocation.setLatitude(12.9716);
		geoLocation.setLongitude(77.5946);
		
		address.setGeoLocation(geoLocation);
		address.setDoorNo("5");
		address.setPincode("56004");
			address.setStreet("147/J, 10th Cross, 12th Main, 3rd Block, Koramangala, Bengaluru, Karnataka 560034");
			owner.setGender("Male");
			owner.setAdditionalDetails(new Object());
			owner.setCorrespondenceAddress("No.97, 3rd Floor, Umiya Emporium, Hosur Main Road, Madiwala, Opposite Forum Mall, Bengaluru, Karnataka 560029");
			owner.setFatherOrHusbandName("Mr Jacob");
			owner.setInstitutionId("Institue");
			owner.setIsPrimaryOwner(true);
			owner.setName("Mr George");
			owner.setOwnerType("Joint");
			owner.setMobileNumber("7894567345");
			ownerInfoList.add(owner);
			property = new Property();
			property.setAccountId("AccountId ");
			property.setAcknowldgementNumber("AcknowldgementNumber " );
			property.setId(String.valueOf(1234));
			property.setAddress(address);
			property.setOwners(ownerInfoList);
			property.setChannel(Property.ChannelEnum.CITIZEN);
			property.setPropertyType("Domestic");
			property.setTenantId("pb");
			property.setLandArea(2400.f);
			propertyList.add(property);
		return propertyList;
	}

}
