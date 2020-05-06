package org.egov.bpa.web.controller;

import java.util.List;

import javax.validation.Valid;

import org.egov.bpa.service.BPAService;
import org.egov.bpa.service.LandService;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.util.ResponseInfoFactory;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.LandInfo;
import org.egov.bpa.web.model.LandRequest;
import org.egov.bpa.web.model.LandResponse;
import org.egov.bpa.web.model.LandSearchCriteria;
import org.egov.bpa.web.model.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/v1/land")
public class LandController {
	
	@Autowired
	private LandService landService;

//	@Autowired
//	private BPAUtil landUtil;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;
	
	@PostMapping(value = "land/_create")
	public ResponseEntity<LandResponse> landCreate(@Valid @RequestBody LandRequest landRequest) {
		//landUtil.defaultJsonPathConfig();
		List<LandInfo> landInfo = landService.create(landRequest);
		LandResponse response = LandResponse.builder().landInfo(landInfo)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(landRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	

	@PostMapping(value = "land/_update")
	public ResponseEntity<LandResponse> landUpdate(@Valid @RequestBody LandRequest landRequest) {

		List<LandInfo> landInfo = landService.update(landRequest);
		LandResponse response = LandResponse.builder().landInfo(landInfo)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(landRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@PostMapping(value = "land/_search")
	public ResponseEntity<LandResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute LandSearchCriteria criteria) {

		List<LandInfo> landInfo = landService.search(criteria, requestInfoWrapper.getRequestInfo());
		LandResponse response = LandResponse.builder().landInfo(landInfo)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
}
