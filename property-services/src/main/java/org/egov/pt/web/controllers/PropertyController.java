package org.egov.pt.web.controllers;

import java.util.*;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.models.oldProperty.MigrationCount;
import org.egov.pt.models.oldProperty.OldProperty;
import org.egov.pt.models.oldProperty.OldPropertyCriteria;
import org.egov.pt.models.oldProperty.OldPropertyRequest;
import org.egov.pt.service.MigrationService;
import org.egov.pt.service.PropertyService;
import org.egov.pt.util.ResponseInfoFactory;
import org.egov.pt.web.contracts.AssessmentRequest;
import org.egov.pt.web.contracts.PropertyRequest;
import org.egov.pt.web.contracts.PropertyResponse;
import org.egov.pt.web.contracts.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import static org.egov.pt.service.MigrationService.COUNT_QUERY;

@Controller
@RequestMapping("/property")
public class PropertyController {

	@Autowired
	private PropertyService propertyService;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	@Autowired
	private MigrationService migrationService;

	@PostMapping("/_create")
	public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest propertyRequest) {

		Property property = propertyService.createProperty(propertyRequest);
		ResponseInfo resInfo = responseInfoFactory.createResponseInfoFromRequestInfo(propertyRequest.getRequestInfo(), true);
		PropertyResponse response = PropertyResponse.builder()
				.properties(Arrays.asList(property))
				.responseInfo(resInfo)
				.build();
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	
	@PostMapping("/_update")
	public ResponseEntity<PropertyResponse> update(@Valid @RequestBody PropertyRequest propertyRequest) {
		
		Property property = propertyService.updateProperty(propertyRequest);
		ResponseInfo resInfo = responseInfoFactory.createResponseInfoFromRequestInfo(propertyRequest.getRequestInfo(), true);
		PropertyResponse response = PropertyResponse.builder()
				.properties(Arrays.asList(property))
				.responseInfo(resInfo)
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/_search")
	public ResponseEntity<PropertyResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute PropertyCriteria propertyCriteria) {
		
		List<Property> properties = propertyService.searchProperty(propertyCriteria,requestInfoWrapper.getRequestInfo());
		PropertyResponse response = PropertyResponse.builder().properties(properties).responseInfo(
				responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/_migration")
	public ResponseEntity<?> propertyMigration(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
															  @Valid @ModelAttribute OldPropertyCriteria propertyCriteria, @RequestParam String SingleTenantId) {
		long startTime = System.nanoTime();
		Map<String, String> resultMap = null;
		Map<String, String> errorMap = new HashMap<>();

		resultMap = migrationService.initiateProcess(requestInfoWrapper,propertyCriteria,errorMap, SingleTenantId);

		long endtime = System.nanoTime();
		long elapsetime = endtime - startTime;
		System.out.println("Elapsed time--->"+elapsetime);
		
		return new ResponseEntity<>(resultMap, HttpStatus.OK);
	}

	@RequestMapping(value = "/_plainauditsearch", method = RequestMethod.POST)
	public ResponseEntity<PropertyResponse> plainsearch(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
														@Valid @ModelAttribute PropertyCriteria propertyCriteria) {
		List<Property> properties = propertyService.searchPropertyPlainSearch(propertyCriteria, requestInfoWrapper.getRequestInfo());
		PropertyResponse response = PropertyResponse.builder().properties(properties).responseInfo(
				responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
//	@RequestMapping(value = "/_cancel", method = RequestMethod.POST)
//	public ResponseEntity<PropertyResponse> cancel(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
//												   @Valid @ModelAttribute PropertyCancelCriteria propertyCancelCriteria) {
//		
//		List<Property> properties = propertyService.cancelProperty(propertyCancelCriteria,requestInfoWrapper.getRequestInfo());
//		PropertyResponse response = PropertyResponse.builder().properties(properties).responseInfo(
//				responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
//				.build();
//		return new ResponseEntity<>(response, HttpStatus.OK);
//	}

}