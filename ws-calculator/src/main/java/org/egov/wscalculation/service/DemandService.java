package org.egov.wscalculation.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.egov.wscalculation.constants.WSCalculationConstant;
import org.egov.wscalculation.producer.WSCalculationProducer;
import org.egov.wscalculation.repository.DemandRepository;
import org.egov.wscalculation.repository.ServiceRequestRepository;
import org.egov.wscalculation.repository.WSCalculationDao;
import org.egov.wscalculation.repository.WaterConnectionRepository;
import org.egov.wscalculation.util.CalculatorUtil;
import org.egov.wscalculation.util.WSCalculationUtil;
import org.egov.wscalculation.validator.WSCalculationWorkflowValidator;
import org.egov.wscalculation.web.models.BillResponseV2;
import org.egov.wscalculation.web.models.Calculation;
import org.egov.wscalculation.web.models.CalculationCriteria;
import org.egov.wscalculation.web.models.CalculationReq;
import org.egov.wscalculation.web.models.Demand;
import org.egov.wscalculation.web.models.Demand.StatusEnum;
import org.egov.wscalculation.web.models.DemandDetail;
import org.egov.wscalculation.web.models.DemandDetailAndCollection;
import org.egov.wscalculation.web.models.DemandRequest;
import org.egov.wscalculation.web.models.DemandResponse;
import org.egov.wscalculation.web.models.GetBillCriteria;
import org.egov.wscalculation.web.models.Property;
import org.egov.wscalculation.web.models.RequestInfoWrapper;
import org.egov.wscalculation.web.models.TaxHeadEstimate;
import org.egov.wscalculation.web.models.TaxPeriod;
import org.egov.wscalculation.web.models.WaterConnection;
import org.egov.wscalculation.web.models.WaterConnectionRequest;
import org.egov.wscalculation.web.models.WaterDetails;
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

	@Autowired
	private WSCalculationDao waterCalculatorDao;

	@Autowired
	private CalculatorUtil calculatorUtils;

	@Autowired
	private EstimationService estimationService;

	@Autowired
	private WSCalculationProducer wsCalculationProducer;

	@Autowired
	private WSCalculationUtil wsCalculationUtil;

	@Autowired
	private WSCalculationWorkflowValidator wsCalulationWorkflowValidator;

	@Autowired
	private WaterConnectionRepository waterConnectionRepository;
	/**
	 * Creates or updates Demand
	 * 
	 * @param requestInfo  The RequestInfo of the calculation request
	 * @param calculations The Calculation Objects for which demand has to be
	 *                     generated or updated
	 */
	public List<Demand> generateDemand(CalculationReq request, List<Calculation> calculations,
			Map<String, Object> masterMap, boolean isForConnectionNo) {
//		@SuppressWarnings("unchecked")
//		Map<String, Object> financialYearMaster = (Map<String, Object>) masterMap
//				.get(WSCalculationConstant.BILLING_PERIOD);
//		Long fromDate = (Long) financialYearMaster.get(WSCalculationConstant.STARTING_DATE_APPLICABLES);
//		Long toDate = (Long) financialYearMaster.get(WSCalculationConstant.ENDING_DATE_APPLICABLES);

		// List that will contain Calculation for new demands
		List<Calculation> createCalculations = new LinkedList<>();
		// List that will contain Calculation for old demands
		List<Calculation> updateCalculations = new LinkedList<>();
		List<Demand> demands = null;
		
		if (!CollectionUtils.isEmpty(calculations)) {
			// Collect required parameters for demand search
			String tenantId = calculations.get(0).getTenantId();
			Long fromDateSearch = null;
			Long toDateSearch = null;
			Set<String> consumerCodes;
			if (isForConnectionNo) {
				fromDateSearch = request.getTaxPeriodFrom();
				toDateSearch = request.getTaxPeriodTo();
				consumerCodes = calculations.stream().map(calculation -> calculation.getConnectionNo())
						.collect(Collectors.toSet());
			} else {
				consumerCodes = calculations.stream().map(calculation -> calculation.getApplicationNO())
						.collect(Collectors.toSet());
			}

			demands = searchDemand(tenantId, consumerCodes, fromDateSearch, toDateSearch,
					request.getRequestInfo());
			Set<String> connectionNumbersFromDemands = new HashSet<>();
			if (!CollectionUtils.isEmpty(demands))
				connectionNumbersFromDemands = demands.stream().map(Demand::getConsumerCode)
						.collect(Collectors.toSet());

			// If demand already exists add it updateCalculations else
			// createCalculations
			for (Calculation calculation : calculations) {
				if (!connectionNumbersFromDemands
						.contains(isForConnectionNo ? calculation.getConnectionNo() : calculation.getApplicationNO()))
					createCalculations.add(calculation);
				else
					updateCalculations.add(calculation);
			}
		}
		List<Demand> createdDemands = new ArrayList<>();
		if (!CollectionUtils.isEmpty(createCalculations))
			createdDemands = createDemand(request.getRequestInfo(), createCalculations, masterMap, isForConnectionNo,
					request.getTaxPeriodFrom(), request.getTaxPeriodTo());

		if (!CollectionUtils.isEmpty(updateCalculations)) {
			createdDemands = updateDemandForCalculation(request.getRequestInfo(), updateCalculations, request.getTaxPeriodFrom(), request.getTaxPeriodTo(),
					isForConnectionNo);
		}
		return createdDemands;
	}

	/**
	 * Creates or updates Demand
	 * 
	 * @param requestInfo  The RequestInfo of the calculation request
	 * @param calculations The Calculation Objects for which demand has to be
	 *                     generated or updated
	 */
	public List<Demand> generateDemandForBillingCycleInBulk(CalculationReq request, List<Calculation> calculations,
			Map<String, Object> masterMap, boolean isForConnectionNo) {

		boolean isDemandAvailable = false;
		List<Demand> createDemands = new ArrayList<>();
		List<Demand> updateDemands = new ArrayList<>();
		List<Demand> demandRes = new ArrayList<>();

		try {
		if (!CollectionUtils.isEmpty(calculations)) {
			for (Calculation calculation : calculations) {		
				// Collect required parameters for demand search
				String tenantId = calculation.getTenantId();
				Long fromDateSearch = null;
				Long toDateSearch = null;
				String consumerCodes;
				if (isForConnectionNo) {
					fromDateSearch = calculation.getFrom();
					toDateSearch = calculation.getTo();
					consumerCodes = calculation.getConnectionNo();
				} else {
					consumerCodes = calculation.getApplicationNO();
				}

				isDemandAvailable = waterCalculatorDao.isConnectionDemandAvailableForBillingCycle(tenantId, fromDateSearch, toDateSearch, consumerCodes);

				// If demand already exists add it updateCalculations else
				if (!isDemandAvailable)
					createDemands.add(createDemandForNonMeteredInBulk(request.getRequestInfo(), calculation, masterMap, isForConnectionNo,
							request.getTaxPeriodFrom(), request.getTaxPeriodTo()));
				else
					updateDemands.add(createDemandForNonMeteredInBulk(request.getRequestInfo(), calculation, masterMap, isForConnectionNo,
							request.getTaxPeriodFrom(), request.getTaxPeriodTo()));
			}
		}

		//Save the bulk demands for metered connections
		if (!createDemands.isEmpty()) {
			log.info("Creating Non metered Demands list size: {} and Demand Object" + createDemands.size(), createDemands.toString());
			demandRes.addAll(demandRepository.saveDemand(request.getRequestInfo(), createDemands));

		}
		//Save the bulk demands for non metered connections
		if(!updateDemands.isEmpty()) {
			log.info("Updating Non metered Demands list size: {} and Demand Object" + updateDemands.size(), updateDemands.toString());
			demandRes.addAll(demandRepository.updateDemand(request.getRequestInfo(), updateDemands));

		}
		}catch (Exception e) {
			e.printStackTrace();
		}

		return createDemands;
	}

	/**
	 * 
	 * @param requestInfo  RequestInfo
	 * @param calculations List of Calculation
	 * @param masterMap    Master MDMS Data
	 * @return Returns list of demands
	 */
	private List<Demand> createDemand(RequestInfo requestInfo, List<Calculation> calculations,
			Map<String, Object> masterMap, boolean isForConnectionNO, long taxPeriodFrom, long taxPeriodTo) {
		List<Demand> demandRes = new LinkedList<>();
		List<Demand> demandReq = new LinkedList<>();
		List<Demand> demandsForMetered = new LinkedList<>();

		for (Calculation calculation : calculations) {
			WaterConnection connection = calculation.getWaterConnection();
			if (connection == null) {
				throw new CustomException("INVALID_WATER_CONNECTION",
						"Demand cannot be generated for "
								+ (isForConnectionNO ? calculation.getConnectionNo() : calculation.getApplicationNO())
								+ " Water Connection with this number does not exist ");
			}
			WaterConnectionRequest waterConnectionRequest = WaterConnectionRequest.builder().waterConnection(connection)
					.requestInfo(requestInfo).build();
			
			log.info("waterConnectionRequest: {}",waterConnectionRequest);
			Property property = wsCalculationUtil.getProperty(waterConnectionRequest);
			log.info("Property: {}",property);
			
			String tenantId = calculation.getTenantId();
			String consumerCode = isForConnectionNO ? calculation.getConnectionNo() : calculation.getApplicationNO();
			User owner = property.getOwners().get(0).toCommonUser();
			if (!CollectionUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionHolders())) {
				owner = waterConnectionRequest.getWaterConnection().getConnectionHolders().get(0).toCommonUser();
			}
			List<DemandDetail> demandDetails = new LinkedList<>();
			calculation.getTaxHeadEstimates().forEach(taxHeadEstimate -> {
				demandDetails.add(DemandDetail.builder().taxAmount(taxHeadEstimate.getEstimateAmount())
						.taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode()).collectionAmount(BigDecimal.ZERO)
						.tenantId(tenantId).build());
			});
			@SuppressWarnings("unchecked")
			Map<String, Object> financialYearMaster = (Map<String, Object>) masterMap
					.get(WSCalculationConstant.BILLING_PERIOD);

			if (taxPeriodFrom == 0 && taxPeriodTo == 0) {
				taxPeriodFrom = (Long) financialYearMaster.get(WSCalculationConstant.STARTING_DATE_APPLICABLES);
				taxPeriodTo = (Long) financialYearMaster.get(WSCalculationConstant.ENDING_DATE_APPLICABLES);
			}
			Long expiryDaysInmillies = (Long) financialYearMaster.get(WSCalculationConstant.Demand_Expiry_Date_String);
			//Long expiryDate = System.currentTimeMillis() + expiryDaysInmillies;

			BigDecimal minimumPayableAmount = calculation.getTotalAmount();
			String businessService = isForConnectionNO ? configs.getBusinessService()
					: WSCalculationConstant.ONE_TIME_FEE_SERVICE_FIELD;

			addRoundOffTaxHead(calculation.getTenantId(), demandDetails);
			Demand demand = Demand.builder().consumerCode(consumerCode).demandDetails(demandDetails).payer(owner)
						.minimumAmountPayable(minimumPayableAmount).tenantId(tenantId).taxPeriodFrom(taxPeriodFrom)
						.taxPeriodTo(taxPeriodTo).consumerType("waterConnection").businessService(businessService)
						.status(StatusEnum.valueOf("ACTIVE")).billExpiryTime(expiryDaysInmillies).build();
						
			// For the metered connections demand has to create one by one
			if (WSCalculationConstant.meteredConnectionType.equalsIgnoreCase(connection.getConnectionType())) {
				demandsForMetered.add(demand);
				
			} else {
				demandReq.add(demand);
			}
				 
			
		}
		//Save the bulk demands for metered connections
		if (!demandsForMetered.isEmpty()) {
			log.info("Demands list size: {} and Demand Object" + demandsForMetered.toString(), demandsForMetered.size());
			demandRes.addAll(demandRepository.saveDemand(requestInfo, demandsForMetered));
			fetchBill(demandRes, requestInfo);
			
		}
		//Save the bulk demands for non metered connections
		if(!demandReq.isEmpty()) {
		log.info("Non metered Demands list size: {} and Demand Object" + demandReq.toString(), demandReq.size());
		demandRes.addAll(demandRepository.saveDemand(requestInfo, demandReq));
		
		}
		
		return demandRes;
	}
	
	/**
	 * 
	 * @param requestInfo  RequestInfo
	 * @param calculations List of Calculation
	 * @param masterMap    Master MDMS Data
	 * @return Returns list of demands
	 */
	private Demand createDemandForNonMeteredInBulk(RequestInfo requestInfo, Calculation calculation,
			Map<String, Object> masterMap, boolean isForConnectionNO, long taxPeriodFrom, long taxPeriodTo) {

			WaterConnection connection = calculation.getWaterConnection();
			if (connection == null) {
				throw new CustomException("INVALID_WATER_CONNECTION",
						"Demand cannot be generated for "
								+ (isForConnectionNO ? calculation.getConnectionNo() : calculation.getApplicationNO())
								+ " Water Connection with this number does not exist ");
			}
			WaterConnectionRequest waterConnectionRequest = WaterConnectionRequest.builder().waterConnection(connection)
					.requestInfo(requestInfo).build();
			
			log.info("waterConnectionRequest: {}",waterConnectionRequest);
			Property property = wsCalculationUtil.getProperty(waterConnectionRequest);
			log.info("Property: {}",property);
			
			String tenantId = calculation.getTenantId();
			String consumerCode = isForConnectionNO ? calculation.getConnectionNo() : calculation.getApplicationNO();
			User owner = property.getOwners().get(0).toCommonUser();
			if (!CollectionUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionHolders())) {
				owner = waterConnectionRequest.getWaterConnection().getConnectionHolders().get(0).toCommonUser();
			}
			List<DemandDetail> demandDetails = new LinkedList<>();
			calculation.getTaxHeadEstimates().forEach(taxHeadEstimate -> {
				demandDetails.add(DemandDetail.builder().taxAmount(taxHeadEstimate.getEstimateAmount())
						.taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode()).collectionAmount(BigDecimal.ZERO)
						.tenantId(tenantId).build());
			});
			@SuppressWarnings("unchecked")
			Map<String, Object> financialYearMaster = (Map<String, Object>) masterMap
					.get(WSCalculationConstant.BILLING_PERIOD);

			if (taxPeriodFrom == 0 && taxPeriodTo == 0) {
				taxPeriodFrom = (Long) financialYearMaster.get(WSCalculationConstant.STARTING_DATE_APPLICABLES);
				taxPeriodTo = (Long) financialYearMaster.get(WSCalculationConstant.ENDING_DATE_APPLICABLES);
			}
			Long expiryDaysInmillies = (Long) financialYearMaster.get(WSCalculationConstant.Demand_Expiry_Date_String);
			//Long expiryDate = System.currentTimeMillis() + expiryDaysInmillies;

			BigDecimal minimumPayableAmount = calculation.getTotalAmount();
			String businessService = isForConnectionNO ? configs.getBusinessService()
					: WSCalculationConstant.ONE_TIME_FEE_SERVICE_FIELD;

			addRoundOffTaxHead(calculation.getTenantId(), demandDetails);
			Demand demand = Demand.builder().consumerCode(consumerCode).demandDetails(demandDetails).payer(owner)
						.minimumAmountPayable(minimumPayableAmount).tenantId(tenantId).taxPeriodFrom(taxPeriodFrom)
						.taxPeriodTo(taxPeriodTo).consumerType("waterConnection").businessService(businessService)
						.status(StatusEnum.valueOf("ACTIVE")).billExpiryTime(expiryDaysInmillies).build();
						
		return demand;
	}


	/**
	 * Returns the list of new DemandDetail to be added for updating the demand
	 * 
	 * @param calculation   The calculation object for the update request
	 * @param demandDetails The list of demandDetails from the existing demand
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
		List<DemandDetail> combinedBillDetails = new LinkedList<>(demandDetails);
		combinedBillDetails.addAll(newDemandDetails);
		addRoundOffTaxHead(calculation.getTenantId(), combinedBillDetails);
		return combinedBillDetails;
	}

	/**
	 * Adds roundOff taxHead if decimal values exists
	 * 
	 * @param tenantId      The tenantId of the demand
	 * @param demandDetails The list of demandDetail
	 */
	private void addRoundOffTaxHead(String tenantId, List<DemandDetail> demandDetails) {
		BigDecimal totalTax = BigDecimal.ZERO;

		BigDecimal previousRoundOff = BigDecimal.ZERO;

		/*
		 * Sum all taxHeads except RoundOff as new roundOff will be calculated
		 */
		for (DemandDetail demandDetail : demandDetails) {
			if (!demandDetail.getTaxHeadMasterCode().equalsIgnoreCase(WSCalculationConstant.WS_Round_Off))
				totalTax = totalTax.add(demandDetail.getTaxAmount());
			else
				previousRoundOff = previousRoundOff.add(demandDetail.getTaxAmount());
		}

		BigDecimal decimalValue = totalTax.remainder(BigDecimal.ONE);
		BigDecimal midVal = BigDecimal.valueOf(0.5);
		BigDecimal roundOff = BigDecimal.ZERO;

		/*
		 * If the decimal amount is greater than 0.5 we subtract it from 1 and put it as
		 * roundOff taxHead so as to nullify the decimal eg: If the tax is 12.64 we will
		 * add extra tax roundOff taxHead of 0.36 so that the total becomes 13
		 */
		if (decimalValue.compareTo(midVal) >= 0)
			roundOff = BigDecimal.ONE.subtract(decimalValue);

		/*
		 * If the decimal amount is less than 0.5 we put negative of it as roundOff
		 * taxHead so as to nullify the decimal eg: If the tax is 12.36 we will add
		 * extra tax roundOff taxHead of -0.36 so that the total becomes 12
		 */
		if (decimalValue.compareTo(midVal) < 0)
			roundOff = decimalValue.negate();

		/*
		 * If roundOff already exists in previous demand create a new roundOff taxHead
		 * with roundOff amount equal to difference between them so that it will be
		 * balanced when bill is generated. eg: If the previous roundOff amount was of
		 * -0.36 and the new roundOff excluding the previous roundOff is 0.2 then the
		 * new roundOff will be created with 0.2 so that the net roundOff will be 0.2
		 * -(-0.36)
		 */
		if (previousRoundOff.compareTo(BigDecimal.ZERO) != 0) {
			roundOff = roundOff.subtract(previousRoundOff);
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
	 * @param tenantId      The tenantId of the tradeLicense
	 * @param consumerCodes The set of consumerCode of the demands
	 * @param requestInfo   The RequestInfo of the incoming request
	 * @return Lis to demands for the given consumerCode
	 */
	private List<Demand> searchDemand(String tenantId, Set<String> consumerCodes, Long taxPeriodFrom, Long taxPeriodTo,
			RequestInfo requestInfo) {
		Object result = serviceRequestRepository.fetchResult(
				getDemandSearchURL(tenantId, consumerCodes, taxPeriodFrom, taxPeriodTo),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());
		try {
			return mapper.convertValue(result, DemandResponse.class).getDemands();
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING_ERROR", "Failed to parse response from Demand Search");
		}

	}

	/**
	 * Creates demand Search url based on tenantId,businessService, and
	 * 
	 * @return demand search url
	 */
	public StringBuilder getDemandSearchURLForDemandId() {
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
		url.append("isPaymentCompleted=false");
		return url;
	}

	/**
	 * 
	 * @param tenantId    TenantId
	 * @param consumerCode    Connection Number
	 * @param requestInfo - RequestInfo
	 * @return List of Demand
	 */
	private List<Demand> searchDemandBasedOnConsumerCode(String tenantId, String consumerCode, RequestInfo requestInfo) {
		String uri = getDemandSearchURLForDemandId().toString();
		uri = uri.replace("{1}", tenantId);
		uri = uri.replace("{2}", configs.getBusinessService());
		uri = uri.replace("{3}", consumerCode);
		Object result = serviceRequestRepository.fetchResult(new StringBuilder(uri),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());
		try {
			return mapper.convertValue(result, DemandResponse.class).getDemands();
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING_ERROR", "Failed to parse response from Demand Search");
		}
	}

	/**
	 * Creates demand Search url based on tenantId,businessService, period from,
	 * period to and ConsumerCode
	 * 
	 * @return demand search url
	 */
	public StringBuilder getDemandSearchURL(String tenantId, Set<String> consumerCodes, Long taxPeriodFrom,
			Long taxPeriodTo) {
		StringBuilder url = new StringBuilder(configs.getBillingServiceHost());
		String businessService = taxPeriodFrom == null ? WSCalculationConstant.ONE_TIME_FEE_SERVICE_FIELD
				: configs.getBusinessService();
		url.append(configs.getDemandSearchEndPoint());
		url.append("?");
		url.append("tenantId=");
		url.append(tenantId);
		url.append("&");
		url.append("businessService=");
		url.append(businessService);
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
	 * 
	 * @param getBillCriteria    Bill Criteria
	 * @param requestInfoWrapper contains request info wrapper
	 * @return updated demand response
	 */
	public List<Demand> updateDemands(GetBillCriteria getBillCriteria, RequestInfoWrapper requestInfoWrapper) {

		if (getBillCriteria.getAmountExpected() == null)
			getBillCriteria.setAmountExpected(BigDecimal.ZERO);
		RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();
		Map<String, JSONArray> billingSlabMaster = new HashMap<>();

		Map<String, JSONArray> timeBasedExemptionMasterMap = new HashMap<>();
		mstrDataService.setWaterConnectionMasterValues(requestInfo, getBillCriteria.getTenantId(), billingSlabMaster,
				timeBasedExemptionMasterMap);

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

		// Loop through the consumerCodes and re-calculate the time base applicable
		Map<String, Demand> consumerCodeToDemandMap = res.getDemands().stream()
				.collect(Collectors.toMap(Demand::getId, Function.identity()));
		List<Demand> demandsToBeUpdated = new LinkedList<>();
		boolean isMigratedCon = isMigratedConnection(getBillCriteria.getConsumerCodes().get(0),getBillCriteria.getTenantId());
		log.info("-------updateDemands------------isMigratedCon--------"+isMigratedCon);
		List<Demand> demands = res.getDemands();
		demands.sort( (d1,d2)-> d1.getTaxPeriodFrom().compareTo(d2.getTaxPeriodFrom()));
		log.info("-------updateDemands------------demands--------"+demands);
		Demand oldDemand = demands.get(0);
		log.info("-------updateDemands------------oldDemand--------"+oldDemand);
		String tenantId = getBillCriteria.getTenantId();

		List<TaxPeriod> taxPeriods = mstrDataService.getTaxPeriodList(requestInfoWrapper.getRequestInfo(), tenantId,
				WSCalculationConstant.SERVICE_FIELD_VALUE_WS);

		consumerCodeToDemandMap.forEach((id, demand) -> {
			BigDecimal totalTax = demand.getDemandDetails().stream().map(DemandDetail::getTaxAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			BigDecimal totalCollection = demand.getDemandDetails().stream().map(DemandDetail::getCollectionAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			List<String> taxHeadMasterCodes = demand.getDemandDetails().stream().map(DemandDetail::getTaxHeadMasterCode).collect(Collectors.toList());;
			if (!(isMigratedCon && oldDemand.getId().equalsIgnoreCase(demand.getId()))) {
				log.info("-------updateDemands-----inside if-------demand.getId()--------"+demand.getId()+"-------oldDemand.getId()---------"+oldDemand.getId());
				if (!demand.getIsPaymentCompleted() && totalTax.compareTo(totalCollection) > 0
						&& !taxHeadMasterCodes.contains(WSCalculationConstant.WS_TIME_PENALTY)) {
					if (demand.getStatus() != null && WSCalculationConstant.DEMAND_CANCELLED_STATUS
							.equalsIgnoreCase(demand.getStatus().toString()))
						throw new CustomException(WSCalculationConstant.EG_WS_INVALID_DEMAND_ERROR,
								WSCalculationConstant.EG_WS_INVALID_DEMAND_ERROR_MSG);
					applyTimeBasedApplicables(demand, requestInfoWrapper, timeBasedExemptionMasterMap, taxPeriods);
					addRoundOffTaxHead(tenantId, demand.getDemandDetails());
					demandsToBeUpdated.add(demand);
				}
		}
		});

		// Call demand update in bulk to update the interest or penalty
		DemandRequest request = DemandRequest.builder().demands(demandsToBeUpdated).requestInfo(requestInfo).build();
		repository.fetchResult(utils.getUpdateDemandUrl(), request);
		return res.getDemands();

	}
	
	private boolean isMigratedConnection(final String connectionNumber, final String tenantId) {
		Boolean isMigrated = false;
		String connectionAddlDetail = waterConnectionRepository.fetchConnectionAdditonalDetails(connectionNumber,
				tenantId);
		log.info("isMigratedConnection-----connectionAddlDetail-->" + connectionAddlDetail);
		Map<String, Object> result = null;
		try {
			result = mapper.readValue(connectionAddlDetail, HashMap.class);
			isMigrated = (Boolean) result.getOrDefault("isMigrated", false);
		} catch (Exception e) {
			log.error("Exception while reading connection migration flag");
		}
		log.info("isMigratedConnection-----isMigrated-->" + isMigrated);
		return isMigrated;

	}

	/**
	 * Updates demand for the given list of calculations
	 * 
	 * @param requestInfo  The RequestInfo of the calculation request
	 * @param calculations List of calculation object
	 * @return Demands that are updated
	 */
	private List<Demand> updateDemandForCalculation(RequestInfo requestInfo, List<Calculation> calculations,
			Long fromDate, Long toDate, boolean isForConnectionNo) {
		List<Demand> demands = new LinkedList<>();
		Long fromDateSearch = isForConnectionNo ? fromDate : null;
		Long toDateSearch = isForConnectionNo ? toDate : null;

		for (Calculation calculation : calculations) {
			Set<String> consumerCodes = isForConnectionNo
					? Collections.singleton(calculation.getWaterConnection().getConnectionNo())
					: Collections.singleton(calculation.getWaterConnection().getApplicationNo());
			List<Demand> searchResult = searchDemand(calculation.getTenantId(), consumerCodes, fromDateSearch,
					toDateSearch, requestInfo);
			if (CollectionUtils.isEmpty(searchResult))
				throw new CustomException("INVALID_DEMAND_UPDATE",
						"No demand exists for Number: " + consumerCodes.toString());
			Demand demand = searchResult.get(0);
			demand.setDemandDetails(getUpdatedDemandDetails(calculation, demand.getDemandDetails()));

			if (isForConnectionNo) {
				WaterConnection connection = calculation.getWaterConnection();
				if (connection == null) {
					List<WaterConnection> waterConnectionList = calculatorUtils.getWaterConnection(requestInfo,
							calculation.getConnectionNo(), calculation.getTenantId());
					int size = waterConnectionList.size();
					connection = waterConnectionList.get(size - 1);

				}

				if (("MODIFY_WATER_CONNECTION").equalsIgnoreCase(connection.getApplicationType())) {
					WaterConnectionRequest waterConnectionRequest = WaterConnectionRequest.builder()
							.waterConnection(connection).requestInfo(requestInfo).build();
					Property property = wsCalculationUtil.getProperty(waterConnectionRequest);
					User owner = property.getOwners().get(0).toCommonUser();
					if (!CollectionUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionHolders())) {
						owner = waterConnectionRequest.getWaterConnection().getConnectionHolders().get(0)
								.toCommonUser();
					}
					if (!(demand.getPayer().getUuid().equalsIgnoreCase(owner.getUuid())))
						demand.setPayer(owner);
				}

			}

			demands.add(demand);
		}

		log.info("Updated Demand Details " + demands.toString());
		return demandRepository.updateDemand(requestInfo, demands);
	}

	/**
	 * Applies Penalty/Rebate/Interest to the incoming demands
	 * 
	 * If applied already then the demand details will be updated
	 * 
	 * @param demand                      - Demand Object
	 * @param requestInfoWrapper          RequestInfoWrapper Object
	 * @param timeBasedExemptionMasterMap - List of TimeBasedExemption details
	 * @param taxPeriods                  - List of tax periods
	 * @return Returns TRUE if successful, FALSE otherwise
	 */

	private boolean applyTimeBasedApplicables(Demand demand, RequestInfoWrapper requestInfoWrapper,
			Map<String, JSONArray> timeBasedExemptionMasterMap, List<TaxPeriod> taxPeriods) {

		String tenantId = demand.getTenantId();
		String demandId = demand.getId();
		Long expiryDate = demand.getBillExpiryTime();
		TaxPeriod taxPeriod = taxPeriods.stream().filter(t -> demand.getTaxPeriodFrom().compareTo(t.getFromDate()) >= 0
				&& demand.getTaxPeriodTo().compareTo(t.getToDate()) <= 0).findAny().orElse(null);

		if (taxPeriod == null) {
			log.info("Demand Expired!! ->> Consumer Code " + demand.getConsumerCode() + " Demand Id -->> "
					+ demand.getId());
			return false;
		}
		boolean isCurrentDemand = false;
		if (!(taxPeriod.getFromDate() <= System.currentTimeMillis()
				&& taxPeriod.getToDate() >= System.currentTimeMillis()))
			isCurrentDemand = true;

		if (expiryDate < System.currentTimeMillis()) {
			BigDecimal waterChargeApplicable = BigDecimal.ZERO;
			BigDecimal oldPenalty = BigDecimal.ZERO;
			BigDecimal oldInterest = BigDecimal.ZERO;

			for (DemandDetail detail : demand.getDemandDetails()) {
				if (WSCalculationConstant.TAX_APPLICABLE.contains(detail.getTaxHeadMasterCode())) {
					waterChargeApplicable = waterChargeApplicable.add(detail.getTaxAmount());
				}
				if (detail.getTaxHeadMasterCode().equalsIgnoreCase(WSCalculationConstant.WS_TIME_PENALTY)) {
					oldPenalty = oldPenalty.add(detail.getTaxAmount());
				}
				if (detail.getTaxHeadMasterCode().equalsIgnoreCase(WSCalculationConstant.WS_TIME_INTEREST)) {
					oldInterest = oldInterest.add(detail.getTaxAmount());
				}
			}

			boolean isPenaltyUpdated = false;
			boolean isInterestUpdated = false;

			List<DemandDetail> details = demand.getDemandDetails();

			Map<String, BigDecimal> interestPenaltyEstimates = payService.applyPenaltyRebateAndInterest(
					waterChargeApplicable, taxPeriod.getFinancialYear(), timeBasedExemptionMasterMap, expiryDate);
			if (null == interestPenaltyEstimates)
				return isCurrentDemand;

			BigDecimal penalty = interestPenaltyEstimates.get(WSCalculationConstant.WS_TIME_PENALTY);
			BigDecimal interest = interestPenaltyEstimates.get(WSCalculationConstant.WS_TIME_INTEREST);
			
			if(penalty == null)
				penalty = BigDecimal.ZERO;
			if(interest == null)
				interest = BigDecimal.ZERO;

			DemandDetailAndCollection latestPenaltyDemandDetail, latestInterestDemandDetail;

			if (interest.compareTo(BigDecimal.ZERO) != 0) {
				latestInterestDemandDetail = utils
						.getLatestDemandDetailByTaxHead(WSCalculationConstant.WS_TIME_INTEREST, details);
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
				details.add(DemandDetail.builder().taxAmount(penalty.setScale(2, 2))
						.taxHeadMasterCode(WSCalculationConstant.WS_TIME_PENALTY).demandId(demandId).tenantId(tenantId)
						.build());
			if (!isInterestUpdated && interest.compareTo(BigDecimal.ZERO) > 0)
				details.add(DemandDetail.builder().taxAmount(interest.setScale(2, 2))
						.taxHeadMasterCode(WSCalculationConstant.WS_TIME_INTEREST).demandId(demandId).tenantId(tenantId)
						.build());
		}

		return isCurrentDemand;
	}

	/**
	 * Updates the amount in the latest demandDetail by adding the diff between new
	 * and old amounts to it
	 * 
	 * @param newAmount        The new tax amount for the taxHead
	 * @param latestDetailInfo The latest demandDetail for the particular taxHead
	 */
	private void updateTaxAmount(BigDecimal newAmount, DemandDetailAndCollection latestDetailInfo) {
		BigDecimal diff = newAmount.subtract(latestDetailInfo.getTaxAmountForTaxHead());
		BigDecimal newTaxAmountForLatestDemandDetail = latestDetailInfo.getLatestDemandDetail().getTaxAmount()
				.add(diff);
		latestDetailInfo.getLatestDemandDetail().setTaxAmount(newTaxAmountForLatestDemandDetail);
	}

	/**
	 * 
	 * @param tenantId TenantId for getting master data.
	 */
	public void generateDemandForTenantId(String tenantId, RequestInfo requestInfo) {
		requestInfo.getUserInfo().setTenantId(tenantId);
		Map<String, Object> billingMasterData = calculatorUtils.loadBillingFrequencyMasterData(requestInfo, tenantId);
		long taxPeriodFrom = billingMasterData.get("taxPeriodFrom") == null ? 0l
				: (long) billingMasterData.get("taxPeriodFrom");
		long taxPeriodTo = billingMasterData.get("taxPeriodTo") == null ? 0l : (long) billingMasterData.get("taxPeriodTo");
		if(taxPeriodFrom == 0 || taxPeriodTo == 0) {
			throw new CustomException("NO_BILLING_PERIODS","Billing Period does not available for tenant: "+ tenantId);
		}
		
		generateDemandForULB(billingMasterData, requestInfo, tenantId, taxPeriodFrom, taxPeriodTo);
	}

	/**
	 * 
	 * @param master      Master MDMS Data
	 * @param requestInfo Request Info
	 * @param tenantId    Tenant Id
	 */
	public void generateDemandForULB(Map<String, Object> master, RequestInfo requestInfo, String tenantId,
			Long taxPeriodFrom, Long taxPeriodTo) {
		try {
			List<TaxPeriod> taxPeriods = calculatorUtils.getTaxPeriodsFromMDMS(requestInfo, tenantId);

//			java.util.Optional<TaxPeriod> matchingObject = taxPeriods.stream().
//				    filter(p -> p.getFromDate().equals(taxPeriodFrom)).findFirst();
			
			int generateDemandToIndex = IntStream.range(0, taxPeriods.size())
				     .filter(p -> taxPeriodFrom.equals(taxPeriods.get(p).getFromDate()))
				     .findFirst().getAsInt();
			
			log.info("Billing master data values for non metered connection:: {}", master);
			List<WaterDetails> connectionNos = waterCalculatorDao.getConnectionsNoList(tenantId,
					WSCalculationConstant.nonMeterdConnection, taxPeriodFrom, taxPeriodTo);

			//Generate bulk demands for connections in below count
			int bulkSaveDemandCount = configs.getBulkSaveDemandCount() != null ? configs.getBulkSaveDemandCount() : 1;
			log.info("connectionNos: {} and bulkSaveDemandCount: {}", connectionNos.size(), bulkSaveDemandCount);
			List<CalculationCriteria> calculationCriteriaList = new ArrayList<>();
			for (int connectionNosIndex = 0; connectionNosIndex < connectionNos.size(); connectionNosIndex++) {
				WaterDetails waterConnection = connectionNos.get(connectionNosIndex);
				
				try {
					int generateDemandFromIndex = 0;
					Long lastDemandFromDate = waterCalculatorDao.searchLastDemandGenFromDate(waterConnection.getConnectionNo(), tenantId);
					
					if(lastDemandFromDate != null) {
					generateDemandFromIndex = IntStream.range(0, taxPeriods.size())
						     .filter(p -> lastDemandFromDate.equals(taxPeriods.get(p).getFromDate()))
						     .findFirst().getAsInt();
					//Increased one index to generate the next quarter demand
					generateDemandFromIndex++;
					}
					log.info("lastDemandFromDate: {} and generateDemandFromIndex: {}",lastDemandFromDate, generateDemandFromIndex);
					
					for (int taxPeriodIndex = generateDemandFromIndex; generateDemandFromIndex <= generateDemandToIndex; taxPeriodIndex++) {
						generateDemandFromIndex++;
						TaxPeriod taxPeriod = taxPeriods.get(taxPeriodIndex);
						log.info("FromPeriod: {} and ToPeriod: {}",taxPeriod.getFromDate(),taxPeriod.getToDate());
						log.info("taxPeriodIndex: {} and generateDemandFromIndex: {} and generateDemandToIndex: {}",taxPeriodIndex, generateDemandFromIndex, generateDemandToIndex);

						boolean isConnectionValid = isValidBillingCycle(waterConnection, requestInfo, tenantId,
								taxPeriod.getFromDate(), taxPeriod.getToDate());
						if (isConnectionValid) {
							
							CalculationCriteria calculationCriteria = CalculationCriteria.builder().tenantId(tenantId)
									.assessmentYear(taxPeriod.getFinancialYear())
									.from(taxPeriod.getFromDate())
									.to(taxPeriod.getToDate())
									.connectionNo(waterConnection.getConnectionNo())
									.build();
							calculationCriteriaList.add(calculationCriteria);
							log.info("connectionNosIndex: {} and connectionNos.size(): {}",connectionNosIndex, connectionNos.size());

							if(connectionNosIndex == bulkSaveDemandCount || 
									(connectionNosIndex == connectionNos.size()-1 && taxPeriodIndex == generateDemandToIndex)) {
								log.info("Controller entered into producer logic: ",connectionNosIndex, connectionNos.size());

								CalculationReq calculationReq = CalculationReq.builder()
										.calculationCriteria(calculationCriteriaList)
//										.taxPeriodFrom(taxPeriod.getFromDate())
//										.taxPeriodTo(taxPeriod.getToDate())
										.requestInfo(requestInfo)
										.isconnectionCalculation(true)
										.build();
								log.info("Pushing calculation req to the kafka topic with bulk data of calculationCriteriaList size: {}", calculationCriteriaList.size());
								wsCalculationProducer.push(configs.getCreateDemand(), calculationReq);
								calculationCriteriaList.clear();
							} 
						}
					}
				}catch (Exception e) {
					e.printStackTrace();
					log.error("Exception occurred while generating demand for water connectionno: "+waterConnection.getConnectionNo() + " tenantId: "+tenantId);
				}

			}
		}catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occurred while processing the demand generation for tenantId: "+tenantId);
		}
	}

	private boolean isValidBillingCycle(WaterDetails waterConnection, RequestInfo requestInfo, String tenantId,
			long taxPeriodFrom, long taxPeriodTo) {
		// TODO Auto-generated method stub

		boolean isConnectionValid = true;

		if (waterConnection.getConnectionExecutionDate() > taxPeriodTo)
			isConnectionValid = false;
		/*
		 * if (waterConnection.getConnectionExecutionDate() < taxPeriodFrom) {
		 * 
		 * isConnectionValid = fetchBill(waterConnection, taxPeriodFrom, taxPeriodTo,
		 * tenantId, requestInfo);
		 * 
		 * }
		 */

		return isConnectionValid;

	}

	private boolean fetchBill(WaterDetails waterConnection, long taxPeriodFrom, long taxPeriodTo, String tenantId,
			RequestInfo requestInfo) {

		final boolean[] isConnectionValid = { false };

		Object result = serviceRequestRepository.fetchResult(
				calculatorUtils.getFetchBillURL(tenantId, waterConnection.getConnectionNo()),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());
		BillResponseV2 billResponse = mapper.convertValue(result, BillResponseV2.class);
		billResponse.getBill().forEach(bill -> {
			bill.getBillDetails().forEach(billDetail -> {

				long previousBillingCycleToDate = taxPeriodFrom - 86400000;
				if (billDetail.getToPeriod() == previousBillingCycleToDate) {
					isConnectionValid[0] = true;
				}

			});

		});

		return isConnectionValid[0];

	}

	/**
	 * -
	 * 
	 * @param billingFrequency Billing Frequency details
	 * @param dayOfMonth       Day of the given month
	 * @return true if current day is for generation of demand
	 */
	private boolean isCurrentDateIsMatching(String billingFrequency, long dayOfMonth) {
		if (billingFrequency.equalsIgnoreCase(WSCalculationConstant.Monthly_Billing_Period)
				&& (dayOfMonth == LocalDateTime.now().getDayOfMonth())) {
			return true;
		} else if (billingFrequency.equalsIgnoreCase(WSCalculationConstant.Quaterly_Billing_Period)) {
			return true;
		}
		return true;
	}

	public boolean fetchBill(List<Demand> demandResponse, RequestInfo requestInfo) {
		boolean notificationSent = false;
		for (Demand demand : demandResponse) {
			try {
				Object result = serviceRequestRepository.fetchResult(
						calculatorUtils.getFetchBillURL(demand.getTenantId(), demand.getConsumerCode()),
						RequestInfoWrapper.builder().requestInfo(requestInfo).build());
				HashMap<String, Object> billResponse = new HashMap<>();
				billResponse.put("requestInfo", requestInfo);
				billResponse.put("billResponse", result);
				wsCalculationProducer.push(configs.getPayTriggers(), billResponse);
				notificationSent = true;
			} catch (Exception ex) {
				log.error("Fetch Bill Error", ex);
			}
		}
		return notificationSent;
	}

	/**
	 * compare and update the demand details
	 * 
	 * @param calculation   - Calculation object
	 * @param demandDetails - List Of Demand Details
	 * @return combined demand details list
	 */
	private List<DemandDetail> getUpdatedAdhocTax(Calculation calculation, List<DemandDetail> demandDetails) {

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
		List<DemandDetail> combinedBillDetails = new LinkedList<>(demandDetails);
		combinedBillDetails.addAll(newDemandDetails);
		addRoundOffTaxHead(calculation.getTenantId(), combinedBillDetails);
		return combinedBillDetails;
	}

	/**
	 * Search demand based on demand id and updated the tax heads with new adhoc tax
	 * heads
	 * 
	 * @param requestInfo  - Request Info Object
	 * @param calculations - List of Calculation to update the Demand
	 * @return List of calculation
	 */
	public List<Calculation> updateDemandForAdhocTax(RequestInfo requestInfo, List<Calculation> calculations) {
		List<Demand> demands = new LinkedList<>();
		for (Calculation calculation : calculations) {
			String consumerCode = calculation.getConnectionNo();
			List<Demand> searchResult = searchDemandBasedOnConsumerCode(calculation.getTenantId(), consumerCode,
					requestInfo);
			if (CollectionUtils.isEmpty(searchResult))
				throw new CustomException("INVALID_DEMAND_UPDATE",
						"No demand exists for Number: " + consumerCode);
			
			Collections.sort(searchResult, new Comparator<Demand>() {
				@Override
				public int compare(Demand d1, Demand d2) {
					return d1.getTaxPeriodFrom().compareTo(d2.getTaxPeriodFrom());
				}
			});
			
			Demand demand = searchResult.get(0);
			demand.setDemandDetails(getUpdatedAdhocTax(calculation, demand.getDemandDetails()));
			demands.add(demand);
		}

		log.info("Updated Demand Details " + demands.toString());
		demandRepository.updateDemand(requestInfo, demands);
		return calculations;
	}
	
	public Boolean fetchBillScheduler(Set<String> consumerCodes,String tenantId, RequestInfo requestInfo) {
		for (String consumerCode : consumerCodes) {
			try {
				Object result = serviceRequestRepository.fetchResult(
						calculatorUtils.getFetchBillURL(tenantId, consumerCode),
						RequestInfoWrapper.builder().requestInfo(requestInfo).build());

			} catch (Exception ex) {
				log.error("Fetch Bill Error For tenantId:{} consumercode: {} and Exception is: {}",tenantId,consumerCodes, ex);
			}
		}
		return Boolean.TRUE;
	}
	
    /**
     * Creates demand
     * @param requestInfo The RequestInfo of the calculation Request
     * @param demands The demands to be created
     * @return The list of demand created
     */
	public void saveDemand(RequestInfo requestInfo, List<Demand> demands){
		try{
			DemandRequest request = new DemandRequest(requestInfo,demands);
			wsCalculationProducer.push(configs.getSaveDemand(), request);
		}catch(Exception e){
			throw new CustomException("PARSING_ERROR","Failed to push the save demand data to kafka topic");
		}
	}
	


}