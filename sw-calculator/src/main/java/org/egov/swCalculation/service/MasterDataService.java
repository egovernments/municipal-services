package org.egov.swCalculation.service;

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

import net.minidev.json.JSONArray;

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
				throw new CustomException(SWCalculationConstant.EG_WS_FINANCIAL_MASTER_NOT_FOUND,
						SWCalculationConstant.EG_WS_FINANCIAL_MASTER_NOT_FOUND_MSG + assessmentYear);
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
	
}
