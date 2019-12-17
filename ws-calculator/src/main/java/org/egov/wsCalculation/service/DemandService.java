package org.egov.wsCalculation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.Assessment;
import org.egov.wsCalculation.model.Calculation;
import org.egov.wsCalculation.model.Demand;
import org.egov.wsCalculation.model.Demand.StatusEnum;
import org.egov.wsCalculation.model.DemandDetail;
import org.egov.wsCalculation.model.DemandDetailAndCollection;
import org.egov.wsCalculation.model.DemandRequest;
import org.egov.wsCalculation.model.DemandResponse;
import org.egov.wsCalculation.model.GetBillCriteria;
import org.egov.wsCalculation.model.RequestInfoWrapper;
import org.egov.wsCalculation.model.TaxHeadEstimate;
import org.egov.wsCalculation.model.TaxPeriod;
import org.egov.wsCalculation.model.WaterConnection;
import org.egov.wsCalculation.repository.DemandRepository;
import org.egov.wsCalculation.repository.ServiceRequestRepository;
import org.egov.wsCalculation.util.WSCalculationUtil;
import org.egov.wsCalculation.config.WSCalculationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class DemandService {

	@Autowired
	private ServiceRequestRepository repository;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private PayService payService;

	@Autowired
	private MasterDataService mstrDataService;

	@Autowired
	private WSCalculationUtil utils;

	@Autowired
	private WSCalculationConfiguration configs;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private DemandRepository demandRepository;


	/**
	 * Creates or updates Demand
	 * 
	 * @param requestInfo
	 *            The RequestInfo of the calculation request
	 * @param calculations
	 *            The Calculation Objects for which demand has to be generated
	 *            or updated
	 */
	public List<Demand> generateDemand(RequestInfo requestInfo, List<Calculation> calculations,
			Map<String, Object> masterMap) {
		List<Demand> createdDemands = new ArrayList<>();
		// List that will contain Calculation for new demands
		List<Calculation> createCalculations = new LinkedList<>();

		
		@SuppressWarnings("unchecked")
		Map<String, Object> financialYearMaster =  (Map<String, Object>) masterMap
				.get(WSCalculationConstant.BillingPeriod);
		Long fromDate = (Long) financialYearMaster.get(WSCalculationConstant.STARTING_DATE_APPLICABLES);
		Long toDate = (Long) financialYearMaster.get(WSCalculationConstant.ENDING_DATE_APPLICABLES);
		
		// List that will contain Calculation for old demands
		List<Calculation> updateCalculations = new LinkedList<>();

		if (!CollectionUtils.isEmpty(calculations)) {
			// Collect required parameters for demand search
			String tenantId = calculations.get(0).getTenantId();
			Set<String> consumerCodes = calculations.stream().map(calculation -> calculation.getConnectionNo())
					.collect(Collectors.toSet());
			List<Demand> demands = searchDemand(tenantId, consumerCodes, fromDate, toDate, requestInfo);
			Set<String> connectionNumbersFromDemands = new HashSet<>();
			if (!CollectionUtils.isEmpty(demands))
				connectionNumbersFromDemands = demands.stream().map(Demand::getConsumerCode)
						.collect(Collectors.toSet());

			// If demand already exists add it updateCalculations else
			// createCalculations
			for (Calculation calculation : calculations) {
				if (!connectionNumbersFromDemands.contains(calculation.getConnectionNo()))
					createCalculations.add(calculation);
				else
					updateCalculations.add(calculation);
			}
		}

		if (!CollectionUtils.isEmpty(createCalculations))
			createdDemands = createDemand(requestInfo, createCalculations, masterMap);

		if (!CollectionUtils.isEmpty(updateCalculations))
			createdDemands = updateDemandForCalculation(requestInfo, updateCalculations, fromDate, toDate);
		return createdDemands;
	}
	
	/**
	 * 
	 * @param requestInfo
	 * @param calculations
	 * @param masterMap
	 * @return
	 */
	
	private List<Demand> createDemand(RequestInfo requestInfo, List<Calculation> calculations,
			Map<String, Object> masterMap) {
		List<Demand> demands = new LinkedList<>();
		for (Calculation calculation : calculations) {
			WaterConnection connection = null;

			if (calculation.getWaterConnection() != null)
				connection = calculation.getWaterConnection();

			// else if (calculation != null)
			// connection = utils.getConnection(requestInfo,
			// calculation.getWaterConnection().getApplicationNo(),
			// calculation.getTenantId());

			if (connection == null)
				throw new CustomException("INVALID CONNECTION NUMBER",
						"Demand cannot be generated for connectionNumber "
								+ calculation.getConnectionNo()
								+ " Water Connection with this number does not exist ");

			String tenantId = calculation.getTenantId();
			String consumerCode = calculation.getConnectionNo();
			User owner = connection.getProperty().getOwners().get(0).toCommonUser();
			
			List<DemandDetail> demandDetails = new LinkedList<>();

			calculation.getTaxHeadEstimates().forEach(taxHeadEstimate -> {
				demandDetails.add(DemandDetail.builder().taxAmount(taxHeadEstimate.getEstimateAmount())
						.taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode()).collectionAmount(BigDecimal.ZERO)
						.tenantId(tenantId).build());
			});
			
			@SuppressWarnings("unchecked")
			Map<String, Object> financialYearMaster =  (Map<String, Object>) masterMap
					.get(WSCalculationConstant.BillingPeriod);

			Long fromDate = (Long) financialYearMaster.get(WSCalculationConstant.STARTING_DATE_APPLICABLES);
			Long toDate = (Long) financialYearMaster.get(WSCalculationConstant.ENDING_DATE_APPLICABLES);
			Long expiryDate = (Long) financialYearMaster.get(WSCalculationConstant.Demand_Expiry_Date_String);
			
		
//			@SuppressWarnings("unchecked")
//			Map<String, Map<String, Object>> financialYearMaster = (Map<String, Map<String, Object>>) masterMap
//					.get(WSCalculationConstant.FINANCIALYEAR_MASTER_KEY);
//
//			Map<String, Object> finYearMap = financialYearMaster.get(assessmentYear);
//			Long fromDate = (Long) finYearMap.get(WSCalculationConstant.FINANCIAL_YEAR_STARTING_DATE);
//			Long toDate = (Long) finYearMap.get(WSCalculationConstant.FINANCIAL_YEAR_ENDING_DATE);
//			Long billExpiryTime = System.currentTimeMillis() + configs.getDemandBillExpiryTime();

			addRoundOffTaxHead(calculation.getTenantId(), demandDetails);

			demands.add(Demand.builder().consumerCode(consumerCode).demandDetails(demandDetails).payer(owner)
					.minimumAmountPayable(configs.getMinimumPayableAmount()).tenantId(tenantId).taxPeriodFrom(fromDate)
					.taxPeriodTo(toDate).consumerType("waterConnection").businessService(configs.getBusinessService())
					.status(StatusEnum.valueOf("ACTIVE")).billExpiryTime(expiryDate).build());
		}
		log.info("Demand Object" + demands.toString());
		return demandRepository.saveDemand(requestInfo, demands);
	}

	/**
	 * Returns the list of new DemandDetail to be added for updating the demand
	 * 
	 * @param calculation
	 *            The calculation object for the update request
	 * @param demandDetails
	 *            The list of demandDetails from the existing demand
	 * @return The list of new DemandDetails
	 */
	private List<DemandDetail> getUpdatedDemandDetails(Calculation calculation, List<DemandDetail> demandDetails) {

		List<DemandDetail> newDemandDetails = new ArrayList<>();
		Map<String, List<DemandDetail>> taxHeadToDemandDetail = new HashMap<>();

		demandDetails.forEach(demandDetail -> {
			if (!taxHeadToDemandDetail.containsKey(demandDetail.getTaxHeadMasterCode())) {
				List<DemandDetail> demandDetailList = new LinkedList<>();
				demandDetailList.add(demandDetail);
				taxHeadToDemandDetail.put(demandDetail.getTaxHeadMasterCode(), demandDetailList);
			} else
				taxHeadToDemandDetail.get(demandDetail.getTaxHeadMasterCode()).add(demandDetail);
		});

		BigDecimal diffInTaxAmount;
		List<DemandDetail> demandDetailList;
		BigDecimal total;

		for (TaxHeadEstimate taxHeadEstimate : calculation.getTaxHeadEstimates()) {
			if (!taxHeadToDemandDetail.containsKey(taxHeadEstimate.getTaxHeadCode()))
				newDemandDetails.add(DemandDetail.builder().taxAmount(taxHeadEstimate.getEstimateAmount())
						.taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode()).tenantId(calculation.getTenantId())
						.collectionAmount(BigDecimal.ZERO).build());
			else {
				demandDetailList = taxHeadToDemandDetail.get(taxHeadEstimate.getTaxHeadCode());
				total = demandDetailList.stream().map(DemandDetail::getTaxAmount).reduce(BigDecimal.ZERO,
						BigDecimal::add);
				diffInTaxAmount = taxHeadEstimate.getEstimateAmount().subtract(total);
				if (diffInTaxAmount.compareTo(BigDecimal.ZERO) != 0) {
					newDemandDetails.add(DemandDetail.builder().taxAmount(diffInTaxAmount)
							.taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode()).tenantId(calculation.getTenantId())
							.collectionAmount(BigDecimal.ZERO).build());
				}
			}
		}
		List<DemandDetail> combinedBillDetials = new LinkedList<>(demandDetails);
		combinedBillDetials.addAll(newDemandDetails);
		addRoundOffTaxHead(calculation.getTenantId(), combinedBillDetials);
		return combinedBillDetials;
	}

	/**
	 * Adds roundOff taxHead if decimal values exists
	 * 
	 * @param tenantId
	 *            The tenantId of the demand
	 * @param demandDetails
	 *            The list of demandDetail
	 */
	private void addRoundOffTaxHead(String tenantId, List<DemandDetail> demandDetails) {
		BigDecimal totalTax = BigDecimal.ZERO;

		DemandDetail prevRoundOffDemandDetail = null;

		/*
		 * Sum all taxHeads except RoundOff as new roundOff will be calculated
		 */
		for (DemandDetail demandDetail : demandDetails) {
			if (!demandDetail.getTaxHeadMasterCode().equalsIgnoreCase(WSCalculationConstant.WS_Round_Off))
				totalTax = totalTax.add(demandDetail.getTaxAmount());
			else
				prevRoundOffDemandDetail = demandDetail;
		}

		BigDecimal decimalValue = totalTax.remainder(BigDecimal.ONE);
		BigDecimal midVal = new BigDecimal(0.5);
		BigDecimal roundOff = BigDecimal.ZERO;

		/*
		 * If the decimal amount is greater than 0.5 we subtract it from 1 and
		 * put it as roundOff taxHead so as to nullify the decimal eg: If the
		 * tax is 12.64 we will add extra tax roundOff taxHead of 0.36 so that
		 * the total becomes 13
		 */
		if (decimalValue.compareTo(midVal) > 0)
			roundOff = BigDecimal.ONE.subtract(decimalValue);

		/*
		 * If the decimal amount is less than 0.5 we put negative of it as
		 * roundOff taxHead so as to nullify the decimal eg: If the tax is 12.36
		 * we will add extra tax roundOff taxHead of -0.36 so that the total
		 * becomes 12
		 */
		if (decimalValue.compareTo(midVal) < 0)
			roundOff = decimalValue.negate();

		/*
		 * If roundOff already exists in previous demand create a new roundOff
		 * taxHead with roundOff amount equal to difference between them so that
		 * it will be balanced when bill is generated. eg: If the previous
		 * roundOff amount was of -0.36 and the new roundOff excluding the
		 * previous roundOff is 0.2 then the new roundOff will be created with
		 * 0.2 so that the net roundOff will be 0.2 -(-0.36)
		 */
		if (prevRoundOffDemandDetail != null) {
			roundOff = roundOff.subtract(prevRoundOffDemandDetail.getTaxAmount());
		}

		if (roundOff.compareTo(BigDecimal.ZERO) != 0) {
			DemandDetail roundOffDemandDetail = DemandDetail.builder().taxAmount(roundOff)
					.taxHeadMasterCode(WSCalculationConstant.WS_Round_Off).tenantId(tenantId)
					.collectionAmount(BigDecimal.ZERO).build();

			demandDetails.add(roundOffDemandDetail);
		}
	}

	/**
	 * Searches demand for the given consumerCode and tenantIDd
	 * 
	 * @param tenantId
	 *            The tenantId of the tradeLicense
	 * @param consumerCodes
	 *            The set of consumerCode of the demands
	 * @param requestInfo
	 *            The RequestInfo of the incoming request
	 * @return Lis to demands for the given consumerCode
	 */
	private List<Demand> searchDemand(String tenantId, Set<String> consumerCodes,Long taxPeriodFrom, Long taxPeriodTo, RequestInfo requestInfo) {
		String uri = getDemandSearchURL();
		uri = uri.replace("{1}", tenantId);
		uri = uri.replace("{2}", configs.getBusinessService());
		uri = uri.replace("{3}", StringUtils.join(consumerCodes, ','));
		uri = uri.replace("{4}", taxPeriodFrom.toString());
		uri = uri.replace("{5}", taxPeriodTo.toString());
		Object result = serviceRequestRepository.fetchResult(new StringBuilder(uri),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		DemandResponse response;
		try {
			response = mapper.convertValue(result, DemandResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response from Demand Search");
		}

		if (CollectionUtils.isEmpty(response.getDemands()))
			return null;

		else
			return response.getDemands();

	}
	
	/**
	 * 
	 * @param tenantId
	 * @param consumerCodes
	 * @param taxPeriodFrom
	 * @param taxPeriodTo
	 * @param requestInfo
	 * @return List of Demand
	 */
	private List<Demand> searchDemandBasedOnConsumerCode(String tenantId, Set<String> consumerCodes,RequestInfo requestInfo) {
		String uri = getDemandSearchURLForUpdate();
		uri = uri.replace("{1}", tenantId);
		uri = uri.replace("{2}", configs.getBusinessService());
		uri = uri.replace("{3}", StringUtils.join(consumerCodes, ','));
		Object result = serviceRequestRepository.fetchResult(new StringBuilder(uri),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		DemandResponse response;
		try {
			response = mapper.convertValue(result, DemandResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response from Demand Search");
		}

		if (CollectionUtils.isEmpty(response.getDemands()))
			return null;

		else
			return response.getDemands();

	}

	
	/**
	 * Creates demand Search url based on tenanatId,businessService, and
	 * 
	 * @return demand search url
	 */
	public String getDemandSearchURLForUpdate() {
		StringBuilder url = new StringBuilder(configs.getBillingServiceHost());
		url.append(configs.getDemandSearchEndPoint());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("businessService=");
		url.append("{2}");
		url.append("&");
		url.append("consumerCode=");
		url.append("{3}");
		return url.toString();
	}
	/**
	 * Creates demand Search url based on tenanatId,businessService, period from, period to and
	 * ConsumerCode 
	 * 
	 * @return demand search url
	 */
	public String getDemandSearchURL() {
		StringBuilder url = new StringBuilder(configs.getBillingServiceHost());
		url.append(configs.getDemandSearchEndPoint());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("businessService=");
		url.append("{2}");
		url.append("&");
		url.append("consumerCode=");
		url.append("{3}");
		url.append("&");
		url.append("periodFrom=");
		url.append("{4}");
		url.append("&");
		url.append("periodTo=");
		url.append("{5}");
		return url.toString();
	}

	/**
	 * 
	 * @param getBillCriteria Bill Criteria
	 * @param requestInfoWrapper contains request info wrapper
	 * @return updated demand response
	 */
	public List<Demand> updateDemands(GetBillCriteria getBillCriteria, RequestInfoWrapper requestInfoWrapper) {

		if (getBillCriteria.getAmountExpected() == null)
			getBillCriteria.setAmountExpected(BigDecimal.ZERO);
		RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();
		Map<String, JSONArray> billingSlabMaster = new HashMap<>();

		Map<String, JSONArray> timeBasedExmeptionMasterMap = new HashMap<>();
		mstrDataService.setWaterConnectionMasterValues(requestInfo, getBillCriteria.getTenantId(), billingSlabMaster,
				timeBasedExmeptionMasterMap);

		if (CollectionUtils.isEmpty(getBillCriteria.getConsumerCodes()))
			getBillCriteria.setConsumerCodes(Collections.singletonList(getBillCriteria.getConnectionNumber()));

		DemandResponse res = mapper.convertValue(
				repository.fetchResult(utils.getDemandSearchUrl(getBillCriteria), requestInfoWrapper),
				DemandResponse.class);
		if (CollectionUtils.isEmpty(res.getDemands())) {
			Map<String, String> map = new HashMap<>();
			map.put(WSCalculationConstant.EMPTY_DEMAND_ERROR_CODE, WSCalculationConstant.EMPTY_DEMAND_ERROR_MESSAGE);
			throw new CustomException(map);
		}

		/**
		 * Loop through the consumerCodes and re-calculate the time based
		 * applicables
		 */

		Map<String, Demand> consumerCodeToDemandMap = res.getDemands().stream()
				.collect(Collectors.toMap(Demand::getId, Function.identity()));
		
		if(consumerCodeToDemandMap.size() != getBillCriteria.getConsumerCodes().size()) {
			throw new CustomException("DEMAND NOT FOUND",
					"No demand found for the criteria");
		}
		List<Demand> demandsToBeUpdated = new LinkedList<>();

		String tenantId = getBillCriteria.getTenantId();

		List<TaxPeriod> taxPeriods = mstrDataService.getTaxPeriodList(requestInfoWrapper.getRequestInfo(), tenantId);
		
		consumerCodeToDemandMap.forEach((id, demand) ->{
			if (demand.getStatus() != null
					&& WSCalculationConstant.DEMAND_CANCELLED_STATUS.equalsIgnoreCase(demand.getStatus().toString()))
				throw new CustomException(WSCalculationConstant.EG_WS_INVALID_DEMAND_ERROR,
						WSCalculationConstant.EG_WS_INVALID_DEMAND_ERROR_MSG);
			applytimeBasedApplicables(demand, requestInfoWrapper, timeBasedExmeptionMasterMap, taxPeriods);
			addRoundOffTaxHead(tenantId, demand.getDemandDetails());
			demandsToBeUpdated.add(demand);
		});
//		for (String consumerCode : getBillCriteria.getConsumerCodes()) {
//			Demand demand = consumerCodeToDemandMap.get(consumerCode);
//			if (demand == null)
//				throw new CustomException(WSCalculationConstant.EMPTY_DEMAND_ERROR_CODE,
//						"No demand found for the consumerCode: " + consumerCode);
//
//			if (demand.getStatus() != null
//					&& WSCalculationConstant.DEMAND_CANCELLED_STATUS.equalsIgnoreCase(demand.getStatus().toString()))
//				throw new CustomException(WSCalculationConstant.EG_WS_INVALID_DEMAND_ERROR,
//						WSCalculationConstant.EG_WS_INVALID_DEMAND_ERROR_MSG);
//			applytimeBasedApplicables(demand, requestInfoWrapper, timeBasedExmeptionMasterMap, taxPeriods);
//			addRoundOffTaxHead(tenantId, demand.getDemandDetails());
//			demandsToBeUpdated.add(demand);
//		}

		/**
		 * Call demand update in bulk to update the interest or penalty
		 */
		DemandRequest request = DemandRequest.builder().demands(demandsToBeUpdated).requestInfo(requestInfo).build();
		StringBuilder updateDemandUrl = utils.getUpdateDemandUrl();
		repository.fetchResult(updateDemandUrl, request);
		return res.getDemands();

	}

	/**
	 * Updates demand for the given list of calculations
	 * 
	 * @param requestInfo
	 *            The RequestInfo of the calculation request
	 * @param calculations
	 *            List of calculation object
	 * @return Demands that are updated
	 */
	private List<Demand> updateDemandForCalculation(RequestInfo requestInfo, List<Calculation> calculations, Long fromDate, Long toDate) {
		List<Demand> demands = new LinkedList<>();
		for (Calculation calculation : calculations) {

			List<Demand> searchResult = searchDemand(calculation.getTenantId(),
					Collections.singleton(calculation.getWaterConnection().getConnectionNo()), fromDate, toDate, requestInfo);

			if (CollectionUtils.isEmpty(searchResult))
				throw new CustomException("INVALID UPDATE", "No demand exists for connection Number: "
						+ calculation.getWaterConnection().getConnectionNo());

			Demand demand = searchResult.get(0);
			List<DemandDetail> demandDetails = demand.getDemandDetails();
			List<DemandDetail> updatedDemandDetails = getUpdatedDemandDetails(calculation, demandDetails);
			demand.setDemandDetails(updatedDemandDetails);
			demands.add(demand);
		}
		log.info("Updated Demand Details " +demands.toString());
		return demandRepository.updateDemand(requestInfo, demands);
	}

	
	/**
	 * Applies Penalty/Rebate/Interest to the incoming demands
	 * 
	 * If applied already then the demand details will be updated
	 * 
	 * @param demand
	 * @param requestInfoWrapper
	 * @param timeBasedExmeptionMasterMap
	 * @param taxPeriods
	 * @return
	 */

	private boolean applytimeBasedApplicables(Demand demand, RequestInfoWrapper requestInfoWrapper,
			Map<String, JSONArray> timeBasedExmeptionMasterMap, List<TaxPeriod> taxPeriods) {

		boolean isCurrentDemand = false;
		String tenantId = demand.getTenantId();
		String demandId = demand.getId();
		Long expiryDate = demand.getBillExpiryTime();
		TaxPeriod taxPeriod = taxPeriods.stream().filter(t -> demand.getTaxPeriodFrom().compareTo(t.getFromDate()) >= 0
				&& demand.getTaxPeriodTo().compareTo(t.getToDate()) <= 0).findAny().orElse(null);
		
		if (taxPeriod == null) {
			log.info("Demand Expired!! ->> Consumer Code "+ demand.getConsumerCode() +" Demand Id -->> "+ demand.getId());
			return isCurrentDemand;
		}
		
		if (!(taxPeriod.getFromDate() <= System.currentTimeMillis()
				&& taxPeriod.getToDate() >= System.currentTimeMillis()))
			isCurrentDemand = true;
		
		if(expiryDate < System.currentTimeMillis()) {
		BigDecimal waterChargeApplicable = BigDecimal.ZERO;
		BigDecimal oldPenality = BigDecimal.ZERO;
		BigDecimal oldInterest = BigDecimal.ZERO;
		

		for (DemandDetail detail : demand.getDemandDetails()) {
			if (WSCalculationConstant.TAX_APPLICABLE.contains(detail.getTaxHeadMasterCode())) {
				waterChargeApplicable = waterChargeApplicable.add(detail.getTaxAmount());
			}
			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(WSCalculationConstant.WS_TIME_PENALTY)) {
				oldPenality = oldPenality.add(detail.getTaxAmount());
			}
			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(WSCalculationConstant.WS_TIME_INTEREST)) {
				oldInterest = oldInterest.add(detail.getTaxAmount());
			}
		}
		
		boolean isPenaltyUpdated = false;
		boolean isInterestUpdated = false;
		
		List<DemandDetail> details = demand.getDemandDetails();

		Map<String, BigDecimal> interestPenaltyEstimates = payService.applyPenaltyRebateAndInterest(
				waterChargeApplicable, taxPeriod.getFinancialYear(), timeBasedExmeptionMasterMap, expiryDate);
		if (null == interestPenaltyEstimates)
			return isCurrentDemand;

		BigDecimal penalty = interestPenaltyEstimates.get(WSCalculationConstant.WS_TIME_PENALTY);
		BigDecimal interest = interestPenaltyEstimates.get(WSCalculationConstant.WS_TIME_INTEREST);

		DemandDetailAndCollection latestPenaltyDemandDetail, latestInterestDemandDetail;

		if (interest.compareTo(BigDecimal.ZERO) != 0) {
			latestInterestDemandDetail = utils.getLatestDemandDetailByTaxHead(WSCalculationConstant.WS_TIME_INTEREST,
					details);
			if (latestInterestDemandDetail != null) {
				updateTaxAmount(interest, latestInterestDemandDetail);
				isInterestUpdated = true;
			}
		}

		if (penalty.compareTo(BigDecimal.ZERO) != 0) {
			latestPenaltyDemandDetail = utils.getLatestDemandDetailByTaxHead(WSCalculationConstant.WS_TIME_PENALTY,
					details);
			if (latestPenaltyDemandDetail != null) {
				updateTaxAmount(penalty, latestPenaltyDemandDetail);
				isPenaltyUpdated = true;
			}
		}

		if (!isPenaltyUpdated && penalty.compareTo(BigDecimal.ZERO) > 0)
			details.add(
					DemandDetail.builder().taxAmount(penalty).taxHeadMasterCode(WSCalculationConstant.WS_TIME_PENALTY)
							.demandId(demandId).tenantId(tenantId).build());
		if (!isInterestUpdated && interest.compareTo(BigDecimal.ZERO) > 0)
			details.add(
					DemandDetail.builder().taxAmount(interest).taxHeadMasterCode(WSCalculationConstant.WS_TIME_INTEREST)
							.demandId(demandId).tenantId(tenantId).build());
		}

		return isCurrentDemand;
	}

	/**
	 * Updates the amount in the latest demandDetail by adding the diff between
	 * new and old amounts to it
	 * 
	 * @param newAmount
	 *            The new tax amount for the taxHead
	 * @param latestDetailInfo
	 *            The latest demandDetail for the particular taxHead
	 */
	private void updateTaxAmount(BigDecimal newAmount, DemandDetailAndCollection latestDetailInfo) {
		BigDecimal diff = newAmount.subtract(latestDetailInfo.getTaxAmountForTaxHead());
		BigDecimal newTaxAmountForLatestDemandDetail = latestDetailInfo.getLatestDemandDetail().getTaxAmount()
				.add(diff);
		latestDetailInfo.getLatestDemandDetail().setTaxAmount(newTaxAmountForLatestDemandDetail);
	}

}
