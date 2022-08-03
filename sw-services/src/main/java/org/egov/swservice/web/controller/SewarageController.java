package org.egov.swservice.web.controller;

import java.util.List;

import javax.validation.Valid;

import org.egov.swservice.web.models.DocumentRequest;
import org.egov.swservice.web.models.RequestInfoWrapper;
import org.egov.swservice.web.models.SearchCriteria;
import org.egov.swservice.web.models.SewerageConnection;
import org.egov.swservice.web.models.SewerageConnectionRequest;
import org.egov.swservice.web.models.SewerageConnectionResponse;
import org.egov.swservice.service.DocumentService;
import org.egov.swservice.service.SewerageService;
import org.egov.swservice.util.ResponseInfoFactory;
import org.egov.swservice.util.SWConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
    SewerageService sewerageService;

	@Autowired
	private final ResponseInfoFactory responseInfoFactory;
	
	@Autowired
	private DocumentService documentService;

	@RequestMapping(value = "/_create", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<SewerageConnectionResponse> createWaterConnection(
			@Valid @RequestBody SewerageConnectionRequest sewerageConnectionRequest, @RequestParam(required = false) boolean isMigration) {
		if(sewerageConnectionRequest.getSewerageConnection().getAdditionalDetails().toString().contains("isMigrated"))
		{
			isMigration=true;
		}
		List<SewerageConnection> sewerageConnection = sewerageService.createSewerageConnection(sewerageConnectionRequest, isMigration);
		SewerageConnectionResponse response = SewerageConnectionResponse.builder().sewerageConnections(sewerageConnection)
				.responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(sewerageConnectionRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/_search", method = RequestMethod.POST)
	public ResponseEntity<SewerageConnectionResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute SearchCriteria criteria) {
		List<SewerageConnection> sewerageConnectionList = sewerageService.search(criteria,
				requestInfoWrapper.getRequestInfo());

		SewerageConnectionResponse response = SewerageConnectionResponse.builder()
				.sewerageConnections(sewerageConnectionList).responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@RequestMapping(value = "/_plainsearch", method = RequestMethod.POST)
    public ResponseEntity<SewerageConnectionResponse> plainsearch(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                        @Valid @ModelAttribute SearchCriteria criteria) {
        List<SewerageConnection> sewerageConnectionList = sewerageService.searchSewerageConnectionPlainSearch(criteria, requestInfoWrapper.getRequestInfo());
        SewerageConnectionResponse response = SewerageConnectionResponse.builder().sewerageConnections(sewerageConnectionList).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
	
	
	@RequestMapping(value = "/_update", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<SewerageConnectionResponse> updateSewerageConnection(
			@Valid @RequestBody SewerageConnectionRequest sewerageConnectionRequest) {
		List<SewerageConnection> sewerageConnection = sewerageService.updateSewerageConnection(sewerageConnectionRequest);
		SewerageConnectionResponse response = SewerageConnectionResponse.builder().sewerageConnections(sewerageConnection)
				.responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(sewerageConnectionRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}
	
	@RequestMapping(value = "/documents/_create", method = RequestMethod.POST)
	public ResponseEntity<String> saveDocuments(@Valid @RequestBody DocumentRequest documentRequest) {

		documentService.saveDocuments(documentRequest, documentRequest.getRequestInfo());
		return new ResponseEntity<>("SW Connection FilestoreIds Saved", HttpStatus.CREATED);
	}
	
	@RequestMapping(value="/disconnect", method=RequestMethod.POST)
	public ResponseEntity<String> disConnectSwerageConnection(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,@RequestParam String connectionNo,@RequestParam String tenantId ){
		
		sewerageService.disConnectSewerageConnection(connectionNo,requestInfoWrapper.getRequestInfo(),tenantId);
		return new ResponseEntity<>(SWConstants.SUCCESS_DISCONNECT_MSG, HttpStatus.CREATED);
	}

}
