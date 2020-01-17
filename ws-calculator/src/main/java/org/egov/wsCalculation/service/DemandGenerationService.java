package org.egov.wsCalculation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.egov.common.contract.request.RequestInfo;
import org.egov.wsCalculation.model.CalculationCriteria;
import org.egov.wsCalculation.model.CalculationReq;
import org.egov.wsCalculation.model.DemandNotificationObj;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DemandGenerationService {

	@Autowired
	ObjectMapper mapper;

	@Autowired
	DemandService demandService;

	@Autowired
	WSCalculationService wSCalculationService;

	/**
	 * 
	 * @param record
	 *            record is calculation response.
	 * @param topic
	 *            topic is demand generation topic for water.
	 */
	public void process(List<HashMap<String, Object>> records) {
		records.forEach(record -> {
			log.info(record.get("calculationReq").toString());
		});

		{
			try {
				records.forEach(record -> {
					List<CalculationCriteria> calculationCriteria = new ArrayList<CalculationCriteria>();
					CalculationReq calculationReq = new CalculationReq();
					calculationReq = mapper.convertValue(record.get("calculationReq"), CalculationReq.class);
					RequestInfo requestInfo = mapper.convertValue(record.get("calculationReq"), CalculationReq.class)
							.getRequestInfo();
					calculationCriteria.addAll(calculationReq.getCalculationCriteria());
					log.info(calculationCriteria.toString());
					// bulkDemandGeneration(calculationReq,masterMap);
				});

			} catch (Exception ex) {
				log.error(ex.toString());
				// log.error("Error occured while processing the record from
				// topic : " + topic);
			}

		}
	}

	/**
	 * Get CalculationReq and Calculate the Tax Head on Water Charge
	 *
	 * @param request
	 * @return List of calculation.
	 */
	// public List<Calculation> bulkDemandGeneration(CalculationReq request,
	// Map<String, Object> masterMap) {
	// List<Calculation> calculations =
	// wSCalculationService.getCalculations(request, masterMap);
	// demandService.generateDemand(request.getRequestInfo(), calculations,
	// masterMap);
	// return calculations;
	// }
}
