package org.egov.swcalculation.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.MdmsResponse;
import org.egov.swcalculation.constants.SWCalculationConstant;
import org.egov.swcalculation.model.CalculationCriteria;
import org.egov.swcalculation.model.RequestInfoWrapper;
import org.egov.swcalculation.model.TaxHeadMaster;
import org.egov.swcalculation.model.TaxHeadMasterResponse;
import org.egov.swcalculation.model.TaxPeriod;
import org.egov.swcalculation.model.TaxPeriodResponse;
import org.egov.swcalculation.repository.Repository;
import org.egov.swcalculation.util.CalculatorUtils;
import org.egov.swcalculation.util.SWCalculationUtil;
import org.egov.tracer.model.CustomException;
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
	private SWCalculationUtil swCalculationUtil;
	
	@Autowired
	private Repository repository;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private CalculatorUtils calculatorUtils;
	
	@Autowired
	private EstimationService estimationService;
	
	/**
	 * Fetches and creates map of all required masters
	 * 
	 * @param request
	 *            The calculation request
	 * @return
	 */
	public Map<String, Object> getMasterMap(RequestInfo requestInfo, String tenantId,String serviceFieldValue) {
		List<TaxPeriod> taxPeriods = getTaxPeriodList(requestInfo, tenantId,serviceFieldValue);
		List<TaxHeadMaster> taxHeadMasters = getTaxHeadMasterMap(requestInfo, tenantId,serviceFieldValue);
		Map<String, Map<String, Object>> financialYearMaster = getFinancialYear(requestInfo, tenantId);
		Map<String, Object> masterMap = new HashMap<>();
		masterMap.put(SWCalculationConstant.TAXPERIOD_MASTER_KEY, taxPeriods);
		masterMap.put(SWCalculationConstant.TAXHEADMASTER_MASTER_KEY, taxHeadMasters);
		masterMap.put(SWCalculationConstant.FINANCIALYEAR_MASTER_KEY, financialYearMaster);
		return masterMap;
	}
	
	/**
	 * Fetch Tax Head Masters From billing service
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @return
	 */
	public List<TaxPeriod> getTaxPeriodList(RequestInfo requestInfo, String tenantId,String serviceFieldValue) {
		TaxPeriodResponse res = mapper.convertValue(
				repository.fetchResult(swCalculationUtil.getTaxPeriodSearchUrl(tenantId,serviceFieldValue), RequestInfoWrapper.builder().requestInfo(requestInfo).build()),
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
	public List<TaxHeadMaster> getTaxHeadMasterMap(RequestInfo requestInfo, String tenantId,String serviceFieldValue) {

		StringBuilder uri = swCalculationUtil.getTaxHeadSearchUrl(tenantId,serviceFieldValue);
		TaxHeadMasterResponse res = mapper.convertValue(
				repository.fetchResult(uri, RequestInfoWrapper.builder().requestInfo(requestInfo).build()),
				TaxHeadMasterResponse.class);
		return res.getTaxHeadMasters();
	}

	
	/**
	 * Fetches Financial Year from Mdms Api
	 *
	 * @param req
	 * @return
	 */
	
	@SuppressWarnings("unchecked")
	public Map<String, Map<String, Object>> getFinancialYear(RequestInfo requestInfo, String tenantId) {
		Set<String> assessmentYears = new HashSet<>(1);
		assessmentYears.add(estimationService.getAssessmentYear());
		MdmsCriteriaReq mdmsCriteriaReq = calculatorUtils.getFinancialYearRequest(requestInfo, assessmentYears,
				tenantId);
		Object res = repository.fetchResult(calculatorUtils.getMdmsSearchUrl(), mdmsCriteriaReq);
		Map<String, Map<String, Object>> financialYearMap = new HashMap<>();
		for (String assessmentYear : assessmentYears) {
			String jsonPath = SWCalculationConstant.MDMS_FINACIALYEAR_PATH.replace("{}", assessmentYear);
			try {
				List<Map<String, Object>> jsonOutput = JsonPath.read(res, jsonPath);
				Map<String, Object> financialYearProperties = jsonOutput.get(0);
				financialYearMap.put(assessmentYear, financialYearProperties);
			} catch (IndexOutOfBoundsException e) {
				throw new CustomException(SWCalculationConstant.EG_SW_FINANCIAL_MASTER_NOT_FOUND,
						SWCalculationConstant.EG_SW_FINANCIAL_MASTER_NOT_FOUND_MSG + assessmentYear);
			}
		}
		return financialYearMap;
	}
	
	
	/**
	 * Method to enrich the Sewerage Connection data Map
	 * 
	 * @param requestInfo
	 * @param tenantId
	 */
	public void setSewerageConnectionMasterValues(RequestInfo requestInfo, String tenantId,
			Map<String, JSONArray> billingSlabMaster, Map<String, JSONArray> timeBasedExemptionMasterMap) {

		MdmsResponse response = mapper.convertValue(repository.fetchResult(calculatorUtils.getMdmsSearchUrl(),
				calculatorUtils.getWaterConnectionModuleRequest(requestInfo, tenantId)), MdmsResponse.class);
		Map<String, JSONArray> res = response.getMdmsRes().get(SWCalculationConstant.SW_TAX_MODULE);
		for (Entry<String, JSONArray> entry : res.entrySet()) {
			/* Masters which need to be parsed will be contained in the list */
			if (SWCalculationConstant.SW_BILLING_SLAB_MASTER.contains(entry.getKey()))
				billingSlabMaster.put(entry.getKey(), entry.getValue());

			/* Master not contained in list will be stored as it is */
			timeBasedExemptionMasterMap.put(entry.getKey(), entry.getValue());
		}
	}
	
	
	/**
	 * Returns the 'APPLICABLE' master object from the list of inputs
	 *
	 * filters the Input based on their effective financial year and starting
	 * day
	 *
	 * If an object is found with effective year same as assessment year that
	 * master entity will be returned
	 *
	 * If exact match is not found then the entity with latest effective
	 * financial year which should be lesser than the assessment year
	 *
	 * NOTE : applicable points to single object out of all the entries for a
	 * given master which fits the period of the property being assessed
	 *
	 * @param assessmentYear
	 * @param masterList
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getApplicableMaster(String assessmentYear, List<Object> masterList) {

		Map<String, Object> objToBeReturned = null;
		String maxYearFromTheList = "0";
		Long maxStartTime = 0l;

		for (Object object : masterList) {

			Map<String, Object> objMap = (Map<String, Object>) object;
			String objFinYear = ((String) objMap.get(SWCalculationConstant.FROMFY_FIELD_NAME)).split("-")[0];
			if (!objMap.containsKey(SWCalculationConstant.STARTING_DATE_APPLICABLES)) {
				if (objFinYear.compareTo(assessmentYear.split("-")[0]) == 0)
					return objMap;

				else if (assessmentYear.split("-")[0].compareTo(objFinYear) > 0
						&& maxYearFromTheList.compareTo(objFinYear) <= 0) {
					maxYearFromTheList = objFinYear;
					objToBeReturned = objMap;
				}
			} else {
				String objStartDay = ((String) objMap.get(SWCalculationConstant.STARTING_DATE_APPLICABLES));
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
		try {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			Date date = df.parse(startDay);
			return date.getTime();
		} catch (ParseException e) {
			throw new CustomException("INVALID STARTDAY", "The startDate of the penalty cannot be parsed");
		}
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

		BigDecimal rate = null != configMap.get(SWCalculationConstant.RATE_FIELD_NAME)
				? BigDecimal.valueOf(((Number) configMap.get(SWCalculationConstant.RATE_FIELD_NAME)).doubleValue())
				: null;

		BigDecimal maxAmt = null != configMap.get(SWCalculationConstant.MAX_AMOUNT_FIELD_NAME) ? BigDecimal
				.valueOf(((Number) configMap.get(SWCalculationConstant.MAX_AMOUNT_FIELD_NAME)).doubleValue()) : null;

		BigDecimal minAmt = null != configMap.get(SWCalculationConstant.MIN_AMOUNT_FIELD_NAME) ? BigDecimal
				.valueOf(((Number) configMap.get(SWCalculationConstant.MIN_AMOUNT_FIELD_NAME)).doubleValue()) : null;

		BigDecimal flatAmt = null != configMap.get(SWCalculationConstant.FLAT_AMOUNT_FIELD_NAME)
				? BigDecimal
						.valueOf(((Number) configMap.get(SWCalculationConstant.FLAT_AMOUNT_FIELD_NAME)).doubleValue())
				: BigDecimal.ZERO;

		if (null == rate)
			currentApplicable = flatAmt.compareTo(applicableAmount) > 0 ? applicableAmount : flatAmt;
		else {
			currentApplicable = applicableAmount.multiply(rate.divide(SWCalculationConstant.HUNDRED));

			if (null != maxAmt && BigDecimal.ZERO.compareTo(maxAmt) < 0 && currentApplicable.compareTo(maxAmt) > 0)
				currentApplicable = maxAmt;
			else if (null != minAmt && currentApplicable.compareTo(minAmt) < 0)
				currentApplicable = minAmt;
		}
		return currentApplicable;
	}
	
	
	/**
	 * 
	 * @param master
	 * @param billingPeriodMap
	 * @return master map with date period
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> enrichBillingPeriod(CalculationCriteria criteria, ArrayList<?> mdmsResponse,
			Map<String, Object> masterMap) {

		log.info("Billing Frequency Map {}", mdmsResponse.toString());

		int size = mdmsResponse.size();

		Map<String, Object> master = new HashMap<>();
		for (int i = 0; i < size; i++) {
			if ((((Map<String, Object>) mdmsResponse.get(i)).get(SWCalculationConstant.ConnectionType).toString())
					.equalsIgnoreCase(criteria.getSewerageConnection().getConnectionType())) {
				master = (Map<String, Object>) mdmsResponse.get(i);
				break;
			}
		}
		Map<String, Object> billingPeriod = new HashMap<>();
		if (SWCalculationConstant.Monthly_Billing_Period
				.equalsIgnoreCase(master.get(SWCalculationConstant.BILLING_CYCLE_CONST).toString())) {
			estimationService.getMonthStartAndEndDate(billingPeriod);
		}
		if (SWCalculationConstant.Quaterly_Billing_Period
				.equalsIgnoreCase(master.get(SWCalculationConstant.BILLING_CYCLE_CONST).toString())) {
			estimationService.getQuaterStartAndEndDate(billingPeriod);
		} else {
			LocalDateTime demandEndDate = LocalDateTime.now();
			demandEndDate = setCurrentDateValueToStartingOfDay(demandEndDate);

			billingPeriod.put(SWCalculationConstant.STARTING_DATE_APPLICABLES,
					Timestamp.valueOf(demandEndDate).getTime()
							- (Long) master.get(SWCalculationConstant.Demand_End_Date_String));
			billingPeriod.put(SWCalculationConstant.ENDING_DATE_APPLICABLES,
					Timestamp.valueOf(demandEndDate).getTime());
		}

		log.info("Demand Expiry Date : " + master.get(SWCalculationConstant.Demand_Expiry_Date_String));
		billingPeriod.put(SWCalculationConstant.Demand_Expiry_Date_String,
				new BigInteger(String.valueOf(master.get(SWCalculationConstant.Demand_Expiry_Date_String)))
						.longValue());
		masterMap.put(SWCalculationConstant.BILLING_PERIOD, billingPeriod);
		return masterMap;
	}

	
	public LocalDateTime setCurrentDateValueToStartingOfDay(LocalDateTime localDateTime) {
		return localDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
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
		MdmsCriteriaReq mdmsCriteriaReq = calculatorUtils.getBillingFrequency(requestInfo, tenantId);
		Object res = repository.fetchResult(calculatorUtils.getMdmsSearchUrl(), mdmsCriteriaReq);
		if (res == null) {
			throw new CustomException("MDMS_ERROR_FOR_BILLING_FREQUENCY", "ERROR IN FETCHING THE BILLING FREQUENCY");
		}
		ArrayList<?> mdmsResponse = JsonPath.read(res, SWCalculationConstant.JSONPATH_ROOT_FOR_BilingPeriod);
		enrichBillingPeriod(criteria, mdmsResponse, masterMap);
		return masterMap;
	}
	
	public JSONArray getMasterListOfReceiver(RequestInfo requestInfo, String tenantId) {
		ArrayList<String> masterDetails = new ArrayList<>();
		masterDetails.add(SWCalculationConstant.SMS_RECIEVER_MASTER);
		MdmsResponse response = mapper.convertValue(
				repository.fetchResult(calculatorUtils.getMdmsSearchUrl(), calculatorUtils
						.getMdmsReqCriteria(requestInfo, tenantId, masterDetails, SWCalculationConstant.SW_TAX_MODULE)),
				MdmsResponse.class);
		Map<String, JSONArray> res = response.getMdmsRes().get(SWCalculationConstant.SW_TAX_MODULE);
		return res.get(SWCalculationConstant.SMS_RECIEVER_MASTER);
	}
	
	 /**
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @param masterMap
	 */
	public void loadBillingSlabsAndTimeBasedExemptions(RequestInfo requestInfo, String tenantId,
			Map<String, Object> masterMap) {

		MdmsResponse response = mapper.convertValue(repository.fetchResult(calculatorUtils.getMdmsSearchUrl(),
				calculatorUtils.getWaterConnectionModuleRequest(requestInfo, tenantId)), MdmsResponse.class);
		Map<String, JSONArray> res = response.getMdmsRes().get(SWCalculationConstant.SW_TAX_MODULE);
		for (Entry<String, JSONArray> entry : res.entrySet()) {

			String masterName = entry.getKey();

			/* Masters which need to be parsed will be contained in the list */
			if (SWCalculationConstant.SW_BILLING_SLAB_MASTER.contains(entry.getKey()))
				masterMap.put(masterName, entry.getValue());

			/* Master not contained in list will be stored as it is */
			masterMap.put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @return all masters that is needed for calculation and demand generation.
	 */
	public Map<String, Object> loadMasterData(RequestInfo requestInfo, String tenantId) {
		Map<String, Object> master = new HashMap<>();
		master = getMasterMap(requestInfo, tenantId,SWCalculationConstant.SERVICE_FIELD_VALUE_SW);
		loadBillingSlabsAndTimeBasedExemptions(requestInfo, tenantId, master);
		loadBillingFrequencyMasterData(requestInfo, tenantId, master);
		return master;
	}

	/**
	 * 
	 * @param requestInfo
	 * @param connectionType
	 * @param tenantId
	 * @return Master For Billing Period
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> loadBillingFrequencyMasterData(RequestInfo requestInfo, String tenantId, Map<String, Object> masterMap) {
		MdmsCriteriaReq mdmsCriteriaReq = calculatorUtils.getBillingFrequency(requestInfo, tenantId);
		Object res = repository.fetchResult(calculatorUtils.getMdmsSearchUrl(), mdmsCriteriaReq);
		if (res == null) {
			throw new CustomException("MDMS_ERROR_FOR_BILLING_FREQUENCY", "ERROR IN FETCHING THE BILLING FREQUENCY");
		}
		ArrayList<?> mdmsResponse = JsonPath.read(res, SWCalculationConstant.JSONPATH_ROOT_FOR_BilingPeriod);
		masterMap.put(SWCalculationConstant.Billing_Period_Master, mdmsResponse);
		return masterMap;
	}
	
	/**
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @param roadType
	 * @param usageType
	 * @param masterMap
	 *            return master data with exception master data
	 */
	public Map<String, Object> loadExcemptionMaster(RequestInfo requestInfo, String tenantId) {
		Map<String, Object> master = getMasterMap(requestInfo, tenantId, SWCalculationConstant.ONE_TIME_FEE_SERVICE_FIELD);
		MdmsResponse response = mapper.convertValue(repository.fetchResult(calculatorUtils.getMdmsSearchUrl(),
				calculatorUtils.getEstimationMasterCriteria(requestInfo, tenantId)), MdmsResponse.class);
		Map<String, JSONArray> res = response.getMdmsRes().get(SWCalculationConstant.SW_TAX_MODULE);
		for (Map.Entry<String, JSONArray> resp : res.entrySet()) {
			master.put(resp.getKey(), resp.getValue());
		}
		return master;
	}
	
	/**
	 * 
	 * @param masterMap
	 * @return master map contains demand start date, end date and expiry date
	 */
	public Map<String, Object> enrichBillingPeriodForFee(Map<String, Object> masterMap) {
		Map<String, Object> billingPeriod = new HashMap<>();
		billingPeriod.put(SWCalculationConstant.STARTING_DATE_APPLICABLES, System.currentTimeMillis());
		billingPeriod.put(SWCalculationConstant.ENDING_DATE_APPLICABLES, System.currentTimeMillis() + SWCalculationConstant.APPLICATION_FEE_DEMAND_END_DATE);
		billingPeriod.put(SWCalculationConstant.Demand_Expiry_Date_String, SWCalculationConstant.APPLICATION_FEE_DEMAND_EXP_DATE);
		masterMap.put(SWCalculationConstant.BILLING_PERIOD, billingPeriod);
		return masterMap;
	}
}

