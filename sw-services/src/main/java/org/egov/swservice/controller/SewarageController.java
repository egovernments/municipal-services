package org.egov.swservice.controller;

import java.util.List;

import javax.validation.Valid;
import org.egov.swservice.model.RequestInfoWrapper;
import org.egov.swservice.model.SearchCriteria;
import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.model.SewerageConnectionResponse;
import org.egov.swservice.service.SewarageService;
import org.egov.swservice.util.ResponseInfoFactory;
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
@RequestMapping("/swc")
public class SewarageController {

	@Autowired
	SewarageService sewarageService;

	@Autowired
	private final ResponseInfoFactory responseInfoFactory;

	@RequestMapping(value = "/_create", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<SewerageConnectionResponse> createWaterConnection(
			@Valid @RequestBody SewerageConnectionRequest sewerageConnectionRequest) {
		List<SewerageConnection> sewerageConnection = sewarageService.createSewarageConnection(sewerageConnectionRequest);
		SewerageConnectionResponse response = SewerageConnectionResponse.builder().sewerageConnections(sewerageConnection)
				.responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(sewerageConnectionRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/_search", method = RequestMethod.POST)
	public ResponseEntity<SewerageConnectionResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute SearchCriteria criteria) {
		List<SewerageConnection> sewerageConnectionList = sewarageService.search(criteria,
				requestInfoWrapper.getRequestInfo());

		SewerageConnectionResponse response = SewerageConnectionResponse.builder()
				.sewerageConnections(sewerageConnectionList).responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@RequestMapping(value = "/_update", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<SewerageConnectionResponse> updateSewerageConnection(
			@Valid @RequestBody SewerageConnectionRequest sewerageConnectionRequest) {
		List<SewerageConnection> sewerageConnection = sewarageService.updateSewarageConnection(sewerageConnectionRequest);
		SewerageConnectionResponse response = SewerageConnectionResponse.builder().sewerageConnections(sewerageConnection)
				.responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(sewerageConnectionRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

}
