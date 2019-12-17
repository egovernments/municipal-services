package org.egov.wsCalculation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.model.CalculationCriteria;
import org.egov.wsCalculation.model.CalculationReq;
import org.egov.wsCalculation.model.CalculationRes;
import org.egov.wsCalculation.model.Category;
import org.egov.wsCalculation.model.TaxHeadEstimate;
import org.egov.wsCalculation.model.TaxHeadMaster;
import org.egov.wsCalculation.model.WaterConnection;
import org.egov.wsCalculation.repository.WSCalculationDao;
import org.egov.wsCalculation.util.CalculatorUtil;
import org.egov.wsCalculation.util.MRConstants;
import org.egov.wsCalculation.validator.MDMSValidator;
import org.egov.wsCalculation.validator.WSCalculationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.Calculation;

@Service
@Slf4j
public class WSCalculationServiceImpl implements WSCalculationService {

	@Autowired
	WSCalculationValidator wSCalculationValidator;

	@Autowired
	private MasterDataService mDataService;

	@Autowired
	private PayService payService;

	@Autowired
	EstimationService estimationService;
	
	@Autowired
	CalculatorUtil calculatorUtil;
	
	@Autowired
	DemandService demandService;
	
	@Autowired
    MasterDataService masterDataService; 

	@Autowired
	WSCalculationDao wSCalculationDao;
	
	@Autowired
	MDMSValidator mdmsValidator;



	/**
	 * Get CalculationReq and Calculate the Tax Head on Water Charge
	 */
	public List<Calculation> getCalculation(CalculationReq request) {
		Map<String, Object> masterMap = mDataService.getMasterMap(request);
		List<Calculation> calculations = getCalculations(request, masterMap);
		demandService.generateDemand(request.getRequestInfo(), calculations, masterMap);
		return calculations;
	}

	
	/**
	 * It will take calculation and return calculation with tax head code 
	 * 
	 * @param requestInfo
	 * @param criteria Calculation criteria on meter charge
	 * @param estimatesAndBillingSlabs Billing Slabs
	 * @param masterMap
	 * @return Calculation With Tax head
	 */
	public Calculation getCalculation(RequestInfo requestInfo, CalculationCriteria criteria,
			Map<String, List> estimatesAndBillingSlabs, Map<String, Object> masterMap) {

		@SuppressWarnings("unchecked")
		List<TaxHeadEstimate> estimates = estimatesAndBillingSlabs.get("estimates");
		@SuppressWarnings("unchecked")
		List<String> billingSlabIds = estimatesAndBillingSlabs.get("billingSlabIds");
		WaterConnection waterConnection = criteria.getWaterConnection();

		String tenantId = null != waterConnection.getProperty().getTenantId()
				? waterConnection.getProperty().getTenantId()
				: criteria.getTenantId();

		@SuppressWarnings("unchecked")
		Map<String, Category> taxHeadCategoryMap = ((List<TaxHeadMaster>) masterMap
				.get(WSCalculationConstant.TAXHEADMASTER_MASTER_KEY)).stream()
						.collect(Collectors.toMap(TaxHeadMaster::getCode, TaxHeadMaster::getCategory));

		BigDecimal taxAmt = BigDecimal.ZERO;
		BigDecimal waterCharge = BigDecimal.ZERO;
		BigDecimal penalty = BigDecimal.ZERO;
		BigDecimal exemption = BigDecimal.ZERO;
		BigDecimal rebate = BigDecimal.ZERO;

		for (TaxHeadEstimate estimate : estimates) {

			Category category = taxHeadCategoryMap.get(estimate.getTaxHeadCode());
			estimate.setCategory(category);

			switch (category) {

			case CHARGES:
				waterCharge = waterCharge.add(estimate.getEstimateAmount());
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
		TaxHeadEstimate decimalEstimate = payService.roundOfDecimals(taxAmt.add(penalty).add(waterCharge),
				rebate.add(exemption));
		if (null != decimalEstimate) {
			decimalEstimate.setCategory(taxHeadCategoryMap.get(decimalEstimate.getTaxHeadCode()));
			estimates.add(decimalEstimate);
			if (decimalEstimate.getEstimateAmount().compareTo(BigDecimal.ZERO) >= 0)
				taxAmt = taxAmt.add(decimalEstimate.getEstimateAmount());
			else
				rebate = rebate.add(decimalEstimate.getEstimateAmount());
		}

		BigDecimal totalAmount = taxAmt.add(penalty).add(rebate).add(exemption).add(waterCharge);
		// // false in the argument represents that the demand shouldn't be updated from
		// // this call
		// BigDecimal collectedAmtForOldDemand =
		// demandService.getCarryForwardAndCancelOldDemand(ptTax, criteria,
		// requestInfo, false);

		// if (collectedAmtForOldDemand.compareTo(BigDecimal.ZERO) > 0)
		// estimates.add(TaxHeadEstimate.builder().taxHeadCode(PT_ADVANCE_CARRYFORWARD)
		// .estimateAmount(collectedAmtForOldDemand).build());
		// else if (collectedAmtForOldDemand.compareTo(BigDecimal.ZERO) < 0)
		// throw new CustomException(EG_PT_DEPRECIATING_ASSESSMENT_ERROR,
		// EG_PT_DEPRECIATING_ASSESSMENT_ERROR_MSG_ESTIMATE);

		return Calculation.builder().totalAmount(totalAmount).taxAmount(taxAmt).penalty(penalty).exemption(exemption)
				.waterConnection(waterConnection).rebate(rebate).tenantId(tenantId).taxHeadEstimates(estimates).billingSlabIds(billingSlabIds).connectionNo(criteria.getConnectionNo()).build();
	}
	
	/**
	 * 
	 * @param request Contains calculation request
	 * @return List of Calculation with different tax head
	 */
	List<Calculation> getCalculations(CalculationReq request, Map<String, Object> masterMap) {
		List<Calculation> calculations = new ArrayList<>(request.getCalculationCriteria().size());
		String tenantId = request.getCalculationCriteria().get(0).getTenantId();
		for (CalculationCriteria criteria : request.getCalculationCriteria()) {
			Map<String, List> estimationMap = estimationService.getEstimationMap(criteria, request.getRequestInfo());
			masterDataService.getBillingFrequencyMasterData(request.getRequestInfo(), criteria.getWaterConnection().getConnectionType(), tenantId ,masterMap);
			Calculation calculation = getCalculation(request.getRequestInfo(), criteria, estimationMap, masterMap);
			calculations.add(calculation);
		}
		return calculations;
	}


	@Override
	public void jobscheduler() {
		// TODO Auto-generated method stub
		ArrayList<String> tenentIds =	wSCalculationDao.searchTenentIds();
	  
	  for(String tenentId : tenentIds){
		 Map<String, Object> billingPeriodJson = ((Map<String,Object>)mdmsValidator.validateMasterDataWithoutFilter(tenentId));
		 List<Map<String, Object>> jsonOutput = (List<Map<String, Object>>) billingPeriodJson;
		
		String jsonPath = MRConstants.JSONPATH_ROOT;
			try {
			
				Map<String, Object> financialYearProperties = jsonOutput.get(0);
				log.info(financialYearProperties.toString());
			
			} catch (IndexOutOfBoundsException e) {
				throw new CustomException(WSCalculationConstant.EG_WS_FINANCIAL_MASTER_NOT_FOUND,
						WSCalculationConstant.EG_WS_FINANCIAL_MASTER_NOT_FOUND_MSG );
			}
		
		
		
	  }
        
	

	}
}
