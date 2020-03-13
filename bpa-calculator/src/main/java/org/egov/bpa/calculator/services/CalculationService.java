package org.egov.bpa.calculator.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.common.metrics.stats.Total;
import org.egov.bpa.calculator.config.BPACalculatorConfig;
import org.egov.bpa.calculator.kafka.broker.BPACalculatorProducer;
import org.egov.bpa.calculator.repository.ServiceRequestRepository;
import org.egov.bpa.calculator.utils.BPACalculatorConstants;
import org.egov.bpa.calculator.utils.CalculationUtils;
import org.egov.bpa.calculator.web.models.BillingSlabSearchCriteria;
import org.egov.bpa.calculator.web.models.Calculation;
import org.egov.bpa.calculator.web.models.CalculationReq;
import org.egov.bpa.calculator.web.models.CalculationRes;
import org.egov.bpa.calculator.web.models.CalculationType;
import org.egov.bpa.calculator.web.models.CalulationCriteria;
import org.egov.bpa.calculator.web.models.bpa.BPA;
import org.egov.bpa.calculator.web.models.bpa.EstimatesAndSlabs;
import org.egov.bpa.calculator.web.models.demand.Category;
import org.egov.bpa.calculator.web.models.demand.TaxHeadEstimate;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class CalculationService {

	

	@Autowired
	private MDMSService mdmsService;

	@Autowired
	private DemandService demandService;
;

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
		String tenantId = calculationReq.getCalulationCriteria().get(0).getTenantId();
		Object mdmsData = mdmsService.mDMSCall(calculationReq, tenantId);
		List<Calculation> calculations = getCalculation(calculationReq.getRequestInfo(),
				calculationReq.getCalulationCriteria(), mdmsData);
		demandService.generateDemand(calculationReq.getRequestInfo(), calculations, mdmsData);
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
	 * @return List of calculations for all applicationNumbers or tradeLicenses in
	 *         criterias
	 */
	public List<Calculation> getCalculation(RequestInfo requestInfo, List<CalulationCriteria> criterias,
			Object mdmsData) {
		List<Calculation> calculations = new LinkedList<>();
		for (CalulationCriteria criteria : criterias) {
			BPA bpa;
			if (criteria.getBpa() == null && criteria.getApplicationNo() != null) {
				bpa = utils.getBuildingPlan(requestInfo, criteria.getApplicationNo(), criteria.getTenantId());
				if(bpa.getAdditionalDetails() == null) {
					bpa.setAdditionalDetails(new Object());
				}
				criteria.setBpa(bpa);
			}

			EstimatesAndSlabs estimatesAndSlabs = getTaxHeadEstimates(criteria, requestInfo, mdmsData);
			List<TaxHeadEstimate> taxHeadEstimates = estimatesAndSlabs.getEstimates();

			Calculation calculation = new Calculation();
			calculation.setBpa(criteria.getBpa());
			calculation.setTenantId(criteria.getTenantId());
			calculation.setTaxHeadEstimates(taxHeadEstimates);
			calculation.setFeeType(criteria.getFeeType());
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
	private EstimatesAndSlabs getTaxHeadEstimates(CalulationCriteria calulationCriteria, RequestInfo requestInfo,
			Object mdmsData) {
		List<TaxHeadEstimate> estimates = new LinkedList<>();
		EstimatesAndSlabs estimatesAndSlabs;
		if (calulationCriteria.getFeeType().equalsIgnoreCase(BPACalculatorConstants.LOW_RISK_PERMIT_FEE_TYPE)) {

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

		/*
		 * if(calulationCriteria.getBpa().getAdhocPenalty()!=null)
		 * estimates.add(getAdhocPenalty(calulationCriteria));
		 * 
		 * if(calulationCriteria.getTradelicense().getTradeLicenseDetail().
		 * getAdhocExemption()!=null)
		 * estimates.add(getAdhocExemption(calulationCriteria));
		 */

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
	private EstimatesAndSlabs getBaseTax(CalulationCriteria calulationCriteria, RequestInfo requestInfo,
			Object mdmsData) {
		BPA bpa = calulationCriteria.getBpa();
		EstimatesAndSlabs estimatesAndSlabs = new EstimatesAndSlabs();
		BillingSlabSearchCriteria searchCriteria = new BillingSlabSearchCriteria();
		searchCriteria.setTenantId(bpa.getTenantId());


		Map calculationTypeMap = mdmsService.getCalculationType(requestInfo, bpa, mdmsData,
				calulationCriteria.getFeeType());
		Object additionalDetails = bpa.getAdditionalDetails();

		log.info(calculationTypeMap.toString());

		if (calculationTypeMap.get("BHRSPSQMT") != null) {
			try {
				Map buildingheightCostyPrSqmt = (Map) calculationTypeMap.get("BHRSPSQMT");
				String ulbGrade = mdmsService.getUlbGrade(mdmsData, bpa.getTenantId());
				if(buildingheightCostyPrSqmt.get(ulbGrade) == null) {
					
					throw new CustomException("INVALID ULB ", "Cost for SqMtr is not configured for ULB " +ulbGrade);
				}
				if(additionalDetails == null) {
					additionalDetails = new HashMap();
				}
				double costPerSqmt = Double.valueOf(buildingheightCostyPrSqmt.get(ulbGrade).toString());
				Map areaData = edcrService.getEDCRPlanData(bpa);
				List<TaxHeadEstimate> estimates = new ArrayList<TaxHeadEstimate>();

				// PERMIT FEE
				TaxHeadEstimate estimate = new TaxHeadEstimate();
				BigDecimal permitFee = BigDecimal
						.valueOf(costPerSqmt * (Double.valueOf( areaData.get(BPACalculatorConstants.BUILDING_HEIGHT).toString())));
				if (permitFee.compareTo(BigDecimal.ZERO) == -1)
					throw new CustomException("INVALID AMOUNT", "Tax amount is negative");

				Object charitableTrustBuilding = ((Map) additionalDetails).get("isCharitableTrustBuilding");
				
				if ( charitableTrustBuilding != null && ((boolean) charitableTrustBuilding)) {
					permitFee = permitFee.divide(BigDecimal.valueOf(2));
				}
				estimate.setEstimateAmount(permitFee.setScale(0, RoundingMode.UP));
				estimate.setCategory(Category.FEE);

				String taxHeadCode = utils.getTaxHeadCode(calulationCriteria.getFeeType());
				estimate.setTaxHeadCode(taxHeadCode);
				estimates.add(estimate);
				// estimatesAndSlabs.setEstimates(Collections.singletonList(estimate));

				// DEVELOPMENT CHARGES
				if (areaData.get("BLOCKS") != null && ((List) areaData.get("BLOCKS")).size() > 1) {
					estimate = new TaxHeadEstimate();

					estimate.setCategory(Category.CHARGES);
					estimate.setEstimateAmount(
							BigDecimal.valueOf(Double.valueOf( calculationTypeMap.get("developmentCharges").toString())).setScale(0, RoundingMode.UP));
					taxHeadCode = utils.getTaxHeadCode(calulationCriteria.getFeeType()+'_'+BPACalculatorConstants.DEVELOPENT_CHARGE);
					estimate.setTaxHeadCode(taxHeadCode);
					estimates.add(estimate);
				}

				Object affordableHousingScheme = ((Map) additionalDetails).get("isAffordableHousingScheme");
				// SHULTER FUND
				if (  (affordableHousingScheme == null || !(Boolean) affordableHousingScheme )
						&& areaData.get(BPACalculatorConstants.BUILT_UP_AREA) != null
						&& calculationTypeMap.get("shelterFund") != null) {
					Map shelterFundMap = (Map) calculationTypeMap.get("shelterFund");
					Double shelterFundLeast = Double.valueOf( shelterFundMap.get("from").toString());
					Double shelterFundMax = Double.valueOf(shelterFundMap.get("to").toString());
					Double totalBuiltupArea = Double.valueOf(areaData.get(BPACalculatorConstants.BUILT_UP_AREA).toString());
					if (totalBuiltupArea.compareTo(shelterFundLeast) >= 0
							&& totalBuiltupArea.compareTo(shelterFundMax) <= 0) {
						estimate = new TaxHeadEstimate();

						estimate.setCategory(Category.CHARGES);
						estimate.setEstimateAmount(BigDecimal.valueOf(Double.valueOf(shelterFundMap.get(ulbGrade).toString()))
								.multiply(BigDecimal.valueOf(totalBuiltupArea
										* (Double.valueOf(shelterFundMap.get("percentageOfBuiltUpArea").toString()) / 100))).setScale(0, RoundingMode.UP));
						taxHeadCode = utils.getTaxHeadCode(calulationCriteria.getFeeType()+'_'+BPACalculatorConstants.SHELTER_FUND);
						estimate.setTaxHeadCode(taxHeadCode);
						estimates.add(estimate);
					}
				}else {
					log.warn("Shelter Fund is configured in the calculation type");
				}

				// SCRUTINY FEE
				if (calculationTypeMap.get("scrutinyFee") != null) {
					Double scrutinyFee = Double.valueOf(calculationTypeMap.get("scrutinyFee").toString());
					estimate = new TaxHeadEstimate();

					estimate.setCategory(Category.FEE);
					estimate.setEstimateAmount(BigDecimal.valueOf(scrutinyFee).setScale(0, RoundingMode.UP));
					taxHeadCode = utils.getTaxHeadCode(calulationCriteria.getFeeType()+'_'+BPACalculatorConstants.SCRUTINY_FEE);
					estimate.setTaxHeadCode(taxHeadCode);
					
					estimates.add(estimate);
				}else {
					log.warn("scrutiny fee is configured in the calculation type");
				}
				
				Object annualExpenditure = ((Map) additionalDetails).get("annualExpectedExpenditure");
				// LABOUR CESS
				if (calculationTypeMap.get("labourCess") != null && 
						annualExpenditure !=null) {
					
					Double labourCessPercent = Double.valueOf(calculationTypeMap.get("labourCess").toString());
					Double anuualExp = Double.valueOf(annualExpenditure.toString());
					estimate = new TaxHeadEstimate();

					estimate.setCategory(Category.TAX);
					estimate.setEstimateAmount(BigDecimal.valueOf( anuualExp* (labourCessPercent/100)).setScale(0, RoundingMode.UP));
					taxHeadCode = utils.getTaxHeadCode(calulationCriteria.getFeeType()+'_'+BPACalculatorConstants.LABOUR_CESS);
					estimate.setTaxHeadCode(taxHeadCode);
					
					estimates.add(estimate);
				}else {
					log.warn("Labour cess is not conifured in the calculation type or annual expenditure is updated in BPA");
				}
					
				estimatesAndSlabs.setEstimates(estimates);
				return estimatesAndSlabs;
			} catch (Exception e) {
				log.error(e.getMessage());
				calculationTypeMap = mdmsService.defaultMap(calulationCriteria.getFeeType());
			}

		}

		int amountCalculationType = (int) calculationTypeMap.get(BPACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT);

		TaxHeadEstimate estimate = new TaxHeadEstimate();
		BigDecimal totalTax = BigDecimal.valueOf(amountCalculationType);
		if (totalTax.compareTo(BigDecimal.ZERO) == -1)
			throw new CustomException("INVALID AMOUNT", "Tax amount is negative");

		estimate.setEstimateAmount(totalTax.setScale(0, RoundingMode.UP));
		estimate.setCategory(Category.FEE);

		String taxHeadCode = utils.getTaxHeadCode(calulationCriteria.getFeeType());
		estimate.setTaxHeadCode(taxHeadCode);

		estimatesAndSlabs.setEstimates(Collections.singletonList(estimate));

		return estimatesAndSlabs;


	}

}
