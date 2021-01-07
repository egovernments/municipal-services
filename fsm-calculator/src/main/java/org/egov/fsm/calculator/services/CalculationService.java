package org.egov.fsm.calculator.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.fsm.calculator.config.CalculatorConfig;
import org.egov.fsm.calculator.kafka.broker.CalculatorProducer;
import org.egov.fsm.calculator.utils.CalculationUtils;
import org.egov.fsm.calculator.utils.CalculatorConstants;
import org.egov.fsm.calculator.web.models.Calculation;
import org.egov.fsm.calculator.web.models.CalculationReq;
import org.egov.fsm.calculator.web.models.CalculationRes;
import org.egov.fsm.calculator.web.models.CalulationCriteria;
import org.egov.fsm.calculator.web.models.EstimatesAndSlabs;
import org.egov.fsm.calculator.web.models.FSM;
import org.egov.fsm.calculator.web.models.demand.Category;
import org.egov.fsm.calculator.web.models.demand.TaxHeadEstimate;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class CalculationService {

	

	@Autowired
	private MDMSService mdmsService;

	@Autowired
	private DemandService demandService;


	
	@Autowired
	private CalculatorConfig config;

	@Autowired
	private CalculationUtils utils;

	@Autowired
	private CalculatorProducer producer;


	@Autowired
	private FSMService fsmService;

	/**
	 * Calculates tax estimates and creates demand
	 * 
	 * @param calculationReq
	 *            The calculationCriteria request
	 * @return List of calculations for all applicationNumbers or tradeLicenses
	 *         in calculationReq
	 */
	public List<Calculation> calculate(CalculationReq calculationReq) {
		String tenantId = calculationReq.getCalulationCriteria().get(0)
				.getTenantId();
		Object mdmsData = mdmsService.mDMSCall(calculationReq, tenantId);
		List<Calculation> calculations = getCalculation(calculationReq.getRequestInfo(),calculationReq.getCalulationCriteria(), mdmsData);
		demandService.generateDemand(calculationReq.getRequestInfo(),calculations, mdmsData);
		CalculationRes calculationRes = CalculationRes.builder().calculations(calculations).build();
//		producer.push(config.getSaveTopic(), calculationRes);
		return calculations;
	}

	/***
	 * Calculates tax estimates
	 * 
	 * @param requestInfo
	 *            The requestInfo of the calculation request
	 * @param criterias
	 *            list of CalculationCriteria containing the tradeLicense or
	 *            applicationNumber
	 * @return List of calculations for all applicationNumbers or tradeLicenses
	 *         in criterias
	 */
	public List<Calculation> getCalculation(RequestInfo requestInfo,
			List<CalulationCriteria> criterias, Object mdmsData) {
		List<Calculation> calculations = new LinkedList<>();
		for (CalulationCriteria criteria : criterias) {
			FSM fsm;
			if (criteria.getFsm() == null
					&& criteria.getApplicationNo() != null) {
				fsm = fsmService.getFsmApplication(requestInfo, criteria.getTenantId(),
						criteria.getApplicationNo());
				criteria.setFsm(fsm);
			}

			EstimatesAndSlabs estimatesAndSlabs = getTaxHeadEstimates(criteria,
					requestInfo, mdmsData);
			List<TaxHeadEstimate> taxHeadEstimates = estimatesAndSlabs
					.getEstimates();

			Calculation calculation = new Calculation();
			calculation.setFsm(criteria.getFsm());
			calculation.setTenantId(criteria.getTenantId());
			calculation.setTaxHeadEstimates(taxHeadEstimates);
			calculation.setFeeType( criteria.getFeeType());
			calculations.add(calculation);

		}
		return calculations;
	}

	/**
	 * Creates TacHeadEstimates
	 * 
	 * @param calulationCriteria
	 *            CalculationCriteria containing the tradeLicense or
	 *            applicationNumber
	 * @param requestInfo
	 *            The requestInfo of the calculation request
	 * @return TaxHeadEstimates and the billingSlabs used to calculate it
	 */
	private EstimatesAndSlabs getTaxHeadEstimates(
			CalulationCriteria calulationCriteria, RequestInfo requestInfo,
			Object mdmsData) {
		List<TaxHeadEstimate> estimates = new LinkedList<>();
		EstimatesAndSlabs estimatesAndSlabs = getBaseTax(calulationCriteria, requestInfo, mdmsData);
		estimates.addAll(estimatesAndSlabs.getEstimates());

		estimatesAndSlabs.setEstimates(estimates);

		return estimatesAndSlabs;
	}

	/**
	 * Calculates base tax and cretaes its taxHeadEstimate
	 * 
	 * @param calulationCriteria
	 *            CalculationCriteria containing the tradeLicense or
	 *            applicationNumber
	 * @param requestInfo
	 *            The requestInfo of the calculation request
	 * @return BaseTax taxHeadEstimate and billingSlabs used to calculate it
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private EstimatesAndSlabs getBaseTax(CalulationCriteria calulationCriteria, RequestInfo requestInfo,
			Object mdmsData) {
		FSM fsm = calulationCriteria.getFsm();
		EstimatesAndSlabs estimatesAndSlabs = new EstimatesAndSlabs();
		ArrayList<TaxHeadEstimate> estimates = new ArrayList<TaxHeadEstimate>();
		TaxHeadEstimate estimate = new TaxHeadEstimate();
		Map<String, String> additionalDetails = fsm.getadditionalDetails() != null ? (Map<String, String>)fsm.getadditionalDetails()
				: new HashMap<String, String>();
		BigDecimal tripAmount  = BigDecimal.valueOf(Double.valueOf((String)additionalDetails.get("tripAmount")));;
		
		
//		}catch( Exception e) {
//			throw new CustomException(CalculatorConstants.INVALID_AMOUNT, "Trip Amouont is Invalid");
//		}
		
		BigDecimal calculatedAmout = BigDecimal.valueOf(calulationCriteria.getFsm().getNoOfTrips()).multiply( tripAmount) ;

		
		if (calculatedAmout.compareTo(BigDecimal.ZERO) == -1)
			throw new CustomException(CalculatorConstants.INVALID_AMOUNT, "Tax amount is negative");

		estimate.setEstimateAmount(calculatedAmout);
		estimate.setCategory(Category.FEE);

		String taxHeadCode = utils.getTaxHeadCode(calulationCriteria.getFeeType());
		estimate.setTaxHeadCode(taxHeadCode);
		estimates.add(estimate);
		estimatesAndSlabs.setEstimates(estimates);
		return estimatesAndSlabs;
	}

}
