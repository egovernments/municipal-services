package org.egov.wsCalculation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.egov.waterConnection.model.OwnerInfo;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.wsCalculation.constants.WSCalculationConfiguration;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.Assessment;
import org.egov.wsCalculation.model.Bill;
import org.egov.wsCalculation.model.BillResponse;
import org.egov.wsCalculation.model.Calculation;
import org.egov.wsCalculation.model.CalculationCriteria;
import org.egov.wsCalculation.model.CalculationReq;
import org.egov.wsCalculation.model.Demand;
import org.egov.wsCalculation.model.DemandDetail;
import org.egov.wsCalculation.model.DemandRequest;
import org.egov.wsCalculation.model.DemandResponse;
import org.egov.wsCalculation.model.GetBillCriteria;
import org.egov.wsCalculation.model.RequestInfoWrapper;
import org.egov.wsCalculation.model.TaxHeadEstimate;
import org.egov.wsCalculation.model.TaxHeadMaster;
import org.egov.wsCalculation.model.TaxPeriod;
import org.egov.wsCalculation.repository.ServiceRequestRepository;
import org.egov.wsCalculation.util.WSCalculationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class DemandService {

	@Autowired
	private WSCalculationService wSCalculationService;

	@Autowired
	private ServiceRequestRepository repository;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private PayService payService;

	@Autowired
	private MasterDataService mstrDataService;

	@Autowired
	private AssessmentService assessmentService;

	@Autowired
	private WSCalculationUtil utils;

	@Autowired
	private WSCalculationConfiguration configs;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private EstimationService estimationService;

	/*
	 * Generates and persists the demand to billing service for the given water
	 * connection
	 * 
	 * if the water connection has been assessed already for the given financial
	 * year then
	 * 
	 * it carry forwards the old collection amount to the new demand as advance
	 * 
	 * @param request
	 * 
	 * @return
	 */
	public Map<String, Calculation> generateDemands(CalculationReq request) {

		List<CalculationCriteria> criterias = request.getCalculationCriteria();
		List<Demand> demands = new ArrayList<>();
		List<String> lesserAssessments = new ArrayList<>();
		Map<String, String> consumerCodeFinYearMap = new HashMap<>();

		Map<String, Calculation> waterCalculationMap = estimationService.getEstimationWaterMap(request);
		for (CalculationCriteria criteria : criterias) {

			WaterConnection waterConnection = criteria.getWaterConnection();

			String assessmentNumber = waterConnection.getConnectionNo();

			// ws_tax for the new assessment
			BigDecimal newTax = BigDecimal.ZERO;
			Optional<TaxHeadEstimate> advanceCarryforwardEstimate = waterCalculationMap.get(assessmentNumber)
					.getTaxHeadEstimates().stream()
					.filter(estimate -> estimate.getTaxHeadCode().equalsIgnoreCase(WSCalculationConstant.WS_TAX))
					.findAny();
			if (advanceCarryforwardEstimate.isPresent())
				newTax = advanceCarryforwardEstimate.get().getEstimateAmount();

			// true represents that the demand should be updated from this call
			BigDecimal carryForwardCollectedAmount = getCarryForwardAndCancelOldDemand(newTax, criteria,
					request.getRequestInfo(), true);

			if (carryForwardCollectedAmount.doubleValue() >= 0.0) {

				Demand demand = prepareDemand(waterConnection,
						waterCalculationMap.get(waterConnection.getConnectionNo()), request.getRequestInfo());

				demands.add(demand);
				consumerCodeFinYearMap.put(demand.getConsumerCode(), "2019-20");

			} else {
				lesserAssessments.add(assessmentNumber);
			}
		}

		if (!CollectionUtils.isEmpty(lesserAssessments)) {
			throw new CustomException(WSCalculationConstant.EG_WS_DEPRECIATING_ASSESSMENT_ERROR,
					WSCalculationConstant.EG_WS_DEPRECIATING_ASSESSMENT_ERROR_MSG + lesserAssessments);
		}

		DemandRequest dmReq = DemandRequest.builder().demands(demands).requestInfo(request.getRequestInfo()).build();
		String url = new StringBuilder().append(configs.getBillingServiceHost())
				.append(configs.getDemandCreateEndPoint()).toString();
		DemandResponse res = new DemandResponse();

		try {
			res = restTemplate.postForObject(url, dmReq, DemandResponse.class);

		} catch (HttpClientErrorException e) {
			throw new ServiceCallException(e.getResponseBodyAsString());
		}
		log.info(" The demand Response is : " + res);
		assessmentService.saveAssessments(res.getDemands(), consumerCodeFinYearMap, request.getRequestInfo());
		return waterCalculationMap;
	}

	/**
	 * if any previous assessments and demands associated with it exists for the
	 * same financial year
	 * 
	 * Then Returns the collected amount of previous demand if the current
	 * assessment is for the current year
	 * 
	 * and cancels the previous demand by updating it's status to inactive
	 * 
	 * @param criteria
	 * @return
	 */
	protected BigDecimal getCarryForwardAndCancelOldDemand(BigDecimal newTax,
			org.egov.wsCalculation.model.CalculationCriteria criteria, RequestInfo requestInfo, boolean cancelDemand) {

		WaterConnection waterConnection = criteria.getWaterConnection();

		BigDecimal carryForward = BigDecimal.ZERO;
		BigDecimal oldTaxAmt = BigDecimal.ZERO;

		if (null == waterConnection.getId())
			return carryForward;

		Demand demand = getLatestDemandForCurrentFinancialYear(requestInfo, waterConnection);

		if (null == demand)
			return carryForward;

		carryForward = utils.getTotalCollectedAmountAndPreviousCarryForward(demand);

		for (DemandDetail detail : demand.getDemandDetails()) {
			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(WSCalculationConstant.WS_TAX))
				oldTaxAmt = oldTaxAmt.add(detail.getTaxAmount());
		}

		// if (oldTaxAmt.compareTo(newTax) > 0) {
		// boolean isDepreciationAllowed =
		// utils.isAssessmentDepreciationAllowed(criteria.getAssessmentYear(),
		// waterConnection.getProperty().getTenantId(),
		// waterConnection.getProperty().getPropertyId(),
		// new RequestInfoWrapper(requestInfo));
		// if (!isDepreciationAllowed)
		// carryForward = BigDecimal.valueOf(-1);
		// }

		if (BigDecimal.ZERO.compareTo(carryForward) >= 0 || !cancelDemand)
			return carryForward;

		demand.setStatus(Demand.StatusEnum.CANCELLED);
		DemandRequest request = DemandRequest.builder().demands(Arrays.asList(demand)).requestInfo(requestInfo).build();
		StringBuilder updateDemandUrl = utils.getUpdateDemandUrl();
		repository.fetchResult(updateDemandUrl, request);

		return carryForward;
	}

	/**
	 * @param requestInfo
	 * @param water connection
	 * @return
	 */
	public Demand getLatestDemandForCurrentFinancialYear(RequestInfo requestInfo, WaterConnection waterConnection) {
		String financialYear = "2019-20";

		Assessment assessment = Assessment.builder().connectionId(waterConnection.getId())
				.tenantId(waterConnection.getProperty().getTenantId()).assessmentYear(financialYear).build();

		List<Assessment> assessments = assessmentService.getMaxAssessment(assessment);

		if (CollectionUtils.isEmpty(assessments))
			return null;

		Assessment latestAssessment = assessments.get(0);

		DemandResponse res = mapper.convertValue(
				repository.fetchResult(utils.getDemandSearchUrl(latestAssessment), new RequestInfoWrapper(requestInfo)),
				DemandResponse.class);
		BigDecimal totalCollectedAmount = res.getDemands().get(0).getDemandDetails().stream()
				.map(d -> d.getCollectionAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);

		if (totalCollectedAmount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
			// The total collected amount is fractional most probably because of
			// previous
			// round off dropping prior to BS/CS 1.1 release
			throw new CustomException("INVALID_COLLECT_AMOUNT",
					"The collected amount is fractional, please contact support for data correction");
		}

		return res.getDemands().get(0);
	}

	/**
	 * Prepares Demand object based on the incoming calculation object and
	 * property
	 * 
	 * @param property
	 * @param calculation
	 * @return
	 */
	private Demand prepareDemand(WaterConnection waterConnection, Calculation calculation, RequestInfo requestInfo) {

		String tenantId = waterConnection.getProperty().getTenantId();
		String propertyType = waterConnection.getProperty().getPropertyType();
		String consumerCode = waterConnection.getProperty().getPropertyId()
				+ WSCalculationConstant.WS_CONSUMER_CODE_SEPARATOR + waterConnection.getConnectionNo();
		OwnerInfo owner = null;
		Demand demand = getLatestDemandForCurrentFinancialYear(requestInfo, waterConnection);

		List<DemandDetail> details = new ArrayList<>();

		details = getAdjustedDemandDetails(tenantId, calculation, demand);

		return Demand.builder().tenantId(tenantId).businessService(configs.getWsModuleCode()).consumerType(propertyType)
				.consumerCode(consumerCode).payer(requestInfo.getUserInfo()).taxPeriodFrom(calculation.getFromDate())
				.taxPeriodTo(calculation.getToDate()).status(Demand.StatusEnum.ACTIVE)
				.minimumAmountPayable(BigDecimal.valueOf(configs.getPtMinAmountPayable())).demandDetails(details)
				.build();
	}

	/**
	 * Creates demandDetails for the new demand by adding all old demandDetails
	 * and then adding demandDetails using the difference between the new and
	 * old tax amounts for each taxHead
	 * 
	 * @param tenantId
	 *            The tenantId of the property
	 * @param calculation
	 *            The calculation object for the property
	 * @param oldDemand
	 *            The oldDemand against the property
	 * @return List of DemanDetails for the new demand
	 */
	private List<DemandDetail> getAdjustedDemandDetails(String tenantId, Calculation calculation, Demand oldDemand) {

		List<DemandDetail> details = new ArrayList<>();

		/* Create map of taxHead to list of DemandDetail */

		Map<String, List<DemandDetail>> taxHeadCodeDetailMap = new LinkedHashMap<>();
		if (oldDemand != null) {
			for (DemandDetail detail : oldDemand.getDemandDetails()) {
				if (taxHeadCodeDetailMap.containsKey(detail.getTaxHeadMasterCode()))
					taxHeadCodeDetailMap.get(detail.getTaxHeadMasterCode()).add(detail);
				else {
					List<DemandDetail> detailList = new LinkedList<>();
					detailList.add(detail);
					taxHeadCodeDetailMap.put(detail.getTaxHeadMasterCode(), detailList);
				}
			}
		}

		for (TaxHeadEstimate estimate : calculation.getTaxHeadEstimates()) {

			List<DemandDetail> detailList = taxHeadCodeDetailMap.get(estimate.getTaxHeadCode());
			taxHeadCodeDetailMap.remove(estimate.getTaxHeadCode());

			if (estimate.getTaxHeadCode().equalsIgnoreCase(WSCalculationConstant.WS_ADVANCE_CARRYFORWARD))
				continue;

			if (!CollectionUtils.isEmpty(detailList)) {
				details.addAll(detailList);
				BigDecimal amount = detailList.stream().map(DemandDetail::getTaxAmount).reduce(BigDecimal.ZERO,
						BigDecimal::add);

				details.add(DemandDetail.builder().taxHeadMasterCode(estimate.getTaxHeadCode())
						.taxAmount(estimate.getEstimateAmount().subtract(amount)).collectionAmount(BigDecimal.ZERO)
						.tenantId(tenantId).build());
			} else {
				details.add(DemandDetail.builder().taxHeadMasterCode(estimate.getTaxHeadCode())
						.taxAmount(estimate.getEstimateAmount()).collectionAmount(BigDecimal.ZERO).tenantId(tenantId)
						.build());
			}
		}

		/*
		 * If some taxHeads are in old demand but not in new one a new
		 * demandetail is added for each taxhead to balance it out during
		 * apportioning
		 */

		for (Map.Entry<String, List<DemandDetail>> entry : taxHeadCodeDetailMap.entrySet()) {
			List<DemandDetail> demandDetails = entry.getValue();
			BigDecimal taxAmount = demandDetails.stream().map(DemandDetail::getTaxAmount).reduce(BigDecimal.ZERO,
					BigDecimal::add);
			BigDecimal collectionAmount = demandDetails.stream().map(DemandDetail::getCollectionAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal netAmount = collectionAmount.subtract(taxAmount);
			details.add(DemandDetail.builder().taxHeadMasterCode(entry.getKey()).taxAmount(netAmount)
					.collectionAmount(BigDecimal.ZERO).tenantId(tenantId).build());
		}

		return details;
	}

	/**
	 * Generates and returns bill from billing service
	 * 
	 * updates the demand with penalty and rebate if applicable before
	 * generating bill
	 * 
	 * @param getBillCriteria
	 * @param requestInfoWrapper
	 */
	public BillResponse getBill(GetBillCriteria getBillCriteria, RequestInfoWrapper requestInfoWrapper) {

		DemandResponse res = updateDemands(getBillCriteria, requestInfoWrapper);

		/**
		 * Loop through the demands and call generateBill for each demand. Group
		 * the Bills and return the bill response
		 */
		List<Bill> bills = new LinkedList<>();
		BillResponse billResponse;
		ResponseInfo responseInfo = null;
		StringBuilder billGenUrl;

		for (Demand demand : res.getDemands()) {
			billGenUrl = utils.getBillGenUrl(getBillCriteria.getTenantId(), demand.getId(), demand.getConsumerCode());
			billResponse = mapper.convertValue(repository.fetchResult(billGenUrl, requestInfoWrapper),
					BillResponse.class);
			responseInfo = billResponse.getResposneInfo();
			bills.addAll(billResponse.getBill());
		}

		return BillResponse.builder().resposneInfo(responseInfo).bill(bills).build();
	}

	/**
	 * Method updates the demands based on the getBillCriteria
	 * 
	 * The response will be the list of demands updated for the 
	 * @param getBillCriteria
	 * @param requestInfoWrapper
	 * @return
	 */
	public DemandResponse updateDemands(GetBillCriteria getBillCriteria, RequestInfoWrapper requestInfoWrapper) {
		
		if(getBillCriteria.getAmountExpected() == null) getBillCriteria.setAmountExpected(BigDecimal.ZERO);
		RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();
		Map<String, Map<String, List<Object>>> propertyBasedExemptionMasterMap = new HashMap<>();
		Map<String, JSONArray> timeBasedExmeptionMasterMap = new HashMap<>();
		mstrDataService.setPropertyMasterValues(requestInfo, getBillCriteria.getTenantId(),
				propertyBasedExemptionMasterMap, timeBasedExmeptionMasterMap);

		if(CollectionUtils.isEmpty(getBillCriteria.getConsumerCodes()))
			getBillCriteria.setConsumerCodes(Collections.singletonList(getBillCriteria.getPropertyId()+ WSCalculationConstant.WS_CONSUMER_CODE_SEPARATOR +getBillCriteria.getAssessmentNumber()));

		DemandResponse res = mapper.convertValue(
				repository.fetchResult(utils.getDemandSearchUrl(getBillCriteria), requestInfoWrapper),
				DemandResponse.class);
		if (CollectionUtils.isEmpty(res.getDemands())) {
			Map<String, String> map = new HashMap<>();
			map.put(WSCalculationConstant.EMPTY_DEMAND_ERROR_CODE, WSCalculationConstant.EMPTY_DEMAND_ERROR_MESSAGE);
			throw new CustomException(map);
		}
		

		/**
		 * Loop through the consumerCodes and re-calculate the time based applicables
		 */

		Map<String,Demand> consumerCodeToDemandMap = res.getDemands().stream()
				.collect(Collectors.toMap(Demand::getConsumerCode,Function.identity()));

		List<Demand> demandsToBeUpdated = new LinkedList<>();

		String tenantId = getBillCriteria.getTenantId();

		List<TaxPeriod> taxPeriods = mstrDataService.getTaxPeriodList(requestInfoWrapper.getRequestInfo(), tenantId);

		for (String consumerCode : getBillCriteria.getConsumerCodes()) {
			Demand demand = consumerCodeToDemandMap.get(consumerCode);
			if (demand == null)
				throw new CustomException(WSCalculationConstant.EMPTY_DEMAND_ERROR_CODE,
						"No demand found for the consumerCode: " + consumerCode);

			if (demand.getStatus() != null
					&& WSCalculationConstant.DEMAND_CANCELLED_STATUS.equalsIgnoreCase(demand.getStatus().toString()))
				throw new CustomException(WSCalculationConstant.EG_WS_INVALID_DEMAND_ERROR,
						WSCalculationConstant.EG_WS_INVALID_DEMAND_ERROR_MSG);

		
			roundOffDecimalForDemand(demand, requestInfoWrapper);

			demandsToBeUpdated.add(demand);

		}


		/**
		 * Call demand update in bulk to update the interest or penalty
		 */
		DemandRequest request = DemandRequest.builder().demands(demandsToBeUpdated).requestInfo(requestInfo).build();
		StringBuilder updateDemandUrl = utils.getUpdateDemandUrl();
		repository.fetchResult(updateDemandUrl, request);
		return res;



	}
	
	
	/**
	 * 
	 * Balances the decimal values in the newly updated demand by performing a roundoff
	 * 
	 * @param demand
	 * @param requestInfoWrapper
	 */
	public void roundOffDecimalForDemand(Demand demand, RequestInfoWrapper requestInfoWrapper) {
		
		List<DemandDetail> details = demand.getDemandDetails();
		String tenantId = demand.getTenantId();
		String demandId = demand.getId();

		BigDecimal taxAmount = BigDecimal.ZERO;

		// Collecting the taxHead master codes with the isDebit field in a Map
		Map<String, Boolean> isTaxHeadDebitMap = mstrDataService.getTaxHeadMasterMap(requestInfoWrapper.getRequestInfo(), tenantId).stream()
				.collect(Collectors.toMap(TaxHeadMaster::getCode, TaxHeadMaster::getIsDebit));

		/*
		 * Summing the credit amount and Debit amount in to separate variables(based on the taxhead:isdebit map) to send to roundoffDecimal method
		 */
		for (DemandDetail detail : demand.getDemandDetails()) {
				taxAmount = taxAmount.add(detail.getTaxAmount());
		}

		/*
		 *  An estimate object will be returned incase if there is a decimal value
		 *  
		 *  If no decimal value found null object will be returned 
		 */
		TaxHeadEstimate roundOffEstimate = payService.roundOfDecimals(taxAmount);



		BigDecimal decimalRoundOff = null != roundOffEstimate
				? roundOffEstimate.getEstimateAmount() : BigDecimal.ZERO;

		if(decimalRoundOff.compareTo(BigDecimal.ZERO)!=0){
				details.add(DemandDetail.builder().taxAmount(roundOffEstimate.getEstimateAmount())
						.taxHeadMasterCode(roundOffEstimate.getTaxHeadCode()).demandId(demandId).tenantId(tenantId).build());
		}


	}
	
	
	
}
