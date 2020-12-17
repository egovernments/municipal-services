package org.egov.fsm.service;


import java.util.Arrays;
import java.util.List;

import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalculationService {

	private ServiceRequestRepository serviceRequestRepository;

	private FSMConfiguration config;

	@Autowired
	public CalculationService(ServiceRequestRepository serviceRequestRepository, FSMConfiguration config) {
		this.serviceRequestRepository = serviceRequestRepository;
		this.config = config;
	}

	public void addCalculation(FSMConfiguration fsmRequest, String feeType) {

	//TDOD prepare calculation request object and call calculation service

//		this.serviceRequestRepository.fetchResult(url, calulcationRequest);
	}

}
