package org.egov.pt.calculator.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.pt.calculator.repository.Repository;
import org.egov.pt.calculator.util.CalculatorConstants;
import org.egov.pt.calculator.util.CalculatorUtils;
import org.egov.pt.calculator.util.Configurations;
import org.egov.pt.calculator.validator.CalculationValidator;
import org.egov.pt.calculator.web.models.*;
import org.egov.pt.calculator.web.models.demand.*;
import org.egov.pt.calculator.web.models.property.OwnerInfo;
import org.egov.pt.calculator.web.models.property.Property;
import org.egov.pt.calculator.web.models.property.PropertyDetail;
import org.egov.pt.calculator.web.models.property.RequestInfoWrapper;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

import static org.egov.pt.calculator.util.CalculatorConstants.*;

@Service
@Slf4j
public class DemandService {

	@Autowired
	private EstimationService estimationService;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Configurations configs;

	@Autowired
	private AssessmentService assessmentService;

	@Autowired
	private CalculatorUtils utils;

	@Autowired
	private Repository repository;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private PayService payService;

	@Autowired
	private MasterDataService mstrDataService;

	@Autowired
	private CalculationValidator validator;

	@Autowired
	private MasterDataService mDataService;

	@Autowired
    private PaymentService paymentService;

	/**
	 * Generates and persists the demand to billing service for the given property
	 * 
	 * if the property has been assessed already for the given financial year then
	 * 
	 * it carry forwards the old collection amount to the new demand as advance
	 * 
	 * @param request
	 * @return
	 */
	public Map<String, Calculation> calculateAndGenerateDemands(CalculationReq request, boolean generateDemand) {

		List<CalculationCriteria> criterias = request.getCalculationCriteria();
		List<Demand> demands = new ArrayList<>();
		List<String> lesserAssessments = new ArrayList<>();
		Map<String, String> consumerCodeFinYearMap = new HashMap<>();
		Map<String,Object> masterMap = mDataService.getMasterMap(request);


		Map<String, Calculation> propertyCalculationMap = estimationService.getEstimationPropertyMap(request,masterMap);
		
		if(generateDemand)
		for (CalculationCriteria criteria : criterias) {

			Property property = criteria.getProperty();

			PropertyDetail detail = property.getPropertyDetails().get(0);

			Calculation calculation = propertyCalculationMap.get(property.getPropertyDetails().get(0).getAssessmentNumber());
			
			String assessmentNumber = detail.getAssessmentNumber();
			Map<String, Object> finyearMap = (Map<String, Object>) masterMap.get(FINANCIALYEAR_MASTER_KEY);
			Map<String, Object> finYear = (Map<String, Object>) finyearMap.get(detail.getFinancialYear());
			Long startingDate = (Long) finYear.get("startingDate");
			Long endingDate = (Long) finYear.get("endingDate");
			criteria.setFromDate(startingDate);
			criteria.setToDate(endingDate);
			// pt_tax for the new assessment
			BigDecimal newTax =  BigDecimal.ZERO;
			Optional<TaxHeadEstimate> advanceCarryforwardEstimate = propertyCalculationMap.get(assessmentNumber).getTaxHeadEstimates()
			.stream().filter(estimate -> estimate.getTaxHeadCode().equalsIgnoreCase(CalculatorConstants.PT_TAX))
				.findAny();
			if(advanceCarryforwardEstimate.isPresent())
				newTax = advanceCarryforwardEstimate.get().getEstimateAmount();

			Demand oldDemand = utils.getLatestDemandForCurrentFinancialYear(request.getRequestInfo(),criteria);

			// true represents that the demand should be updated from this call
			BigDecimal carryForwardCollectedAmount = getCarryForwardAndCancelOldDemand(newTax, criteria,
					request.getRequestInfo(),oldDemand, true);

			if (carryForwardCollectedAmount.doubleValue() >= 0.0) {

				Demand demand = prepareDemand(property, calculation ,oldDemand);

				// Add billingSLabs in demand additionalDetails as map with key calculationDescription
				demand.setAdditionalDetails(Collections.singletonMap(BILLINGSLAB_KEY, calculation.getBillingSlabIds()));

				demands.add(demand);
				consumerCodeFinYearMap.put(demand.getConsumerCode(), detail.getFinancialYear());

			}else {
				lesserAssessments.add(assessmentNumber);
			}
		}
		
		if (!CollectionUtils.isEmpty(lesserAssessments)) {
			throw new CustomException(CalculatorConstants.EG_PT_DEPRECIATING_ASSESSMENT_ERROR,
					CalculatorConstants.EG_PT_DEPRECIATING_ASSESSMENT_ERROR_MSG + lesserAssessments);
		}
		if (generateDemand) {
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
		}
		return propertyCalculationMap;
	}

