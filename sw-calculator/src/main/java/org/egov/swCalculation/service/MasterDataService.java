package org.egov.swCalculation.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.MdmsResponse;
import org.egov.swCalculation.constants.SWCalculationConstant;
import org.egov.swCalculation.model.CalculationReq;
import org.egov.swCalculation.model.RequestInfoWrapper;
import org.egov.swCalculation.model.TaxHeadMaster;
import org.egov.swCalculation.model.TaxHeadMasterResponse;
import org.egov.swCalculation.model.TaxPeriod;
import org.egov.swCalculation.model.TaxPeriodResponse;
import org.egov.swCalculation.repository.Repository;
import org.egov.swCalculation.util.CalculatorUtils;
import org.egov.swCalculation.util.SWCalculationUtil;
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
	SWCalculationUtil swCalculationUtil;
	
	@Autowired
	Repository repository;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	CalculatorUtils calculatorUtils;
	
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
	public List<TaxPeriod> getTaxPeriodList(RequestInfo requestInfo, String tenantId) {

		StringBuilder uri = swCalculationUtil.getTaxPeriodSearchUrl(tenantId);
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

		StringBuilder uri = swCalculationUtil.getTaxHeadSearchUrl(tenantId);
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
	 * Method to enrich the Water Connection data Map
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

			String masterName = entry.getKey();

			/* Masters which need to be parsed will be contained in the list */
			if (SWCalculationConstant.SW_BILLING_SLAB_MASTER.contains(entry.getKey()))
				billingSlabMaster.put(masterName, entry.getValue());

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
	public Map<String, Object> getBillingPeriod(ArrayList<?> mdmsResponse, Map<String, Object> masterMap) {
		log.info("Billing Frequency Map" + mdmsResponse.toString());
		Map<String, Object> master = (Map<String, Object>) mdmsResponse.get(0);
		Map<String, Object> billingPeriod = new HashMap<>();
		LocalDateTime demandStartingDate = LocalDateTime.now();
		demandStartingDate = setCurrentDateValueToStartingOfDay(demandStartingDate);
		Long demandEndDateMillis = (Long) master.get(SWCalculationConstant.Demand_End_Date_String);
		BigInteger expiryDate = new BigInteger(String.valueOf(master.get(SWCalculationConstant.Demand_Expiry_Date_String)));
		Long demandExpiryDateMillis  = expiryDate.longValue();
		billingPeriod.put(SWCalculationConstant.STARTING_DATE_APPLICABLES,
				Timestamp.valueOf(demandStartingDate).getTime());
		billingPeriod.put(SWCalculationConstant.ENDING_DATE_APPLICABLES,
				Timestamp.valueOf(demandStartingDate).getTime() + demandEndDateMillis);

		billingPeriod.put(SWCalculationConstant.Demand_Expiry_Date_String,demandExpiryDateMillis);
		masterMap.put(SWCalculationConstant.BillingPeriod, billingPeriod);
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
	public Map<String, Object> getBillingFrequencyMasterData(RequestInfo requestInfo,
			String connectionType, String tenantId, Map<String, Object> masterMap) {
		String jsonPath = SWCalculationConstant.JSONPATH_ROOT_FOR_BilingPeriod;
		MdmsCriteriaReq mdmsCriteriaReq = calculatorUtils.getBillingFrequency(requestInfo, connectionType, tenantId);
		StringBuilder url = calculatorUtils.getMdmsSearchUrl();
		Object res = repository.fetchResult(url, mdmsCriteriaReq);
		ArrayList<?> mdmsResponse = JsonPath.read(res, jsonPath);
		if (res == null) {
			throw new CustomException("MDMS ERROR FOR BILLING FREQUENCY", "ERROR IN FETCHING THE BILLING FREQUENCY");
		}
		getBillingPeriod(mdmsResponse, masterMap);
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
		JSONArray receiverList = res.get(SWCalculationConstant.SMS_RECIEVER_MASTER);
		return receiverList;
	}
}
