package org.egov.wsCalculation.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.BillingSlab;
import org.egov.wsCalculation.model.CalculationCriteria;
import org.egov.wsCalculation.model.Property;
import org.egov.wsCalculation.model.RequestInfoWrapper;
import org.egov.wsCalculation.model.SearchCriteria;
import org.egov.wsCalculation.model.Slab;
import org.egov.wsCalculation.model.TaxHeadEstimate;
import org.egov.wsCalculation.model.WaterConnection;
import org.egov.wsCalculation.util.CalculatorUtil;
import org.egov.wsCalculation.util.WaterCessUtil;
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
	private WaterCessUtil waterCessUtil;
	
	@Autowired
	private CalculatorUtil calculatorUtil;
	

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
	public Map<String, List> getEstimationMap(CalculationCriteria criteria, RequestInfo requestInfo, Map<String, Object> masterData) {
		BigDecimal taxAmt = BigDecimal.ZERO;
		WaterConnection waterConnection = null;
		String tenantId = requestInfo.getUserInfo().getTenantId();
		if(criteria.getWaterConnection() == null && !criteria.getConnectionNo().isEmpty()) {
			waterConnection = calculatorUtil.getWaterConnection(requestInfo, criteria.getConnectionNo(), tenantId);
			criteria.setWaterConnection(waterConnection);
		}
		if(criteria.getWaterConnection() == null) {
			throw new CustomException("Water Connection not found for given criteria ", "Water Connection are not present for "+ criteria.getConnectionNo()+" connection no");
		}
		Map<String, JSONArray> billingSlabMaster = new HashMap<>();
		Map<String, JSONArray> timeBasedExemptionMasterMap = new HashMap<>();
		ArrayList<String> billingSlabIds = new ArrayList<>();
		billingSlabMaster.put(WSCalculationConstant.WC_BILLING_SLAB_MASTER,
				(JSONArray) masterData.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER));
		timeBasedExemptionMasterMap.put(WSCalculationConstant.WC_WATER_CESS_MASTER,
				(JSONArray) (masterData.getOrDefault(WSCalculationConstant.WC_WATER_CESS_MASTER, null)));
//		mDataService.setWaterConnectionMasterValues(requestInfo, tenantId, billingSlabMaster,
//				timeBasedExemptionMasterMap);
		BigDecimal waterCharge = getWaterEstimationCharge(waterConnection, criteria, billingSlabMaster, billingSlabIds, requestInfo);
		taxAmt = waterCharge;
		List<TaxHeadEstimate> taxHeadEstimates = getEstimatesForTax(taxAmt,
				criteria.getWaterConnection(), timeBasedExemptionMasterMap,
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		Map<String, List> estimatesAndBillingSlabs = new HashMap<>();
		estimatesAndBillingSlabs.put("estimates", taxHeadEstimates);
		//Billing slab id
		estimatesAndBillingSlabs.put("billingSlabIds", billingSlabIds);
		return estimatesAndBillingSlabs;
	}

	/**
	 * 
	 * @param waterCharge
	 * @param connection
	 * @param timeBasedExemeptionMasterMap
	 * @param requestInfoWrapper
	 * @return
	 */
	private List<TaxHeadEstimate> getEstimatesForTax(BigDecimal waterCharge,
			WaterConnection connection,
			Map<String, JSONArray> timeBasedExemeptionMasterMap, RequestInfoWrapper requestInfoWrapper) {
		List<TaxHeadEstimate> estimates = new ArrayList<>();
		String assesmentYear = WSCalculationConstant.Assesment_Year;
		// water_charge
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_CHARGE)
				.estimateAmount(waterCharge.setScale(2, 2)).build());

		// Water_cess
		if (timeBasedExemeptionMasterMap.get(WSCalculationConstant.WC_WATER_CESS_MASTER) != null) {
			List<Object> waterCessMasterList = timeBasedExemeptionMasterMap
					.get(WSCalculationConstant.WC_WATER_CESS_MASTER);
			BigDecimal waterCess;
			waterCess = waterCessUtil.getWaterCess(waterCharge, assesmentYear, waterCessMasterList);
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_WATER_CESS)
					.estimateAmount(waterCess).build());
		}
