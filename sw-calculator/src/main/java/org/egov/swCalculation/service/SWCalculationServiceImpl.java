package org.egov.swCalculation.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.swCalculation.constants.SWCalculationConstant;
import org.egov.swCalculation.model.Calculation;
import org.egov.swCalculation.model.CalculationCriteria;
import org.egov.swCalculation.model.CalculationReq;
import org.egov.swCalculation.model.CalculationRes;
import org.egov.swCalculation.model.Category;
import org.egov.swCalculation.model.SewerageConnection;
import org.egov.swCalculation.model.TaxHeadEstimate;
import org.egov.swCalculation.model.TaxHeadMaster;
import org.egov.swCalculation.repository.SewerageCalculatorDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SWCalculationServiceImpl implements SWCalculationService {
	
	@Autowired
	MasterDataService mDataService;
	
	@Autowired
	EstimationService estimationService;
	
	@Autowired
	PayService payService;
	
	@Autowired
	DemandService demandService;
	
	@Autowired
	SewerageCalculatorDao sewerageCalculatorDao;
	
	/**
	 * Get Calculation Request and return Calculated Response
	 */
	@Override
	public CalculationRes getCalculation(CalculationReq request) {
		Map<String, Object> masterMap = mDataService.getMasterMap(request);
		List<Calculation> calculations = getCalculations(request, masterMap);
		demandService.generateDemand(request.getRequestInfo(), calculations, masterMap);
		return new CalculationRes(new ResponseInfo(),calculations);
	}

	
/**
 * 
 * @param requestInfo
 * @param criteria
 * @param estimatesAndBillingSlabs
 * @param masterMap
 * @return
 */
	public Calculation getCalculation(RequestInfo requestInfo, CalculationCriteria criteria,
			Map<String, List> estimatesAndBillingSlabs, Map<String, Object> masterMap) {

		@SuppressWarnings("unchecked")
		List<TaxHeadEstimate> estimates = estimatesAndBillingSlabs.get("estimates");
		@SuppressWarnings("unchecked")
		List<String> billingSlabIds = estimatesAndBillingSlabs.get("billingSlabIds");
		
		SewerageConnection sewerageConnection = criteria.getSewerageConnection();


		// String assessmentNumber = null != detail.getAssessmentNumber() ?
		// detail.getAssessmentNumber() : criteria.getAssesmentNumber();
		String tenantId = criteria.getTenantId();
		
		@SuppressWarnings("unchecked")
		Map<String, Category> taxHeadCategoryMap = ((List<TaxHeadMaster>) masterMap
				.get(SWCalculationConstant.TAXHEADMASTER_MASTER_KEY)).stream()
						.collect(Collectors.toMap(TaxHeadMaster::getCode, TaxHeadMaster::getCategory));

		BigDecimal taxAmt = BigDecimal.ZERO;
		BigDecimal sewerageCharge = BigDecimal.ZERO;
		BigDecimal penalty = BigDecimal.ZERO;
		BigDecimal exemption = BigDecimal.ZERO;
		BigDecimal rebate = BigDecimal.ZERO;

		for (TaxHeadEstimate estimate : estimates) {

			Category category = taxHeadCategoryMap.get(estimate.getTaxHeadCode());
			estimate.setCategory(category);

			switch (category) {

			case CHARGES:
				sewerageCharge = sewerageCharge.add(estimate.getEstimateAmount());
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
		TaxHeadEstimate decimalEstimate = payService.roundOfDecimals(taxAmt.add(penalty).add(sewerageCharge),
				rebate.add(exemption));
		if (null != decimalEstimate) {
			decimalEstimate.setCategory(taxHeadCategoryMap.get(decimalEstimate.getTaxHeadCode()));
			estimates.add(decimalEstimate);
			if (decimalEstimate.getEstimateAmount().compareTo(BigDecimal.ZERO) >= 0)
				taxAmt = taxAmt.add(decimalEstimate.getEstimateAmount());
			else
				rebate = rebate.add(decimalEstimate.getEstimateAmount());
		}

		BigDecimal totalAmount = taxAmt.add(penalty).add(rebate).add(exemption).add(sewerageCharge);
		
		return Calculation.builder().totalAmount(totalAmount).taxAmount(taxAmt).penalty(penalty).exemption(exemption)
				.charge(sewerageCharge).sewerageConnection(sewerageConnection).rebate(rebate).tenantId(tenantId)
				.taxHeadEstimates(estimates).billingSlabIds(billingSlabIds).connectionNo(criteria.getConnectionNo())
				.build();
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
			mDataService.getBillingFrequencyMasterData(request.getRequestInfo(), criteria.getSewerageConnection().getConnectionType(), tenantId ,masterMap);
			Calculation calculation = getCalculation(request.getRequestInfo(), criteria, estimationMap, masterMap);
			calculations.add(calculation);
		}
		return calculations;
	}
	
	/**
	 * Generate Demand Based on Time (Monthly, Quarterly, Yearly)
	 */
	public void generateDemandBasedOnTimePeriod() {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime date = LocalDateTime.now();
		log.info("Time schedule start for sewerage demand generation on : " + date.format(dateTimeFormatter));
		List<String> tenantIds = sewerageCalculatorDao.getTenantId();
		if (tenantIds.isEmpty())
			return;
		log.info("Tenant Ids : " + tenantIds.toString());
		tenantIds.forEach(tenantId -> {
			demandService.generateDemandForTenantId(tenantId);
		});
	}
	
}
