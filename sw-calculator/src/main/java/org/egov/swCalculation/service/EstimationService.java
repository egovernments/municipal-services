package org.egov.swCalculation.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swCalculation.constants.SWCalculationConstant;
import org.egov.swCalculation.model.BillingSlab;
import org.egov.swCalculation.model.CalculationCriteria;
import org.egov.swCalculation.model.Property;
import org.egov.swCalculation.model.RequestInfoWrapper;
import org.egov.swCalculation.model.SearchCriteria;
import org.egov.swCalculation.model.SewerageConnection;
import org.egov.swCalculation.model.Slab;
import org.egov.swCalculation.model.TaxHeadEstimate;
import org.egov.swCalculation.util.CalculatorUtils;
import org.egov.swCalculation.util.SewerageCessUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Service
@Slf4j
public class EstimationService {

	@Autowired
	private CalculatorUtils calculatorUtil;

	@Autowired
	private SewerageCessUtil sewerageCessUtil;

	@Autowired
	private ObjectMapper mapper;

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
	public Map<String, List> getEstimationMap(CalculationCriteria criteria, RequestInfo requestInfo,
			Map<String, Object> masterData) {
		SewerageConnection sewerageConnection = null;
		BigDecimal taxAmt = BigDecimal.ZERO;
		String tenantId = criteria.getTenantId();
		if (criteria.getSewerageConnection() == null && !criteria.getConnectionNo().isEmpty()) {
			sewerageConnection = calculatorUtil.getSewerageConnection(requestInfo, criteria.getConnectionNo(),
					tenantId);
			criteria.setSewerageConnection(sewerageConnection);
		}
		if (criteria.getSewerageConnection() == null) {
			throw new CustomException("Sewerage Connection not found for given criteria ",
					"Sewerage Connection are not present for " + criteria.getConnectionNo() + " connection no");
		}
		Map<String, JSONArray> billingSlabMaster = new HashMap<>();
		Map<String, JSONArray> timeBasedExemptionMasterMap = new HashMap<>();
		ArrayList<String> billingSlabIds = new ArrayList<>();

		billingSlabMaster.put(SWCalculationConstant.SW_BILLING_SLAB_MASTER,
				(JSONArray) masterData.get(SWCalculationConstant.SW_BILLING_SLAB_MASTER));
		timeBasedExemptionMasterMap.put(SWCalculationConstant.SW_SEWERAGE_CESS_MASTER,
				(JSONArray) (masterData.getOrDefault(SWCalculationConstant.SW_SEWERAGE_CESS_MASTER, null)));
		// mDataService.setSewerageConnectionMasterValues(requestInfo, tenantId,
		// billingSlabMaster,
		// timeBasedExemptionMasterMap);
		BigDecimal sewarageCharge = getSewerageEstimationCharge(sewerageConnection, criteria, billingSlabMaster,
				billingSlabIds, requestInfo);
		taxAmt = sewarageCharge;
		List<TaxHeadEstimate> taxHeadEstimates = getEstimatesForTax(taxAmt, criteria.getSewerageConnection(),
				timeBasedExemptionMasterMap, RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		Map<String, List> estimatesAndBillingSlabs = new HashMap<>();
		estimatesAndBillingSlabs.put("estimates", taxHeadEstimates);
		estimatesAndBillingSlabs.put("billingSlabIds", billingSlabIds);
		return estimatesAndBillingSlabs;
	}

	private List<TaxHeadEstimate> getEstimatesForTax(BigDecimal sewarageCharge, SewerageConnection connection,
			Map<String, JSONArray> timeBasedExemeptionMasterMap, RequestInfoWrapper requestInfoWrapper) {

		List<TaxHeadEstimate> estimates = new ArrayList<>();
		String assesmentYear = SWCalculationConstant.Assesment_Year;
		// sewerage_charge
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_CHARGE)
				.estimateAmount(sewarageCharge.setScale(2, 2)).build());

