package org.egov.bpa.calculator.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.assertj.core.internal.DeepDifference.Difference;
import org.egov.bpa.calculator.config.BPACalculatorConfig;
import org.egov.bpa.calculator.kafka.broker.BPACalculatorProducer;
import org.egov.bpa.calculator.repository.ServiceRequestRepository;
import org.egov.bpa.calculator.utils.BPACalculatorConstants;
import org.egov.bpa.calculator.utils.CalculationUtils;
import org.egov.bpa.calculator.web.models.BillingSlabSearchCriteria;
import org.egov.bpa.calculator.web.models.Calculation;
import org.egov.bpa.calculator.web.models.CalculationReq;
import org.egov.bpa.calculator.web.models.CalculationRes;
import org.egov.bpa.calculator.web.models.CalulationCriteria;
import org.egov.bpa.calculator.web.models.bpa.BPA;
import org.egov.bpa.calculator.web.models.bpa.EstimatesAndSlabs;
import org.egov.bpa.calculator.web.models.demand.Category;
import org.egov.bpa.calculator.web.models.demand.TaxHeadEstimate;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
	private EDCRService edcrService;
	
	@Autowired
	private BPACalculatorConfig config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private CalculationUtils utils;

	@Autowired
	private BPACalculatorProducer producer;

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
		producer.push(config.getSaveTopic(), calculationRes);
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
			BPA bpa;
			if (criteria.getBpa() == null
					&& criteria.getApplicationNo() != null) {
				bpa = utils.getBuildingPlan(requestInfo,
						criteria.getApplicationNo(), criteria.getTenantId());
				criteria.setBpa(bpa);
			}

			EstimatesAndSlabs estimatesAndSlabs = getTaxHeadEstimates(criteria,
					requestInfo, mdmsData);
			List<TaxHeadEstimate> taxHeadEstimates = estimatesAndSlabs
					.getEstimates();

			Calculation calculation = new Calculation();
			calculation.setBpa(criteria.getBpa());
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
		EstimatesAndSlabs estimatesAndSlabs;
		if (calulationCriteria.getFeeType().equalsIgnoreCase(BPACalculatorConstants.LOW_RISK_PERMIT_FEE_TYPE)) {

//			 stopping Application fee for lowrisk applicaiton according to BBI-391
			calulationCriteria.setFeeType(BPACalculatorConstants.MDMS_CALCULATIONTYPE_LOW_APL_FEETYPE);
			estimatesAndSlabs = getBaseTax(calulationCriteria, requestInfo, mdmsData);

			estimates.addAll(estimatesAndSlabs.getEstimates());

			calulationCriteria.setFeeType(BPACalculatorConstants.MDMS_CALCULATIONTYPE_LOW_SANC_FEETYPE);
			estimatesAndSlabs = getBaseTax(calulationCriteria, requestInfo, mdmsData);

			estimates.addAll(estimatesAndSlabs.getEstimates());

			calulationCriteria.setFeeType(BPACalculatorConstants.LOW_RISK_PERMIT_FEE_TYPE);

		} else {
			estimatesAndSlabs = getBaseTax(calulationCriteria, requestInfo, mdmsData);
			estimates.addAll(estimatesAndSlabs.getEstimates());
		}

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
	private EstimatesAndSlabs getBaseTax(CalulationCriteria calulationCriteria,
 RequestInfo requestInfo,
			Object mdmsData) {
		BPA ocbpa = calulationCriteria.getBpa();
		EstimatesAndSlabs estimatesAndSlabs = new EstimatesAndSlabs();
		BillingSlabSearchCriteria searchCriteria = new BillingSlabSearchCriteria();
		searchCriteria.setTenantId(ocbpa.getTenantId());

		Map calculationTypeMap = mdmsService.getCalculationType(requestInfo, ocbpa, mdmsData,
				calulationCriteria.getFeeType());
		int amountCalculationType = 0;
		if (calculationTypeMap.containsKey("calsiLogic")) {

			LinkedHashMap ocEdcr = edcrService.getEDCRDetails(requestInfo, ocbpa);
			String jsonString = new JSONObject(ocEdcr).toString();
			DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
			JSONArray permitNumber = context.read("edcrDetail.*.permitNumber");

			Double ocTotalBuitUpArea = context.read("edcrDetail[0].planDetail.virtualBuilding.totalBuitUpArea");

			String bpaDcr = null;
			DocumentContext edcrContext = null;
			if (!CollectionUtils.isEmpty(permitNumber)) {
				BPA bpa = edcrService.getBuildingPlan(requestInfo, permitNumber.get(0).toString(), ocbpa.getTenantId());
				bpaDcr = bpa.getEdcrNumber();
				if (bpaDcr != null) {
					LinkedHashMap edcr = edcrService.getEDCRDetails(requestInfo, ocbpa);
					String jsonData = new JSONObject(edcr).toString();
					edcrContext = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonData);

				}
			}
			Double bpaTotalBuitUpArea = edcrContext.read("edcrDetail[0].planDetail.virtualBuilding.totalBuitUpArea");
			Double diffInBuildArea = ocTotalBuitUpArea - bpaTotalBuitUpArea;

			if (diffInBuildArea > 10) {
				String jsonData = new JSONObject(calculationTypeMap).toString();
				DocumentContext calcContext = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonData);
				JSONArray data = calcContext.read("calsiLogic.*.deviation");
				System.out.println(data.get(0));
				JSONArray data1 = (JSONArray) data.get(0);
				for (int i = 0; i < data1.size(); i++) {
					LinkedHashMap diff = (LinkedHashMap) data1.get(i);
					Integer from = (Integer) diff.get("from");
					Integer to = (Integer) diff.get("to");
					Integer uom = (Integer) diff.get("uom");
					Integer mf = (Integer) diff.get("MF");
					if (diffInBuildArea >= from && diffInBuildArea <= to) {
						amountCalculationType = (int) (diffInBuildArea * mf * uom);
						break;
					}

				}
			}
		} else {
			amountCalculationType = Integer
					.parseInt(calculationTypeMap.get(BPACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT).toString());
		}

		TaxHeadEstimate estimate = new TaxHeadEstimate();
		BigDecimal totalTax = BigDecimal.valueOf(amountCalculationType);
		if (totalTax.compareTo(BigDecimal.ZERO) == -1)
			throw new CustomException("INVALID AMOUNT", "Tax amount is negative");

		estimate.setEstimateAmount(totalTax);
		estimate.setCategory(Category.FEE);

		String taxHeadCode = utils.getTaxHeadCode(ocbpa.getBusinessService(), calulationCriteria.getFeeType());
		estimate.setTaxHeadCode(taxHeadCode);

		estimatesAndSlabs.setEstimates(Collections.singletonList(estimate));

		return estimatesAndSlabs;

	}

}
