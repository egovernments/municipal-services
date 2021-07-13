package org.egov.gcservice.web.controller;

import java.util.List;

import javax.validation.Valid;
import org.egov.gcservice.web.models.RequestInfoWrapper;
import org.egov.gcservice.web.models.SearchCriteria;
import org.egov.gcservice.web.models.GarbageConnection;
import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.gcservice.web.models.GarbageConnectionResponse;
import org.egov.gcservice.service.GarbageService;
import org.egov.gcservice.util.ResponseInfoFactory;
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
@RequestMapping("/gc")
public class GarbageController {

	@Autowired
    GarbageService garbageService;

	@Autowired
	private final ResponseInfoFactory responseInfoFactory;

	@RequestMapping(value = "/_create", method = RequestMethod.POST, produces = "application/json", consumes="application/json")
	public ResponseEntity<GarbageConnectionResponse> createGarbageConnection(
			@Valid @RequestBody GarbageConnectionRequest garbageConnectionRequest) {
		List<GarbageConnection> garbageConnection = garbageService.createGarbageConnection(garbageConnectionRequest);
		GarbageConnectionResponse response = GarbageConnectionResponse.builder().GarbageConnections(garbageConnection)
				.responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(garbageConnectionRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/_search", method = RequestMethod.POST)
	public ResponseEntity<GarbageConnectionResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute SearchCriteria criteria) {
		List<GarbageConnection> GarbageConnectionList = garbageService.search(criteria,
				requestInfoWrapper.getRequestInfo());

		GarbageConnectionResponse response = GarbageConnectionResponse.builder()
				.GarbageConnections(GarbageConnectionList).responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@RequestMapping(value = "/_update", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<GarbageConnectionResponse> updateGarbageConnection(
			@Valid @RequestBody GarbageConnectionRequest garbageConnectionRequest) {
		List<GarbageConnection> GarbageConnection = garbageService.updateGarbageConnection(garbageConnectionRequest);
		GarbageConnectionResponse response = GarbageConnectionResponse.builder().GarbageConnections(GarbageConnection)
				.responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(garbageConnectionRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

}