//		 get applicable rebate and penalty
//		Map<String, BigDecimal> rebatePenaltyMap = payService.applyPenaltyRebateAndInterest(payableTax, BigDecimal.ZERO,
//				assessmentYear, timeBasedExemeptionMasterMap);
//		if (null != rebatePenaltyMap) {
//			BigDecimal rebate = rebatePenaltyMap.get(WSCalculationConstant.WS_TIME_REBATE);
//			BigDecimal penalty = rebatePenaltyMap.get(WSCalculationConstant.WS_TIME_PENALTY);
//			BigDecimal interest = rebatePenaltyMap.get(WSCalculationConstant.WS_TIME_INTEREST);
//			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_TIME_REBATE)
//					.estimateAmount(rebate).build());
//			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_TIME_PENALTY)
//					.estimateAmount(penalty).build());
//			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_TIME_INTEREST)
//					.estimateAmount(interest).build());
//			payableTax = payableTax.add(rebate).add(penalty).add(interest);
//		}
		return estimates;
	}

	/**
	 * method to do a first level filtering on the slabs based on the values
	 * present in the Water Details
	 */

	public BigDecimal getWaterEstimationCharge(WaterConnection waterConnection, CalculationCriteria criteria, 
			Map<String, JSONArray> billingSlabMaster, ArrayList<String> billingSlabIds, RequestInfo requestInfo) {
		BigDecimal waterCharge = BigDecimal.ZERO;
		if (billingSlabMaster.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER) == null)
			throw new CustomException("No Billing Slab are found on criteria ", "Billing Slab are Empty");
		ObjectMapper mapper = new ObjectMapper();
		List<BillingSlab> mappingBillingSlab;
		try {
			mappingBillingSlab = mapper.readValue(
					billingSlabMaster.get(WSCalculationConstant.WC_BILLING_SLAB_MASTER).toJSONString(),
					mapper.getTypeFactory().constructCollectionType(List.class, BillingSlab.class));
		} catch (IOException e) {
			throw new CustomException("Parsing Exception", " Billing Slab can not be parsed!");
		}
        String calculationAttribute = "Pipe Size";
		List<BillingSlab> billingSlabs = getSlabsFiltered(waterConnection, mappingBillingSlab, calculationAttribute, requestInfo);
		if (billingSlabs == null || billingSlabs.isEmpty())
			throw new CustomException("No Billing Slab are found on criteria ", "Billing Slab are Empty");
		if (billingSlabs.size() > 1)
			throw new CustomException("More than one Billing Slab are found on criteria ",
					"More than one billing slab found");
		billingSlabIds.add(billingSlabs.get(0).id);
		log.info(" Billing Slab Id For Water Charge Calculation --->  " + billingSlabIds.toString());

		// WaterCharge Calculation
		Double totalUOM = 0.0;
		totalUOM = getUnitOfMeasurement(waterConnection, calculationAttribute, criteria);
		if (totalUOM == 0.0)
			return waterCharge;
		BillingSlab billSlab = billingSlabs.get(0);
		// IF calculation type is flat then take flat rate else take slab and calculate the charge
		//For metered connection calculation on graded fee slab
		//For Non metered connection calculation on normal connection
		if (isRangeCalculation(calculationAttribute)) {
			if (waterConnection.getConnectionType().equalsIgnoreCase(WSCalculationConstant.meteredConnectionType)) {
				for (Slab slab : billSlab.slabs) {
					if (totalUOM > slab.to) {
						waterCharge = waterCharge.add(BigDecimal.valueOf(((slab.to) - (slab.from)) * slab.charge));
						totalUOM = totalUOM - ((slab.to) - (slab.from));
					} else if (totalUOM < slab.to) {
						waterCharge = waterCharge.add(BigDecimal.valueOf(totalUOM * slab.charge));
						totalUOM = ((slab.to) - (slab.from)) - totalUOM;
						break;
					}
				}
				if (billSlab.minimumCharge > waterCharge.doubleValue()) {
					waterCharge = BigDecimal.valueOf(billSlab.minimumCharge);
				}
			} else if (waterConnection.getConnectionType()
					.equalsIgnoreCase(WSCalculationConstant.nonMeterdConnection)) {
				for (Slab slab : billSlab.slabs) {
					if (totalUOM >= slab.from && totalUOM < slab.to) {
						waterCharge = BigDecimal.valueOf((totalUOM * slab.charge));
						if (billSlab.minimumCharge > waterCharge.doubleValue()) {
							waterCharge = BigDecimal.valueOf(billSlab.minimumCharge);
						}
						break;
					}
				}
			}
		} else {
			waterCharge = BigDecimal.valueOf(billSlab.minimumCharge);
		}
		return waterCharge;
	}

	private List<BillingSlab> getSlabsFiltered(WaterConnection waterConnection, List<BillingSlab> billingSlabs,
			String calculationAttribue, RequestInfo requestInfo) {
		Property property = waterConnection.getProperty();
		// get billing Slab
		log.debug(" the slabs count : " + billingSlabs.size());
		final String buildingType = property.getUsageCategory();
		// final String buildingType = "Domestic";
		final String connectionType = waterConnection.getConnectionType();
		final String calculationAttribute = calculationAttribue;

		return billingSlabs.stream().filter(slab -> {
			boolean isBuildingTypeMatching = slab.buildingType.equalsIgnoreCase(buildingType);
			boolean isConnectionTypeMatching = slab.connectionType.equalsIgnoreCase(connectionType);
			boolean isCalculationAttributeMatching = slab.calculationAttribute.equalsIgnoreCase(calculationAttribute);
			return isBuildingTypeMatching && isConnectionTypeMatching && isCalculationAttributeMatching;
		}).collect(Collectors.toList());
	}
	
	private String getCalculationAttribute(Map<String, Object> calculationAttributeMap, String connectionType) {
		String calculationAttribute = null;
		if (calculationAttributeMap == null)
			throw new CustomException("CALCULATION_ATTRIBUTE_MASTER_NOT_FOUND",
					"Calculation attribute master not found!!");
		return calculationAttribute;
	}
	
	/**
	 * 
	 * @param type will be calculation Attribute
	 * @return true if calculation Attribute is not Flat else false
	 */
	private boolean isRangeCalculation(String type) {
		if (type.equalsIgnoreCase(WSCalculationConstant.flatRateCalculationAttribute))
			return false;
		return true;
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
	
	private Double getUnitOfMeasurement(WaterConnection waterConnection, String calculationAttribute,
			CalculationCriteria criteria) {
		Double totalUnit = 0.0;
		if (waterConnection.getConnectionType().equals(WSCalculationConstant.meteredConnectionType)) {
			totalUnit = (criteria.getCurrentReading() - criteria.getLastReading());
			return totalUnit;
		} else if (waterConnection.getConnectionType().equals(WSCalculationConstant.nonMeterdConnection)
				&& calculationAttribute.equalsIgnoreCase(WSCalculationConstant.noOfTapsConst)) {
			if (waterConnection.getNoOfTaps() == null)
				return totalUnit;
			return totalUnit = new Double(waterConnection.getNoOfTaps());
		} else if (waterConnection.getConnectionType().equals(WSCalculationConstant.nonMeterdConnection)
				&& calculationAttribute.equalsIgnoreCase(WSCalculationConstant.pipeSizeConst)) {
			if (waterConnection.getPipeSize() == null)
				return totalUnit;
			return totalUnit = waterConnection.getPipeSize();
		}
		return 0.0;
	}
	
	public Map<String, Object> getQuaterStartAndEndDate(Map<String, Object> billingPeriod){
		Date date = new Date();
		Calendar fromDateCalendar = Calendar.getInstance();
		fromDateCalendar.setTime(date);
		fromDateCalendar.set(Calendar.MONTH, fromDateCalendar.get(Calendar.MONTH)/3 * 3);
		fromDateCalendar.set(Calendar.DAY_OF_MONTH, 1);
		setTimeToBeginningOfDay(fromDateCalendar);
		Calendar toDateCalendar = Calendar.getInstance();
		toDateCalendar.setTime(date);
		toDateCalendar.set(Calendar.MONTH, toDateCalendar.get(Calendar.MONTH)/3 * 3 + 2);
		toDateCalendar.set(Calendar.DAY_OF_MONTH, toDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		setTimeToEndofDay(toDateCalendar);
		billingPeriod.put(WSCalculationConstant.STARTING_DATE_APPLICABLES, fromDateCalendar.getTimeInMillis());
		billingPeriod.put(WSCalculationConstant.ENDING_DATE_APPLICABLES, toDateCalendar.getTimeInMillis());
		return billingPeriod;
	}
	
	public Map<String, Object> getMonthStartAndEndDate(Map<String, Object> billingPeriod){
		Date date = new Date();
		Calendar monthStartDate = Calendar.getInstance();
		monthStartDate.setTime(date);
		monthStartDate.set(Calendar.DAY_OF_MONTH, monthStartDate.getActualMinimum(Calendar.DAY_OF_MONTH));
		setTimeToBeginningOfDay(monthStartDate);
	    
		Calendar monthEndDate = Calendar.getInstance();
		monthEndDate.setTime(date);
		monthEndDate.set(Calendar.DAY_OF_MONTH, monthEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
		setTimeToEndofDay(monthEndDate);
		billingPeriod.put(WSCalculationConstant.STARTING_DATE_APPLICABLES, monthStartDate.getTimeInMillis());
		billingPeriod.put(WSCalculationConstant.ENDING_DATE_APPLICABLES, monthEndDate.getTimeInMillis());
		return billingPeriod;
	}
	
	private static void setTimeToBeginningOfDay(Calendar calendar) {
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
	}

	private static void setTimeToEndofDay(Calendar calendar) {
	    calendar.set(Calendar.HOUR_OF_DAY, 23);
	    calendar.set(Calendar.MINUTE, 59);
	    calendar.set(Calendar.SECOND, 59);
	    calendar.set(Calendar.MILLISECOND, 999);
	}
	
	
	/**
	 * 
	 * @param criteria
	 * @param requestInfo
	 * @param masterData
	 * @return Fee Estimation Map
	 */
	public Map<String, List> getFeeEstimation(CalculationCriteria criteria, RequestInfo requestInfo,
			Map<String, Object> masterData) {
		WaterConnection waterConnection = null;
		String tenantId = requestInfo.getUserInfo().getTenantId();
		if (criteria.getWaterConnection() == null && !criteria.getApplicationNo().isEmpty()) {
			SearchCriteria searchCriteria = new SearchCriteria();
			searchCriteria.setApplicationNumber(criteria.getApplicationNo());
			searchCriteria.setTenantId(criteria.getTenantId());
			waterConnection = calculatorUtil.getWaterConnectionOnApplicationNO(requestInfo, searchCriteria, tenantId);
			criteria.setWaterConnection(waterConnection);
		}
		if (criteria.getWaterConnection() == null) {
			throw new CustomException("WATER_CONNECTION_NOT_FOUND",
					"Water Connection are not present for " + criteria.getApplicationNo() + " Application no");
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

		JSONArray feeSlab = (JSONArray) masterData.getOrDefault(WSCalculationConstant.WC_FEESLAB_MASTER, null);
		if (feeSlab == null)
			throw new CustomException("FEE_SLAB_NOT_FOUND", "fee salb master data not found!!");
		JSONObject feeObj = mapper.convertValue(feeSlab.get(0), JSONObject.class);
		if (feeObj.get(WSCalculationConstant.FORM_FEE_CONST) != null) {
			formFee = new BigDecimal(feeObj.getAsNumber(WSCalculationConstant.FORM_FEE_CONST).toString());
		}
		if (feeObj.get(WSCalculationConstant.SCRUTINY_FEE_CONST) != null) {
			scrutinyFee = new BigDecimal(feeObj.getAsNumber(WSCalculationConstant.SCRUTINY_FEE_CONST).toString());
		}
		if (feeObj.get(WSCalculationConstant.OTHER_CHARGE_CONST) != null) {
			otherCharges = new BigDecimal(feeObj.getAsNumber(WSCalculationConstant.OTHER_CHARGE_CONST).toString());
		}
		if (feeObj.get(WSCalculationConstant.TAX_PERCENTAGE_CONST) != null) {
			taxAndCessPercentage = new BigDecimal(
					feeObj.getAsNumber(WSCalculationConstant.TAX_PERCENTAGE_CONST).toString());
		}
		if (feeObj.get(WSCalculationConstant.METER_COST_CONST) != null && criteria.getWaterConnection()
				.getConnectionType().equalsIgnoreCase(WSCalculationConstant.meteredConnectionType)) {
			meterCost = new BigDecimal(feeObj.getAsNumber(WSCalculationConstant.METER_COST_CONST).toString());
		}
		roadCuttingCharge = getChargeForRoadCutting(masterData, criteria.getWaterConnection().getRoadType(),
				criteria.getWaterConnection().getRoadCuttingArea());
		roadPlotCharge = getPlotSizeFee(masterData, criteria.getWaterConnection().getProperty().getLandArea());
		usageTypeCharge = getUsageTypeFee(masterData, criteria.getWaterConnection().getProperty().getUsageCategory(),
				criteria.getWaterConnection().getRoadCuttingArea());
		totalCharge = formFee.add(scrutinyFee).add(otherCharges).add(meterCost).add(roadCuttingCharge)
				.add(roadPlotCharge).add(usageTypeCharge);
		tax = totalCharge.multiply(taxAndCessPercentage.divide(WSCalculationConstant.HUNDRED));
		//
		if(!(formFee.compareTo(BigDecimal.ZERO) == 0))
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_FORM_FEE)
				.estimateAmount(formFee.setScale(2, 2)).build());
		if(!(scrutinyFee.compareTo(BigDecimal.ZERO) == 0))
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_SCRUTINY_FEE)
				.estimateAmount(scrutinyFee.setScale(2, 2)).build());
		if(!(meterCost.compareTo(BigDecimal.ZERO) == 0))
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_METER_CHARGE)
				.estimateAmount(meterCost.setScale(2, 2)).build());
		if(!(otherCharges.compareTo(BigDecimal.ZERO) == 0))
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_OTHER_CHARGE)
				.estimateAmount(otherCharges.setScale(2, 2)).build());
		if(!(roadCuttingCharge.compareTo(BigDecimal.ZERO) == 0))
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_ROAD_CUTTING_CHARGE)
				.estimateAmount(roadCuttingCharge.setScale(2, 2)).build());
		if(!(usageTypeCharge.compareTo(BigDecimal.ZERO) == 0))
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_ONE_TIME_FEE)
				.estimateAmount(usageTypeCharge.setScale(2, 2)).build());
		if(!(roadPlotCharge.compareTo(BigDecimal.ZERO) == 0))
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_SECURITY_CHARGE)
				.estimateAmount(roadPlotCharge.setScale(2, 2)).build());
		if(!(tax.compareTo(BigDecimal.ZERO) == 0))
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_TAX_AND_CESS)
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
		JSONArray roadSlab = (JSONArray) masterData.getOrDefault(WSCalculationConstant.WC_ROADTYPE_MASTER, null);
		BigDecimal charge = BigDecimal.ZERO;
		BigDecimal cuttingArea = new BigDecimal(roadCuttingArea.toString());
		JSONObject masterSlab = new JSONObject();
		if(roadSlab != null) {
			masterSlab.put("RoadType", roadSlab);
			JSONArray filteredMasters = JsonPath.read(masterSlab, "$.RoadType[?(@.code=='"+roadType+"')]");
			JSONObject master = mapper.convertValue(filteredMasters.get(0), JSONObject.class);
			charge = new BigDecimal(master.getAsNumber(WSCalculationConstant.UNIT_COST_CONST).toString());
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
		JSONArray plotSlab = (JSONArray) masterData.getOrDefault(WSCalculationConstant.WC_PLOTSLAB_MASTER, null);
		JSONObject masterSlab = new JSONObject();
		if (plotSlab != null) {
			masterSlab.put("PlotSizeSlab", plotSlab);
			JSONArray filteredMasters = JsonPath.read(masterSlab, "$.PlotSizeSlab[?(@.from <="+ plotSize +"&& @.to > " + plotSize +")]");
			JSONObject master = mapper.convertValue(filteredMasters.get(0), JSONObject.class);
			charge = new BigDecimal(master.getAsNumber(WSCalculationConstant.UNIT_COST_CONST).toString());
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
		JSONArray usageSlab = (JSONArray) masterData.getOrDefault(WSCalculationConstant.WC_PROPERTYUSAGETYPE_MASTER, null);
		JSONObject masterSlab = new JSONObject();
		BigDecimal cuttingArea = new BigDecimal(roadCuttingArea.toString());
		if(usageSlab != null) {
			masterSlab.put("PropertyUsageType", usageSlab);
			JSONArray filteredMasters = JsonPath.read(masterSlab, "$.PropertyUsageType[?(@.code=='"+usageType+"')]");
			JSONObject master = mapper.convertValue(filteredMasters.get(0), JSONObject.class);
			charge = new BigDecimal(master.getAsNumber(WSCalculationConstant.UNIT_COST_CONST).toString());
			charge = charge.multiply(cuttingArea);
		}
		return charge;
	}
}
