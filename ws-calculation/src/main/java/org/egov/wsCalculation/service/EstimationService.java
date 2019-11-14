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
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.wsCalculation.model.BillingSlab;
import org.egov.wsCalculation.model.CalculationCriteria;
import org.egov.wsCalculation.model.RequestInfoWrapper;
import org.egov.wsCalculation.model.TaxHeadEstimate;
import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class EstimationService {

	@Autowired
	MasterDataService mDataService;
	
	/**
	 * Generates a List of Tax head estimates with tax head code,
	 * tax head category and the amount to be collected for the key.
     *
     * @param criteria criteria based on which calculation will be done.
     * @param requestInfo request info from incoming request.
	 * @return Map<String, Double>
	 */
	private Map<String, List> getEstimationMap(CalculationCriteria criteria, RequestInfo requestInfo) {
		BigDecimal taxAmt = BigDecimal.ZERO;
		WaterConnection waterConnection = criteria.getWaterConnection();
		String assessmentYear = "2019-20";
		String tenantId = requestInfo.getUserInfo().getTenantId();

		BigDecimal waterCharge = getWaterEstimationCharge(waterConnection, requestInfo);

		Map<String, Map<String, List<Object>>> waterBasedExemptionMasterMap = new HashMap<>();
		Map<String, JSONArray> timeBasedExemptionMasterMap = new HashMap<>();
		mDataService.setWaterConnectionMasterValues(requestInfo, tenantId, waterBasedExemptionMasterMap,
				timeBasedExemptionMasterMap);
		List<TaxHeadEstimate> taxHeadEstimates = getEstimatesForTax(assessmentYear, taxAmt, criteria.getWaterConnection(),
				waterBasedExemptionMasterMap, timeBasedExemptionMasterMap,
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		Map<String, List> estimatesAndBillingSlabs = new HashMap<>();
		estimatesAndBillingSlabs.put("estimates", taxHeadEstimates);
		estimatesAndBillingSlabs.put("billingSlabIds", new ArrayList<>());
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
	private List<TaxHeadEstimate> getEstimatesForTax(String assessmentYear, BigDecimal taxAmt, WaterConnection connection,
													 Map<String, Map<String, List<Object>>> waterBasedExemptionMasterMap,
													 Map<String, JSONArray> timeBasedExemeptionMasterMap, RequestInfoWrapper requestInfoWrapper) {
		List<TaxHeadEstimate> estimates = new ArrayList<>();
		PropertyDetail detail = property.getPropertyDetails().get(0);
		BigDecimal payableTax = taxAmt;

		// taxes
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConfiguration.Water_Charge).estimateAmount(taxAmt.setScale(2, 2)).build());
		
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
		return estimates;
	}
	
	/**
	 * method to do a first level filtering on the slabs based on the values present in the Water Details
	 */
	
	public BigDecimal getWaterEstimationCharge(WaterConnection waterConnection, RequestInfo requestInfo) {
		BigDecimal waterCharege = BigDecimal.ZERO;
		Double waterChargeToCompare = 45.0;
		List<Double> waterCharges = new ArrayList<>();
		List<BillingSlab> billingSlabs = getSlabsFiltered(waterConnection, requestInfo);
		if (billingSlabs == null || billingSlabs.isEmpty())
			throw new CustomException("No Billing Slab are found on criteria ", "Billing Slab are Emplty");
		if (billingSlabs.size() > 1)
			throw new CustomException("MOre than one Billing Slab are found on criteria ",
					"More than one billing slab found");
		log.info(billingSlabs.get(0).toString());
		if (isRangeCalculation("connectionAttribute")) {
			billingSlabs.forEach(billingSlab -> {
				billingSlab.slabs.forEach(range -> {
					if (waterChargeToCompare > range.from && waterChargeToCompare < range.to) {
						waterCharges.add((waterChargeToCompare * range.charge) + range.minimumCharge);
					}
				});
			});
		} else {
			billingSlabs.forEach(billingSlab -> {
				billingSlab.slabs.forEach(range -> {
					waterCharges.add(range.charge + range.minimumCharge);
				});
			});
		}
		if (!waterCharges.isEmpty())
			waterCharege = BigDecimal.valueOf(waterCharges.get(0));
		return waterCharege;
	}
	private List<BillingSlab> getSlabsFiltered(WaterConnection waterConnection, RequestInfo requestInfo) {
		Property property = waterConnection.getProperty();
		String tenantId = property.getTenantId();
		// get billing Slab
		List<BillingSlab> billingSlabs = new ArrayList<>();

		log.debug(" the slabs count : " + billingSlabs.size());
		final String propertyType = property.getPropertyType();
		final String connectionType = waterConnection.getConnectionType();
		final String calculationAttribute = "Water consumption";
		final String unitOfMeasurement = "kL";

		return billingSlabs.stream().filter(slab -> {
			boolean isPropertyTypeMatching = slab.BuildingType.equals(propertyType);
			boolean isConnectionTypeMatching = slab.ConnectionType.equals(connectionType);
			boolean isCalculationAttributeMatching = slab.CalculationAttribute.equals(calculationAttribute);
			boolean isUnitOfMeasurementMatcing = slab.UOM.equals(unitOfMeasurement);
			return isPropertyTypeMatching && isConnectionTypeMatching && isCalculationAttributeMatching
					&& isUnitOfMeasurementMatcing;
		}).collect(Collectors.toList());
	}

	private boolean isRangeCalculation(String type) {
		if (type.equalsIgnoreCase("Flat"))
			return false;
		return true;
	}
}
