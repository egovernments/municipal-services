package org.egov.wsCalculation.util;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReadingResponse;
import org.egov.wsCalculation.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MeterReadingUtil {

	private ServiceRequestRepository serviceRequestRepository;

	@Value("${egov.meterReading.service.host}")
	private String meterReadingHost;

	@Value("${egov.meterReading.createendpoint}")
	private String createMeterReadingendpoint;

	@Autowired
	public MeterReadingUtil(ServiceRequestRepository serviceRequestRepository) {
		this.serviceRequestRepository = serviceRequestRepository;

	}

	public MeterConnectionRequest getMeterReadingRequest(RequestInfo requestInfo, MeterReading meterReading) {
		MeterConnectionRequest meterConnectionRequest = MeterConnectionRequest.builder().requestInfo(requestInfo)
				.meterReading(meterReading).build();
		return meterConnectionRequest;
	}

	public StringBuilder getDemandGenerationCreateURL() {
		return new StringBuilder().append(meterReadingHost).append(createMeterReadingendpoint);
	}

	public List<MeterReading> getMeterReadingDetails(Object result) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			MeterReadingResponse meterReadingResponse = mapper.convertValue(result, MeterReadingResponse.class);
			return meterReadingResponse.getMeterReadings();
		} catch (Exception ex) {
			throw new CustomException("PARSING ERROR", "The property json cannot be parsed");
		}
	}

}
