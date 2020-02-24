package org.egov.swService.service;

import java.util.Arrays;
import java.util.List;

import org.egov.swService.model.CalculationCriteria;
import org.egov.swService.model.CalculationReq;
import org.egov.swService.model.CalculationRes;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.repository.ServiceRequestRepository;
import org.egov.swService.util.SWConstants;
import org.egov.swService.util.SewerageServicesUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CalculationService {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private SewerageServicesUtil sewerageServicesUtil;

	/**
	 * 
	 * @param request
	 * 
	 *            If action would be APPROVE_FOR_CONNECTION then
	 * 
	 *            Estimate the fee for sewerage application and generate the demand
	 * 
	 */
	public void calculateFeeAndGenerateDemand(SewerageConnectionRequest request) {
		if (SWConstants.APPROVE_CONNECTION_CONST.equalsIgnoreCase(request.getSewerageConnection().getAction())) {
			CalculationCriteria criteria = CalculationCriteria.builder()
					.applicationNo(request.getSewerageConnection().getApplicationNo())
					.sewerageConnection(request.getSewerageConnection())
					.tenantId(request.getSewerageConnection().getProperty().getTenantId()).build();
			List<CalculationCriteria> calculationCriterias = Arrays.asList(criteria);
			CalculationReq calRequest = CalculationReq.builder().calculationCriteria(calculationCriterias)
					.requestInfo(request.getRequestInfo()).isconnectionCalculation(false).build();
			try {
				Object response = serviceRequestRepository.fetchResult(sewerageServicesUtil.getCalculatorURL(), calRequest);
				CalculationRes calResponse = mapper.convertValue(response, CalculationRes.class);
				log.info(mapper.writeValueAsString(calResponse));
			} catch (Exception ex) {
				log.error("Calculation response error!!", ex);
				throw new CustomException("SEWERAGE_CALCULATION_EXCEPTION", "Calculation response can not parsed!!!");
			}
		}

	}
}
