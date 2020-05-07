package org.egov.land.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.bpa.util.ResponseInfoFactory;
import org.egov.bpa.web.model.RequestInfoWrapper;
import org.egov.land.service.LandService;
import org.egov.land.web.models.LandInfo;
import org.egov.land.web.models.LandRequest;
import org.egov.land.web.models.LandResponse;
import org.egov.land.web.models.LandSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LandController {
	
	@Autowired
	private LandService landService;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;
	
	@PostMapping(value = "/land/_create")
	public ResponseEntity<LandResponse> create(@Valid @RequestBody LandRequest landRequest) {
//		landUtil.defaultJsonPathConfig();
		LandInfo landInfo = landService.create(landRequest);
		List<LandInfo> landInfos = new ArrayList<LandInfo>();
		landInfos.add(landInfo);
		LandResponse response = LandResponse.builder().landInfo(landInfos)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(landRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	

	@PostMapping(value = "/land/_update")
	public ResponseEntity<LandResponse> update(@Valid @RequestBody LandRequest landRequest) {

		LandInfo landInfo = landService.update(landRequest);
		List<LandInfo> landInfos = new ArrayList<LandInfo>();
		landInfos.add(landInfo);
		LandResponse response = LandResponse.builder().landInfo(landInfos)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(landRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@PostMapping(value = "/land/_search")
	public ResponseEntity<LandResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute LandSearchCriteria criteria) {

		List<LandInfo> landInfo = landService.search(criteria, requestInfoWrapper.getRequestInfo());
		LandResponse response = LandResponse.builder().landInfo(landInfo)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
}
