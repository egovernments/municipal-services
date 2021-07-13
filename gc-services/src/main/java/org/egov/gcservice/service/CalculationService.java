package org.egov.gcservice.service;

import java.util.Arrays;
import java.util.List;

import org.egov.gcservice.web.models.CalculationCriteria;
import org.egov.gcservice.web.models.CalculationReq;
import org.egov.gcservice.web.models.CalculationRes;
import org.egov.gcservice.web.models.Property;
import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.gcservice.repository.ServiceRequestRepository;
import org.egov.gcservice.util.GarbageServicesUtil;
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
	private GarbageServicesUtil garbageServicesUtil;

	/**
	 * 
	 * @param request
	 * 
	 *            If action would be APPROVE_FOR_CONNECTION then
	 * 
	 *            Estimate the fee for sewerage application and generate the
	 *            demand
	 */
	public void calculateFeeAndGenerateDemand(GarbageConnectionRequest request, Property property) {
//		if (request.getGarbageConnection().getProcessInstance().getAction().equalsIgnoreCase("APPROVE_FOR_CONNECTION")){
//			StringBuilder uri = garbageServicesUtil.getCalculatorURL();
//			CalculationCriteria criteria = CalculationCriteria.builder()
//					.applicationNo(request.getGarbageConnection().getApplicationNo())
//					.GarbageConnection(request.getGarbageConnection())
//					.tenantId(property.getTenantId()).build();
//			List<CalculationCriteria> calculationCriterias = Arrays.asList(criteria);
//			CalculationReq calRequest = CalculationReq.builder().calculationCriteria(calculationCriterias)
//					.requestInfo(request.getRequestInfo()).isconnectionCalculation(false).build();
//			try {
//				Object response = serviceRequestRepository.fetchResult(uri, calRequest);
//				CalculationRes calResponse = mapper.convertValue(response, CalculationRes.class);
//				log.info(mapper.writeValueAsString(calResponse));
//			} catch (Exception ex) {
//				log.error("Calculation response error!!", ex);
//				throw new CustomException("SEWERAGE_CALCULATION_EXCEPTION", "Calculation response can not parsed!!!");
//			}
//		}

	}
}
