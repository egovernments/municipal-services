package org.egov.wscalculation.service;


import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.egov.wscalculation.model.AuditDetails;
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
		AuditDetails auditDetails = getAuditDetails(meterConnectionRequest.getRequestInfo().getUserInfo().getUuid(),
				true);
		meterConnectionRequest.getMeterReading().setId(UUID.randomUUID().toString());
		if (meterConnectionRequest.getMeterReading().getLastReadingDate() == null
				|| meterConnectionRequest.getMeterReading().getLastReadingDate() == 0) {
			Long lastReadingDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
			meterConnectionRequest.getMeterReading().setLastReadingDate(lastReadingDate);
		}
		meterConnectionRequest.getMeterReading().setAuditDetails(auditDetails);
	}
	
	/**
     * Method to return auditDetails for create/update flows
     *
     * @param by
     * @param isCreate
     * @return AuditDetails
     */
    public AuditDetails getAuditDetails(String by, Boolean isCreate) {
        Long time = System.currentTimeMillis();
        if(isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
    }

}
