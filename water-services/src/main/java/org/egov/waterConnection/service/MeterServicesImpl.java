package org.egov.waterConnection.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.waterConnection.model.MeterConnectionRequest;
import org.egov.waterConnection.model.MeterReading;
import org.egov.waterConnection.repository.ServiceRequestRepository;
import org.egov.waterConnection.util.MeterReadingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MeterServicesImpl implements MeterServices {

	@Autowired
	MeterReadingUtil meterReadingUtil;

	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	public MeterServicesImpl(ServiceRequestRepository serviceRequestRepository) {
		this.serviceRequestRepository = serviceRequestRepository;

	}

	@Override
	public List<MeterReading> addMeterReading(MeterConnectionRequest meterConnectionRequest) {
		List<MeterReading> meterReadingsList = new ArrayList<MeterReading>(1);
		Object result = serviceRequestRepository.fetchResult(meterReadingUtil.getDemandGenerationCreateURL(),
				meterConnectionRequest);
		meterReadingUtil.getMeterReadingDetails(result);
		meterReadingsList.add(meterConnectionRequest.getMeterReading());
		return meterReadingsList;
	}

}
