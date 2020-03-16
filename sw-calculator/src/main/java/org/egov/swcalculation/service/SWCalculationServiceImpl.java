package org.egov.swcalculation.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swcalculation.constants.SWCalculationConstant;
import org.egov.swcalculation.model.Calculation;
import org.egov.swcalculation.model.CalculationCriteria;
import org.egov.swcalculation.model.CalculationReq;
import org.egov.swcalculation.model.Category;
import org.egov.swcalculation.model.SewerageConnection;
import org.egov.swcalculation.model.TaxHeadEstimate;
import org.egov.swcalculation.model.TaxHeadMaster;
import org.egov.swcalculation.repository.SewerageCalculatorDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SWCalculationServiceImpl implements SWCalculationService {

	@Autowired
	private MasterDataService mDataService;

	@Autowired
	private EstimationService estimationService;

	@Autowired
	private PayService payService;

	@Autowired
	private DemandService demandService;

	@Autowired
	private SewerageCalculatorDao sewerageCalculatorDao;

	/**
	 * Get CalculationReq and Calculate the Tax Head on Sewerage Charge
	 */
	public List<Calculation> getCalculation(CalculationReq request) {
		List<Calculation> calculations = new ArrayList<>();

		if (request.getIsconnectionCalculation()) {
			// Calculate and create demand for connection
			Map<String, Object> masterMap = mDataService.loadMasterData(request.getRequestInfo(),
					request.getCalculationCriteria().get(0).getTenantId());
			calculations = getCalculations(request, masterMap);
			demandService.generateDemand(request.getRequestInfo(), calculations, masterMap,
					request.getIsconnectionCalculation());
			unsetSewerageConnection(calculations);
		} else {
			// Calculate and create demand for application
			Map<String, Object> masterData = mDataService.loadExcemptionMaster(request.getRequestInfo(),
					request.getCalculationCriteria().get(0).getTenantId());
			calculations = getFeeCalculation(request, masterData);
			demandService.generateDemand(request.getRequestInfo(), calculations, masterData,
					request.getIsconnectionCalculation());
			unsetSewerageConnection(calculations);
		}
		
		return calculations;
	}

	/**
	 * 
	 * @param requestInfo
	 * @param criteria
	 * @param estimatesAndBillingSlabs
	 * @param masterMap
	 * @return
	 * 
	 */
	public Calculation getCalculation(RequestInfo requestInfo, CalculationCriteria criteria,
			Map<String, List> estimatesAndBillingSlabs, Map<String, Object> masterMap, boolean isConnectionFee) {

		@SuppressWarnings("unchecked")
		List<TaxHeadEstimate> estimates = estimatesAndBillingSlabs.get("estimates");
		@SuppressWarnings("unchecked")
		List<String> billingSlabIds = estimatesAndBillingSlabs.get("billingSlabIds");
		SewerageConnection sewerageConnection = criteria.getSewerageConnection();
		String tenantId = null != sewerageConnection.getProperty().getTenantId()
				? sewerageConnection.getProperty().getTenantId()
				: criteria.getTenantId();

		@SuppressWarnings("unchecked")
		Map<String, Category> taxHeadCategoryMap = ((List<TaxHeadMaster>) masterMap
				.get(SWCalculationConstant.TAXHEADMASTER_MASTER_KEY)).stream()
						.collect(Collectors.toMap(TaxHeadMaster::getCode, TaxHeadMaster::getCategory, (OldValue, NewValue) -> NewValue));

		BigDecimal taxAmt = BigDecimal.ZERO;
		BigDecimal sewerageCharge = BigDecimal.ZERO;
		BigDecimal penalty = BigDecimal.ZERO;
		BigDecimal exemption = BigDecimal.ZERO;
		BigDecimal rebate = BigDecimal.ZERO;
		BigDecimal fee = BigDecimal.ZERO;

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

			case FEE:
				fee = fee.add(estimate.getEstimateAmount());
				break;

			default:
				taxAmt = taxAmt.add(estimate.getEstimateAmount());
				break;
			}
			
		}

		TaxHeadEstimate decimalEstimate = payService.roundOfDecimals(taxAmt.add(penalty).add(fee).add(sewerageCharge),
				rebate.add(exemption), isConnectionFee);
		if (null != decimalEstimate) {
			decimalEstimate.setCategory(taxHeadCategoryMap.get(decimalEstimate.getTaxHeadCode()));
			estimates.add(decimalEstimate);
			if (decimalEstimate.getEstimateAmount().compareTo(BigDecimal.ZERO) >= 0)
				taxAmt = taxAmt.add(decimalEstimate.getEstimateAmount());
			else
				rebate = rebate.add(decimalEstimate.getEstimateAmount());
		}

		BigDecimal totalAmount = taxAmt.add(penalty).add(rebate).add(exemption).add(sewerageCharge).add(fee);
		return Calculation.builder().totalAmount(totalAmount).taxAmount(taxAmt).penalty(penalty).exemption(exemption)
				.charge(sewerageCharge).fee(fee).sewerageConnection(sewerageConnection).rebate(rebate)
				.tenantId(tenantId).taxHeadEstimates(estimates).billingSlabIds(billingSlabIds)
				.connectionNo(criteria.getConnectionNo()).applicationNO(criteria.getApplicationNo()).build();
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

	/**
	 * 
	 * @param request
	 *            would be calculations request
	 * @param masterMap
	 *            master data
	 * @return all calculations including sewerage charge and taxhead on that
	 */
	List<Calculation> getCalculations(CalculationReq request, Map<String, Object> masterMap) {
		List<Calculation> calculations = new ArrayList<>(request.getCalculationCriteria().size());
		for (CalculationCriteria criteria : request.getCalculationCriteria()) {
			Map<String, List> estimationMap = estimationService.getEstimationMap(criteria, request.getRequestInfo(),
					masterMap);
			ArrayList<?> billingFrequencyMap = (ArrayList<?>) masterMap
					.get(SWCalculationConstant.Billing_Period_Master);
			mDataService.enrichBillingPeriod(criteria, billingFrequencyMap, masterMap);
			Calculation calculation = getCalculation(request.getRequestInfo(), criteria, estimationMap, masterMap,
					true);
			calculations.add(calculation);
		}
		return calculations;
	}

	/**
	 * 
	 * 
	 * @param request
	 * @return List of calculation.
	 */
	public List<Calculation> bulkDemandGeneration(CalculationReq request, Map<String, Object> masterMap) {
		List<Calculation> calculations = getCalculations(request, masterMap);
		demandService.generateDemand(request.getRequestInfo(), calculations, masterMap, true);
		return calculations;
	}

	/**
	 * 
	 * @param request
	 * @return list of calculation based on request
	 */
	public List<Calculation> getEstimation(CalculationReq request) {
		Map<String, Object> masterData = mDataService.loadExcemptionMaster(request.getRequestInfo(),
				request.getCalculationCriteria().get(0).getTenantId());
		List<Calculation> calculations = getFeeCalculation(request, masterData);
		unsetSewerageConnection(calculations);
		return calculations;
	}

	/**
	 * 
	 * @param request
	 * @param masterMap
	 * @return list of calculation based on estimation criteria
	 */
	List<Calculation> getFeeCalculation(CalculationReq request, Map<String, Object> masterMap) {
		List<Calculation> calculations = new ArrayList<>(request.getCalculationCriteria().size());
		for (CalculationCriteria criteria : request.getCalculationCriteria()) {
			Map<String, List> estimationMap = estimationService.getFeeEstimation(criteria, request.getRequestInfo(),
					masterMap);
			mDataService.enrichBillingPeriodForFee(masterMap);
			Calculation calculation = getCalculation(request.getRequestInfo(), criteria, estimationMap, masterMap,
					false);
			calculations.add(calculation);
		}
		return calculations;
	}

	public void unsetSewerageConnection(List<Calculation> calculation) {
		calculation.forEach(cal -> cal.setSewerageConnection(null));
	}

}
