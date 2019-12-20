package org.egov.wsCalculation.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.Idgen.IdResponse;
import org.egov.wsCalculation.config.WSCalculationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EnrichmentService {

	
	private WSCalculationConfiguration config;

	@Autowired
	public EnrichmentService( WSCalculationConfiguration config) {
		
		this.config = config;
	}




	
	/**
	 * Enriches the incoming createRequest
	 * 
	 * @param meterConnectionRequest
	 *            The create request for the meter reading
	 */

	public void enrichMeterReadingRequest(MeterConnectionRequest meterConnectionRequest) {
		meterConnectionRequest.getMeterReading().setId(UUID.randomUUID().toString());
		//meterConnectionRequest.getMeterReading().setCurrentReadingDate(Instant.now().getEpochSecond() * 1000);
		//setIdgenIds(meterConnectionRequest);
	}

}
