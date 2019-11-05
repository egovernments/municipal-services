package org.egov.wsCalculation.service;

import java.util.List;

import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;


public interface MeterServices {
	public List<MeterReading> addMeterReading(MeterConnectionRequest meterConnectionRequest);
}