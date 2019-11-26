package org.egov.bpa.web.controller;

import javax.validation.Valid;

import org.egov.bpa.service.BPAService;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPAResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/s1")
public class BPAController {

	@Autowired
	private BPAService bpaService;

	@PostMapping(value = "/bpa/_create", consumes = "application/json")
	public BPARequest create(@Valid @RequestBody BPARequest bpaRequest) {
		bpaService.create(bpaRequest);
		return bpaRequest;
	}

}
