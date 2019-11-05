package org.egov.wsCalculation.service;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReadingSearchCriteria;


public interface MeterService {
	public List<MeterReading> addMeterReading(MeterConnectionRequest meterConnectionRequest);
	
	public List<MeterReading> searchMeterReadings(MeterReadingSearchCriteria criteria, RequestInfo requestInfo);
	
}