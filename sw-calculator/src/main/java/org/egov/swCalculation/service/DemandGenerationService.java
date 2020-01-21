package org.egov.swCalculation.service;

import java.util.HashMap;
import java.util.Map;
import org.egov.common.contract.request.RequestInfo;
import org.egov.swCalculation.model.CalculationReq;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
	SWCalculationService sWCalculationService;

	/**
	 * 
	 * @param record
	 *            record is calculation response.
	 * @param topic
	 *            topic is demand generation topic for water.
	 */
	public void process(HashMap<String, Object> record, String topic) {
		try {

			HashMap<String, Object> masterMap = (HashMap<String, Object>) record.get("masterMap");
			Map<String, Object> info = (Map<String, Object>) record.get("requestInfo");
			RequestInfo requestInfo = mapper.convertValue(info, RequestInfo.class);
			CalculationReq calculationReq = (CalculationReq) record.get("calculationReq");
			String jsonString = new JSONObject(masterMap).toString();
			// bulkDemandGeneration(calculationReq,masterMap);

		} catch (Exception ex) {
			log.error(ex.toString());
			log.error("Error occured while processing the record from topic : " + topic);
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
	// sWCalculationService.getCalculations(request, masterMap);
	// demandService.generateDemand(request.getRequestInfo(), calculations,
	// masterMap);
	// return calculations;
	// }
}
