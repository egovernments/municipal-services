package org.egov.wsCalculation.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.constants.WSCalculationConfiguration;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.CalculationReq;
import org.egov.wsCalculation.model.RequestInfoWrapper;
import org.egov.wsCalculation.model.TaxHeadMaster;
import org.egov.wsCalculation.model.TaxHeadMasterResponse;
import org.egov.wsCalculation.model.TaxPeriod;
import org.egov.wsCalculation.model.TaxPeriodResponse;
import org.egov.wsCalculation.repository.ServiceRequestRepository;
import org.egov.wsCalculation.util.CalculatorUtil;
import org.egov.wsCalculation.util.WSCalculationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

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
			Map<String, Map<String, List<Object>>> waterBasedExemptionMasterMap,
			Map<String, JSONArray> timeBasedExemptionMasterMap) {

		MdmsResponse response = mapper.convertValue(repository.fetchResult(calculatorUtils.getMdmsSearchUrl(),
				calculatorUtils.getPropertyModuleRequest(requestInfo, tenantId)), MdmsResponse.class);
		Map<String, JSONArray> res = response.getMdmsRes().get(WSCalculationConstant.WS_TAX_MODULE);
		for (Entry<String, JSONArray> entry : res.entrySet()) {

			String masterName = entry.getKey();

			/* Masters which need to be parsed will be contained in the list */
			if (WSCalculationConstant.WS_BASED_EXEMPTION_MASTERS.contains(entry.getKey()))
				waterBasedExemptionMasterMap.put(masterName, getParsedMaster(entry));

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
		String financialYear = "2019-20";
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
		return null;
	}

	/**
	 * Method to enrich the water connection Master data Map
	 * 
	 * @param requestInfo
	 * @param tenantId
	 */
	public void setPropertyMasterValues(RequestInfo requestInfo, String tenantId,
			Map<String, Map<String, List<Object>>> propertyBasedExemptionMasterMap,
			Map<String, JSONArray> timeBasedExemptionMasterMap) {

		MdmsResponse response = mapper.convertValue(repository.fetchResult(calculatorUtils.getMdmsSearchUrl(),
				calculatorUtils.getPropertyModuleRequest(requestInfo, tenantId)), MdmsResponse.class);
		Map<String, JSONArray> res = response.getMdmsRes().get(WSCalculationConstant.WATER_TAX_MODULE);
		for (Entry<String, JSONArray> entry : res.entrySet()) {

			String masterName = entry.getKey();

			/* Masters which need to be parsed will be contained in the list */
			if (WSCalculationConstant.PROPERTY_BASED_EXEMPTION_MASTERS.contains(entry.getKey()))
				propertyBasedExemptionMasterMap.put(masterName, getParsedMaster(entry));

			/* Master not contained in list will be stored as it is */
			timeBasedExemptionMasterMap.put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Returns the 'APPLICABLE' master object from the list of inputs
	 *
	 * filters the Input based on their effective financial year and starting day
	 *
	 * If an object is found with effective year same as assessment year that master entity will be returned
	 *
	 * If exact match is not found then the entity with latest effective financial year which should be lesser than the assessment year
	 *
	 * NOTE : applicable points to single object  out of all the entries for a given master which fits the period of the property being assessed
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
			String objFinYear = ((String) objMap.get(WSCalculationConstant.FROMFY_FIELD_NAME)).split("-")[0];
			if(!objMap.containsKey(WSCalculationConstant.STARTING_DATE_APPLICABLES)){
				if (objFinYear.compareTo(assessmentYear.split("-")[0]) == 0)
					return  objMap;

				else if (assessmentYear.split("-")[0].compareTo(objFinYear) > 0 && maxYearFromTheList.compareTo(objFinYear) <= 0) {
					maxYearFromTheList = objFinYear;
					objToBeReturned = objMap;
				}
			}
			else{
				String objStartDay = ((String) objMap.get(WSCalculationConstant.STARTING_DATE_APPLICABLES));
				if (assessmentYear.split("-")[0].compareTo(objFinYear) >= 0 && maxYearFromTheList.compareTo(objFinYear) <= 0) {
					maxYearFromTheList = objFinYear;
					Long startTime = getStartDayInMillis(objStartDay);
					Long currentTime = System.currentTimeMillis();
					if(startTime < currentTime && maxStartTime < startTime){
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
	 * @param startDay StartDay of applicable
	 * @return
	 */
	private Long getStartDayInMillis(String startDay){

		Long startTime = null;
		try{
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			Date date = df.parse(startDay);
			startTime = date.getTime();
		}
		catch (ParseException e) {
			throw new CustomException("INVALID STARTDAY","The startDate of the penalty cannot be parsed");
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

		BigDecimal maxAmt = null != configMap.get(WSCalculationConstant.MAX_AMOUNT_FIELD_NAME)
				? BigDecimal.valueOf(((Number) configMap.get(WSCalculationConstant.MAX_AMOUNT_FIELD_NAME)).doubleValue())
				: null;

		BigDecimal minAmt = null != configMap.get(WSCalculationConstant.MIN_AMOUNT_FIELD_NAME)
				? BigDecimal.valueOf(((Number) configMap.get(WSCalculationConstant.MIN_AMOUNT_FIELD_NAME)).doubleValue())
				: null;

		BigDecimal flatAmt = null != configMap.get(WSCalculationConstant.FLAT_AMOUNT_FIELD_NAME)
				? BigDecimal.valueOf(((Number) configMap.get(WSCalculationConstant.FLAT_AMOUNT_FIELD_NAME)).doubleValue())
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

}
