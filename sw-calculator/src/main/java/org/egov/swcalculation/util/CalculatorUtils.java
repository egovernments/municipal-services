package org.egov.swcalculation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.swcalculation.config.SWCalculationConfiguration;
import org.egov.swcalculation.constants.SWCalculationConstant;
import org.egov.swcalculation.model.RequestInfoWrapper;
import org.egov.swcalculation.model.SearchCriteria;
import org.egov.swcalculation.model.SewerageConnection;
import org.egov.swcalculation.model.SewerageConnectionResponse;
import org.egov.swcalculation.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.Getter;

@Component
@Getter
public class CalculatorUtils {

	@Autowired
	private SWCalculationConfiguration configurations;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	/**
	 * Prepares and returns Mdms search request with financial master criteria
	 *
	 * @param requestInfo
	 * @param assesmentYears
	 * @return
	 */
	public MdmsCriteriaReq getFinancialYearRequest(RequestInfo requestInfo, Set<String> assesmentYears,
			String tenantId) {

		String assessmentYearStr = StringUtils.join(assesmentYears, ",");
		MasterDetail mstrDetail = MasterDetail.builder().name(SWCalculationConstant.FINANCIAL_YEAR_MASTER)
				.filter("[?(@." + SWCalculationConstant.FINANCIAL_YEAR_RANGE_FEILD_NAME + " IN [" + assessmentYearStr
						+ "]" + " && @.module== '" + SWCalculationConstant.SERVICE_FIELD_VALUE_SW + "')]")
				.build();
		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(SWCalculationConstant.FINANCIAL_MODULE)
				.masterDetails(Arrays.asList(mstrDetail)).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(moduleDetail)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	/**
	 * Returns the url for mdms search endpoint
	 *
	 * @return
	 */
	public StringBuilder getMdmsSearchUrl() {
		return new StringBuilder().append(configurations.getMdmsHost()).append(configurations.getMdmsEndPoint());
	}

	/**
	 * Call SW-services to get sewerage for the given connectionNo and tenantID
	 * 
	 * @param requestInfo
	 *            The RequestInfo of the incoming request
	 * @param connectionNo
	 *            The connectionNumber whose sewerage connection has to be
	 *            fetched
	 * @param tenantId
	 *            The tenantId of the sewerage connection
	 * @return The water connection fo the particular connection no
	 */
	public SewerageConnection getSewerageConnection(RequestInfo requestInfo, String connectionNo, String tenantId) {
		ObjectMapper mapper = new ObjectMapper();
		Object result = serviceRequestRepository.fetchResult(getSewerageSearchURL(tenantId, connectionNo),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		SewerageConnectionResponse response = null;
		try {
			response = mapper.convertValue(result, SewerageConnectionResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Error while parsing response of Sewerage Search");
		}

		if (response == null || CollectionUtils.isEmpty(response.getSewerageConnections()))
			return null;

		return response.getSewerageConnections().get(0);
	}

	/**
	 * Creates tradeLicense search url based on tenantId and applicationNumber
	 * 
	 * @return water search url
	 */
	private StringBuilder getSewerageSearchURL(String tenantId, String connectionNo) {
		StringBuilder url = new StringBuilder(configurations.getSewerageConnectionHost());
		url.append(configurations.getSewerageConnectionSearchEndPoint());
		url.append("?");
		url.append("tenantId=");
		url.append(tenantId);
		url.append("&");
		url.append("connectionNumber=");
		url.append(connectionNo);
		return url;
	}

	/**
	 * Methods provides all the usage category master for Sewerage Service
	 * module
	 */
	public MdmsCriteriaReq getWaterConnectionModuleRequest(RequestInfo requestInfo, String tenantId) {
		List<MasterDetail> details = new ArrayList<>();
		details.add(MasterDetail.builder().name(SWCalculationConstant.SW_REBATE_MASTER).build());
		details.add(MasterDetail.builder().name(SWCalculationConstant.SW_PENANLTY_MASTER).build());
		details.add(MasterDetail.builder().name(SWCalculationConstant.SW_INTEREST_MASTER).build());
		details.add(MasterDetail.builder().name(SWCalculationConstant.SW_BILLING_SLAB_MASTER).build());
		details.add(MasterDetail.builder().name(SWCalculationConstant.CALCULATION_ATTRIBUTE_CONST)
				.filter("[?(@.active== " + true + ")]").build());
		ModuleDetail mdDtl = ModuleDetail.builder().masterDetails(details)
				.moduleName(SWCalculationConstant.SW_TAX_MODULE).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(mdDtl)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	public MdmsCriteriaReq getBillingFrequency(RequestInfo requestInfo, String tenantId) {

		MasterDetail mstrDetail = MasterDetail.builder().name(SWCalculationConstant.BILLING_PERIOD)
				.filter("[?(@.active== " + true + ")]").build();
		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(SWCalculationConstant.SW_MODULE)
				.masterDetails(Arrays.asList(mstrDetail)).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(moduleDetail)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	/**
	 * Methods provides all the usage category master for Water Service module
	 */
	public MdmsCriteriaReq getMdmsReqCriteria(RequestInfo requestInfo, String tenantId, ArrayList<String> masterDetails,
			String moduleName) {

		List<MasterDetail> details = new ArrayList<>();
		masterDetails.forEach(masterName -> details.add(MasterDetail.builder().name(masterName).build()));
		ModuleDetail mdDtl = ModuleDetail.builder().masterDetails(details).moduleName(moduleName).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(mdDtl)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	/**
	 * 
	 * @param tenantId
	 * @return uri of fetch bill
	 */
	public StringBuilder getFetchBillURL(String tenantId, String consumerCode) {

		return new StringBuilder().append(configurations.getBillingServiceHost())
				.append(configurations.getFetchBillEndPoint()).append(SWCalculationConstant.URL_PARAMS_SEPARATER)
				.append(SWCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(tenantId)
				.append(SWCalculationConstant.SEPARATER).append(SWCalculationConstant.CONSUMER_CODE_SEARCH_FIELD_NAME)
				.append(consumerCode).append(SWCalculationConstant.SEPARATER)
				.append(SWCalculationConstant.BUSINESSSERVICE_FIELD_FOR_SEARCH_URL)
				.append(SWCalculationConstant.SEWERAGE_TAX_SERVICE_CODE);
	}
	
	/**
	 * 
	 * @param requestInfo
	 * @param connectionNo
	 * @param tenantId
	 * @return sewerage connection
	 */
	public SewerageConnection getSewerageConnectionOnApplicationNO(RequestInfo requestInfo, SearchCriteria searchCriteria,
			String tenantId) {
		ObjectMapper mapper = new ObjectMapper();
		String url = getSewerageSearchURL(searchCriteria);
		Object result = serviceRequestRepository.fetchResult(new StringBuilder(url),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());
		SewerageConnectionResponse response = null;
		try {
			response = mapper.convertValue(result, SewerageConnectionResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Error while parsing response of Sewerage Connection Search");
		}

		if (response == null || CollectionUtils.isEmpty(response.getSewerageConnections()))
			return null;

		return response.getSewerageConnections().get(0);
	}


	/**
	 * Creates sewerageConnection search url based on tenantId and connectionNumber
	 * 
	 * @return water search url
	 */
	private String getSewerageSearchURL(SearchCriteria searchCriteria) {
		StringBuilder url = new StringBuilder(configurations.getSewerageConnectionHost());
		url.append(configurations.getSewerageConnectionSearchEndPoint());
		url.append("?");
		url.append("tenantId=" + searchCriteria.getTenantId());
		if (searchCriteria.getConnectionNumber() != null) {
			url.append("&");
			url.append("connectionNumber=" + searchCriteria.getConnectionNumber());
		}
		if (searchCriteria.getApplicationNumber() != null) {
			url.append("&");
			url.append("applicationNumber=" + searchCriteria.getApplicationNumber());
		}
		return url.toString();
	}
	
	/**
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @param roadType
	 * @param usageType
	 * @return mdms request for master data
	 */
	public MdmsCriteriaReq getEstimationMasterCriteria(RequestInfo requestInfo, String tenantId) {
		List<MasterDetail> details = new ArrayList<>();
		details.add(MasterDetail.builder().name(SWCalculationConstant.SC_PLOTSLAB_MASTER)
				.filter("[?(@.isActive== " + true + ")]").build());
		details.add(MasterDetail.builder().name(SWCalculationConstant.SC_PROPERTYUSAGETYPE_MASTER)
				.filter("[?(@.isActive== " + true + ")]").build());
		details.add(MasterDetail.builder().name(SWCalculationConstant.SC_FEESLAB_MASTER)
				.filter("[?(@.isActive== " + true + ")]").build());
		details.add(MasterDetail.builder().name(SWCalculationConstant.SC_ROADTYPE_MASTER)
				.filter("[?(@.isActive== " + true + ")]").build());
		ModuleDetail mdDtl = ModuleDetail.builder().masterDetails(details)
				.moduleName(SWCalculationConstant.SW_TAX_MODULE).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(mdDtl)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}
	
	/**
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @return MdmsCriteria
	 */
	private MdmsCriteriaReq getBillingFrequencyForScheduler(RequestInfo requestInfo, String tenantId) {

		MasterDetail mstrDetail = MasterDetail.builder().name(SWCalculationConstant.BILLING_PERIOD)
				.filter("[?(@.active== " + true + " && @.connectionType== '" + SWCalculationConstant.nonMeterdConnection
						+ "')]")
				.build();
		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(SWCalculationConstant.SW_MODULE)
				.masterDetails(Arrays.asList(mstrDetail)).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(moduleDetail)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	/**
	 * 
	 * @param requestInfo
	 * @param connectionType
	 * @param tenantId
	 * @return Master For Billing Period
	 */
	public Map<String, Object> loadBillingFrequencyMasterData(RequestInfo requestInfo, String tenantId) {
		MdmsCriteriaReq mdmsCriteriaReq = getBillingFrequencyForScheduler(requestInfo, tenantId);
		Object res = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
		if (res == null) {
			throw new CustomException("MDMS_ERROR_FOR_BILLING_FREQUENCY", "ERROR IN FETCHING THE BILLING FREQUENCY");
		}
		List<Map<String, Object>> jsonOutput = JsonPath.read(res, SWCalculationConstant.JSONPATH_ROOT_FOR_BilingPeriod);
		return jsonOutput.get(0);
	}
}
