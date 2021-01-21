package org.egov.vendorregistory.web.controller;


import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.vendorregistory.service.VendorService;
import org.egov.vendorregistory.util.ResponseInfoFactory;
import org.egov.vendorregistory.util.VendorUtil;
import org.egov.vendorregistory.web.model.RequestInfoWrapper;
import org.egov.vendorregistory.web.model.Vendor;
import org.egov.vendorregistory.web.model.VendorRequest;
import org.egov.vendorregistory.web.model.VendorResponse;
import org.egov.vendorregistory.web.model.VendorSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1")
public class VendorController {

	@Autowired
	private VendorService vendorService;
	
	@Autowired
	private VendorUtil vendorUtil;
	
	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	
	@PostMapping(value = "/_create")
	public ResponseEntity<VendorResponse> create(@Valid @RequestBody VendorRequest vendorRequest){
		vendorUtil.defaultJsonPathConfig();		
		Vendor vendor =  vendorService.create(vendorRequest);
		List<Vendor> vendorList = new ArrayList<Vendor>();
		vendorList.add(vendor);
		VendorResponse response = VendorResponse.builder().vendor(vendorList)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(vendorRequest.getRequestInfo(), true))
				.build();
		
		return new ResponseEntity<>(response,HttpStatus.OK);
		
	}
	
	
	
	@PostMapping(value = "/_search")
	public ResponseEntity<VendorResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute VendorSearchCriteria criteria){
		List<Vendor> vendorList = vendorService.Vendorsearch(criteria, requestInfoWrapper.getRequestInfo());
		VendorResponse response = VendorResponse.builder().vendor(vendorList).responseInfo(
				responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
		
	}
}