	/**
	 * Generates and returns bill from billing service
	 * 
	 * updates the demand with penalty and rebate if applicable before generating
	 * bill
	 * 
	 * @param getBillCriteria
	 * @param requestInfoWrapper
	 */
	public BillResponse getBill(GetBillCriteria getBillCriteria, RequestInfoWrapper requestInfoWrapper) {

		DemandResponse res = updateDemands(getBillCriteria, requestInfoWrapper);

		/**
		 * Loop through the demands and call generateBill for each demand.
		 * Group the Bills and return the bill responsew
		 */
		List<Bill> bills = new LinkedList<>();
		BillResponse billResponse;
		ResponseInfo responseInfo = null;
		StringBuilder billGenUrl;

		Set<String> consumerCodes = res.getDemands().stream().map(Demand::getConsumerCode).collect(Collectors.toSet());

		// If toDate or fromDate is not given bill is generated across all taxPeriod for the given consumerCode
		if(getBillCriteria.getToDate()==null || getBillCriteria.getFromDate()==null){
			for(String consumerCode : consumerCodes){
				billGenUrl = utils.getBillGenUrl(getBillCriteria.getTenantId(), consumerCode);
				billResponse = mapper.convertValue(repository.fetchResult(billGenUrl, requestInfoWrapper), BillResponse.class);
				responseInfo = billResponse.getResposneInfo();
				bills.addAll(billResponse.getBill());
			}
		}
		// else if toDate and fromDate is given bill is generated for the taxPeriod corresponding to given dates for the given consumerCode
		else {
			for(Demand demand : res.getDemands()){
				billGenUrl = utils.getBillGenUrl(getBillCriteria.getTenantId(),demand.getId(),demand.getConsumerCode());
				billResponse = mapper.convertValue(repository.fetchResult(billGenUrl, requestInfoWrapper), BillResponse.class);
				responseInfo = billResponse.getResposneInfo();
				bills.addAll(billResponse.getBill());
			}
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
		
		if(getBillCriteria.getAmountExpected() == null) 
			getBillCriteria.setAmountExpected(BigDecimal.ZERO);
		validator.validateGetBillCriteria(getBillCriteria);
		RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();
		Map<String,Map<String, List<Object>>> masterMap = new HashMap<>();
		Map<String, JSONArray> jsonMasterMap = new HashMap<>();
		mstrDataService.setPropertyMasterValues(requestInfo, getBillCriteria.getTenantId(),
		masterMap, jsonMasterMap);

		/*if(CollectionUtils.isEmpty(getBillCriteria.getConsumerCodes()))
			getBillCriteria.setConsumerCodes(Collections.singletonList(getBillCriteria.getPropertyId()+ PT_CONSUMER_CODE_SEPARATOR +getBillCriteria.getAssessmentNumber()));
*/
		DemandResponse res = mapper.convertValue(
				repository.fetchResult(utils.getDemandSearchUrl(getBillCriteria), requestInfoWrapper),
				DemandResponse.class);
		if (CollectionUtils.isEmpty(res.getDemands())) {
			Map<String, String> map = new HashMap<>();
			map.put(EMPTY_DEMAND_ERROR_CODE, EMPTY_DEMAND_ERROR_MESSAGE);
			throw new CustomException(map);
		}


		/**
		 * Loop through the consumerCodes and re-calculate the time based applicables
		 */

		// Map<String,Demand> consumerCodeToDemandMap = res.getDemands().stream()
		// 		.collect(Collectors.toMap(Demand::getConsumerCode,Function.identity()));

		List<Demand> demandsToBeUpdated = new LinkedList<>();

		String tenantId = getBillCriteria.getTenantId();

		List<TaxPeriod> taxPeriods = mstrDataService.getTaxPeriodList(requestInfoWrapper.getRequestInfo(), tenantId);

		for (Demand demand : res.getDemands()) {
			//Demand demand = consumerCodeToDemandMap.get(consumerCode);
			// if (demand == null)
			// 	throw new CustomException(EMPTY_DEMAND_ERROR_CODE,
			// 			"No demand found for the consumerCode: " + consumerCode);

			if (demand.getStatus() != null
					&& DEMAND_CANCELLED_STATUS.equalsIgnoreCase(demand.getStatus().toString()))
				throw new CustomException(EG_PT_INVALID_DEMAND_ERROR,
						EG_PT_INVALID_DEMAND_ERROR_MSG);

			applytimeBasedApplicables(demand, requestInfoWrapper, taxPeriods, jsonMasterMap);

			roundOffDecimalForDemand(demand, requestInfoWrapper);

			demandsToBeUpdated.add(demand);

		}


		/**
		 * Call demand update in bulk to update the interest or penalty
		 */
		
		  DemandRequest request =DemandRequest.builder().demands(demandsToBeUpdated).requestInfo(requestInfo).build(); 
		  StringBuilder updateDemandUrl = utils.getUpdateDemandUrl(); 
		  repository.fetchResult(updateDemandUrl,request);
		
		return res;
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
	protected BigDecimal getCarryForwardAndCancelOldDemand(BigDecimal newTax, CalculationCriteria criteria, RequestInfo requestInfo
			, Demand demand,boolean cancelDemand) {

		Property property = criteria.getProperty();

		BigDecimal carryForward = BigDecimal.ZERO;
		BigDecimal oldTaxAmt = BigDecimal.ZERO;

		if(null == property.getPropertyId()) return carryForward;

	    // Demand demand = getLatestDemandForCurrentFinancialYear(requestInfo, property);
		
		if(null == demand) return carryForward;

		carryForward = utils.getTotalCollectedAmountAndPreviousCarryForward(demand);
		
		for (DemandDetail detail : demand.getDemandDetails()) {
			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(CalculatorConstants.PT_TAX))
				oldTaxAmt = oldTaxAmt.add(detail.getTaxAmount());
		}			

		log.debug("The old tax amount in string : " + oldTaxAmt.toPlainString());
		log.debug("The new tax amount in string : " + newTax.toPlainString());
		
		if (oldTaxAmt.compareTo(newTax) > 0) {
			boolean isDepreciationAllowed = utils.isAssessmentDepreciationAllowed(demand,new RequestInfoWrapper(requestInfo));
			if (!isDepreciationAllowed)
				carryForward = BigDecimal.valueOf(-1);
		}

		if (BigDecimal.ZERO.compareTo(carryForward) > 0 || !cancelDemand) return carryForward;
		
		demand.setStatus(Demand.StatusEnum.CANCELLED);
		DemandRequest request = DemandRequest.builder().demands(Arrays.asList(demand)).requestInfo(requestInfo).build();
		StringBuilder updateDemandUrl = utils.getUpdateDemandUrl();
		repository.fetchResult(updateDemandUrl, request);

		return carryForward;
	}

	public Demand getLatestDemandForCurrentFinancialYear(RequestInfo requestInfo, Property property) {

		Assessment assessment = Assessment.builder().propertyId(property.getPropertyId())
				.tenantId(property.getTenantId())
				.assessmentYear(property.getPropertyDetails().get(0).getFinancialYear()).build();

		List<Assessment> assessments = assessmentService.getMaxAssessment(assessment);

		if (CollectionUtils.isEmpty(assessments))
			return null;

		Assessment latestAssessment = assessments.get(0);
		log.debug(" the latest assessment : " + latestAssessment);

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
	 * Prepares Demand object based on the incoming calculation object and property
	 * 
	 * @param property
	 * @param calculation
	 * @return
	 */
	private Demand prepareDemand(Property property, Calculation calculation,Demand demand) {

		String tenantId = property.getTenantId();
		PropertyDetail detail = property.getPropertyDetails().get(0);
		String propertyType = detail.getPropertyType();
		String consumerCode = property.getPropertyId();
		OwnerInfo owner = null;
		if (null != detail.getCitizenInfo())
			owner = detail.getCitizenInfo();
		else
			owner = detail.getOwners().iterator().next();
		
	   // Demand demand = utils.getLatestDemandForCurrentFinancialYear(requestInfo, property);

		List<DemandDetail> details = new ArrayList<>();

		details = getAdjustedDemandDetails(tenantId,calculation,demand);

		return Demand.builder().tenantId(tenantId).businessService(configs.getPtModuleCode()).consumerType(propertyType)
				.consumerCode(consumerCode).payer(owner.toCommonUser()).taxPeriodFrom(calculation.getFromDate())
				.taxPeriodTo(calculation.getToDate()).status(Demand.StatusEnum.ACTIVE)
				.minimumAmountPayable(BigDecimal.valueOf(configs.getPtMinAmountPayable())).demandDetails(details)
				.build();
	}

	/**
	 * Applies Penalty/Rebate/Interest to the incoming demands
	 * 
	 * If applied already then the demand details will be updated
	 * 
	 * @param demand
	 * @return
	 */
	private boolean applytimeBasedApplicables(Demand demand, RequestInfoWrapper requestInfoWrapper,
			List<TaxPeriod> taxPeriods, Map<String, JSONArray> jsonMasterMap) {

		boolean isCurrentDemand = false;
		boolean isInterestUpdated = false;
		String tenantId = demand.getTenantId();
		String demandId = demand.getId();
		List<DemandDetail> details = demand.getDemandDetails();
		TaxPeriod taxPeriod = taxPeriods.stream().filter(t -> demand.getTaxPeriodFrom().compareTo(t.getFromDate()) >= 0
				&& demand.getTaxPeriodTo().compareTo(t.getToDate()) <= 0).findAny().orElse(null);

		if (!(taxPeriod.getFromDate() <= System.currentTimeMillis()
				&& taxPeriod.getToDate() >= System.currentTimeMillis())) {
			isCurrentDemand = true;
		}

		/**
		 * Commenting the below as Partial payment is disabled and sending null to
		 * applyPenaltyRebateAndInterest get the payments done against this demand
		 * List<Payment> payments = paymentService.getPaymentsFromDemand(demand,
		 * requestInfoWrapper);
		 */
		Map<String, BigDecimal> rebatePenaltyEstimates = payService.applyPenaltyRebateAndInterest(demand, null,
				taxPeriods, jsonMasterMap);

		if (null == rebatePenaltyEstimates)
			return isCurrentDemand;

		BigDecimal rebate = rebatePenaltyEstimates.get(PT_TIME_REBATE);
		BigDecimal promotionalRebate = rebatePenaltyEstimates.get(PT_PROMOTIONAL_REBATE);
		BigDecimal interest = rebatePenaltyEstimates.get(PT_TIME_INTEREST);
		DemandDetailAndCollection latestInterestDemandDetail;

		BigDecimal oldRebate = null;
		for (DemandDetail demandDetail : details) {
			if (demandDetail.getTaxHeadMasterCode().equalsIgnoreCase(PT_TIME_REBATE)) {
				oldRebate = demandDetail.getTaxAmount();
			}
		}
		if (rebate != null) {
			if (oldRebate == null) {
				details.add(DemandDetail.builder().taxAmount(rebate).taxHeadMasterCode(PT_TIME_REBATE)
						.demandId(demandId).tenantId(tenantId).build());
			} else if (rebate.compareTo(oldRebate) != 0) {
				utils.getLatestDemandDetailByTaxHead(PT_TIME_REBATE, details).getLatestDemandDetail()
						.setTaxAmount(rebate);
			}
		}

		BigDecimal oldPromotionalRebate = null;
		for (DemandDetail demandDetail : details) {
			if (demandDetail.getTaxHeadMasterCode().equalsIgnoreCase(PT_PROMOTIONAL_REBATE)) {
				oldPromotionalRebate = demandDetail.getTaxAmount();
			}
		}
		if (promotionalRebate != null) {
			if (oldPromotionalRebate == null) {
				details.add(DemandDetail.builder().taxAmount(promotionalRebate).taxHeadMasterCode(PT_PROMOTIONAL_REBATE)
						.demandId(demandId).tenantId(tenantId).build());
			} else if (promotionalRebate.compareTo(oldPromotionalRebate) != 0) {
				utils.getLatestDemandDetailByTaxHead(PT_PROMOTIONAL_REBATE, details).getLatestDemandDetail()
						.setTaxAmount(promotionalRebate);
			}
		}

		if (interest != null) {
			latestInterestDemandDetail = utils.getLatestDemandDetailByTaxHead(PT_TIME_INTEREST, details);
			if (latestInterestDemandDetail != null) {
				updateTaxAmount(interest, latestInterestDemandDetail);
				isInterestUpdated = true;
			}

			if (!isInterestUpdated)
				details.add(DemandDetail.builder().taxAmount(interest).taxHeadMasterCode(PT_TIME_INTEREST)
						.demandId(demandId).tenantId(tenantId).build());
		}
		return isCurrentDemand;
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
		BigDecimal currentRoundOff = null;

		for (DemandDetail detail : demand.getDemandDetails()) {

			log.info("Tax Amount of " + detail.getTaxHeadMasterCode() + " is : " + detail.getTaxAmount() );
			log.info("Collection Amount of " + detail.getTaxHeadMasterCode() + " is : " + detail.getCollectionAmount() );
			if (!detail.getTaxHeadMasterCode().equalsIgnoreCase(PT_ROUNDOFF)) {
				taxAmount = taxAmount.add(detail.getTaxAmount().subtract(detail.getCollectionAmount()));
			} else {
				currentRoundOff = detail.getTaxAmount().subtract(detail.getCollectionAmount());
			}
		}
		log.info("Tax Amount of Demand : " + taxAmount );
		log.info("Current RoundOff Amount of Demand : " + currentRoundOff );
		/*
		 * An estimate object will be returned incase if there is a decimal value If no
		 * decimal value found null object will be returned
		 * 
		 * 
		 */
		if(taxAmount.compareTo(BigDecimal.ZERO) != 0){
		TaxHeadEstimate roundOffEstimate = payService.roundOffDecimals(taxAmount, currentRoundOff);
		log.info("Tax Head Estimate of Demand : " + roundOffEstimate );

		if (roundOffEstimate != null) {
			if (currentRoundOff == null) {
				details.add(DemandDetail.builder().taxAmount(roundOffEstimate.getEstimateAmount())
						.taxHeadMasterCode(roundOffEstimate.getTaxHeadCode()).demandId(demandId).tenantId(tenantId)
						.build());
			} else {
				utils.getLatestDemandDetailByTaxHead(PT_ROUNDOFF, details).getLatestDemandDetail()
						.setTaxAmount(roundOffEstimate.getEstimateAmount());
			}
		}
		}else if (currentRoundOff != null && currentRoundOff.compareTo(BigDecimal.ZERO) != 0 && !CollectionUtils.isEmpty(details.stream().filter(dtl -> dtl.getTaxHeadMasterCode().equals(PT_ROUNDOFF)).collect(Collectors.toList())))
		{
				utils.getLatestDemandDetailByTaxHead(PT_ROUNDOFF, details).getLatestDemandDetail()
						.setTaxAmount(BigDecimal.ZERO);
		}

	}

	/**
	 * Creates demandDetails for the new demand by adding all old demandDetails and then adding demandDetails
	 * using the difference between the new and old tax amounts for each taxHead
	 * @param tenantId The tenantId of the property
	 * @param calculation The calculation object for the property
	 * @param oldDemand The oldDemand against the property
	 * @return List of DemanDetails for the new demand
	 */
	private List<DemandDetail> getAdjustedDemandDetails(String tenantId,Calculation calculation,Demand oldDemand){

		List<DemandDetail> details = new ArrayList<>();

		/*Create map of taxHead to list of DemandDetail*/

		Map<String, List<DemandDetail>> taxHeadCodeDetailMap = new LinkedHashMap<>();
		if(oldDemand!=null){
			for(DemandDetail detail : oldDemand.getDemandDetails()){
				if(taxHeadCodeDetailMap.containsKey(detail.getTaxHeadMasterCode()))
					taxHeadCodeDetailMap.get(detail.getTaxHeadMasterCode()).add(detail);
				else {
					List<DemandDetail> detailList  = new LinkedList<>();
					detailList.add(detail);
					taxHeadCodeDetailMap.put(detail.getTaxHeadMasterCode(),detailList);
				}
			}
		}

		for (TaxHeadEstimate estimate : calculation.getTaxHeadEstimates()) {

			List<DemandDetail> detailList = taxHeadCodeDetailMap.get(estimate.getTaxHeadCode());
			taxHeadCodeDetailMap.remove(estimate.getTaxHeadCode());

			if (estimate.getTaxHeadCode().equalsIgnoreCase(CalculatorConstants.PT_ADVANCE_CARRYFORWARD))
				continue;

			if(!CollectionUtils.isEmpty(detailList)){
				details.addAll(detailList);
				BigDecimal amount= detailList.stream().map(DemandDetail::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

				details.add(DemandDetail.builder().taxHeadMasterCode(estimate.getTaxHeadCode())
						.taxAmount(estimate.getEstimateAmount().subtract(amount))
						.collectionAmount(BigDecimal.ZERO)
						.tenantId(tenantId).build());
			}
			else{
				details.add(DemandDetail.builder().taxHeadMasterCode(estimate.getTaxHeadCode())
						.taxAmount(estimate.getEstimateAmount())
						.collectionAmount(BigDecimal.ZERO)
						.tenantId(tenantId).build());
			}
		}

		/*
		* If some taxHeads are in old demand but not in new one a new demandetail
		*  is added for each taxhead to balance it out during apportioning
		* */

		for(Map.Entry<String, List<DemandDetail>> entry : taxHeadCodeDetailMap.entrySet()){
			List<DemandDetail> demandDetails = entry.getValue();
			BigDecimal taxAmount= demandDetails.stream().map(DemandDetail::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal collectionAmount= demandDetails.stream().map(DemandDetail::getCollectionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal netAmount = collectionAmount.subtract(taxAmount);
			details.add(DemandDetail.builder().taxHeadMasterCode(entry.getKey())
					.taxAmount(netAmount)
					.collectionAmount(BigDecimal.ZERO)
					.tenantId(tenantId).build());
		}

		return details;
	}

	/**
	 * Updates the amount in the latest demandDetail by adding the diff between
	 * new and old amounts to it
	 * @param newAmount The new tax amount for the taxHead
	 * @param latestDetailInfo The latest demandDetail for the particular taxHead
	 */
	private void updateTaxAmount(BigDecimal newAmount,DemandDetailAndCollection latestDetailInfo){
		BigDecimal diff = newAmount.subtract(latestDetailInfo.getTaxAmountForTaxHead());
		BigDecimal newTaxAmountForLatestDemandDetail = latestDetailInfo.getLatestDemandDetail().getTaxAmount().add(diff);
		latestDetailInfo.getLatestDemandDetail().setTaxAmount(newTaxAmountForLatestDemandDetail);
	}
	
	public DemandResponse createPTDemands(List<Demand> demands, RequestInfo requestInfo) {

		DemandRequest demandReq = DemandRequest.builder().demands(demands).requestInfo(requestInfo).build();
		String url = new StringBuilder().append(configs.getBillingServiceHost())
				.append(configs.getDemandCreateEndPoint()).toString();
		DemandResponse res = new DemandResponse();

		try {
			res = restTemplate.postForObject(url, demandReq, DemandResponse.class);

		} catch (HttpClientErrorException e) {
			throw new ServiceCallException(e.getResponseBodyAsString());
		}
		return res;
	}

	public DemandResponse updatePTDemands(List<Demand> demands, RequestInfo requestInfo) {
		DemandRequest demandReq = DemandRequest.builder().demands(demands).requestInfo(requestInfo).build();
		String url = new StringBuilder().append(configs.getBillingServiceHost())
				.append(configs.getDemandUpdateEndPoint()).toString();
		DemandResponse res = new DemandResponse();

		try {
			res = restTemplate.postForObject(url, demandReq, DemandResponse.class);

		} catch (HttpClientErrorException e) {
			throw new ServiceCallException(e.getResponseBodyAsString());
		}
		return res;
	}

}
