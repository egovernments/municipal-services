package org.egov.pt.calculator.service;

import static org.egov.pt.calculator.util.CalculatorConstants.BILLING_SLAB_MATCH_AREA;
import static org.egov.pt.calculator.util.CalculatorConstants.BILLING_SLAB_MATCH_ERROR_CODE;
import static org.egov.pt.calculator.util.CalculatorConstants.BILLING_SLAB_MATCH_ERROR_MESSAGE;
import static org.egov.pt.calculator.util.CalculatorConstants.BILLING_SLAB_MATCH_FLOOR;
import static org.egov.pt.calculator.util.CalculatorConstants.BILLING_SLAB_MATCH_USAGE_DETAIL;
import static org.egov.pt.calculator.util.CalculatorConstants.EG_PT_DEPRECIATING_ASSESSMENT_ERROR;
import static org.egov.pt.calculator.util.CalculatorConstants.EG_PT_DEPRECIATING_ASSESSMENT_ERROR_MSG_ESTIMATE;
import static org.egov.pt.calculator.util.CalculatorConstants.EG_PT_ESTIMATE_ARV_NULL;
import static org.egov.pt.calculator.util.CalculatorConstants.EG_PT_ESTIMATE_ARV_NULL_MSG;
import static org.egov.pt.calculator.util.CalculatorConstants.EXEMPTION_FIELD_NAME;
import static org.egov.pt.calculator.util.CalculatorConstants.OWNER_TYPE_MASTER;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_ADVANCE_CARRYFORWARD;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_CANCER_CESS;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_ESTIMATE_BILLINGSLABS_UNMATCH;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_ESTIMATE_BILLINGSLABS_UNMATCH_MSG;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_ESTIMATE_BILLINGSLABS_UNMATCH_VACANCT;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_ESTIMATE_BILLINGSLABS_UNMATCH_VACANT_MSG;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_ESTIMATE_BILLINGSLABS_UNMATCH_replace_id;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_FIRE_CESS;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_OWNER_EXEMPTION;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_TAX;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_TIME_INTEREST;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_TIME_PENALTY;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_TIME_REBATE;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_TYPE_VACANT_LAND;
import static org.egov.pt.calculator.util.CalculatorConstants.PT_UNIT_USAGE_EXEMPTION;
import static org.egov.pt.calculator.util.CalculatorConstants.TAXHEADMASTER_MASTER_KEY;
import static org.egov.pt.calculator.util.CalculatorConstants.USAGE_MAJOR_MASTER;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.pt.calculator.util.CalculatorConstants;
import org.egov.pt.calculator.util.CalculatorUtils;
import org.egov.pt.calculator.util.Configurations;
import org.egov.pt.calculator.util.PBFirecessUtils;
import org.egov.pt.calculator.validator.CalculationValidator;
import org.egov.pt.calculator.web.models.BillingSlab;
import org.egov.pt.calculator.web.models.BillingSlabSearchCriteria;
import org.egov.pt.calculator.web.models.Calculation;
import org.egov.pt.calculator.web.models.CalculationCriteria;
import org.egov.pt.calculator.web.models.CalculationReq;
import org.egov.pt.calculator.web.models.CalculationRes;
import org.egov.pt.calculator.web.models.TaxHeadEstimate;
import org.egov.pt.calculator.web.models.demand.Category;
import org.egov.pt.calculator.web.models.demand.Demand;
import org.egov.pt.calculator.web.models.demand.TaxHeadMaster;
import org.egov.pt.calculator.web.models.property.Assessment;
import org.egov.pt.calculator.web.models.property.OwnerInfo;
import org.egov.pt.calculator.web.models.property.Property;
import org.egov.pt.calculator.web.models.property.Unit;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class EstimationService {

	@Autowired
	private BillingSlabService billingSlabService;

	@Autowired
	private PayService payService;

	@Autowired
	private Configurations configs;

	@Autowired
	private MasterDataService mDataService;

	@Autowired
	private DemandService demandService;

	@Autowired
	private PBFirecessUtils firecessUtils;

	@Autowired
	CalculationValidator calcValidator;

	@Autowired
    private EnrichmentService enrichmentService;

	@Autowired
	private AssessmentService assessmentService;

	@Autowired
	private CalculatorUtils utils;

	@Value("${customization.pbfirecesslogic:false}")
	Boolean usePBFirecessLogic;



	/**
	 * Calculates tax and creates demand for the given assessment number
	 * @param calculationReq The calculation request object containing the calculation criteria
	 * @return Map of assessment number to Calculation
	 */
	public Map<String, Calculation> calculateAndCreateDemand(CalculationReq calculationReq){
		Map<String,Calculation> res = demandService.generateDemands(calculationReq);
		return res;
	}

	/**
	 * Generates a map with assessment-number of property as key and estimation
	 * map(taxhead code as key, amount to be paid as value) as value
	 * will be called by calculate api
	 *
	 * @param request incoming calculation request containing the criteria.
	 * @return Map<String, Calculation> key of assessment number and value of calculation object.
	 */
	public Map<String, Calculation> getEstimationPropertyMap(CalculationReq request,Map<String,Object> masterMap) {

		RequestInfo requestInfo = request.getRequestInfo();
		CalculationCriteria criteria = request.getCalculationCriteria();
		Map<String, Calculation> calculationPropertyMap = new HashMap<>();
		Property property = criteria.getPropertyCalculatorWrapper().getProperty();
		Assessment detail = criteria.getPropertyCalculatorWrapper().getAssessment();
		calcValidator.validatePropertyForCalculation(detail, property);
		String assessmentNumber = detail.getAssessmentNumber();
		Calculation calculation = getCalculation(requestInfo, criteria,masterMap);
		calculation.setServiceNumber(property.getPropertyId());
		calculationPropertyMap.put(assessmentNumber, calculation);

		return calculationPropertyMap;
	}

	/**
	 * Method to estimate the tax to be paid for given property
	 * will be called by estimate api
	 *
	 * @param request incoming calculation request containing the criteria.
	 * @return CalculationRes calculation object containing all the tax for the given criteria.
	 */
    public CalculationRes getTaxCalculation(CalculationReq request) {

        CalculationCriteria criteria = request.getCalculationCriteria();
        Property property = criteria.getPropertyCalculatorWrapper().getProperty();
        Assessment detail = criteria.getPropertyCalculatorWrapper().getAssessment();
        calcValidator.validatePropertyForCalculation(detail, property);
        Map<String,Object> masterMap = mDataService.getMasterMap(request);
        return new CalculationRes(new ResponseInfo(), Collections.singletonList(getCalculation(request.getRequestInfo(), criteria, masterMap)));
    }

	/**
	 * Generates a List of Tax head estimates with tax head code,
	 * tax head category and the amount to be collected for the key.
     *
     * @param criteria criteria based on which calculation will be done.
     * @param requestInfo request info from incoming request.
	 * @return Map<String, Double>
	 */
	private Map<String,List> getEstimationMap(CalculationCriteria criteria, RequestInfo requestInfo, Map<String, Object> masterMap) {

		BigDecimal taxAmt = BigDecimal.ZERO;
		BigDecimal usageExemption = BigDecimal.ZERO;
		Property property = criteria.getPropertyCalculatorWrapper().getProperty();
		Assessment detail = criteria.getPropertyCalculatorWrapper().getAssessment();
		String assessmentYear = detail.getFinancialYear();
		String tenantId = property.getTenantId();

		if(criteria.getFromDate()==null || criteria.getToDate()==null)
            enrichmentService.enrichDemandPeriod(criteria,assessmentYear,masterMap);

        List<BillingSlab> filteredBillingSlabs = getSlabsFiltered(detail, property, requestInfo);

		Map<String, Map<String, List<Object>>> propertyBasedExemptionMasterMap = new HashMap<>();
		Map<String, JSONArray> timeBasedExemptionMasterMap = new HashMap<>();
		mDataService.setPropertyMasterValues(requestInfo, tenantId, propertyBasedExemptionMasterMap,
				timeBasedExemptionMasterMap);

		List<String> billingSlabIds = new LinkedList<>();

		/*
		 * by default land should get only one slab from database per tenantId
		 */
		if (PT_TYPE_VACANT_LAND.equalsIgnoreCase(property.getPropertyType()) && filteredBillingSlabs.size() != 1)
			throw new CustomException(PT_ESTIMATE_BILLINGSLABS_UNMATCH_VACANCT,PT_ESTIMATE_BILLINGSLABS_UNMATCH_VACANT_MSG
					.replace("{count}",String.valueOf(filteredBillingSlabs.size())));

		else if (PT_TYPE_VACANT_LAND.equalsIgnoreCase(property.getPropertyType())) {
			taxAmt = taxAmt.add(BigDecimal.valueOf(filteredBillingSlabs.get(0).getUnitRate() * property.getLandArea()));
		} else {

			double unBuiltRate = 0.0;
			int groundUnitsCount = 0;
			Double groundUnitsArea = 0.0;
			int i = 0;

			for (Unit unit : detail.getUnits()) {

				BillingSlab slab = getSlabForCalc(filteredBillingSlabs, unit);
				BigDecimal currentUnitTax = getTaxForUnit(slab, unit);
				billingSlabIds.add(slab.getId()+"|"+i);

				/*
				 * counting the number of units & total area in ground floor for unbuilt area
				 * tax calculation
				 */
				if (unit.getFloorNo().equalsIgnoreCase("0")) {
					groundUnitsCount += 1;
					groundUnitsArea += unit.getUnitArea();
					if (null != slab.getUnBuiltUnitRate())
						unBuiltRate += slab.getUnBuiltUnitRate();
				}
				taxAmt = taxAmt.add(currentUnitTax);
				usageExemption = usageExemption
						.add(getExemption(unit, currentUnitTax, assessmentYear, propertyBasedExemptionMasterMap));
				i++;
			}
			/*
			 * making call to get unbuilt area tax estimate
			 */
			taxAmt = taxAmt.add(getUnBuiltRate(detail, property, unBuiltRate, groundUnitsCount, groundUnitsArea));

			/*
			 * special case to handle property with one unit
			 */
			if (detail.getUnits().size() == 1)
				usageExemption = getExemption(detail.getUnits().get(0), taxAmt, assessmentYear,
						propertyBasedExemptionMasterMap);
		}
		List<TaxHeadEstimate> taxHeadEstimates =  getEstimatesForTax(property, assessmentYear, taxAmt, usageExemption, detail, propertyBasedExemptionMasterMap,
				timeBasedExemptionMasterMap);

		Map<String,List> estimatesAndBillingSlabs = new HashMap<>();
		estimatesAndBillingSlabs.put("estimates",taxHeadEstimates);
		estimatesAndBillingSlabs.put("billingSlabIds",billingSlabIds);

		return estimatesAndBillingSlabs;

	}

	/**
	 * Private method to calculate the un-built area tax estimate
	 *
	 * gives the subtraction of landArea and buildUpArea if both are present.
	 *
	 * on absence of landArea Zero will be given.
	 *
	 * on absence of buildUpArea sum of all unit areas of ground floor
	 *
	 * will be subtracted from the landArea.
	 *
	 * the un-Built UnitRate is the average of unBuilt rates from ground units.
	 *
	 * @param detail The property detail
	 * @param unBuiltRate The unit rate for the un-built area in the given property detail.
	 * @param groundUnitsCount The count of all ground floor units.
	 * @param groundUnitsArea Sum of ground floor units area
	 * @return calculated tax for un-built area in the property detail.
	 */
	private BigDecimal getUnBuiltRate(Assessment detail, Property property, double unBuiltRate, int groundUnitsCount, Double groundUnitsArea) {

        BigDecimal unBuiltAmt = BigDecimal.ZERO;
        if (0.0 < unBuiltRate && null != property.getLandArea() && groundUnitsCount > 0) {

            double diffArea = null != detail.getBuildUpArea() ? property.getLandArea() - detail.getBuildUpArea()
                    : property.getLandArea() - groundUnitsArea;
            // ignoring if land Area is lesser than buildUpArea/groundUnitsAreaSum in estimate instead of throwing error
            // since property service validates the same for calculation
            diffArea = diffArea < 0.0 ? 0.0 : diffArea;
            unBuiltAmt = unBuiltAmt.add(BigDecimal.valueOf((unBuiltRate / groundUnitsCount) * (diffArea)));
        }
			return unBuiltAmt;
    }

	/**
	 * Returns Tax amount value for the unit from the list of slabs passed
	 *
	 * The tax is dependent on the unit rate and unit area for all cases
	 *
	 * except for commercial units which is rented, for this a percent will
	 *
	 * be applied on the annual rent value from the slab.
	 *
	 * arvPercent is not provided in the slab, it will be picked from the config
	 *
	 * which is common for the slab.
	 *
	 * @param slab The single billing slab that has been filtered for this particular unit.
	 * @param unit the unit for which tax should be calculated.
	 * @return calculated tax amount for the incoming unit
	 */
	private BigDecimal getTaxForUnit(BillingSlab slab, Unit unit) {

		boolean isUnitCommercial = unit.getUsageCategory().equalsIgnoreCase(configs.getUsageMajorNonResidential());
		boolean isUnitRented = unit.getOccupancyType().toString().equalsIgnoreCase(configs.getOccupancyTypeRented());
		BigDecimal currentUnitTax;

        if (null == slab) {
            String msg = BILLING_SLAB_MATCH_ERROR_MESSAGE
                    .replace(BILLING_SLAB_MATCH_AREA, unit.getUnitArea().toString())
                    .replace(BILLING_SLAB_MATCH_FLOOR, unit.getFloorNo())
                    .replace(BILLING_SLAB_MATCH_USAGE_DETAIL, "nill");
            throw new CustomException(BILLING_SLAB_MATCH_ERROR_CODE, msg);
        }

		if (isUnitCommercial && isUnitRented) {

			if (unit.getArv() == null)
                throw new CustomException(EG_PT_ESTIMATE_ARV_NULL, EG_PT_ESTIMATE_ARV_NULL_MSG);

			BigDecimal multiplier;
			if (null != slab.getArvPercent())
				multiplier = BigDecimal.valueOf(slab.getArvPercent() / 100);
			else
				multiplier = BigDecimal.valueOf(configs.getArvPercent() / 100);
			currentUnitTax = unit.getArv().multiply(multiplier);
		} else {
			currentUnitTax = BigDecimal.valueOf(unit.getUnitArea() * slab.getUnitRate());
		}
		return currentUnitTax;
	}

	/**
	 * Return an Estimate list containing all the required tax heads
	 * mapped with respective amt to be paid.
	 *
	 * @param assessmentYear year for which calculation is being done
	 * @param taxAmt tax amount for which rebate & penalty will be applied
	 * @param usageExemption  total exemption value given for all unit usages
	 * @param detail proeprty detail object
	 * @param propertyBasedExemptionMasterMap property masters which contains exemption values associated with them
	 * @param timeBasedExemeptionMasterMap masters with period based exemption values
	 */
	private List<TaxHeadEstimate> getEstimatesForTax(Property property, String assessmentYear, BigDecimal taxAmt, BigDecimal usageExemption, Assessment detail,
			Map<String, Map<String, List<Object>>> propertyBasedExemptionMasterMap,
			Map<String, JSONArray> timeBasedExemeptionMasterMap) {

		BigDecimal payableTax = taxAmt;
		List<TaxHeadEstimate> estimates = new ArrayList<>();

		// taxes
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(PT_TAX).estimateAmount(taxAmt.setScale(2, 2)).build());

		// usage exemption
		 usageExemption = usageExemption.setScale(2, 2).negate();
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(PT_UNIT_USAGE_EXEMPTION).estimateAmount(
		        usageExemption).build());
		payableTax = payableTax.add(usageExemption);

		// owner exemption
		BigDecimal userExemption = getExemption(property.getOwners(), payableTax, assessmentYear,
				propertyBasedExemptionMasterMap).setScale(2, 2).negate();
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(PT_OWNER_EXEMPTION).estimateAmount(userExemption).build());
		payableTax = payableTax.add(userExemption);

		// Fire cess
		List<Object> fireCessMasterList = timeBasedExemeptionMasterMap.get(CalculatorConstants.FIRE_CESS_MASTER);
		BigDecimal fireCess;

		if (usePBFirecessLogic) {
			fireCess = firecessUtils.getPBFireCess(property, payableTax, assessmentYear, fireCessMasterList, detail);
			estimates.add(
					TaxHeadEstimate.builder().taxHeadCode(PT_FIRE_CESS).estimateAmount(fireCess.setScale(2, 2)).build());
		} else {
			fireCess = mDataService.getCess(payableTax, assessmentYear, fireCessMasterList);
			estimates.add(
					TaxHeadEstimate.builder().taxHeadCode(PT_FIRE_CESS).estimateAmount(fireCess.setScale(2, 2)).build());

		}

		// Cancer cess
		List<Object> cancerCessMasterList = timeBasedExemeptionMasterMap.get(CalculatorConstants.CANCER_CESS_MASTER);
		BigDecimal cancerCess = mDataService.getCess(payableTax, assessmentYear, cancerCessMasterList);
		estimates.add(
				TaxHeadEstimate.builder().taxHeadCode(PT_CANCER_CESS).estimateAmount(cancerCess.setScale(2, 2)).build());

		// get applicable rebate and penalty
		Map<String, BigDecimal> rebatePenaltyMap = payService.applyPenaltyRebateAndInterest(payableTax, BigDecimal.ZERO,
				 assessmentYear, timeBasedExemeptionMasterMap,null);

		if (null != rebatePenaltyMap) {

			BigDecimal rebate = rebatePenaltyMap.get(PT_TIME_REBATE);
			BigDecimal penalty = rebatePenaltyMap.get(PT_TIME_PENALTY);
			BigDecimal interest = rebatePenaltyMap.get(PT_TIME_INTEREST);
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(PT_TIME_REBATE).estimateAmount(rebate).build());
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(PT_TIME_PENALTY).estimateAmount(penalty).build());
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(PT_TIME_INTEREST).estimateAmount(interest).build());
			payableTax = payableTax.add(rebate).add(penalty).add(interest);
		}

		// AdHoc Values (additional rebate or penalty manually entered by the employee)
		
