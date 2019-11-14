package org.egov.wsCalculation.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.wsCalculation.model.CalculationCriteria;
import org.egov.wsCalculation.model.CalculationReq;
import org.egov.wsCalculation.model.Category;
import org.egov.wsCalculation.model.TaxHeadEstimate;
import org.egov.wsCalculation.model.TaxHeadMaster;
import org.egov.wsCalculation.validator.WSCalculationValidator;
import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

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
	private DemandService demandService;
	
	

	/**
	 * 
	 */
	public List<Calculation> getTaxCalculation(CalculationReq request) {

		CalculationCriteria criteria = request.getCalculationCriteria().get(0);
		WaterConnection waterConnection = criteria.getWaterConnection();
		// wSCalculationValidator.validateWaterConnectionForCalculation(waterConnection);
		Map<String, Object> masterMap = mDataService.getMasterMap(request);
		return new CalculationRes(new ResponseInfo(), Collections.singletonList(getCalculation(request.getRequestInfo(),
				criteria, getEstimationMap(criteria, request.getRequestInfo()), masterMap)));
	}
    
   /**
    * 
    * @param requestInfo
    * @param criteria Calculation criteria on metercharge
    * @param estimatesAndBillingSlabs Billing Slabs
    * @param masterMap
    * @return
    */
    private Calculation getCalculation(RequestInfo requestInfo, CalculationCriteria criteria,
			Map<String, List> estimatesAndBillingSlabs, Map<String, Object> masterMap) {

		List<org.egov.wsCalculation.model.TaxHeadEstimate> estimates = estimatesAndBillingSlabs.get("estimates");
		List<String> billingSlabIds = estimatesAndBillingSlabs.get("billingSlabIds");

		WaterConnection waterConnection = criteria.getWaterConnection();

		String tenantId = null != waterConnection.getProperty().getTenantId()
				? waterConnection.getProperty().getTenantId()
				: criteria.getTenantId();

		Map<String, Map<String, Object>> financialYearMaster = (Map<String, Map<String, Object>>) masterMap
				.get(WSCalculationConfiguration.FINANCIALYEAR_MASTER_KEY);

		Map<String, Object> finYearMap = financialYearMaster.get(WSCalculationConfiguration.Assesment_Year);
		Long fromDate = (Long) finYearMap.get(WSCalculationConfiguration.FINANCIAL_YEAR_STARTING_DATE);
		Long toDate = (Long) finYearMap.get(WSCalculationConfiguration.FINANCIAL_YEAR_ENDING_DATE);
		Map<String, Category> taxHeadCategoryMap = ((List<TaxHeadMaster>) masterMap
				.get(WSCalculationConfiguration.TAXHEADMASTER_MASTER_KEY)).stream()
						.collect(Collectors.toMap(TaxHeadMaster::getCode, TaxHeadMaster::getCategory));

		BigDecimal taxAmt = BigDecimal.ZERO;
		BigDecimal meterServiceCharge = BigDecimal.ZERO;
		BigDecimal penalty = BigDecimal.ZERO;
		BigDecimal exemption = BigDecimal.ZERO;
		BigDecimal rebate = BigDecimal.ZERO;

		for (TaxHeadEstimate estimate : estimates) {

			Category category = taxHeadCategoryMap.get(estimate.getTaxHeadCode());
			estimate.setCategory(category);

			switch (category) {
			 
			case CHARGES:
				meterServiceCharge = meterServiceCharge.add(estimate.getEstimateAmount());
			
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
		TaxHeadEstimate decimalEstimate = payService.roundOfDecimals(taxAmt.add(penalty).add(meterServiceCharge), rebate.add(exemption));
		if (null != decimalEstimate) {
			decimalEstimate.setCategory(taxHeadCategoryMap.get(decimalEstimate.getTaxHeadCode()));
			estimates.add(decimalEstimate);
			if (decimalEstimate.getEstimateAmount().compareTo(BigDecimal.ZERO) >= 0)
				taxAmt = taxAmt.add(decimalEstimate.getEstimateAmount());
			else
				rebate = rebate.add(decimalEstimate.getEstimateAmount());
		}

		BigDecimal totalAmount = taxAmt.add(penalty).add(rebate).add(exemption).add(meterServiceCharge);
//		// false in the argument represents that the demand shouldn't be updated from
//		// this call
//		BigDecimal collectedAmtForOldDemand = demandService.getCarryForwardAndCancelOldDemand(ptTax, criteria,
//				requestInfo, false);

//		if (collectedAmtForOldDemand.compareTo(BigDecimal.ZERO) > 0)
//			estimates.add(TaxHeadEstimate.builder().taxHeadCode(PT_ADVANCE_CARRYFORWARD)
//					.estimateAmount(collectedAmtForOldDemand).build());
//		else if (collectedAmtForOldDemand.compareTo(BigDecimal.ZERO) < 0)
//			throw new CustomException(EG_PT_DEPRECIATING_ASSESSMENT_ERROR,
//					EG_PT_DEPRECIATING_ASSESSMENT_ERROR_MSG_ESTIMATE);

		return Calculation.builder().totalAmount(totalAmount).taxAmount(taxAmt)
				.penalty(penalty).exemption(exemption).rebate(rebate).fromDate(fromDate).toDate(toDate)
				.tenantId(tenantId).taxHeadEstimates(estimates).billingSlabIds(billingSlabIds).build();
	}

}
