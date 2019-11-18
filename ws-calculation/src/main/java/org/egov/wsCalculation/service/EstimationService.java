package org.egov.wsCalculation.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.util.WCConstants;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.BillingSlab;
import org.egov.wsCalculation.model.Calculation;
import org.egov.wsCalculation.model.CalculationCriteria;
import org.egov.wsCalculation.model.CalculationReq;
import org.egov.wsCalculation.model.RequestInfoWrapper;
import org.egov.wsCalculation.model.TaxHeadEstimate;
import org.egov.wsCalculation.util.WaterCessUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class EstimationService {

	@Autowired
	MasterDataService mDataService;
	
	@Autowired
	WaterCessUtil waterCessUtil;
	
	@Autowired
	PayService payService;
	
	WSCalculationService wSCalculationService;
	
	
	/**
	 * Generates a map with assessment-number of property as key and estimation
	 * map(taxhead code as key, amount to be paid as value) as value will be
	 * called by calculate api
	 *
	 * @param request
	 *            incoming calculation request containing the criteria.
	 * @return Map<String, Calculation> key of assessment number and value of
	 *         calculation object.
	 */
	public Map<String, Calculation> getEstimationWaterMap(CalculationReq request) {

		RequestInfo requestInfo = request.getRequestInfo();
		List<CalculationCriteria> criteriaList = request.getCalculationCriteria();
		Map<String, Object> masterMap = mDataService.getMasterMap(request);
		Map<String, Calculation> calculationWaterMap = new HashMap<>();
		for (CalculationCriteria criteria : criteriaList) {
			WaterConnection waterConnection = criteria.getWaterConnection();

			String assessmentNumber = waterConnection.getConnectionNo();
			Calculation calculation = wSCalculationService.getCalculation(requestInfo, criteria,
					getEstimationMap(criteria, requestInfo), masterMap);
			calculation.setServiceNumber(assessmentNumber);
			calculationWaterMap.put(assessmentNumber, calculation);
		}
		return calculationWaterMap;
	}

	/**
	 * Generates a List of Tax head estimates with tax head code, tax head
	 * category and the amount to be collected for the key.
	 *
	 * @param criteria
	 *            criteria based on which calculation will be done.
	 * @param requestInfo
	 *            request info from incoming request.
	 * @return Map<String, Double>
	 */
	public Map<String, List> getEstimationMap(CalculationCriteria criteria, RequestInfo requestInfo) {
		BigDecimal taxAmt = BigDecimal.ZERO;
		WaterConnection waterConnection = criteria.getWaterConnection();
		String assessmentYear = "2019-20";
		String tenantId = requestInfo.getUserInfo().getTenantId();

		Map<String, JSONArray> billingSlabMaster = new HashMap<>();
		Map<String, JSONArray> timeBasedExemptionMasterMap = new HashMap<>();
		mDataService.setWaterConnectionMasterValues(requestInfo, tenantId, billingSlabMaster,
				timeBasedExemptionMasterMap);
		BigDecimal waterCharge = getWaterEstimationCharge(waterConnection,billingSlabMaster, requestInfo);
		taxAmt = waterCharge;
		List<TaxHeadEstimate> taxHeadEstimates = getEstimatesForTax(assessmentYear, taxAmt,
				criteria.getWaterConnection(), billingSlabMaster, timeBasedExemptionMasterMap,
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		Map<String, List> estimatesAndBillingSlabs = new HashMap<>();
		estimatesAndBillingSlabs.put("estimates", taxHeadEstimates);
		estimatesAndBillingSlabs.put("billingSlabIds", new ArrayList<>());
		return estimatesAndBillingSlabs;
	}

	/**
	 * Return an Estimate list containing all the required tax heads mapped with
	 * respective amt to be paid.
	 * 
	 * @param detail
	 *            proeprty detail object
	 * @param assessmentYear
	 *            year for which calculation is being done
	 * @param taxAmt
	 *            tax amount for which rebate & penalty will be applied
	 * @param usageExemption
	 *            total exemption value given for all unit usages
	 * @param propertyBasedExemptionMasterMap
	 *            property masters which contains exemption values associated
	 *            with them
	 * @param timeBasedExemeptionMasterMap
	 *            masters with period based exemption values
	 * @param build
	 */
	private List<TaxHeadEstimate> getEstimatesForTax(String assessmentYear, BigDecimal taxAmt,
			WaterConnection connection, Map<String, JSONArray> waterBasedExemptionMasterMap,
			Map<String, JSONArray> timeBasedExemeptionMasterMap, RequestInfoWrapper requestInfoWrapper) {
		List<TaxHeadEstimate> estimates = new ArrayList<>();
		BigDecimal payableTax = taxAmt;
		String assesmentYear = WSCalculationConstant.Assesment_Year;
		// water_charge
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_CHARGE)
				.estimateAmount(taxAmt.setScale(2, 2)).build());

		// Water_cess
		List<Object> waterCessMasterList = timeBasedExemeptionMasterMap
				.get(WSCalculationConstant.WC_WATER_CESS_MASTER);
		BigDecimal waterCess;
		waterCess = waterCessUtil.getWaterCess(payableTax, assesmentYear, waterCessMasterList, connection);
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_WATER_CESS)
				.estimateAmount(waterCess).build());
		// get applicable rebate and penalty
		Map<String, BigDecimal> rebatePenaltyMap = payService.applyPenaltyRebateAndInterest(payableTax, BigDecimal.ZERO,
				assessmentYear, timeBasedExemeptionMasterMap);
		if (null != rebatePenaltyMap) {
			BigDecimal rebate = rebatePenaltyMap.get(WSCalculationConstant.WS_TIME_REBATE);
			BigDecimal penalty = rebatePenaltyMap.get(WSCalculationConstant.WS_TIME_PENALTY);
			BigDecimal interest = rebatePenaltyMap.get(WSCalculationConstant.WS_TIME_INTEREST);
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_TIME_REBATE)
					.estimateAmount(rebate).build());
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_TIME_PENALTY)
					.estimateAmount(penalty).build());
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_TIME_INTEREST)
					.estimateAmount(interest).build());
			payableTax = payableTax.add(rebate).add(penalty).add(interest);
		}
		return estimates;
	}

	/**
	 * method to do a first level filtering on the slabs based on the values
	 * present in the Water Details
	 */

	public BigDecimal getWaterEstimationCharge(WaterConnection waterConnection,
			Map<String, JSONArray> billingSlabMaster, RequestInfo requestInfo) {
		BigDecimal waterCharege = BigDecimal.ZERO;
		Double waterChargeToCompare = 45.0;
		if (billingSlabMaster.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER) == null)
			throw new CustomException("No Billing Slab are found on criteria ", "Billing Slab are Emplty");
		ObjectMapper mapper = new ObjectMapper();
		List<BillingSlab> mappingBillingSlab;
		try {
			String str = billingSlabMaster.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER).toJSONString();
			log.info(str);
			mappingBillingSlab = mapper.readValue(
					str,
					mapper.getTypeFactory().constructCollectionType(List.class, BillingSlab.class));
		} catch (IOException e) {
			throw new CustomException("Parsing Exception", " Billing Slab can not be parsed!");
		}
		List<Double> waterCharges = new ArrayList<>();
		List<BillingSlab> billingSlabs = getSlabsFiltered(waterConnection, mappingBillingSlab, requestInfo);
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

	private List<BillingSlab> getSlabsFiltered(WaterConnection waterConnection, List<BillingSlab> billingSlabs, RequestInfo requestInfo) {
		Property property = waterConnection.getProperty();
		String tenantId = property.getTenantId();
		// get billing Slab

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
