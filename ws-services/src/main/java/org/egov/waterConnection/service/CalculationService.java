package org.egov.waterConnection.service;

import java.util.Arrays;
import java.util.List;

import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.constants.WCConstants;
import org.egov.waterConnection.model.CalculationCriteria;
import org.egov.waterConnection.model.CalculationReq;
import org.egov.waterConnection.model.CalculationRes;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.repository.ServiceRequestRepository;
import org.egov.waterConnection.util.WaterServicesUtil;
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
	private WaterServicesUtil waterServiceUtil;

	/**
	 * 
	 * @param request
	 * 
	 * If action would be APPROVE_FOR_CONNECTION then
	 * 
	 *Estimate the fee for water application and generate the demand
	 * 
	 */
	public void calculateFeeAndGenerateDemand(WaterConnectionRequest request) {
		if (request.getWaterConnection().getAction().equalsIgnoreCase(WCConstants.APPROVE_CONNECTION_CONST)) {
			StringBuilder uri = waterServiceUtil.getCalculatorURL();
			CalculationCriteria criteria = CalculationCriteria.builder()
					.applicationNo(request.getWaterConnection().getApplicationNo())
					.waterConnection(request.getWaterConnection())
					.tenantId(request.getWaterConnection().getProperty().getTenantId()).build();
			List<CalculationCriteria> calculationCriterias = Arrays.asList(criteria);
			CalculationReq calRequest = CalculationReq.builder().calculationCriteria(calculationCriterias)
					.requestInfo(request.getRequestInfo()).isconnectionCalculation(false).build();
			try {
				Object response = serviceRequestRepository.fetchResult(uri, calRequest);
				CalculationRes calResponse = mapper.convertValue(response, CalculationRes.class);
				log.info(mapper.writeValueAsString(calResponse));
			} catch (Exception ex) {
				log.error("Calculation response error!!", ex);
				throw new CustomException("WATER_CALCULATION_EXCEPTION", "Calculation response can not parsed!!!");
			}
		}

	}
}
