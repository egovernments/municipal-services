package org.egov.wsCalculation.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.CalculationCriteria;
import org.egov.wsCalculation.model.CalculationReq;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.MdmsResponse;
import org.egov.mdms.model.ModuleDetail;
import org.egov.wsCalculation.model.RequestInfoWrapper;
import org.egov.wsCalculation.model.TaxHeadMaster;
import org.egov.wsCalculation.model.TaxHeadMasterResponse;
import org.egov.wsCalculation.model.TaxPeriod;
import org.egov.wsCalculation.model.TaxPeriodResponse;
import org.egov.wsCalculation.repository.ServiceRequestRepository;
import org.egov.wsCalculation.util.CalculatorUtil;
import org.egov.wsCalculation.util.WSCalculationUtil;
import org.javers.common.collections.Arrays;
import org.egov.wsCalculation.config.WSCalculationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
@Slf4j
@Service
public class MasterDataService {

	@Autowired
	private ServiceRequestRepository repository;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WSCalculationUtil wSCalculationUtil;

	@Autowired
	private WSCalculationConfiguration config;

	@Autowired
	CalculatorUtil calculatorUtils;
	
	@Autowired
	EstimationService estimationService;

	/**
	 * Fetches and creates map of all required masters
	 * 
	 * @param request
	 *            The calculation request
	 * @return
	 */
	public Map<String, Object> getMasterMap(CalculationReq request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getCalculationCriteria().get(0).getTenantId();
		Map<String, Object> masterMap = new HashMap<>();
		List<TaxPeriod> taxPeriods = getTaxPeriodList(requestInfo, tenantId);
		List<TaxHeadMaster> taxHeadMasters = getTaxHeadMasterMap(requestInfo, tenantId);
		Map<String, Map<String, Object>> financialYearMaster = getFinancialYear(request);
		masterMap.put(WSCalculationConstant.TAXPERIOD_MASTER_KEY, taxPeriods);
		masterMap.put(WSCalculationConstant.TAXHEADMASTER_MASTER_KEY, taxHeadMasters);
		masterMap.put(WSCalculationConstant.FINANCIALYEAR_MASTER_KEY, financialYearMaster);
		return masterMap;
	}

	/**
	 * Fetch Tax Head Masters From billing service
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @return
	 */
	public List<TaxPeriod> getTaxPeriodList(RequestInfo requestInfo, String tenantId) {

		StringBuilder uri = wSCalculationUtil.getTaxPeriodSearchUrl(tenantId);
		TaxPeriodResponse res = mapper.convertValue(
				repository.fetchResult(uri, RequestInfoWrapper.builder().requestInfo(requestInfo).build()),
				TaxPeriodResponse.class);
		return res.getTaxPeriods();
	}

	/**
	 * Fetch Tax Head Masters From billing service
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @return
	 */
	public List<TaxHeadMaster> getTaxHeadMasterMap(RequestInfo requestInfo, String tenantId) {

		StringBuilder uri = wSCalculationUtil.getTaxHeadSearchUrl(tenantId);
		TaxHeadMasterResponse res = mapper.convertValue(
				repository.fetchResult(uri, RequestInfoWrapper.builder().requestInfo(requestInfo).build()),
				TaxHeadMasterResponse.class);
		return res.getTaxHeadMasters();
	}

