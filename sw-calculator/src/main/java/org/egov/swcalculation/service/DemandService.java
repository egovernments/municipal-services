package org.egov.swcalculation.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.swcalculation.config.SWCalculationConfiguration;
import org.egov.swcalculation.constants.SWCalculationConstant;
import org.egov.swcalculation.model.AuditDetails;
import org.egov.swcalculation.model.Calculation;
import org.egov.swcalculation.model.CalculationCriteria;
import org.egov.swcalculation.model.CalculationReq;
import org.egov.swcalculation.model.Demand;
import org.egov.swcalculation.model.Demand.StatusEnum;
import org.egov.swcalculation.model.DemandDetail;
import org.egov.swcalculation.model.DemandDetailAndCollection;
import org.egov.swcalculation.model.DemandRequest;
import org.egov.swcalculation.model.DemandResponse;
import org.egov.swcalculation.model.GetBillCriteria;
import org.egov.swcalculation.model.RequestInfoWrapper;
import org.egov.swcalculation.model.SewerageConnection;
import org.egov.swcalculation.model.TaxHeadEstimate;
import org.egov.swcalculation.model.TaxPeriod;
import org.egov.swcalculation.producer.SWCalculationProducer;
import org.egov.swcalculation.repository.DemandRepository;
import org.egov.swcalculation.repository.ServiceRequestRepository;
import org.egov.swcalculation.repository.SewerageCalculatorDao;
import org.egov.swcalculation.util.CalculatorUtils;
import org.egov.swcalculation.util.SWCalculationUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class DemandService {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private SWCalculationUtil utils;

	@Autowired
	private MasterDataService mstrDataService;

	@Autowired
	private DemandRepository demandRepository;

	@Autowired
	private SWCalculationConfiguration configs;
	
	
	@Autowired
	private ServiceRequestRepository repository;


	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
	PayService payService;
	
    @Autowired
    CalculatorUtils calculatorUtils;
    
    @Autowired
    SewerageCalculatorDao sewerageCalculatorDao;
    
    @Autowired
    SWCalculationService swCalculationService;
    
    @Autowired
    EstimationService estimationService;
    
    @Autowired
    SWCalculationProducer producer;
    
    @Autowired
    KafkaTemplate kafkaTemplate;

	/**
	 * Creates or updates Demand
	 * 
	 * @param requestInfo The RequestInfo of the calculation request
	 * @param calculations The Calculation Objects for which demand has to be generated or updated
	 */
	public List<Demand> generateDemand(RequestInfo requestInfo, List<Calculation> calculations,
			Map<String, Object> masterMap, boolean isForConnectionNo) {
		@SuppressWarnings("unchecked")
		Map<String, Object> financialYearMaster =  (Map<String, Object>) masterMap
				.get(SWCalculationConstant.BillingPeriod);
		Long fromDate = (Long) financialYearMaster.get(SWCalculationConstant.STARTING_DATE_APPLICABLES);
		Long toDate = (Long) financialYearMaster.get(SWCalculationConstant.ENDING_DATE_APPLICABLES);
		
		// List that will contain Calculation for old demands
		List<Calculation> updateCalculations = new LinkedList<>();
		List<Calculation> createCalculations = new LinkedList<>();
		if (!CollectionUtils.isEmpty(calculations)) {
			// Collect required parameters for demand search
			String tenantId = calculations.get(0).getTenantId();
			Long fromDateSearch = null;
			Long toDateSearch = null;
			Set<String> consumerCodes = new HashSet<>();
			if (isForConnectionNo) {
				fromDateSearch = fromDate;
				toDateSearch = toDate;
				consumerCodes = calculations.stream().map(calculation -> calculation.getConnectionNo())
						.collect(Collectors.toSet());
			} else if (!isForConnectionNo) {
				consumerCodes = calculations.stream().map(calculation -> calculation.getApplicationNO())
						.collect(Collectors.toSet());
			}
			
			List<Demand> demands = searchDemand(tenantId, consumerCodes, fromDateSearch, toDateSearch, requestInfo);
			Set<String> connectionNumbersFromDemands = new HashSet<>();
			if (!CollectionUtils.isEmpty(demands))
				connectionNumbersFromDemands = demands.stream().map(Demand::getConsumerCode)
						.collect(Collectors.toSet());
			// List that will contain Calculation for new demands
			// If demand already exists add it updateCalculations else
			// createCalculations
			for (Calculation calculation : calculations) {
				if (!connectionNumbersFromDemands.contains(isForConnectionNo == true ? calculation.getConnectionNo() : calculation.getApplicationNO()))
					createCalculations.add(calculation);
				else
					updateCalculations.add(calculation);
			}
		}
		List<Demand> createdDemands = new ArrayList<>();
		if (!CollectionUtils.isEmpty(createCalculations))
			createdDemands = createDemand(requestInfo, createCalculations, masterMap, isForConnectionNo);

		if (!CollectionUtils.isEmpty(updateCalculations))
			createdDemands = updateDemandForCalculation(requestInfo, updateCalculations, fromDate, toDate, isForConnectionNo);
		return createdDemands;
	}

	/**
	 * Creates demand for the given list of calculations
	 * 
	 * @param requestInfo
	 *            The RequestInfo of the calculation request
	 * @param calculations
	 *            List of calculation object
	 * @return Demands that are created
	 */
	private List<Demand> createDemand(RequestInfo requestInfo, List<Calculation> calculations,
			Map<String, Object> masterMap,boolean isForConnectionNO) {
		List<Demand> demands = new LinkedList<>();
		for (Calculation calculation : calculations) {
			SewerageConnection connection = null;

			if (calculation.getSewerageConnection() != null)
				connection = calculation.getSewerageConnection();

			// else if (calculation != null)
			// connection = utils.getConnection(requestInfo,
			// calculation.getWaterConnection().getApplicationNo(),
			// calculation.getTenantId());

			if (connection == null)
				throw new CustomException("INVALID_SEWERAGE_CONNECTION",
						"Demand cannot be generated for "
								+ (isForConnectionNO == true ? calculation.getConnectionNo() : calculation.getApplicationNO())
								+ " Water Connection with this number does not exist ");

			String consumerCode = isForConnectionNO == true ?  calculation.getConnectionNo() : calculation.getApplicationNO();
			User owner = connection.getProperty().getOwners().get(0).toCommonUser();
			
			List<DemandDetail> demandDetails = new LinkedList<>();
			
			//CreatedBy field in AuditDetails object is not null field for createDemand API
			String userUUID = requestInfo.getUserInfo().getUuid();
			userUUID = userUUID != null ? userUUID : "System";
//			AuditDetails auditDetails = AuditDetails.builder()
//                    .createdBy(userUUID)
//                    .createdTime(new Date().getTime())
//                    .lastModifiedBy(userUUID)
//                    .lastModifiedTime(new Date().getTime()).build();

			calculation.getTaxHeadEstimates().forEach(taxHeadEstimate -> {
				demandDetails.add(DemandDetail.builder().taxAmount(taxHeadEstimate.getEstimateAmount())
						.taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode()).collectionAmount(BigDecimal.ZERO)
//						.tenantId(calculation.getTenantId()).auditDetails(auditDetails).build());
						.tenantId(calculation.getTenantId()).build());
			});
			
			@SuppressWarnings("unchecked")
			Map<String, Object> financialYearMaster =  (Map<String, Object>) masterMap
					.get(SWCalculationConstant.BillingPeriod);

			Long fromDate = (Long) financialYearMaster.get(SWCalculationConstant.STARTING_DATE_APPLICABLES);
			Long toDate = (Long) financialYearMaster.get(SWCalculationConstant.ENDING_DATE_APPLICABLES);
			Long expiryDate = (Long) financialYearMaster.get(SWCalculationConstant.Demand_Expiry_Date_String);
			BigDecimal minimumPaybleAmount = isForConnectionNO == true ? configs.getMinimumPayableAmount() : calculation.getTotalAmount();
			String businessService = isForConnectionNO == true ? configs.getBusinessService() : SWCalculationConstant.ONE_TIME_FEE_SERVICE_FIELD;
		
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
					.minimumAmountPayable(minimumPaybleAmount).tenantId(calculation.getTenantId()).taxPeriodFrom(fromDate)
					.taxPeriodTo(toDate).consumerType("sewerageConnection").businessService(businessService)
					.status(StatusEnum.valueOf("ACTIVE")).billExpiryTime(expiryDate).build());
		}
		log.info("Demand Object" + demands.toString());
		List<Demand> demandRes = demandRepository.saveDemand(requestInfo, demands);
		if(isForConnectionNO)
		fetchBill(demandRes, requestInfo);
		return demandRes;
	}
	
	
	public boolean fetchBill(List<Demand> demandResponse, RequestInfo requestInfo) {
		boolean notificationSent = false;
		for (Demand demand : demandResponse) {
			try {
				Object result = serviceRequestRepository.fetchResult(calculatorUtils.getFetchBillURL(demand.getTenantId(), demand.getConsumerCode()),
						RequestInfoWrapper.builder().requestInfo(requestInfo).build());
				HashMap<String, Object> billResponse = new HashMap<>();
				billResponse.put("requestInfo", requestInfo);
				billResponse.put("billResponse", result);
				producer.push(configs.getPayTriggers(), billResponse);
				notificationSent = true;
			} catch (Exception ex) {
				log.error("Fetch Bill Error", ex);
			}
		}
		return notificationSent;
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
			if (!demandDetail.getTaxHeadMasterCode().equalsIgnoreCase(SWCalculationConstant.MDMS_ROUNDOFF_TAXHEAD))
				totalTax = totalTax.add(demandDetail.getTaxAmount());
			else
				prevRoundOffDemandDetail = demandDetail;
		}

		BigDecimal decimalValue = totalTax.remainder(BigDecimal.ONE);
		BigDecimal midVal = BigDecimal.valueOf(0.5);
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
			demandDetails.add(DemandDetail.builder().taxAmount(roundOff)
					.taxHeadMasterCode(SWCalculationConstant.MDMS_ROUNDOFF_TAXHEAD).tenantId(tenantId)
					.collectionAmount(BigDecimal.ZERO).build());
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
	 * @return List of demands for the given consumerCode
	 */
	private List<Demand> searchDemand(String tenantId, Set<String> consumerCodes, Long taxPeriodFrom, Long taxPeriodTo,
			RequestInfo requestInfo) {
		Object result = serviceRequestRepository.fetchResult(getDemandSearchURL(tenantId, consumerCodes, taxPeriodFrom, taxPeriodTo),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());
		DemandResponse response;
		try {
			response = mapper.convertValue(result, DemandResponse.class);
			if (CollectionUtils.isEmpty(response.getDemands()))
				return null;
			return response.getDemands();
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response from Demand Search");
		}

	}
	
	/**
	 * Creates demand Search url based on tenanatId,businessService, period from, period to and
	 * ConsumerCode 
	 * 
	 * @return demand search url
	 */
	public StringBuilder getDemandSearchURL(String tenantId, Set<String> consumerCodes, Long taxPeriodFrom, Long taxPeriodTo) {
		StringBuilder url = new StringBuilder(configs.getBillingServiceHost());
		url.append(configs.getDemandSearchEndPoint());
		url.append("?");
		url.append("tenantId=");
		url.append(tenantId);
		url.append("&");
		url.append("businessService=");
		url.append(taxPeriodFrom == null  ? SWCalculationConstant.ONE_TIME_FEE_SERVICE_FIELD : configs.getBusinessService());
		url.append("&");
		url.append("consumerCode=");
		url.append(StringUtils.join(consumerCodes, ','));
		if (taxPeriodFrom != null) {
			url.append("&");
			url.append("periodFrom=");
			url.append(taxPeriodFrom.toString());
		}
		if (taxPeriodTo != null) {
			url.append("&");
			url.append("periodTo=");
			url.append(taxPeriodTo.toString());
		}
		return url;
	}
	
	

	/**
	 * Creates demand Search url based on tenanatId,businessService and
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
		return url.toString();
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
	private List<Demand> updateDemandForCalculation(RequestInfo requestInfo, List<Calculation> calculations,
			Long fromDate, Long toDate, boolean isForConnectionNo) {

		List<Demand> demands = new LinkedList<>();
		Long fromDateSearch = isForConnectionNo == true ? fromDate : null;
		Long toDateSearch = isForConnectionNo == true ? toDate : null;

		for (Calculation calculation : calculations) {
			Set<String> consumerCodes = new HashSet<>();
			consumerCodes = isForConnectionNo == true
					? Collections.singleton(calculation.getSewerageConnection().getConnectionNo())
					: Collections.singleton(calculation.getSewerageConnection().getApplicationNo());
			List<Demand> searchResult = searchDemand(calculation.getTenantId(), consumerCodes, fromDateSearch,
					toDateSearch, requestInfo);
			if (CollectionUtils.isEmpty(searchResult))
				throw new CustomException("INVALID_DEMAND_UPDATE", "No demand exists for Number: "
						+ consumerCodes.toString());
			Demand demand = searchResult.get(0);
			List<DemandDetail> demandDetails = demand.getDemandDetails();
			List<DemandDetail> updatedDemandDetails = getUpdatedDemandDetails(calculation, demandDetails);
			demand.setDemandDetails(updatedDemandDetails);
			demands.add(demand);
		}

		log.info("Updated Demand Details " + demands.toString());
		return demandRepository.updateDemand(requestInfo, demands);
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
	 * 
	 * @param getBillCriteria Bill Criteria
	 * @param requestInfoWrapper contains request info wrapper
	 * @return updated demand response
	 */
	public DemandResponse updateDemands(GetBillCriteria getBillCriteria, RequestInfoWrapper requestInfoWrapper) {

		if (getBillCriteria.getAmountExpected() == null)
			getBillCriteria.setAmountExpected(BigDecimal.ZERO);
		Map<String, JSONArray> billingSlabMaster = new HashMap<>();

		Map<String, JSONArray> timeBasedExmeptionMasterMap = new HashMap<>();
		mstrDataService.setSewerageConnectionMasterValues(requestInfoWrapper.getRequestInfo(), getBillCriteria.getTenantId(), billingSlabMaster,
				timeBasedExmeptionMasterMap);

		if (CollectionUtils.isEmpty(getBillCriteria.getConsumerCodes()))
			getBillCriteria.setConsumerCodes(Collections.singletonList(getBillCriteria.getConnectionNumber()));

		DemandResponse res = mapper.convertValue(
				repository.fetchResult(utils.getDemandSearchUrl(getBillCriteria), requestInfoWrapper),
				DemandResponse.class);
		if (CollectionUtils.isEmpty(res.getDemands())) {
			Map<String, String> map = new HashMap<>();
			map.put(SWCalculationConstant.EMPTY_DEMAND_ERROR_CODE, SWCalculationConstant.EMPTY_DEMAND_ERROR_MESSAGE);
			throw new CustomException(map);
		}

		/**
		 * Loop through the consumerCodes and re-calculate the time based
		 * applicables
		 */

		Map<String, Demand> consumerCodeToDemandMap = res.getDemands().stream()
				.collect(Collectors.toMap(Demand::getId, Function.identity()));
//		if(consumerCodeToDemandMap.size() != getBillCriteria.getConsumerCodes().size()) {
//			throw new CustomException("DEMAND NOT FOUND",
//					"No demand found for the criteria");
//		}
		List<Demand> demandsToBeUpdated = new LinkedList<>();

		List<TaxPeriod> taxPeriods = mstrDataService.getTaxPeriodList(requestInfoWrapper.getRequestInfo(), getBillCriteria.getTenantId(), SWCalculationConstant.SERVICE_FIELD_VALUE_SW);
		
		
		consumerCodeToDemandMap.forEach((id, demand) ->{
			if (demand.getStatus() != null
					&& SWCalculationConstant.DEMAND_CANCELLED_STATUS.equalsIgnoreCase(demand.getStatus().toString()))
				throw new CustomException(SWCalculationConstant.EG_SW_INVALID_DEMAND_ERROR,
						SWCalculationConstant.EG_SW_INVALID_DEMAND_ERROR_MSG);
			applytimeBasedApplicables(demand, requestInfoWrapper, timeBasedExmeptionMasterMap, taxPeriods);
			addRoundOffTaxHead(getBillCriteria.getTenantId(), demand.getDemandDetails());
			demandsToBeUpdated.add(demand);
		});
		/**
		 * Call demand update in bulk to update the interest or penalty
		 */
		repository.fetchResult(utils.getUpdateDemandUrl(), 
				DemandRequest.builder().demands(demandsToBeUpdated).requestInfo(requestInfoWrapper.getRequestInfo()).build());
		return res;

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
		TaxPeriod taxPeriod = taxPeriods.stream().filter(t -> demand.getTaxPeriodFrom().compareTo(t.getFromDate()) >= 0
				&& demand.getTaxPeriodTo().compareTo(t.getToDate()) <= 0).findAny().orElse(null);
		
		if (taxPeriod == null) {
			log.info("Demand Expired!!");
			return isCurrentDemand;
		}
		
		if (!(taxPeriod.getFromDate() <= System.currentTimeMillis()
				&& taxPeriod.getToDate() >= System.currentTimeMillis()))
			isCurrentDemand = true;
		
		if(demand.getBillExpiryTime() < System.currentTimeMillis()) {
		BigDecimal sewerageChargeApplicable = BigDecimal.ZERO;
		BigDecimal oldPenality = BigDecimal.ZERO;
		BigDecimal oldInterest = BigDecimal.ZERO;
		

		for (DemandDetail detail : demand.getDemandDetails()) {
			if (SWCalculationConstant.TAX_APPLICABLE.contains(detail.getTaxHeadMasterCode())) {
				sewerageChargeApplicable = sewerageChargeApplicable.add(detail.getTaxAmount());
			}
			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(SWCalculationConstant.SW_TIME_PENALTY)) {
				oldPenality = oldPenality.add(detail.getTaxAmount());
			}
			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(SWCalculationConstant.SW_TIME_INTEREST)) {
				oldInterest = oldInterest.add(detail.getTaxAmount());
			}
		}
		
		boolean isPenaltyUpdated = false;
		boolean isInterestUpdated = false;
		
		Map<String, BigDecimal> interestPenaltyEstimates = payService.applyPenaltyRebateAndInterest(
				sewerageChargeApplicable, taxPeriod.getFinancialYear(), timeBasedExmeptionMasterMap, demand.getBillExpiryTime());
		if (null == interestPenaltyEstimates)
			return isCurrentDemand;

		BigDecimal penalty = interestPenaltyEstimates.get(SWCalculationConstant.SW_TIME_PENALTY);
		BigDecimal interest = interestPenaltyEstimates.get(SWCalculationConstant.SW_TIME_INTEREST);

		DemandDetailAndCollection latestPenaltyDemandDetail, latestInterestDemandDetail;

		if (interest.compareTo(BigDecimal.ZERO) != 0) {
			latestInterestDemandDetail = utils.getLatestDemandDetailByTaxHead(SWCalculationConstant.SW_TIME_INTEREST,
					demand.getDemandDetails());
			if (latestInterestDemandDetail != null) {
				updateTaxAmount(interest, latestInterestDemandDetail);
				isInterestUpdated = true;
			}
		}

		if (penalty.compareTo(BigDecimal.ZERO) != 0) {
			latestPenaltyDemandDetail = utils.getLatestDemandDetailByTaxHead(SWCalculationConstant.SW_TIME_PENALTY,
					demand.getDemandDetails());
			if (latestPenaltyDemandDetail != null) {
				updateTaxAmount(penalty, latestPenaltyDemandDetail);
				isPenaltyUpdated = true;
			}
		}

		if (!isPenaltyUpdated && penalty.compareTo(BigDecimal.ZERO) > 0)
			demand.getDemandDetails().add(
					DemandDetail.builder().taxAmount(penalty).taxHeadMasterCode(SWCalculationConstant.SW_TIME_PENALTY)
							.demandId(demand.getId()).tenantId(demand.getTenantId()).build());
		if (!isInterestUpdated && interest.compareTo(BigDecimal.ZERO) > 0)
			demand.getDemandDetails().add(
					DemandDetail.builder().taxAmount(interest).taxHeadMasterCode(SWCalculationConstant.SW_TIME_INTEREST)
							.demandId(demand.getId()).tenantId(demand.getTenantId()).build());
		}

		return isCurrentDemand;
	}
	
