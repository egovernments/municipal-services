package org.egov.wsCalculation.repository;

import java.util.List;

import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReadingSearchCriteria;

public interface WSCalculationDao {

	public void saveWaterConnection(MeterConnectionRequest meterConnectionRequest);
	
	public List<MeterReading> searchMeterReadings(MeterReadingSearchCriteria criteria);
	
	
	public int isMeterReadingConnectionExist(List<String> ids);

}