	/**
	 * Method to enrich the Water Connection data Map
	 * 
	 * @param requestInfo
	 * @param tenantId
	 */
	public void setWaterConnectionMasterValues(RequestInfo requestInfo, String tenantId,
			Map<String, JSONArray> billingSlabMaster, Map<String, JSONArray> timeBasedExemptionMasterMap) {

		MdmsResponse response = mapper.convertValue(repository.fetchResult(calculatorUtils.getMdmsSearchUrl(),
				calculatorUtils.getWaterConnectionModuleRequest(requestInfo, tenantId)), MdmsResponse.class);
		Map<String, JSONArray> res = response.getMdmsRes().get(WSCalculationConstant.WS_TAX_MODULE);
		for (Entry<String, JSONArray> entry : res.entrySet()) {

			String masterName = entry.getKey();

			/* Masters which need to be parsed will be contained in the list */
			if (WSCalculationConstant.WS_BILLING_SLAB_MASTERS.contains(entry.getKey()))
				billingSlabMaster.put(masterName, entry.getValue());

			/* Master not contained in list will be stored as it is */
			timeBasedExemptionMasterMap.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Parses the master which has an exemption in them
	 * 
	 * @param entry
	 * @return
	 */
	private Map<String, List<Object>> getParsedMaster(Entry<String, JSONArray> entry) {

		JSONArray values = entry.getValue();
		Map<String, List<Object>> codeValueListMap = new HashMap<>();
		for (Object object : values) {

			@SuppressWarnings("unchecked")
			Map<String, Object> objectMap = (Map<String, Object>) object;
			String code = (String) objectMap.get(WSCalculationConstant.CODE_FIELD_NAME);
			if (null == codeValueListMap.get(code)) {

				List<Object> valuesList = new ArrayList<>();
				valuesList.add(objectMap);
				codeValueListMap.put(code, valuesList);
			} else {
				codeValueListMap.get(code).add(objectMap);
			}
		}
		return codeValueListMap;
	}

	/**
	 * Fetches Financial Year from Mdms Api
	 *
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Map<String, Object>> getFinancialYear(CalculationReq req) {
		String tenantId = req.getCalculationCriteria().get(0).getTenantId();
		RequestInfo requestInfo = req.getRequestInfo();
		Set<String> assessmentYears = req.getCalculationCriteria().stream().map(cal -> cal.getAssessmentYear())
				.collect(Collectors.toSet());
		MdmsCriteriaReq mdmsCriteriaReq = calculatorUtils.getFinancialYearRequest(requestInfo, assessmentYears,
				tenantId);
		StringBuilder url = calculatorUtils.getMdmsSearchUrl();
		Object res = repository.fetchResult(url, mdmsCriteriaReq);
		Map<String, Map<String, Object>> financialYearMap = new HashMap<>();
		for (String assessmentYear : assessmentYears) {
			String jsonPath = WSCalculationConstant.MDMS_FINACIALYEAR_PATH.replace("{}", assessmentYear);
			try {
				List<Map<String, Object>> jsonOutput = JsonPath.read(res, jsonPath);
				Map<String, Object> financialYearProperties = jsonOutput.get(0);
				financialYearMap.put(assessmentYear, financialYearProperties);
			} catch (IndexOutOfBoundsException e) {
				throw new CustomException(WSCalculationConstant.EG_WS_FINANCIAL_MASTER_NOT_FOUND,
						WSCalculationConstant.EG_WS_FINANCIAL_MASTER_NOT_FOUND_MSG + assessmentYear);
			}
		}
		return financialYearMap;
	}

	
	/**
	 * 
	 * @param requestInfo
	 * @param connectionType
	 * @param tenantId
	 * @return Master For Billing Period
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getBillingFrequencyMasterData(CalculationCriteria criteria, RequestInfo requestInfo,
			String connectionType, String tenantId, Map<String, Object> masterMap) {
		String jsonPath = WSCalculationConstant.JSONPATH_ROOT_FOR_BilingPeriod;
		MdmsCriteriaReq mdmsCriteriaReq = calculatorUtils.getBillingFrequency(requestInfo, connectionType, tenantId);
		StringBuilder url = calculatorUtils.getMdmsSearchUrl();
		Object res = repository.fetchResult(url, mdmsCriteriaReq);
		ArrayList<?> mdmsResponse = JsonPath.read(res, jsonPath);
		if (res == null) {
			throw new CustomException("MDMS ERROR FOR BILLING FREQUENCY", "ERROR IN FETCHING THE BILLING FREQUENCY");
		}
		getBillingPeriod(criteria, mdmsResponse, masterMap);
		return masterMap;
	}
	
	/**
	 * 
	 * @param master
	 * @param billingPeriodMap
	 * @return master map with date period
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getBillingPeriod(CalculationCriteria criteria, ArrayList<?> mdmsResponse,
			Map<String, Object> masterMap) {
		log.info("Billing Frequency Map" + mdmsResponse.toString());
		Map<String, Object> master = (Map<String, Object>) mdmsResponse.get(0);
		Map<String, Object> billingPeriod = new HashMap<>();
		LocalDateTime demandStartingDate = LocalDateTime.now();
		demandStartingDate = setCurrentDateValueToStartingOfDay(demandStartingDate);
		Long demandEndDateMillis = (Long) master.get(WSCalculationConstant.Demand_End_Date_String);
		if (criteria.getBillingPeriod() != null && ((master.get(WSCalculationConstant.Billing_Cycle_String)).toString()
				.equalsIgnoreCase(WSCalculationConstant.Monthly_Billing_Period))) {
			HashMap<String, Long> demandStartAndEndDate = getDemandStartAndEndValue(criteria.getBillingPeriod());
			billingPeriod.put(WSCalculationConstant.STARTING_DATE_APPLICABLES, demandStartAndEndDate.get("startDate"));
			billingPeriod.put(WSCalculationConstant.ENDING_DATE_APPLICABLES, demandStartAndEndDate.get("endDate"));
		} else {
			billingPeriod.put(WSCalculationConstant.STARTING_DATE_APPLICABLES,
					Timestamp.valueOf(demandStartingDate).getTime());
			billingPeriod.put(WSCalculationConstant.ENDING_DATE_APPLICABLES,
					Timestamp.valueOf(demandStartingDate).getTime() + demandEndDateMillis);
		}
		log.info("Demand Expiry Date : "+ master.get(WSCalculationConstant.Demand_Expiry_Date_String));
		Long demandExpiryDateMillis = Long.valueOf((int)master.get(WSCalculationConstant.Demand_Expiry_Date_String));
		billingPeriod.put(WSCalculationConstant.Demand_Expiry_Date_String, demandExpiryDateMillis);
		masterMap.put(WSCalculationConstant.BillingPeriod, billingPeriod);
		return masterMap;
	}
	

	/**
	 * 
	 * @param assessmentYear Assessment year
	 * @param masterList master list for that applicable
	 * @return master data for that assessment Year
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getApplicableMaster(String assessmentYear, List<Object> masterList) {

		Map<String, Object> objToBeReturned = null;
		String maxYearFromTheList = "0";
		Long maxStartTime = 0l;

		for (Object object : masterList) {

			Map<String, Object> objMap = (Map<String, Object>) object;
			String objFinYear = ((String) objMap.get(WSCalculationConstant.FROMFY_FIELD_NAME)).split("-")[0];
			if (!objMap.containsKey(WSCalculationConstant.STARTING_DATE_APPLICABLES)) {
				if (objFinYear.compareTo(assessmentYear.split("-")[0]) == 0)
					return objMap;

				else if (assessmentYear.split("-")[0].compareTo(objFinYear) > 0
						&& maxYearFromTheList.compareTo(objFinYear) <= 0) {
					maxYearFromTheList = objFinYear;
					objToBeReturned = objMap;
				}
			} else {
				String objStartDay = ((String) objMap.get(WSCalculationConstant.STARTING_DATE_APPLICABLES));
				if (assessmentYear.split("-")[0].compareTo(objFinYear) >= 0
						&& maxYearFromTheList.compareTo(objFinYear) <= 0) {
					maxYearFromTheList = objFinYear;
					Long startTime = getStartDayInMillis(objStartDay);
					Long currentTime = System.currentTimeMillis();
					if (startTime < currentTime && maxStartTime < startTime) {
						objToBeReturned = objMap;
						maxStartTime = startTime;
					}
				}
			}
		}
		return objToBeReturned;
	}

	/**
	 * Converts startDay to epoch
	 * 
	 * @param startDay
	 *            StartDay of applicable
	 * @return
	 */
	private Long getStartDayInMillis(String startDay) {

		Long startTime = null;
		try {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			Date date = df.parse(startDay);
			startTime = date.getTime();
		} catch (ParseException e) {
			throw new CustomException("INVALID STARTDAY", "The startDate of the penalty cannot be parsed");
		}

		return startTime;
	}

	/**
	 * Method to calculate exmeption based on the Amount and exemption map
	 * 
	 * @param applicableAmount
	 * @param config
	 * @return
	 */
	public BigDecimal calculateApplicables(BigDecimal applicableAmount, Object config) {

		BigDecimal currentApplicable = BigDecimal.ZERO;

		if (null == config)
			return currentApplicable;

		@SuppressWarnings("unchecked")
		Map<String, Object> configMap = (Map<String, Object>) config;

		BigDecimal rate = null != configMap.get(WSCalculationConstant.RATE_FIELD_NAME)
				? BigDecimal.valueOf(((Number) configMap.get(WSCalculationConstant.RATE_FIELD_NAME)).doubleValue())
				: null;

		BigDecimal maxAmt = null != configMap.get(WSCalculationConstant.MAX_AMOUNT_FIELD_NAME) ? BigDecimal
				.valueOf(((Number) configMap.get(WSCalculationConstant.MAX_AMOUNT_FIELD_NAME)).doubleValue()) : null;

		BigDecimal minAmt = null != configMap.get(WSCalculationConstant.MIN_AMOUNT_FIELD_NAME) ? BigDecimal
				.valueOf(((Number) configMap.get(WSCalculationConstant.MIN_AMOUNT_FIELD_NAME)).doubleValue()) : null;

		BigDecimal flatAmt = null != configMap.get(WSCalculationConstant.FLAT_AMOUNT_FIELD_NAME)
				? BigDecimal
						.valueOf(((Number) configMap.get(WSCalculationConstant.FLAT_AMOUNT_FIELD_NAME)).doubleValue())
				: BigDecimal.ZERO;

		if (null == rate)
			currentApplicable = flatAmt.compareTo(applicableAmount) > 0 ? applicableAmount : flatAmt;
		else {
			currentApplicable = applicableAmount.multiply(rate.divide(WSCalculationConstant.HUNDRED));

			if (null != maxAmt && BigDecimal.ZERO.compareTo(maxAmt) < 0 && currentApplicable.compareTo(maxAmt) > 0)
				currentApplicable = maxAmt;
			else if (null != minAmt && currentApplicable.compareTo(minAmt) < 0)
				currentApplicable = minAmt;
		}
		return currentApplicable;
	}

	/**
	 * Gets the startDate and the endDate of the financialYear
	 * 
	 * @param requestInfo
	 *            The RequestInfo of the calculationRequest
	 * @param license
	 *            The water connection for which calculation is done
	 * @return Map containing the startDate and endDate
	 */
	public Map<String, Long> getTaxPeriods(RequestInfo requestInfo, Object mdmsData) {
		Map<String, Long> taxPeriods = new HashMap<>();
		try {
			String jsonPath = WSCalculationConstant.MDMS_FINACIALYEAR_PATH.replace("{}", "2019-20");
			List<Map<String, Object>> jsonOutput = JsonPath.read(mdmsData, jsonPath);
			Map<String, Object> financialYearProperties = jsonOutput.get(0);
			Object startDate = financialYearProperties.get(WSCalculationConstant.MDMS_STARTDATE);
			Object endDate = financialYearProperties.get(WSCalculationConstant.MDMS_ENDDATE);
			taxPeriods.put(WSCalculationConstant.MDMS_STARTDATE, (Long) startDate);
			taxPeriods.put(WSCalculationConstant.MDMS_ENDDATE, (Long) endDate);

		} catch (Exception e) {

			throw new CustomException("INVALID FINANCIALYEAR", "No data found for the financialYear: " + "2019-20");
		}
		return taxPeriods;
	}

	public Object mDMSCall(RequestInfo requestInfo, String tenantId) {
		MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId);
		StringBuilder url = getMdmsSearchUrl();
		Object result = repository.fetchResult(url, mdmsCriteriaReq);
		return result;
	}

	/**
	 * Creates MDMS request
	 * 
	 * @param requestInfo
	 *            The RequestInfo of the calculationRequest
	 * @param tenantId
	 *            The tenantId of the tradeLicense
	 * @return MDMSCriteria Request
	 */
	private MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId) {

		// master details for TL module
		List<MasterDetail> fyMasterDetails = new ArrayList<>();
		// filter to only get code field from master data

		final String filterCodeForUom = "$.[?(@.active==true)]";

		fyMasterDetails.add(
				MasterDetail.builder().name(WSCalculationConstant.MDMS_FINANCIALYEAR).filter(filterCodeForUom).build());

//		ModuleDetail fyModuleDtls = ModuleDetail.builder().masterDetails(fyMasterDetails)
//				.moduleName(WSCalculationConstant.MDMS_EGF_MASTER).build();
//
//		List<MasterDetail> tlMasterDetails = new ArrayList<>();
//		tlMasterDetails.add(MasterDetail.builder().name(WSCalculationConstant.MDMS_CALCULATIONTYPE)
//				.filter(filterCodeForUom).build());
//		ModuleDetail tlModuleDtls = ModuleDetail.builder().masterDetails(tlMasterDetails)
//				.moduleName(WSCalculationConstant.MDMS_TRADELICENSE).build();

		List<ModuleDetail> moduleDetails = new ArrayList<>();
//		moduleDetails.add(fyModuleDtls);
//		moduleDetails.add(tlModuleDtls);

		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId).build();

		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	/**
	 * Creates and returns the url for mdms search endpoint
	 *
	 * @return MDMS Search URL
	 */
	private StringBuilder getMdmsSearchUrl() {
		return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
	}
	public LocalDateTime setCurrentDateValueToStartingOfDay(LocalDateTime localDateTime) {
		return localDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
	}
	
