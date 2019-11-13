package org.egov.wsCalculation.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.wsCalculation.model.BillingSlab;
import org.egov.wsCalculation.model.CalculationCriteria;
import org.egov.wsCalculation.model.RequestInfoWrapper;
import org.egov.wsCalculation.model.TaxHeadEstimate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class EstimationService {

	
	/**
	 * Generates a List of Tax head estimates with tax head code,
	 * tax head category and the amount to be collected for the key.
     *
     * @param criteria criteria based on which calculation will be done.
     * @param requestInfo request info from incoming request.
	 * @return Map<String, Double>
	 */
	private Map<String,List> getEstimationMap(CalculationCriteria criteria, RequestInfo requestInfo) {

		BigDecimal taxAmt = BigDecimal.ZERO;
		BigDecimal usageExemption = BigDecimal.ZERO;
		WaterConnection waterConnection = criteria.getWaterConnection();
		String assessmentYear = "2019-20";
		String tenantId = requestInfo.getUserInfo().getTenantId();

		List<BillingSlab> filteredBillingSlabs = getSlabsFiltered(property, requestInfo);

		Map<String, Map<String, List<Object>>> propertyBasedExemptionMasterMap = new HashMap<>();
		Map<String, JSONArray> timeBasedExemptionMasterMap = new HashMap<>();
		mDataService.setPropertyMasterValues(requestInfo, tenantId, propertyBasedExemptionMasterMap,
				timeBasedExemptionMasterMap);

		List<String> billingSlabIds = new LinkedList<>();
		HashMap<Unit, BillingSlab> unitSlabMapping = new HashMap<>();
		List<Unit> groundFloorUnits = new LinkedList<>();

		/*
		 * by default land should get only one slab from database per tenantId
		 */
		if (PT_TYPE_VACANT_LAND.equalsIgnoreCase(detail.getPropertyType()) && filteredBillingSlabs.size() != 1)
			throw new CustomException(PT_ESTIMATE_BILLINGSLABS_UNMATCH_VACANCT,PT_ESTIMATE_BILLINGSLABS_UNMATCH_VACANT_MSG
					.replace("{count}",String.valueOf(filteredBillingSlabs.size())));

		else if (PT_TYPE_VACANT_LAND.equalsIgnoreCase(detail.getPropertyType())) {
			taxAmt = taxAmt.add(BigDecimal.valueOf(filteredBillingSlabs.get(0).getUnitRate() * detail.getLandArea()));
		} else {

			double unBuiltRate = 0.0;
			int groundUnitsCount = 0;
			Double groundUnitsArea = 0.0;
			int i = 0;

			for (Unit unit : detail.getUnits()) {

				BillingSlab slab = getSlabForCalc(filteredBillingSlabs, unit);
				BigDecimal currentUnitTax = getTaxForUnit(slab, unit);
				billingSlabIds.add(slab.getId()+"|"+i);
				unitSlabMapping.put(unit, slab);
				/*
				 * counting the number of units & total area in ground floor for unbuilt area
				 * tax calculation
				 */
				if (unit.getFloorNo().equalsIgnoreCase("0")) {
					groundUnitsCount += 1;
					groundUnitsArea += unit.getUnitArea();
					groundFloorUnits.add(unit);
//					if (null != slab.getUnBuiltUnitRate())
//						unBuiltRate += slab.getUnBuiltUnitRate();
				}
				taxAmt = taxAmt.add(currentUnitTax);
				usageExemption = usageExemption
						.add(getExemption(unit, currentUnitTax, assessmentYear, propertyBasedExemptionMasterMap));
				i++;
			}


			HashMap<Unit, BigDecimal> unBuiltRateCalc = getUnBuiltRate(detail, unitSlabMapping, groundFloorUnits, groundUnitsArea);

			/*
			 * making call to get unbuilt area tax estimate
			 */
			taxAmt = taxAmt.add(unBuiltRateCalc.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));

			/*
			 * special case to handle property with one unit
			 */
//			if (detail.getUnits().size() == 1)
//				usageExemption = getExemption(detail.getUnits().get(0), taxAmt, assessmentYear,
//						propertyBasedExemptionMasterMap);

			for (Map.Entry<Unit,BigDecimal> e: unBuiltRateCalc.entrySet()) {
				BigDecimal ue = getExemption(e.getKey(), e.getValue(), assessmentYear, propertyBasedExemptionMasterMap);
				usageExemption = usageExemption.add(ue);
			}

		}
		List<TaxHeadEstimate> taxHeadEstimates =  getEstimatesForTax(assessmentYear, taxAmt, usageExemption, property, propertyBasedExemptionMasterMap,
				timeBasedExemptionMasterMap, RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		Map<String,List> estimatesAndBillingSlabs = new HashMap<>();
		estimatesAndBillingSlabs.put("estimates",taxHeadEstimates);
		estimatesAndBillingSlabs.put("billingSlabIds",new ArrayList<>());
		return estimatesAndBillingSlabs;
	}
	
	/**
	 * Return an Estimate list containing all the required tax heads
	 * mapped with respective amt to be paid.
	 * @param detail proeprty detail object
	 * @param assessmentYear year for which calculation is being done
	 * @param taxAmt tax amount for which rebate & penalty will be applied
	 * @param usageExemption  total exemption value given for all unit usages
	 * @param propertyBasedExemptionMasterMap property masters which contains exemption values associated with them
	 * @param timeBasedExemeptionMasterMap masters with period based exemption values
	 * @param build
	 */
	private List<TaxHeadEstimate> getEstimatesForTax(String assessmentYear, BigDecimal taxAmt, BigDecimal usageExemption, Property property,
													 Map<String, Map<String, List<Object>>> propertyBasedExemptionMasterMap,
													 Map<String, JSONArray> timeBasedExemeptionMasterMap, RequestInfoWrapper requestInfoWrapper) {

		PropertyDetail detail = property.getPropertyDetails().get(0);
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
		BigDecimal userExemption = getExemption(detail.getOwners(), payableTax, assessmentYear,
				propertyBasedExemptionMasterMap).setScale(2, 2).negate();
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(PT_OWNER_EXEMPTION).estimateAmount(userExemption).build());
		payableTax = payableTax.add(userExemption);

		// Fire cess
		List<Object> fireCessMasterList = timeBasedExemeptionMasterMap.get(CalculatorConstants.FIRE_CESS_MASTER);
		BigDecimal fireCess;

		if (usePBFirecessLogic) {
			fireCess = firecessUtils.getPBFireCess(payableTax, assessmentYear, fireCessMasterList, detail);
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


		List<Receipt> receipts = Collections.emptyList();

		if (property.getPropertyId() != null) {
			rcptService.getReceiptsFromPropertyAndFY(assessmentYear, property.getTenantId(), property.getPropertyId(), requestInfoWrapper);
		}

		// get applicable rebate and penalty
		Map<String, BigDecimal> rebatePenaltyMap = payService.applyPenaltyRebateAndInterest(payableTax, BigDecimal.ZERO,
				 assessmentYear, timeBasedExemeptionMasterMap,receipts);

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
		if (null != detail.getAdhocPenalty())
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(PT_ADHOC_PENALTY)
					.estimateAmount(detail.getAdhocPenalty()).build());

		if (null != detail.getAdhocExemption() && detail.getAdhocExemption().compareTo(payableTax.add(fireCess)) <= 0) {
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(PT_ADHOC_REBATE)
					.estimateAmount(detail.getAdhocExemption().negate()).build());
		}
		else if (null != detail.getAdhocExemption()) {
			throw new CustomException(PT_ADHOC_REBATE_INVALID_AMOUNT, PT_ADHOC_REBATE_INVALID_AMOUNT_MSG + taxAmt);
		}
		return estimates;
	}
	/**
	 * method to do a first level filtering on the slabs based on the values present in Property detail
	 */
	private List<BillingSlab> getSlabsFiltered(WaterConnection waterConnection, RequestInfo requestInfo) {
		PropertyDetail detail = property.getPropertyDetails().get(0);
		String tenantId = property.getTenantId();
		BillingSlabSearchCriteria slabSearchCriteria = BillingSlabSearchCriteria.builder().tenantId(tenantId).build();
		List<BillingSlab> billingSlabs = billingSlabService.searchBillingSlabs(requestInfo, slabSearchCriteria)
				.getBillingSlab();

		log.debug(" the slabs count : " + billingSlabs.size());
		final String all = configs.getSlabValueAll();

		Double plotSize = null != detail.getLandArea() ? detail.getLandArea() : detail.getBuildUpArea();

		final String dtlPtType = detail.getPropertyType();
		final String dtlPtSubType = detail.getPropertySubType();
		final String dtlOwnerShipCat = detail.getOwnershipCategory();
		final String dtlSubOwnerShipCat = detail.getSubOwnershipCategory();
		final String dtlAreaType = property.getAddress().getLocality().getArea();
		final Boolean dtlIsMultiFloored = detail.getNoOfFloors() > 1;

		return billingSlabs.stream().filter(slab -> {

			Boolean slabMultiFloored = slab.getIsPropertyMultiFloored();
			String slabAreaType = slab.getAreaType();
			String slabPropertyType = slab.getPropertyType();
			String slabPropertySubType = slab.getPropertySubType();
			String slabOwnerShipCat = slab.getOwnerShipCategory();
			String slabSubOwnerShipCat = slab.getSubOwnerShipCategory();
			Double slabAreaFrom = slab.getFromPlotSize();
			Double slabAreaTo = slab.getToPlotSize();

			boolean isPropertyMultiFloored = slabMultiFloored.equals(dtlIsMultiFloored);

			boolean isAreaMatching = slabAreaType.equalsIgnoreCase(dtlAreaType)
					|| all.equalsIgnoreCase(slab.getAreaType());

			boolean isPtTypeMatching = slabPropertyType.equalsIgnoreCase(dtlPtType);

			boolean isPtSubTypeMatching = slabPropertySubType.equalsIgnoreCase(dtlPtSubType)
					|| all.equalsIgnoreCase(slabPropertySubType);

			boolean isOwnerShipMatching = slabOwnerShipCat.equalsIgnoreCase(dtlOwnerShipCat)
					|| all.equalsIgnoreCase(slabOwnerShipCat);

			boolean isSubOwnerShipMatching = slabSubOwnerShipCat.equalsIgnoreCase(dtlSubOwnerShipCat)
					|| all.equalsIgnoreCase(slabSubOwnerShipCat);

			boolean isPlotMatching = false;

			if (plotSize == 0.0)
				isPlotMatching = slabAreaFrom <= plotSize && slabAreaTo >= plotSize;
			else
				isPlotMatching = slabAreaFrom < plotSize && slabAreaTo >= plotSize;

			return isPtTypeMatching && isPtSubTypeMatching && isOwnerShipMatching && isSubOwnerShipMatching
					&& isPlotMatching && isAreaMatching && isPropertyMultiFloored;

		}).collect(Collectors.toList());
	}
}
