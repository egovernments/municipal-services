package org.egov.swCalculation.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swCalculation.constants.SWCalculationConstant;
import org.egov.swCalculation.model.Calculation;
import org.egov.swCalculation.model.CalculationCriteria;
import org.egov.swCalculation.model.CalculationReq;
import org.egov.swCalculation.model.RequestInfoWrapper;
import org.egov.swCalculation.model.TaxHeadEstimate;
import org.egov.swCalculation.util.CalculatorUtils;
import org.egov.swService.model.SewerageConnection;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.WaterConnection;
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
	CalculatorUtils calculatorUtil;
	
	@Autowired
	PayService payService;
	
	@Autowired
	SWCalculationService swCalculationService;
/**
 * 
 * @param Calculation request parameter
 * @return Map of Billing Slab and Calculation
 */
	public Map<String, Calculation> getEstimationWaterMap(CalculationReq request) {

		RequestInfo requestInfo = request.getRequestInfo();
		List<CalculationCriteria> criteriaList = request.getCalculationCriteria();
		Map<String, Object> masterMap = mDataService.getMasterMap(request);
		Map<String, Calculation> calculationWaterMap = new HashMap<>();
		for (CalculationCriteria criteria : criteriaList) {
			String connectionNO = criteria.getConnectionNo();
			Map<String, List> estimatesAndBillingSlabs = null;
			estimatesAndBillingSlabs = getEstimationMap(criteria, requestInfo);
			Calculation calculation = swCalculationService.getCalculation(requestInfo, criteria,
					estimatesAndBillingSlabs , masterMap);
			calculation.setConnectionNo(connectionNO);
			calculationWaterMap.put(connectionNO, calculation);
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
		SewerageConnection sewerageConnection = null;
		String assessmentYear = getAssessmentYear();
		String tenantId = requestInfo.getUserInfo().getTenantId();
		if(criteria.getSewerageConnection() == null && !criteria.getConnectionNo().isEmpty()) {
			sewerageConnection = calculatorUtil.getSewerageConnection(requestInfo, criteria.getConnectionNo(), tenantId);
			criteria.setSewerageConnection(sewerageConnection);
		}
		if(criteria.getSewerageConnection() == null) {
			throw new CustomException("Sewerage Connection not found for given criteria ", "Sewerage Connection are not present for "+ criteria.getConnectionNo()+" connection no");
		}
		Map<String, JSONArray> billingSlabMaster = new HashMap<>();
		Map<String, JSONArray> timeBasedExemptionMasterMap = new HashMap<>();
		mDataService.setSewerageConnectionMasterValues(requestInfo, tenantId, billingSlabMaster,
				timeBasedExemptionMasterMap);
		BigDecimal sewarageCharge = getSeweEstimationCharge(sewerageConnection, criteria, billingSlabMaster, requestInfo);
		taxAmt = sewarageCharge;
		List<TaxHeadEstimate> taxHeadEstimates = getEstimatesForTax(assessmentYear, taxAmt,
				criteria.getSewerageConnection(), billingSlabMaster, timeBasedExemptionMasterMap,
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		Map<String, List> estimatesAndBillingSlabs = new HashMap<>();
		estimatesAndBillingSlabs.put("estimates", taxHeadEstimates);
		estimatesAndBillingSlabs.put("billingSlabIds", new ArrayList<>());
		return estimatesAndBillingSlabs;
	}
	
	/**
	 * method to do a first level filtering on the slabs based on the values
	 * present in the Sewerage Details
	 */

	public BigDecimal getSeweEstimationCharge(SewerageConnection sewerageConnection, CalculationCriteria criteria, 
			Map<String, JSONArray> billingSlabMaster, RequestInfo requestInfo) {
		BigDecimal sewerageCharge = BigDecimal.ZERO;
		return sewerageCharge;
	}
	
	
	public String getAssessmentYear() {
		return Integer.toString(YearMonth.now().getYear()) + "-"
				+ (Integer.toString(YearMonth.now().getYear() + 1).substring(0, 2));
	}
	
	
	 /**
	  * 
	  * @param assessmentYear Assessment year
	  * @param taxAmt taxable amount
	  * @param connection
	  * @param sewerageExemptionMasterMap
	  * @param timeBasedExemeptionMasterMap
	  * @param requestInfoWrapper
	  * @return
	  */
		private List<TaxHeadEstimate> getEstimatesForTax(String assessmentYear, BigDecimal taxAmt,
				SewerageConnection sewerageConnection, Map<String, JSONArray> sewerageBasedExemptionMasterMap,
				Map<String, JSONArray> timeBasedExemeptionMasterMap, RequestInfoWrapper requestInfoWrapper) {
			List<TaxHeadEstimate> estimates = new ArrayList<>();
			BigDecimal payableTax = taxAmt;
			// water_charge
			estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_CHARGE)
					.estimateAmount(taxAmt.setScale(2, 2)).build());

			// Water_cess
//			List<Object> waterCessMasterList = timeBasedExemeptionMasterMap
//					.get(WSCalculationConstant.WC_WATER_CESS_MASTER);
//			BigDecimal waterCess;
//			waterCess = waterCessUtil.getSewerageCess(payableTax, assesmentYear, waterCessMasterList, connection);
//			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_WATER_CESS)
//					.estimateAmount(waterCess).build());
			// get applicable rebate and penalty
			Map<String, BigDecimal> rebatePenaltyMap = payService.applyPenaltyRebateAndInterest(payableTax, BigDecimal.ZERO,
					assessmentYear, timeBasedExemeptionMasterMap);
			if (null != rebatePenaltyMap) {
				BigDecimal rebate = rebatePenaltyMap.get(SWCalculationConstant.SW_TIME_REBATE);
				BigDecimal penalty = rebatePenaltyMap.get(SWCalculationConstant.SW_TIME_REBATE);
				BigDecimal interest = rebatePenaltyMap.get(SWCalculationConstant.SW_TIME_INTEREST);
				estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_TIME_REBATE)
						.estimateAmount(rebate).build());
				estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_TIME_REBATE)
						.estimateAmount(penalty).build());
				estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_TIME_INTEREST)
						.estimateAmount(interest).build());
				payableTax = payableTax.add(rebate).add(penalty).add(interest);
			}
			return estimates;
		}
}
