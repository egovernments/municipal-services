package org.egov.wsCalculation.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.BillingSlab;
import org.egov.wsCalculation.model.Calculation;
import org.egov.wsCalculation.model.CalculationCriteria;
import org.egov.wsCalculation.model.CalculationReq;
import org.egov.wsCalculation.model.Property;
import org.egov.wsCalculation.model.RequestInfoWrapper;
import org.egov.wsCalculation.model.Slab;
import org.egov.wsCalculation.model.TaxHeadEstimate;
import org.egov.wsCalculation.model.WaterConnection;
import org.egov.wsCalculation.util.CalculatorUtil;
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
	
	@Autowired
	WSCalculationService wSCalculationService;
	
	@Autowired
	CalculatorUtil calculatorUtil;
	

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
		WaterConnection waterConnection = null;
		String assessmentYear = getAssessmentYear();
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
		mDataService.setWaterConnectionMasterValues(requestInfo, tenantId, billingSlabMaster,
				timeBasedExemptionMasterMap);
		BigDecimal waterCharge = getWaterEstimationCharge(waterConnection, criteria, billingSlabMaster, billingSlabIds, requestInfo);
		taxAmt = waterCharge;
		List<TaxHeadEstimate> taxHeadEstimates = getEstimatesForTax(assessmentYear, taxAmt,
				criteria.getWaterConnection(), billingSlabMaster, timeBasedExemptionMasterMap,
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		Map<String, List> estimatesAndBillingSlabs = new HashMap<>();
		estimatesAndBillingSlabs.put("estimates", taxHeadEstimates);
		//Billing slab id
		estimatesAndBillingSlabs.put("billingSlabIds", billingSlabIds);
		return estimatesAndBillingSlabs;
	}

 /**
  * 
  * @param assessmentYear Assessment year
  * @param taxAmt taxable amount
  * @param connection
  * @param waterBasedExemptionMasterMap
  * @param timeBasedExemeptionMasterMap
  * @param requestInfoWrapper
  * @return
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
//		List<Object> waterCessMasterList = timeBasedExemeptionMasterMap
//				.get(WSCalculationConstant.WC_WATER_CESS_MASTER);
//		BigDecimal waterCess;
//		waterCess = waterCessUtil.getWaterCess(payableTax, assesmentYear, waterCessMasterList, connection);
//		estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_WATER_CESS)
//				.estimateAmount(waterCess).build());
		// get applicable rebate and penalty
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
		
		List<BillingSlab> billingSlabs = getSlabsFiltered(waterConnection, mappingBillingSlab, requestInfo);
		if (billingSlabs == null || billingSlabs.isEmpty())
			throw new CustomException("No Billing Slab are found on criteria ", "Billing Slab are Empty");
		if (billingSlabs.size() > 1)
			throw new CustomException("More than one Billing Slab are found on criteria ",
					"More than one billing slab found");
		billingSlabIds.add(billingSlabs.get(0).id);
		log.info(" Billing Slab Id For Water Charge Calculation --->  "+billingSlabIds.toString());
		
		//WaterCharge Calculation
		 Double totalUOM = 0.0;
		 totalUOM = getUnitOfMeasurement(waterConnection, criteria);
		 if(totalUOM == 0.0)
			 return waterCharge;

		if (isRangeCalculation(waterConnection.getCalculationAttribute())) {
			for (BillingSlab billingSlab : billingSlabs) {
				for (Slab slab : billingSlab.slabs) {
					if (totalUOM >= slab.from && totalUOM < slab.to) {
						waterCharge = BigDecimal.valueOf((totalUOM * slab.charge));
						if (slab.minimumCharge > waterCharge.doubleValue()) {
							waterCharge = BigDecimal.valueOf(slab.minimumCharge);
						}
						break;
					}
				}
			}
		} else {
			for (BillingSlab billingSlab : billingSlabs) {
				waterCharge = BigDecimal.valueOf(billingSlab.slabs.get(0).charge);
				break;
			}
		}
		return waterCharge;
	}

	private List<BillingSlab> getSlabsFiltered(WaterConnection waterConnection, List<BillingSlab> billingSlabs, RequestInfo requestInfo) {
		Property property = waterConnection.getProperty();
		// get billing Slab
		log.debug(" the slabs count : " + billingSlabs.size());
//		final String buildingType = property.getPropertyType();
		final String buildingType = "Domestic";
		final String connectionType = waterConnection.getConnectionType();
		final String calculationAttribute = waterConnection.getCalculationAttribute();
		final String unitOfMeasurement = waterConnection.getUom();

		return billingSlabs.stream().filter(slab -> {
			boolean isBuildingTypeMatching = slab.BuildingType.equalsIgnoreCase(buildingType);
			boolean isConnectionTypeMatching = slab.ConnectionType.equalsIgnoreCase(connectionType);
			boolean isCalculationAttributeMatching = slab.CalculationAttribute.equalsIgnoreCase(calculationAttribute);
			boolean isUnitOfMeasurementMatcing = slab.UOM.equalsIgnoreCase(unitOfMeasurement);
			return isBuildingTypeMatching && isConnectionTypeMatching && isCalculationAttributeMatching
					&& isUnitOfMeasurementMatcing;
		}).collect(Collectors.toList());
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
		return Integer.toString(YearMonth.now().getYear()) + "-"
				+ (Integer.toString(YearMonth.now().getYear() + 1).substring(0, 2));
	}
	
	private Double getUnitOfMeasurement(WaterConnection waterConnection, CalculationCriteria criteria) {
		Double totalUnite = 0.0;
		if (waterConnection.getConnectionType().equals(WSCalculationConstant.meteredConnectionType)) {
			totalUnite = (criteria.getCurrentReading() - criteria.getLastReading());
			return totalUnite;
		} else if (waterConnection.getConnectionType().equals(WSCalculationConstant.nonMeterdConnection)
				&& waterConnection.getCalculationAttribute().equalsIgnoreCase(WSCalculationConstant.noOfTapsConst)) {
			return totalUnite = new Double(waterConnection.getNoOfTaps());
		} else if (waterConnection.getConnectionType().equals(WSCalculationConstant.nonMeterdConnection)
				&& waterConnection.getCalculationAttribute().equalsIgnoreCase(WSCalculationConstant.pipeSizeConst)) {
			return totalUnite = waterConnection.getPipeSize();
		}
		return 0.0;
	}
}