		// sewerage cess
		if (timeBasedExemeptionMasterMap.get(SWCalculationConstant.SW_SEWERAGE_CESS_MASTER) != null) {
			List<Object> sewerageCessMasterList = timeBasedExemeptionMasterMap
					.get(SWCalculationConstant.SW_SEWERAGE_CESS_MASTER);
			BigDecimal sewerageCess = BigDecimal.ZERO;
			sewerageCess = sewerageCessUtil.getSewerageCess(sewarageCharge, assesmentYear, sewerageCessMasterList);
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_WATER_CESS)
					.estimateAmount(sewerageCess).build());
		}

		// get applicable rebate and penalty
		// Map<String, BigDecimal> rebatePenaltyMap =
		// payService.applyPenaltyRebateAndInterest(payableTax, BigDecimal.ZERO,
		// assessmentYear, timeBasedExemeptionMasterMap);
		// if (null != rebatePenaltyMap) {
		// BigDecimal rebate =
		// rebatePenaltyMap.get(WSCalculationConstant.WS_TIME_REBATE);
		// BigDecimal penalty =
		// rebatePenaltyMap.get(WSCalculationConstant.WS_TIME_PENALTY);
		// BigDecimal interest =
		// rebatePenaltyMap.get(WSCalculationConstant.WS_TIME_INTEREST);
		// estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_TIME_REBATE)
		// .estimateAmount(rebate).build());
		// estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_TIME_PENALTY)
		// .estimateAmount(penalty).build());
		// estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_TIME_INTEREST)
		// .estimateAmount(interest).build());
		// payableTax = payableTax.add(rebate).add(penalty).add(interest);
		// }
		return estimates;
	}

	/**
	 * method to do a first level filtering on the slabs based on the values
	 * present in the Sewerage Details
	 */

	public BigDecimal getSewerageEstimationCharge(SewerageConnection sewerageConnection, CalculationCriteria criteria,
			Map<String, JSONArray> billingSlabMaster, ArrayList<String> billingSlabIds, RequestInfo requestInfo) {
		BigDecimal sewerageCharge = BigDecimal.ZERO;
		if (billingSlabMaster.get(SWCalculationConstant.SW_BILLING_SLAB_MASTER) == null)
			throw new CustomException("No Billing Slab are found on criteria ", "Billing Slab are Emplty");
		ObjectMapper mapper = new ObjectMapper();
		List<BillingSlab> mappingBillingSlab;
		try {
			mappingBillingSlab = mapper.readValue(
					billingSlabMaster.get(SWCalculationConstant.SW_BILLING_SLAB_MASTER).toJSONString(),
					mapper.getTypeFactory().constructCollectionType(List.class, BillingSlab.class));
		} catch (IOException e) {
			throw new CustomException("Parsing Exception", " Billing Slab can not be parsed!");
		}

		List<BillingSlab> billingSlabs = getSlabsFiltered(sewerageConnection, mappingBillingSlab, requestInfo);
		if (billingSlabs == null || billingSlabs.isEmpty())
			throw new CustomException("No Billing Slab are found on criteria ", "Billing Slab are Empty");
		if (billingSlabs.size() > 1)
			throw new CustomException("More than one Billing Slab are found on criteria ",
					"More than one billing slab found");
		// Add Billing Slab Ids
		billingSlabIds.add(billingSlabs.get(0).id);

		// Sewerage Charge Calculation
		Double totalUnite = 0.0;
		totalUnite = getCalculationUnit(sewerageConnection, criteria);
		if (totalUnite == 0.0)
			return sewerageCharge;
		if (isRangeCalculation(sewerageConnection.getCalculationAttribute())) {
			for (BillingSlab billingSlab : billingSlabs) {
				for (Slab slab : billingSlab.slabs) {
					if (totalUnite >= slab.from && totalUnite < slab.to) {
						sewerageCharge = BigDecimal.valueOf((totalUnite * slab.charge));
						if (slab.minimumCharge > sewerageCharge.doubleValue()) {
							sewerageCharge = BigDecimal.valueOf(slab.minimumCharge);
						}
						break;
					}
				}
			}
		} else {
			for (BillingSlab billingSlab : billingSlabs) {
				sewerageCharge = BigDecimal.valueOf(billingSlab.slabs.get(0).charge);
				break;
			}
		}
		return sewerageCharge;
	}

	public String getAssessmentYear() {
		LocalDateTime localDateTime = LocalDateTime.now();
		int currentMonth = localDateTime.getMonthValue();
		String assesmentYear = "";
		if (currentMonth >= 4) {
			assesmentYear = Integer.toString(YearMonth.now().getYear()) + "-";
			assesmentYear = assesmentYear
					+ (Integer.toString(YearMonth.now().getYear() + 1).substring(2, assesmentYear.length() - 1));
		} else {
			assesmentYear = Integer.toString(YearMonth.now().getYear() - 1) + "-";
			assesmentYear = assesmentYear
					+ (Integer.toString(YearMonth.now().getYear()).substring(2, assesmentYear.length() - 1));

		}
		return assesmentYear;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param sewarageCharge
	 * @param sewerageConnection
	 * @param timeBasedExemeptionMasterMap
	 * @param requestInfoWrapper
	 * @return
	 */
	@SuppressWarnings("unused")
	private List<TaxHeadEstimate> getEstimatesForTax(String assessmentYear, BigDecimal sewarageCharge,
			SewerageConnection sewerageConnection, Map<String, JSONArray> timeBasedExemeptionMasterMap,
			RequestInfoWrapper requestInfoWrapper) {
		List<TaxHeadEstimate> estimates = new ArrayList<>();
		// sewerage_charge
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_CHARGE)
				.estimateAmount(sewarageCharge.setScale(2, 2)).build());
		return estimates;
	}

	/**
	 * 
	 * @param waterConnection
	 * @param billingSlabs
	 * @param requestInfo
	 * @return List of billing slab based on matching criteria
	 */
	private List<BillingSlab> getSlabsFiltered(SewerageConnection sewerageConnection, List<BillingSlab> billingSlabs,
			RequestInfo requestInfo) {
		Property property = sewerageConnection.getProperty();
		// get billing Slab
		log.debug(" the slabs count : " + billingSlabs.size());
		final String buildingType = property.getUsageCategory();
		final String connectionType = sewerageConnection.getConnectionType();
		final String calculationAttribute = sewerageConnection.getCalculationAttribute();
		final String unitOfMeasurement = sewerageConnection.getUom();

		return billingSlabs.stream().filter(slab -> {
			boolean isBuildingTypeMatching = slab.BuildingType.equalsIgnoreCase(buildingType);
			boolean isConnectionTypeMatching = slab.ConnectionType.equalsIgnoreCase(connectionType);
			boolean isCalculationAttributeMatching = slab.CalculationAttribute.equalsIgnoreCase(calculationAttribute);
			boolean isUnitOfMeasurementMatcing = slab.UOM.equalsIgnoreCase(unitOfMeasurement);
			return isBuildingTypeMatching && isConnectionTypeMatching && isCalculationAttributeMatching
					&& isUnitOfMeasurementMatcing;
		}).collect(Collectors.toList());
	}

	private Double getCalculationUnit(SewerageConnection sewerageConnection, CalculationCriteria criteria) {
		Double totalUnite = 0.0;
		if (sewerageConnection.getConnectionType().equals(SWCalculationConstant.meteredConnectionType)) {
			return totalUnite;
		} else if (sewerageConnection.getConnectionType().equals(SWCalculationConstant.nonMeterdConnection)
				&& sewerageConnection.getCalculationAttribute().equalsIgnoreCase(SWCalculationConstant.noOfToilets)) {
			return totalUnite = new Double(sewerageConnection.getNoOfToilets());
		} else if (sewerageConnection.getConnectionType().equals(SWCalculationConstant.nonMeterdConnection)
				&& sewerageConnection.getCalculationAttribute()
						.equalsIgnoreCase(SWCalculationConstant.noOfWaterClosets)) {
			return totalUnite = new Double(sewerageConnection.getNoOfWaterClosets());
		}
		return totalUnite;
	}

	/**
	 * 
	 * @param type
	 *            will be calculation Attribute
	 * @return true if calculation Attribute is not Flat else false
	 */
	private boolean isRangeCalculation(String type) {
		if (type.equalsIgnoreCase(SWCalculationConstant.flatRateCalculationAttribute))
			return false;
		return true;
	}

	public Map<String, List> getFeeEstimation(CalculationCriteria criteria, RequestInfo requestInfo,
			Map<String, Object> masterData) {
		SewerageConnection sewerageConnection = null;
		String tenantId = requestInfo.getUserInfo().getTenantId();
		if (criteria.getSewerageConnection() == null && !criteria.getApplicationNo().isEmpty()) {
			SearchCriteria searchCriteria = new SearchCriteria();
			searchCriteria.setApplicationNumber(criteria.getApplicationNo());
			searchCriteria.setTenantId(criteria.getTenantId());
			sewerageConnection = calculatorUtil.getSewerageConnectionOnApplicationNO(requestInfo, searchCriteria,
					tenantId);
			criteria.setSewerageConnection(sewerageConnection);
		}
		if (criteria.getSewerageConnection() == null) {
			throw new CustomException("SEWERAGE_CONNECTION_NOT_FOUND",
					"Sewerage Connection are not present for " + criteria.getApplicationNo() + " Application no");
		}
		ArrayList<String> billingSlabIds = new ArrayList<>();
		billingSlabIds.add("");
		List<TaxHeadEstimate> taxHeadEstimates = getTaxHeadForFeeEstimation(criteria, masterData);
		Map<String, List> estimatesAndBillingSlabs = new HashMap<>();
		estimatesAndBillingSlabs.put("estimates", taxHeadEstimates);
		// //Billing slab id
		estimatesAndBillingSlabs.put("billingSlabIds", billingSlabIds);
		return estimatesAndBillingSlabs;
	}

	/**
	 * 
	 * @param criteria
	 * @param masterData
	 * @return return all tax heads
	 */
	private List<TaxHeadEstimate> getTaxHeadForFeeEstimation(CalculationCriteria criteria,
			Map<String, Object> masterData) {
		List<TaxHeadEstimate> estimates = new ArrayList<>();
		BigDecimal formFee = BigDecimal.ZERO;
		BigDecimal scrutinyFee = BigDecimal.ZERO;
		BigDecimal meterCost = BigDecimal.ZERO;
		BigDecimal otherCharges = BigDecimal.ZERO;
		BigDecimal taxAndCessPercentage = BigDecimal.ZERO;
		BigDecimal roadCuttingCharge = BigDecimal.ZERO;
		BigDecimal roadPlotCharge = BigDecimal.ZERO;
		BigDecimal usageTypeCharge = BigDecimal.ZERO;
		BigDecimal tax = BigDecimal.ZERO;
		BigDecimal totalCharge = BigDecimal.ZERO;

		JSONArray feeSlab = (JSONArray) masterData.getOrDefault(SWCalculationConstant.SC_FEESLAB_MASTER, null);
		if (feeSlab == null)
			throw new CustomException("FEE_SLAB_NOT_", "fee salb master data not found!!");
		JSONObject feeObj = mapper.convertValue(feeSlab.get(0), JSONObject.class);
		if (feeObj.get(SWCalculationConstant.FORM_FEE_CONST) != null) {
			formFee = new BigDecimal(feeObj.getAsNumber(SWCalculationConstant.FORM_FEE_CONST).toString());
		}
		if (feeObj.get(SWCalculationConstant.SCRUTINY_FEE_CONST) != null) {
			scrutinyFee = new BigDecimal(feeObj.getAsNumber(SWCalculationConstant.SCRUTINY_FEE_CONST).toString());
		}
		if (feeObj.get(SWCalculationConstant.OTHER_CHARGE_CONST) != null) {
			otherCharges = new BigDecimal(feeObj.getAsNumber(SWCalculationConstant.OTHER_CHARGE_CONST).toString());
		}
		if (feeObj.get(SWCalculationConstant.TAX_PERCENTAGE_CONST) != null) {
			taxAndCessPercentage = new BigDecimal(
					feeObj.getAsNumber(SWCalculationConstant.TAX_PERCENTAGE_CONST).toString());
		}
		if (feeObj.get(SWCalculationConstant.METER_COST_CONST) != null && criteria.getSewerageConnection()
				.getConnectionType().equalsIgnoreCase(SWCalculationConstant.meteredConnectionType)) {
			meterCost = new BigDecimal(feeObj.getAsNumber(SWCalculationConstant.METER_COST_CONST).toString());
		}
		roadCuttingCharge = getChargeForRoadCutting(masterData, criteria.getSewerageConnection().getRoadType(),
				criteria.getSewerageConnection().getRoadCuttingArea());
		roadPlotCharge = getPlotSizeFee(masterData, criteria.getSewerageConnection().getProperty().getLandArea());
		usageTypeCharge = getUsageTypeFee(masterData, criteria.getSewerageConnection().getProperty().getUsageCategory(),
				criteria.getSewerageConnection().getRoadCuttingArea());
		totalCharge = formFee.add(scrutinyFee).add(otherCharges).add(meterCost).add(roadCuttingCharge)
				.add(roadPlotCharge).add(usageTypeCharge);
		tax = totalCharge.multiply(taxAndCessPercentage.divide(SWCalculationConstant.HUNDRED));
		//
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_FORM_FEE)
				.estimateAmount(formFee.setScale(2, 2)).build());
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_SCRUTINY_FEE)
				.estimateAmount(scrutinyFee.setScale(2, 2)).build());
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_METER_CHARGE)
				.estimateAmount(meterCost.setScale(2, 2)).build());
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_OTHER_CHARGE)
				.estimateAmount(otherCharges.setScale(2, 2)).build());
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_ROAD_CUTTING_CHARGE)
				.estimateAmount(roadCuttingCharge.setScale(2, 2)).build());
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_ONE_TIME_FEE)
				.estimateAmount(usageTypeCharge.setScale(2, 2)).build());
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_SECURITY_CHARGE)
				.estimateAmount(roadPlotCharge.setScale(2, 2)).build());
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_TAX_AND_CESS)
				.estimateAmount(tax.setScale(2, 2)).build());
		return estimates;
	}

	/**
	 * 
	 * @param masterData
	 * @param roadType
	 * @param roadCuttingArea
	 * @return road cutting charge
	 */
	private BigDecimal getChargeForRoadCutting(Map<String, Object> masterData, String roadType, Float roadCuttingArea) {
		JSONArray roadSlab = (JSONArray) masterData.getOrDefault(SWCalculationConstant.SC_ROADTYPE_MASTER, null);
		BigDecimal charge = BigDecimal.ZERO;
		BigDecimal cuttingArea = new BigDecimal(roadCuttingArea.toString());
		JSONObject masterSlab = new JSONObject();
		if (roadSlab != null) {
			masterSlab.put("RoadType", roadSlab);
			JSONArray filteredMasters = JsonPath.read(masterSlab, "$.RoadType[?(@.code=='" + roadType + "')]");
			JSONObject master = mapper.convertValue(filteredMasters.get(0), JSONObject.class);
			charge = new BigDecimal(master.getAsNumber(SWCalculationConstant.UNIT_COST_CONST).toString());
			charge = charge.multiply(cuttingArea);
		}
		return charge;
	}

	/**
	 * 
	 * @param masterData
	 * @param plotSize
	 * @return get fee based on plot size
	 */
	private BigDecimal getPlotSizeFee(Map<String, Object> masterData, Double plotSize) {
		BigDecimal charge = BigDecimal.ZERO;
		JSONArray plotSlab = (JSONArray) masterData.getOrDefault(SWCalculationConstant.SC_PLOTSLAB_MASTER, null);
		JSONObject masterSlab = new JSONObject();
		if (plotSlab != null) {
			masterSlab.put("PlotSizeSlab", plotSlab);
			JSONArray filteredMasters = JsonPath.read(masterSlab,
					"$.PlotSizeSlab[?(@.from <=" + plotSize + "&& @.to > " + plotSize + ")]");
			JSONObject master = mapper.convertValue(filteredMasters.get(0), JSONObject.class);
			charge = new BigDecimal(master.getAsNumber(SWCalculationConstant.UNIT_COST_CONST).toString());
		}
		return charge;
	}

	/**
	 * 
	 * @param masterData
	 * @param usageType
	 * @param roadCuttingArea
	 * @return
	 */
	private BigDecimal getUsageTypeFee(Map<String, Object> masterData, String usageType, Float roadCuttingArea) {
		BigDecimal charge = BigDecimal.ZERO;
		JSONArray usageSlab = (JSONArray) masterData.getOrDefault(SWCalculationConstant.SC_PROPERTYUSAGETYPE_MASTER,
				null);
		JSONObject masterSlab = new JSONObject();
		BigDecimal cuttingArea = new BigDecimal(roadCuttingArea.toString());
		if (usageSlab != null) {
			masterSlab.put("PropertyUsageType", usageSlab);
			JSONArray filteredMasters = JsonPath.read(masterSlab,
					"$.PropertyUsageType[?(@.code=='" + usageType + "')]");
			JSONObject master = mapper.convertValue(filteredMasters.get(0), JSONObject.class);
			charge = new BigDecimal(master.getAsNumber(SWCalculationConstant.UNIT_COST_CONST).toString());
			charge = charge.multiply(cuttingArea);
		}
		return charge;
	}
}
