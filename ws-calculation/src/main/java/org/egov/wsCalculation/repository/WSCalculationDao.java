package org.egov.wsCalculation.repository;

import org.egov.wsCalculation.model.MeterConnectionRequest;

public interface WSCalculationDao {

	public void saveWaterConnection(MeterConnectionRequest meterConnectionRequest);

}