//	/**
//	 * 
//	 * @param tenantId
//	 * @param consumerCodes
//	 * @param taxPeriodFrom
//	 * @param taxPeriodTo
//	 * @param requestInfo
//	 * @return List of Demand
//	 */
//	private List<Demand> searchDemandBasedOnConsumerCode(String tenantId, Set<String> consumerCodes,RequestInfo requestInfo) {
//		String uri = getDemandSearchURLForUpdate();
//		uri = uri.replace("{1}", tenantId);
//		uri = uri.replace("{2}", configs.getBusinessService());
//		uri = uri.replace("{3}", StringUtils.join(consumerCodes, ','));
//		Object result = serviceRequestRepository.fetchResult(new StringBuilder(uri),
//				RequestInfoWrapper.builder().requestInfo(requestInfo).build());
//
//		DemandResponse response;
//		try {
//			response = mapper.convertValue(result, DemandResponse.class);
//		} catch (IllegalArgumentException e) {
//			throw new CustomException("PARSING ERROR", "Failed to parse response from Demand Search");
//		}
//
//		if (CollectionUtils.isEmpty(response.getDemands()))
//			return null;
//
//		else
//			return response.getDemands();
//
//	}

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
	 * 
	 * @param tenantId TenantId for getting master data.
	 */
	public void generateDemandForTenantId(String tenantId, RequestInfo requestInfo) {
		requestInfo.getUserInfo().setTenantId(tenantId);
		MdmsCriteriaReq mdmsCriteriaReq = calculatorUtils.getBillingFrequency(requestInfo, tenantId);
		Object res = repository.fetchResult(calculatorUtils.getMdmsSearchUrl(), mdmsCriteriaReq);
		if (res == null) {
			throw new CustomException("MDMS ERROR FOR BILLING FREQUENCY", "ERROR IN FETCHING THE BILLING FREQUENCY");
		}
		generateDemandForULB(JsonPath.read(res, SWCalculationConstant.JSONPATH_ROOT_FOR_BilingPeriod), requestInfo,
				tenantId);
	}
	
	/**
	 * 
	 * @param mdmsResponse
	 * @param requestInfo
	 * @param tenantId
	 */
	@SuppressWarnings("unchecked")
	public void generateDemandForULB(ArrayList<?> mdmsResponse, RequestInfo requestInfo, String tenantId) {
		log.info("Billing Frequency Map" + mdmsResponse.toString());
		Map<String, Object> master = (Map<String, Object>) mdmsResponse.get(0);
		long startDay = (((int) master.get(SWCalculationConstant.Demand_Generate_Date_String)) / 86400000);
		if (isCurrentDateIsMatching((String) master.get(SWCalculationConstant.Billing_Cycle_String), startDay)) {
			List<String> connectionNos = sewerageCalculatorDao.getConnectionsNoList(tenantId,
					SWCalculationConstant.nonMeterdConnection);
			for (String connectionNo : connectionNos) {
				CalculationCriteria calculationCriteria = CalculationCriteria.builder().tenantId(tenantId)
						.assessmentYear(estimationService.getAssessmentYear()).connectionNo(connectionNo).build();
				List<CalculationCriteria> calculationCriteriaList = new ArrayList<>();
				calculationCriteriaList.add(calculationCriteria);
				CalculationReq calculationReq = CalculationReq.builder().calculationCriteria(calculationCriteriaList)
						.requestInfo(requestInfo).isconnectionCalculation(true).build();
				kafkaTemplate.send(configs.getCreateDemand(), calculationReq);
			}
		}
	}
	
	/**
	 * 
	 * @param billingFrequency
	 * @param dayOfMonth
	 * @return true if current day is for generation of demand
	 */
	private boolean isCurrentDateIsMatching(String billingFrequency, long dayOfMonth) {
		if (billingFrequency.equalsIgnoreCase(SWCalculationConstant.Monthly_Billing_Period)
				&& (dayOfMonth == LocalDateTime.now().getDayOfMonth())) {
			return true;
		} else if (billingFrequency.equalsIgnoreCase(SWCalculationConstant.Quaterly_Billing_Period)) {
			return false;
		}
		return true;
	}
}
