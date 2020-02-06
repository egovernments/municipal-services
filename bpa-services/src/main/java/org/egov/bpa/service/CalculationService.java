package org.egov.bpa.service;

import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.CalculationReq;
import org.egov.bpa.web.models.CalulationCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class CalculationService {

	private ServiceRequestRepository serviceRequestRepository;

	private ObjectMapper mapper;

	private BPAConfiguration config;

	@Autowired
	public CalculationService(ServiceRequestRepository serviceRequestRepository,
			ObjectMapper mapper, BPAConfiguration config) {
		this.serviceRequestRepository = serviceRequestRepository;
		this.mapper = mapper;
		this.config = config;
	}
	
	public void addCalculation(BPARequest bpaRequest,String feeType) {
		
		CalculationReq calulcationRequest = new CalculationReq();
		calulcationRequest.setRequestInfo(bpaRequest.getRequestInfo());
		CalulationCriteria calculationCriteria = new CalulationCriteria();
		calculationCriteria.setApplicationNo(bpaRequest.getBPA().getApplicationNo());
		calculationCriteria.setBpa(bpaRequest.getBPA());
		calculationCriteria.setFeeType(feeType);
		calculationCriteria.setTenantId(bpaRequest.getBPA().getTenantId());
		 List<CalulationCriteria> criterias = Arrays.asList(calculationCriteria);
		calulcationRequest.setCalulationCriteria(criterias);
		StringBuilder url = new StringBuilder();
		url.append( this.config.getCalculatorHost());
		url.append(this.config.getCalulatorEndPoint());
		
		this.serviceRequestRepository.fetchResult(url, calulcationRequest);
	}
	
}
