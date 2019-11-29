package org.egov.bpa.web.controller;

import javax.validation.Valid;

import org.egov.bpa.service.BPAService;
import org.egov.bpa.util.ResponseInfoFactory;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPAResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bpa")
public class BPAController {

	@Autowired
	private BPAService bpaService;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	@PostMapping(value = "/appl/_create")
	public ResponseEntity<BPAResponse> create(
			@Valid @RequestBody BPARequest bpaRequest) {
		BPA bpa = bpaService.create(bpaRequest);
		BPAResponse response = BPAResponse
				.builder()
				.BPA(bpa)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(
								bpaRequest.getRequestInfo(), true)).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
