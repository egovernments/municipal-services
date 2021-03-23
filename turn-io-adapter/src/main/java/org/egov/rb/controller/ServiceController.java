package org.egov.rb.controller;

import javax.validation.Valid;


import org.egov.rb.contract.RequestInfoWrapper;
import org.egov.rb.contract.ServiceReqSearchCriteria;
import org.egov.rb.contract.ServiceRequest;
import org.egov.rb.contract.ServiceResponse;
import org.egov.rb.model.MessageRequest;
import org.egov.rb.models.Service;
import org.egov.rb.repository.ServiceRequestRepository;
import org.egov.rb.service.TransformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
@RequestMapping(value = "/v1/requests/")
public class ServiceController {
	
	@Autowired
	private TransformService transformService;
	

	
	@PostMapping("_transform")
	@ResponseBody
	public ResponseEntity<?> transformService(@RequestBody MessageRequest messageRequest) {
		//pgrRequestValidator.validateCreate(serviceRequest);
		ServiceResponse response = transformService.transform(messageRequest);
		ServiceRequestRepository serviceRequestRepository=new ServiceRequestRepository();
		serviceRequestRepository.fetchResult(null, null);
		System.out.println(messageRequest);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