	public JSONArray getMasterListOfReceiver(RequestInfo requestInfo, String tenantId) {
		ArrayList<String> masterDetails = new ArrayList<>();
		masterDetails.add(WSCalculationConstant.SMS_RECIEVER_MASTER);
		MdmsResponse response = mapper.convertValue(
				repository.fetchResult(calculatorUtils.getMdmsSearchUrl(), calculatorUtils
						.getMdmsReqCriteria(requestInfo, tenantId, masterDetails, WSCalculationConstant.WS_TAX_MODULE)),
				MdmsResponse.class);
		Map<String, JSONArray> res = response.getMdmsRes().get(WSCalculationConstant.WS_TAX_MODULE);
		JSONArray receiverList = res.get(WSCalculationConstant.SMS_RECIEVER_MASTER);
		return receiverList;
	}
	
	public HashMap<String, Long> getDemandStartAndEndValue(String billingPeriodString) {
		HashMap<String, Long> dateMap = new HashMap<>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MMM-yyyy");
			ZoneId defaultZoneId = ZoneId.systemDefault();
			String dateAndYearMap[] = billingPeriodString.split("-");

			System.out.println(dateAndYearMap[0].trim());
			Date billingDate = sdf.parse(dateAndYearMap[0].trim() + "-" + dateAndYearMap[1].trim());
			Instant instant = billingDate.toInstant();
			LocalDate billingLocalDate = instant.atZone(defaultZoneId).toLocalDate();
			LocalDate localDateTime = LocalDate.now();
			YearMonth month = YearMonth.from(billingLocalDate);
			if ((billingLocalDate.getYear() == localDateTime.getYear())
					&& (billingLocalDate.getMonthValue() > localDateTime.getMonthValue())) {
				throw new CustomException("BILLING PERIOD ISSUE", "Billing period can not be in future!!");
			}
			if ((billingLocalDate.getYear() > localDateTime.getYear())) {
				throw new CustomException("BILLING PERIOD ISSUE", "Billing period can not be in future!!");
			}
			LocalDate start = month.atDay(1);
			LocalDate end = month.atEndOfMonth();
			long startDate = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
			long endDate = end.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
			dateMap.put("startDate", startDate);
			dateMap.put("endDate", endDate);

		} catch (CustomException | ParseException ex) {
			ex.printStackTrace();
			if(ex instanceof CustomException) 
				throw new CustomException("BILLING PERIOD ISSUE", "Billing period can not be in future!!");
			
			throw new CustomException("BILLING PERIOD PARSING ISSUE", "Billing period can not parsed!!");
		}
		return dateMap;
	}
}
