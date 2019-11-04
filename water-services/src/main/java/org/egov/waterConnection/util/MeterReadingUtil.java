package org.egov.waterConnection.util;

import java.util.ArrayList;
import java.util.List;

import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.MeterReading;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.PropertyRequest;
import org.egov.waterConnection.model.PropertyResponse;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.repository.ServiceRequestRepository;
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

	public List<MeterReading> createDemandGenerationForWaterServices(WaterConnectionRequest waterConnectionRequest) {
		List<MeterReading> meterReadingList = new ArrayList<>();
		meterReadingList.add(waterConnectionRequest.getWaterConnection().getProperty());
		PropertyRequest propertyReq = getPropertyRequest(waterConnectionRequest.getRequestInfo(), meterReadingList);
		Object result = serviceRequestRepository.fetchResult(getDemandGenerationCreateURL(), propertyReq);
		return getMeterReadingDetails(result);
	}

	public StringBuilder getDemandGenerationCreateURL() {
		return new StringBuilder().append(meterReadingHost).append(createMeterReadingendpoint);
	}

	private List<MeterReading> getMeterReadingDetails(Object result) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			MeterConnectionResponse propertyResponse = mapper.convertValue(result, PropertyResponse.class);
			return propertyResponse.getProperties();
		} catch (Exception ex) {
			throw new CustomException("PARSING ERROR", "The property json cannot be parsed");
		}
	}

}
