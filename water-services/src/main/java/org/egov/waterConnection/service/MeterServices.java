package org.egov.waterConnection.service;

import java.util.List;

import org.egov.waterConnection.model.MeterConnectionRequest;
import org.egov.waterConnection.model.MeterReading;

public interface MeterServices {
	public List<MeterReading> addMeterReading(MeterConnectionRequest meterConnectionRequest);
}
