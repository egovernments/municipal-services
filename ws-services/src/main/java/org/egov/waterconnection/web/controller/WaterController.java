package org.egov.waterconnection.web.controller;

import java.util.List;
import javax.validation.Valid;

import org.egov.waterconnection.web.models.DocumentRequest;
import org.egov.waterconnection.web.models.RequestInfoWrapper;
import org.egov.waterconnection.web.models.SearchCriteria;
import org.egov.waterconnection.web.models.WaterConnection;
import org.egov.waterconnection.web.models.WaterConnectionRequest;
import org.egov.waterconnection.web.models.WaterConnectionResponse;
import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.service.DocumentService;
import org.json.JSONObject;
import org.apache.tomcat.util.json.JSONParser;
import org.egov.waterconnection.service.WaterService;
import org.egov.waterconnection.util.ResponseInfoFactory;
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
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Builder
@RestController
@Slf4j
@RequestMapping("/wc")
public class WaterController {

	@Autowired
	private WaterService waterService;

	@Autowired
	private DocumentService documentService;
	
	@Autowired
	private final ResponseInfoFactory responseInfoFactory;

	@RequestMapping(value = "/_create", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<WaterConnectionResponse> createWaterConnection(
			@Valid @RequestBody WaterConnectionRequest waterConnectionRequest, @RequestParam(required = false) boolean isMigration) {
		log.info("++++++++++++++++++==waterConnectionRequest++++++++++++++"+waterConnectionRequest);
//		if(waterConnectionRequest.getWaterConnection().getAdditionalDetails().toString().contains("isMigrated"))
//		{
//		 Object obj =  new JSONParser(waterConnectionRequest.getWaterConnection().getAdditionalDetails().toString());
//		 JSONObject jo = (JSONObject) obj;
//		 isMigration=(boolean)jo.get("isMigrated");
//		log.info("updated isMigration::::"+isMigration);
//
//		}
		if(waterConnectionRequest.getWaterConnection().getAdditionalDetails().toString().contains("isMigrated"))
		{
			isMigration=true;
		}
		
		log.info("isMigration::::"+isMigration);

		List<WaterConnection> waterConnection = waterService.createWaterConnection(waterConnectionRequest,isMigration);
		WaterConnectionResponse response = WaterConnectionResponse.builder().waterConnection(waterConnection)
				.responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(waterConnectionRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/_createMigration", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<WaterConnectionResponse> createWaterConnectionForMigration(
			@Valid @RequestBody WaterConnectionRequest waterConnectionRequest, @RequestParam(required = true) boolean isMigration) {
		log.info("isMigration::::"+isMigration);
		log.info("++++++++++++++++++==waterConnectionRequest++++++++++++++"+waterConnectionRequest);
		isMigration=true;
		List<WaterConnection> waterConnection = waterService.createWaterConnection(waterConnectionRequest,isMigration);
		WaterConnectionResponse response = WaterConnectionResponse.builder().waterConnection(waterConnection)
				.responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(waterConnectionRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	

	@RequestMapping(value = "/_search", method = RequestMethod.POST)
	public ResponseEntity<WaterConnectionResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute SearchCriteria criteria) {
		List<WaterConnection> waterConnectionList = waterService.search(criteria, requestInfoWrapper.getRequestInfo());
		WaterConnectionResponse response = WaterConnectionResponse.builder().waterConnection(waterConnectionList)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(),
						true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/_plainsearch", method = RequestMethod.POST)
    public ResponseEntity<WaterConnectionResponse> plainsearch(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                        @Valid @ModelAttribute SearchCriteria criteria) {
        List<WaterConnection> waterConnectionList = waterService.searchWaterConnectionPlainSearch(criteria, requestInfoWrapper.getRequestInfo());
        WaterConnectionResponse response = WaterConnectionResponse.builder().waterConnection(waterConnectionList).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

	@RequestMapping(value = "/_update", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<WaterConnectionResponse> updateWaterConnection(
			@Valid @RequestBody WaterConnectionRequest waterConnectionRequest) {
		List<WaterConnection> waterConnection = waterService.updateWaterConnection(waterConnectionRequest);
		WaterConnectionResponse response = WaterConnectionResponse.builder().waterConnection(waterConnection)
				.responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(waterConnectionRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@RequestMapping(value = "/documents/_create", method = RequestMethod.POST)
	public ResponseEntity<String> saveDocuments(@Valid @RequestBody DocumentRequest documentRequest) {

		documentService.saveDocuments(documentRequest, documentRequest.getRequestInfo());
		return new ResponseEntity<>("WS Connection FilestoreIds Saved", HttpStatus.CREATED);
	}

	@RequestMapping(value="/disconnect", method=RequestMethod.POST)
	public ResponseEntity<String> disConnectWaterConnection(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,@RequestParam String connectionNo,@RequestParam String tenantId ){
		
		waterService.disConnectWaterConnection(connectionNo,requestInfoWrapper.getRequestInfo(),tenantId);
		return new ResponseEntity<>(WCConstants.SUCCESS_DISCONNECT_MSG, HttpStatus.CREATED);
	}
	
	
}
