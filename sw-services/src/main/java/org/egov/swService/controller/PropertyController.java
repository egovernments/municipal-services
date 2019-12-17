package org.egov.swService.controller;

import java.util.List;

import javax.validation.Valid;

import org.egov.swService.model.Property;
import org.egov.swService.model.PropertyCriteria;
import org.egov.swService.model.PropertyRequest;
import org.egov.swService.model.PropertyResponse;
import org.egov.swService.model.RequestInfoWrapper;
import org.egov.swService.service.PropertyService;
import org.egov.swService.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@RestController
@RequestMapping("/propertyController")
public class PropertyController {
	
	@Autowired
	private final ResponseInfoFactory responseInfoFactory;
	
	
	@Autowired
	private PropertyService propertyService;

	@RequestMapping(value = "/_create", method = RequestMethod.POST)
	public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest propertyRequest) {
		List<Property> properties = propertyService.createProperty(propertyRequest);
		PropertyResponse response = PropertyResponse.builder().properties(properties)
				.responseInfo(
						responseInfoFactory.createResponseInfoFromRequestInfo(propertyRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/_search", method = RequestMethod.POST)
	public ResponseEntity<PropertyResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute PropertyCriteria propertyCriteria) {
		List<Property> properties = propertyService.searchProperty(propertyCriteria,requestInfoWrapper.getRequestInfo());
		PropertyResponse response = PropertyResponse.builder().properties(properties).responseInfo(
				responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}


}
