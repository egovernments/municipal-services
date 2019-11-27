package org.egov.swCalculation.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swCalculation.model.Calculation;
import org.egov.swCalculation.model.CalculationCriteria;
import org.egov.swCalculation.model.CalculationReq;
import org.egov.swCalculation.model.RequestInfoWrapper;
import org.egov.swCalculation.model.TaxHeadEstimate;
import org.egov.swCalculation.util.CalculatorUtils;
import org.egov.swService.model.SewerageConnection;
import org.egov.tracer.model.CustomException;
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
			Calculation calculation = wSCalculationService.getCalculation(requestInfo, criteria,
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
		BigDecimal waterCharge = getSeweEstimationCharge(sewerageConnection, criteria, billingSlabMaster, requestInfo);
		taxAmt = waterCharge;
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
}
