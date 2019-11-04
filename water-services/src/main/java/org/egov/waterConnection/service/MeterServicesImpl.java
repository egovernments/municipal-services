package org.egov.waterConnection.service;

import java.util.List;

import org.egov.waterConnection.model.MeterConnectionRequest;
import org.egov.waterConnection.model.MeterReading;
import org.springframework.stereotype.Component;

@Component
public class MeterServicesImpl implements MeterServices {

	@Override
	public List<MeterReading> addMeterReading(MeterConnectionRequest meterConnectionRequest) {
		return null;
	}

}