/*		if (null != detail.getAdhocPenalty())
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(PT_ADHOC_PENALTY)
					.estimateAmount(detail.getAdhocPenalty()).build());

		if (null != detail.getAdhocExemption() && detail.getAdhocExemption().compareTo(payableTax.add(fireCess)) <= 0) {
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(PT_ADHOC_REBATE)
					.estimateAmount(detail.getAdhocExemption().negate()).build());
		}
		else if (null != detail.getAdhocExemption()) {
			throw new CustomException(PT_ADHOC_REBATE_INVALID_AMOUNT, PT_ADHOC_REBATE_INVALID_AMOUNT_MSG + taxAmt);
		}*/
		
		
		return estimates;
	}

	/**
	 * Prepares Calculation Response based on the provided TaxHeadEstimate List
	 *
	 * All the credit taxHeads will be payable and all debit tax heads will be deducted.
	 *
	 * @param criteria criteria based on which calculation will be done.
	 * @param requestInfo request info from incoming request.
	 * @return Calculation object constructed based on the resulting tax amount and other applicables(rebate/penalty)
	 */
    private Calculation getCalculation(RequestInfo requestInfo, CalculationCriteria criteria,Map<String,Object> masterMap) {

        Map<String,List> estimatesAndBillingSlabs = getEstimationMap(criteria, requestInfo,masterMap);

		List<TaxHeadEstimate> estimates = estimatesAndBillingSlabs.get("estimates");
		List<String> billingSlabIds = estimatesAndBillingSlabs.get("billingSlabIds");

        Property property = criteria.getPropertyCalculatorWrapper().getProperty();
        Assessment detail = criteria.getPropertyCalculatorWrapper().getAssessment();
        String tenantId = null != property.getTenantId() ? property.getTenantId() : criteria.getTenantId();


		Map<String, Category> taxHeadCategoryMap = ((List<TaxHeadMaster>)masterMap.get(TAXHEADMASTER_MASTER_KEY)).stream()
				.collect(Collectors.toMap(TaxHeadMaster::getCode, TaxHeadMaster::getCategory));

		BigDecimal taxAmt = BigDecimal.ZERO;
		BigDecimal penalty = BigDecimal.ZERO;
		BigDecimal exemption = BigDecimal.ZERO;
		BigDecimal rebate = BigDecimal.ZERO;
		BigDecimal ptTax = BigDecimal.ZERO;

		for (TaxHeadEstimate estimate : estimates) {

			Category category = taxHeadCategoryMap.get(estimate.getTaxHeadCode());
			estimate.setCategory(category);

			switch (category) {

			case TAX:
				taxAmt = taxAmt.add(estimate.getEstimateAmount());
				if(estimate.getTaxHeadCode().equalsIgnoreCase(PT_TAX))
					ptTax = ptTax.add(estimate.getEstimateAmount());
				break;

			case PENALTY:
				penalty = penalty.add(estimate.getEstimateAmount());
				break;

			case REBATE:
				rebate = rebate.add(estimate.getEstimateAmount());
				break;

			case EXEMPTION:
				exemption = exemption.add(estimate.getEstimateAmount());
				break;

			default:
				taxAmt = taxAmt.add(estimate.getEstimateAmount());
				break;
			}
		}
		TaxHeadEstimate decimalEstimate = payService.roundOfDecimals(taxAmt.add(penalty), rebate.add(exemption));
        if (null != decimalEstimate) {
			decimalEstimate.setCategory(taxHeadCategoryMap.get(decimalEstimate.getTaxHeadCode()));
            estimates.add(decimalEstimate);
            if (decimalEstimate.getEstimateAmount().compareTo(BigDecimal.ZERO)>=0)
                taxAmt = taxAmt.add(decimalEstimate.getEstimateAmount());
            else
                rebate = rebate.add(decimalEstimate.getEstimateAmount());
        }

		BigDecimal totalAmount = taxAmt.add(penalty).add(rebate).add(exemption);
		// false in the argument represents that the demand shouldn't be updated from this call
		Demand oldDemand = utils.getLatestDemandForCurrentFinancialYear(requestInfo,criteria);
		BigDecimal collectedAmtForOldDemand = demandService.getCarryForwardAndCancelOldDemand(ptTax, criteria, requestInfo,oldDemand, false);

		if(collectedAmtForOldDemand.compareTo(BigDecimal.ZERO) > 0)
			estimates.add(TaxHeadEstimate.builder()
					.taxHeadCode(PT_ADVANCE_CARRYFORWARD)
					.estimateAmount(collectedAmtForOldDemand).build());
		else if(collectedAmtForOldDemand.compareTo(BigDecimal.ZERO) < 0)
			throw new CustomException(EG_PT_DEPRECIATING_ASSESSMENT_ERROR, EG_PT_DEPRECIATING_ASSESSMENT_ERROR_MSG_ESTIMATE);

		return Calculation.builder()
				.totalAmount(totalAmount.subtract(collectedAmtForOldDemand))
				.taxAmount(taxAmt)
				.penalty(penalty)
				.exemption(exemption)
				.rebate(rebate)
				.fromDate(criteria.getFromDate())
				.toDate(criteria.getToDate())
				.tenantId(tenantId)
				.serviceNumber(property.getPropertyId())
				.taxHeadEstimates(estimates)
				.billingSlabIds(billingSlabIds)
				.build();
	}

	/**
	 * method to do a first level filtering on the slabs based on the values present in Property detail
	 */
	private List<BillingSlab> getSlabsFiltered(Assessment detail, Property property, RequestInfo requestInfo) {

		String tenantId = property.getTenantId();
		BillingSlabSearchCriteria slabSearchCriteria = BillingSlabSearchCriteria.builder().tenantId(tenantId).build();
		List<BillingSlab> billingSlabs = billingSlabService.searchBillingSlabs(requestInfo, slabSearchCriteria)
				.getBillingSlab();

		log.debug(" the slabs count : " + billingSlabs.size());
		final String all = configs.getSlabValueAll();

		Double plotSize = null != property.getLandArea() ? property.getLandArea() : detail.getBuildUpArea();

		final String dtlPtType = property.getPropertyType();
		final String dtlOwnerShipCat = property.getOwnershipCategory();
		final String dtlAreaType = property.getAddress().getLocality().getArea();
		final Boolean dtlIsMultiFloored = property.getNoOfFloors() > 1;

		return billingSlabs.stream().filter(slab -> {

			Boolean slabMultiFloored = slab.getIsPropertyMultiFloored();
			String  slabAreaType = slab.getAreaType();
			String  slabPropertyType = slab.getPropertyType();
			String  slabOwnerShipCat = slab.getOwnerShipCategory();
			Double  slabAreaFrom = slab.getFromPlotSize();
			Double  slabAreaTo = slab.getToPlotSize();

			boolean isPropertyMultiFloored = slabMultiFloored.equals(dtlIsMultiFloored);

			boolean isAreaMatching = slabAreaType.equalsIgnoreCase(dtlAreaType) || all.equalsIgnoreCase(slab.getAreaType());

			boolean isPtTypeMatching = slabPropertyType.equalsIgnoreCase(dtlPtType);


			boolean isOwnerShipMatching = slabOwnerShipCat.equalsIgnoreCase(dtlOwnerShipCat)
					|| all.equalsIgnoreCase(slabOwnerShipCat);


			boolean isPlotMatching = false;

			if (plotSize == 0.0)
				isPlotMatching = slabAreaFrom <= plotSize && slabAreaTo >= plotSize;
			else
				isPlotMatching = slabAreaFrom < plotSize && slabAreaTo >= plotSize;

			return isPtTypeMatching && isOwnerShipMatching && isPlotMatching && isAreaMatching && isPropertyMultiFloored;

		}).collect(Collectors.toList());
	}

	/**
	 * Second level filtering to get the matching billing slab for the respective unit
	 * will return only one slab per unit.
	 *
	 * @param billingSlabs slabs filtered with property detail related values
	 * @param unit unit of the property for which the tax has be calculated
	 */
	private BillingSlab getSlabForCalc(List<BillingSlab> billingSlabs, Unit unit) {

		final String all = configs.getSlabValueAll();

		List<BillingSlab> matchingList = new ArrayList<>();

		for (BillingSlab billSlb : billingSlabs) {

			Double floorNo = Double.parseDouble(unit.getFloorNo());

			boolean isMajorMatching = billSlb.getUsageCategory().equalsIgnoreCase(unit.getUsageCategory())
					|| (billSlb.getUsageCategory().equalsIgnoreCase(all));

			boolean isFloorMatching = billSlb.getFromFloor() <= floorNo && billSlb.getToFloor() >= floorNo;

			boolean isOccupancyTypeMatching = billSlb.getOccupancyType().equalsIgnoreCase(unit.getOccupancyType().toString())
					|| (billSlb.getOccupancyType().equalsIgnoreCase(all));

			if (isMajorMatching && isFloorMatching && isOccupancyTypeMatching) {

				matchingList.add(billSlb);
				log.debug(" The Id of the matching slab : " + billSlb.getId());
			}
		}
		if (matchingList.size() == 1)
			return matchingList.get(0);
		else if (matchingList.size() == 0)
			return null;
		else throw new CustomException(PT_ESTIMATE_BILLINGSLABS_UNMATCH, PT_ESTIMATE_BILLINGSLABS_UNMATCH_MSG
					.replace(PT_ESTIMATE_BILLINGSLABS_UNMATCH_replace_id, matchingList.toString()) + unit);
	}

	/**
	 * Usage based exemptions applied on unit.
	 *
	 * The exemption discount will be applied based on the exemption rate of the
	 * usage master types.
	 */
	private BigDecimal getExemption(Unit unit, BigDecimal currentUnitTax, String financialYear,
			Map<String, Map<String, List<Object>>> propertyMasterMap) {

		Map<String, Object> exemption = getExemptionFromUsage(unit, financialYear, propertyMasterMap);
		return mDataService.calculateApplicables(currentUnitTax, exemption);
	}

	/**
	 * Applies discount on Total tax amount OwnerType based on exemptions.
	 */
	private BigDecimal getExemption(List<OwnerInfo> owners, BigDecimal taxAmt, String financialYear,
			Map<String, Map<String, List<Object>>> propertyMasterMap) {

		Map<String, List<Object>> ownerTypeMap = propertyMasterMap.get(OWNER_TYPE_MASTER);
		BigDecimal userExemption = BigDecimal.ZERO;
		final int userCount = owners.size();
		BigDecimal share = taxAmt.divide(BigDecimal.valueOf(userCount),2, 2);

		for (OwnerInfo owner : owners) {

			if (null == ownerTypeMap.get(owner.getOwnerType()))
				continue;

			Map<String, Object> applicableOwnerType = mDataService.getApplicableMaster(financialYear,
					ownerTypeMap.get(owner.getOwnerType()));

			if (null != applicableOwnerType) {

				BigDecimal currentExemption = mDataService.calculateApplicables(share,
						applicableOwnerType.get(EXEMPTION_FIELD_NAME));

				userExemption = userExemption.add(currentExemption);
			}
		}
		return userExemption;
	}

	/**
	 * Returns the appropriate exemption object from the usage masters
	 *
	 * Search happens from child (usageCategoryDetail) to parent
	 * (usageCategoryMajor)
	 *
	 * if any appropriate match is found in getApplicableMasterFromList, then the
	 * exemption object from that master will be returned
	 *
	 * if no match found(for all the four usages) then null will be returned
	 *
	 * @param unit unit for which usage exemption will be applied
	 * @param financialYear year for which calculation is being done
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getExemptionFromUsage(Unit unit, String financialYear,
			Map<String, Map<String, List<Object>>> propertyBasedExemptionMasterMap) {

		Map<String, List<Object>> usageMajors = propertyBasedExemptionMasterMap.get(USAGE_MAJOR_MASTER);

		Map<String, Object> applicableUsageMasterExemption = null;

		if (isExemptionNull(applicableUsageMasterExemption) && null != usageMajors.get(unit.getUsageCategory()))
			applicableUsageMasterExemption = mDataService.getApplicableMaster(financialYear,
					usageMajors.get(unit.getUsageCategory()));

		if (null != applicableUsageMasterExemption)
			applicableUsageMasterExemption = (Map<String, Object>) applicableUsageMasterExemption.get(EXEMPTION_FIELD_NAME);

		return applicableUsageMasterExemption;
	}

	private boolean isExemptionNull(Map<String, Object> applicableUsageMasterExemption) {

		return !(null != applicableUsageMasterExemption
				&& null != applicableUsageMasterExemption.get(EXEMPTION_FIELD_NAME));
	}
}
