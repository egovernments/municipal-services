package org.egov.wsCalculation.controller;

import java.util.List;

import javax.validation.Valid;
import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReadingResponse;
import org.egov.wsCalculation.model.ResponseInfoFactory;
import org.egov.wsCalculation.service.MeterServices;
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
public class MeterReadingController {
	@Autowired
	private final ResponseInfoFactory responseInfoFactory;

	@Autowired
	MeterServices meterService;

	@RequestMapping(value = "/_createMeterReading", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<MeterReadingResponse> createMeterReading(@RequestHeader HttpHeaders headers,
			@Valid @RequestBody MeterConnectionRequest meterConnectionRequest) {
		List<MeterReading> meterReadings = meterService.createMeterReading(meterConnectionRequest);
		MeterReadingResponse response = MeterReadingResponse.builder().meterReadings(meterReadings).responseInfo(
				responseInfoFactory.createResponseInfoFromRequestInfo(meterConnectionRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

}
