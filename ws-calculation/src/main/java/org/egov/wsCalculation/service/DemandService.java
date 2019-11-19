package org.egov.wsCalculation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.OwnerInfo;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.Assessment;
import org.egov.wsCalculation.model.Bill;
import org.egov.wsCalculation.model.BillResponse;
import org.egov.wsCalculation.model.Calculation;
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
import org.egov.wsCalculation.repository.DemandRepository;
import org.egov.wsCalculation.repository.ServiceRequestRepository;
import org.egov.wsCalculation.util.WSCalculationUtil;
import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

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
	private AssessmentService assessmentService;

	@Autowired
	private WSCalculationUtil utils;

	@Autowired
	private WSCalculationConfiguration configs;

	@Autowired
	private EstimationService estimationService;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private DemandRepository demandRepository;

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

		Map<String, Object> masterMap = mstrDataService.getMasterMap(request);
		Map<String, Calculation> waterCalculationMap = estimationService.getEstimationWaterMap(request);
		List<Calculation> calculationList = new ArrayList<>(waterCalculationMap.values());
		generateDemand(request.getRequestInfo(), calculationList, masterMap);
		return waterCalculationMap;
	}

	/**
	 * Creates or updates Demand
	 * 
	 * @param requestInfo
	 *            The RequestInfo of the calculation request
	 * @param calculations
	 *            The Calculation Objects for which demand has to be generated
	 *            or updated
	 */
	public void generateDemand(RequestInfo requestInfo, List<Calculation> calculations, Map<String, Object> masterMap) {

		// List that will contain Calculation for new demands
		List<Calculation> createCalculations = new LinkedList<>();

		// List that will contain Calculation for old demands
		List<Calculation> updateCalculations = new LinkedList<>();

		if (!CollectionUtils.isEmpty(calculations)) {

			// Collect required parameters for demand search
			String tenantId = calculations.get(0).getTenantId();
			Set<String> consumerCodes = calculations.stream().map(calculation -> calculation.getApplicationNO())
					.collect(Collectors.toSet());
			// Set<String> applicationNumbers =
			// calculations.stream().map(calculation ->
			// calculation.getTradeLicense().getApplicationNumber()).collect(Collectors.toSet());
			List<Demand> demands = searchDemand(tenantId, consumerCodes, requestInfo);
			Set<String> applicationNumbersFromDemands = new HashSet<>();
			if (!CollectionUtils.isEmpty(demands))
				applicationNumbersFromDemands = demands.stream().map(Demand::getConsumerCode)
						.collect(Collectors.toSet());

			// If demand already exists add it updateCalculations else
			// createCalculations
			for (Calculation calculation : calculations) {
				if (!applicationNumbersFromDemands.contains(calculation.getApplicationNO()))
					createCalculations.add(calculation);
				else
					updateCalculations.add(calculation);
			}
		}

		if (!CollectionUtils.isEmpty(createCalculations))
			createDemand(requestInfo, createCalculations, masterMap);

		if (!CollectionUtils.isEmpty(updateCalculations))
			updateDemand(requestInfo, updateCalculations);
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
			Map<String, Object> masterMap) {
		List<Demand> demands = new LinkedList<>();
		String assessmentYear = "2019-20";

		for (Calculation calculation : calculations) {
			WaterConnection connection = null;

			if (calculation.getWaterConnection() != null)
				connection = calculation.getWaterConnection();

			// else if (calculation != null)
			// connection = utils.getConnection(requestInfo,
			// calculation.getWaterConnection().getApplicationNo(),
			// calculation.getTenantId());

			if (connection == null)
				throw new CustomException("INVALID APPLICATIONNUMBER",
						"Demand cannot be generated for applicationNumber "
								+ calculation.getWaterConnection().getApplicationNo()
								+ " Water Connection with this number does not exist ");

			String tenantId = calculation.getTenantId();
			String consumerCode = calculation.getApplicationNO();
			User owner = requestInfo.getUserInfo();

			List<DemandDetail> demandDetails = new LinkedList<>();

			calculation.getTaxHeadEstimates().forEach(taxHeadEstimate -> {
				demandDetails.add(DemandDetail.builder().taxAmount(taxHeadEstimate.getEstimateAmount())
						.taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode()).collectionAmount(BigDecimal.ZERO)
						.tenantId(tenantId).build());
			});

			@SuppressWarnings("unchecked")
			Map<String, Map<String, Object>> financialYearMaster = (Map<String, Map<String, Object>>) masterMap
					.get(WSCalculationConstant.FINANCIALYEAR_MASTER_KEY);

			Map<String, Object> finYearMap = financialYearMaster.get(assessmentYear);
			Long fromDate = (Long) finYearMap.get(WSCalculationConstant.FINANCIAL_YEAR_STARTING_DATE);
			Long toDate = (Long) finYearMap.get(WSCalculationConstant.FINANCIAL_YEAR_ENDING_DATE);

			addRoundOffTaxHead(calculation.getTenantId(), demandDetails);

			demands.add(Demand.builder().consumerCode(consumerCode).demandDetails(demandDetails).payer(owner)
					.minimumAmountPayable(configs.getMinimumPayableAmount()).tenantId(tenantId).taxPeriodFrom(fromDate)
					.taxPeriodTo(toDate).consumerType("water connection").businessService(configs.getBusinessService())
					.build());
		}
		return demandRepository.saveDemand(requestInfo, demands);
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
	private List<Demand> updateDemand(RequestInfo requestInfo, List<Calculation> calculations) {
		List<Demand> demands = new LinkedList<>();
		for (Calculation calculation : calculations) {

			List<Demand> searchResult = searchDemand(calculation.getTenantId(),
					Collections.singleton(calculation.getWaterConnection().getApplicationNo()), requestInfo);

			if (CollectionUtils.isEmpty(searchResult))
				throw new CustomException("INVALID UPDATE", "No demand exists for applicationNumber: "
						+ calculation.getWaterConnection().getApplicationNo());

			Demand demand = searchResult.get(0);
			List<DemandDetail> demandDetails = demand.getDemandDetails();
			List<DemandDetail> updatedDemandDetails = getUpdatedDemandDetails(calculation, demandDetails);
			demand.setDemandDetails(updatedDemandDetails);
			demands.add(demand);
		}
		return demandRepository.updateDemand(requestInfo, demands);
	}

	/**
	 * Returns the list of new DemandDetail to be added for updating the demand
	 * 
	 * @param calculation
	 *            The calculation object for the update tequest
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
			if (!demandDetail.getTaxHeadMasterCode().equalsIgnoreCase(WSCalculationConstant.MDMS_ROUNDOFF_TAXHEAD))
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
					.taxHeadMasterCode(WSCalculationConstant.MDMS_ROUNDOFF_TAXHEAD).tenantId(tenantId)
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
	private List<Demand> searchDemand(String tenantId, Set<String> consumerCodes, RequestInfo requestInfo) {
		String uri = getDemandSearchURL();
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
	 * @param water
	 *            connection
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

		return Demand.builder().tenantId("pb").businessService(configs.getWsModuleCode()).consumerType(propertyType)
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
	 * 
	 * @param getBillCriteria
	 * @param requestInfoWrapper
	 * @return
	 */
	public DemandResponse updateDemands(GetBillCriteria getBillCriteria, RequestInfoWrapper requestInfoWrapper) {

		if (getBillCriteria.getAmountExpected() == null)
			getBillCriteria.setAmountExpected(BigDecimal.ZERO);
		RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();
		Map<String, Map<String, List<Object>>> propertyBasedExemptionMasterMap = new HashMap<>();
		Map<String, JSONArray> timeBasedExmeptionMasterMap = new HashMap<>();
		mstrDataService.setPropertyMasterValues(requestInfo, getBillCriteria.getTenantId(),
				propertyBasedExemptionMasterMap, timeBasedExmeptionMasterMap);

		if (CollectionUtils.isEmpty(getBillCriteria.getConsumerCodes()))
			getBillCriteria.setConsumerCodes(Collections.singletonList(getBillCriteria.getPropertyId()
					+ WSCalculationConstant.WS_CONSUMER_CODE_SEPARATOR + getBillCriteria.getAssessmentNumber()));

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
				.collect(Collectors.toMap(Demand::getConsumerCode, Function.identity()));

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
	 * Balances the decimal values in the newly updated demand by performing a
	 * roundoff
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
		Map<String, Boolean> isTaxHeadDebitMap = mstrDataService
				.getTaxHeadMasterMap(requestInfoWrapper.getRequestInfo(), tenantId).stream()
				.collect(Collectors.toMap(TaxHeadMaster::getCode, TaxHeadMaster::getIsDebit));

		/*
		 * Summing the credit amount and Debit amount in to separate
		 * variables(based on the taxhead:isdebit map) to send to
		 * roundoffDecimal method
		 */
		for (DemandDetail detail : demand.getDemandDetails()) {
			taxAmount = taxAmount.add(detail.getTaxAmount());
		}

		/*
		 * An estimate object will be returned incase if there is a decimal
		 * value
		 * 
		 * If no decimal value found null object will be returned
		 */
		TaxHeadEstimate roundOffEstimate = payService.roundOfDecimals(taxAmount);

		BigDecimal decimalRoundOff = null != roundOffEstimate ? roundOffEstimate.getEstimateAmount() : BigDecimal.ZERO;

		if (decimalRoundOff.compareTo(BigDecimal.ZERO) != 0) {
			details.add(DemandDetail.builder().taxAmount(roundOffEstimate.getEstimateAmount())
					.taxHeadMasterCode(roundOffEstimate.getTaxHeadCode()).demandId(demandId).tenantId(tenantId)
					.build());
		}

	}

}
