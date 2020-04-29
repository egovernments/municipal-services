package org.egov.wscalculation.service;


import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.egov.wscalculation.model.MeterConnectionRequest;
import org.springframework.stereotype.Service;

@Service
public class EnrichmentService {

	/**
	 * Enriches the incoming createRequest
	 * 
	 * @param meterConnectionRequest The create request for the meter reading
	 */

	public void enrichMeterReadingRequest(MeterConnectionRequest meterConnectionRequest) {
		meterConnectionRequest.getMeterReading().setId(UUID.randomUUID().toString());
		if (meterConnectionRequest.getMeterReading().getLastReadingDate() == null
				|| meterConnectionRequest.getMeterReading().getLastReadingDate() == 0) {
			Long lastReadingDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
			meterConnectionRequest.getMeterReading().setLastReadingDate(lastReadingDate);
		}
	}

}
