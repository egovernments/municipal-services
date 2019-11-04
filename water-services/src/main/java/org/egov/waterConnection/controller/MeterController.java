package org.egov.waterConnection.controller;

import java.util.List;

import javax.validation.Valid;

import org.egov.waterConnection.model.MeterConnectionRequest;
import org.egov.waterConnection.model.MeterReading;
import org.egov.waterConnection.model.MeterReadingResponse;
import org.egov.waterConnection.service.MeterServices;
import org.egov.waterConnection.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
@RequestMapping("/meterConnection")
public class MeterController {

	@Autowired
	MeterServices meterService;

	@Autowired
	private final ResponseInfoFactory responseInfoFactory;

	@RequestMapping(value = "/_addMeterReading", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<MeterReadingResponse> addMeterReading(@RequestHeader HttpHeaders headers,
			@Valid @RequestBody MeterConnectionRequest meterConnectionRequest) {

		List<MeterReading> meterReadings = meterService.addMeterReading(meterConnectionRequest);
		MeterReadingResponse response = MeterReadingResponse.builder().meterReadings(meterReadings).responseInfo(
				responseInfoFactory.createResponseInfoFromRequestInfo(meterConnectionRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
