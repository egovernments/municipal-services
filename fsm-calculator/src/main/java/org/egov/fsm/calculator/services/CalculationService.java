package org.egov.fsm.calculator.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.calculator.config.CalculatorConfig;
import org.egov.fsm.calculator.kafka.broker.CalculatorProducer;
import org.egov.fsm.calculator.repository.BillingSlabRepository;
import org.egov.fsm.calculator.repository.querybuilder.BillingSlabQueryBuilder;
import org.egov.fsm.calculator.utils.CalculationUtils;
import org.egov.fsm.calculator.utils.CalculatorConstants;
import org.egov.fsm.calculator.web.models.BillingSlabSearchCriteria;
import org.egov.fsm.calculator.web.models.Calculation;
import org.egov.fsm.calculator.web.models.CalculationReq;
import org.egov.fsm.calculator.web.models.CalculationRes;
import org.egov.fsm.calculator.web.models.CalulationCriteria;
import org.egov.fsm.calculator.web.models.EstimatesAndSlabs;
import org.egov.fsm.calculator.web.models.FSM;
import org.egov.fsm.calculator.web.models.demand.Category;
import org.egov.fsm.calculator.web.models.demand.TaxHeadEstimate;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

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
	
	@Autowired
	private BillingSlabQueryBuilder billingSlabQueryBuilder;
	
	@Autowired
	private BillingSlabRepository billingSlabRepository;
	

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
				.getTenantId().split("\\.")[0];
		Object mdmsData = mdmsService.mDMSCall(calculationReq, tenantId);
		List<Map> mdmsMap = JsonPath.read(mdmsData, CalculatorConstants.VEHICLE_MAKE_MODEL_JSON_PATH);
		List<Calculation> calculations = getCalculation(calculationReq.getRequestInfo(),calculationReq.getCalulationCriteria(), mdmsMap);
		demandService.generateDemand(calculationReq.getRequestInfo(),calculations, mdmsData);
		CalculationRes calculationRes = CalculationRes.builder().calculations(calculations).build();
//		producer.push(config.getSaveTopic(), calculationRes);
		return calculations;
	}
	
	
	/**
	 * Calculates tax estimates
	 * 
	 * @param calculationReq
	 *            The calculationCriteria request
	 * @return List of calculations for all applicationNumbers or tradeLicenses
	 *         in calculationReq
	 */
	public List<Calculation> estimate(CalculationReq calculationReq) {
		String tenantId = calculationReq.getCalulationCriteria().get(0)
				.getTenantId().split("\\.")[0];
		Object mdmsData = mdmsService.mDMSCall(calculationReq, tenantId);
		List<Map> mdmsMap = JsonPath.read(mdmsData, CalculatorConstants.VEHICLE_MAKE_MODEL_JSON_PATH);
		List<Calculation> calculations = getCalculation(calculationReq.getRequestInfo(),calculationReq.getCalulationCriteria(), mdmsMap);
		CalculationRes calculationRes = CalculationRes.builder().calculations(calculations).build();
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
			List<CalulationCriteria> criterias, List<Map> mdmsData) {
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
			List<Map> mdmsData) {
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
			List<Map> mdmsData) {
		FSM fsm = calulationCriteria.getFsm();
		EstimatesAndSlabs estimatesAndSlabs = new EstimatesAndSlabs();
		ArrayList<TaxHeadEstimate> estimates = new ArrayList<TaxHeadEstimate>();
		TaxHeadEstimate estimate = new TaxHeadEstimate();
		
		BigDecimal amount = null;
		String capacity = getAmountForVehicleType(fsm.getVehicleType(), mdmsData);
		if (capacity ==null || !NumberUtils.isCreatable(capacity)) {
			throw new CustomException(CalculatorConstants.INVALID_CAPACITY, "Capacity is Invalid for the given vehicleType");
		}
		
		String slumName = fsm.getAddress().getSlumName();
		
		amount = billingSlabRepository.getBillingSlabPrice(billingSlabQueryBuilder.getBillingSlabPriceQuery(fsm.getTenantId(), NumberUtils.toDouble(capacity), slumName));
		
		if(amount == null) {
			throw new CustomException(CalculatorConstants.INVALID_PRICE, "Price not found in Billing Slab for the given vehicleType and slumName");
		}
		
		
		
		BigDecimal calculatedAmout = BigDecimal.valueOf(calulationCriteria.getFsm().getNoOfTrips()).multiply( amount) ;

		
		if (calculatedAmout.compareTo(BigDecimal.ZERO) == -1)
			throw new CustomException(CalculatorConstants.INVALID_PRICE, "Tax amount is negative");

		estimate.setEstimateAmount(calculatedAmout);
		estimate.setCategory(Category.FEE);

		String taxHeadCode = utils.getTaxHeadCode(calulationCriteria.getFeeType());
		estimate.setTaxHeadCode(taxHeadCode);
		estimates.add(estimate);
		estimatesAndSlabs.setEstimates(estimates);
		return estimatesAndSlabs;
	}
	
	public String getAmountForVehicleType(String vehicleType, List<Map> vehicleTypeList) {
		String  amount = null;
		for(Map vehicleTypeMap : vehicleTypeList) {
			if(vehicleTypeMap.get("code").equals(vehicleType)) {
				amount = (String) vehicleTypeMap.get("capacity");
				break;
			}
		}
		return amount;
	}

}
