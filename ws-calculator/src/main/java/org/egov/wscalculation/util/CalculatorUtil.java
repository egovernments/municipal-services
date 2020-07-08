package org.egov.wscalculation.util;


import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.egov.wscalculation.constants.WSCalculationConstant;
import org.egov.wscalculation.model.*;
import org.egov.wscalculation.model.workflow.ProcessInstance;
import org.egov.wscalculation.model.workflow.ProcessInstanceResponse;
import org.egov.wscalculation.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.Getter;

@Component
@Getter
public class CalculatorUtil {

	@Autowired
	private WSCalculationConfiguration calculationConfig;
	
	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	/**
	 * Methods provides all the usage category master for Water Service module
	 */
	public MdmsCriteriaReq getWaterConnectionModuleRequest(RequestInfo requestInfo, String tenantId) {
		List<MasterDetail> details = new ArrayList<>();
		details.add(MasterDetail.builder().name(WSCalculationConstant.WC_REBATE_MASTER).build());
		details.add(MasterDetail.builder().name(WSCalculationConstant.WC_WATER_CESS_MASTER).build());
		details.add(MasterDetail.builder().name(WSCalculationConstant.WC_PENANLTY_MASTER).build());
		details.add(MasterDetail.builder().name(WSCalculationConstant.WC_INTEREST_MASTER).build());
		details.add(MasterDetail.builder().name(WSCalculationConstant.WC_BILLING_SLAB_MASTER).build());
		details.add(MasterDetail.builder().name(WSCalculationConstant.CALCULATION_ATTRIBUTE_CONST).filter("[?(@.active== "+ true +")]").build());
		ModuleDetail mdDtl = ModuleDetail.builder().masterDetails(details)
				.moduleName(WSCalculationConstant.WS_TAX_MODULE).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(mdDtl)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}
	
	/**
	 * Returns the url for mdms search endpoint
	 *
	 * @return
	 */
	public StringBuilder getMdmsSearchUrl() {
		return new StringBuilder().append(calculationConfig.getMdmsHost()).append(calculationConfig.getMdmsEndPoint());
	}
	
	/**
	 * Prepares and returns Mdms search request with financial master criteria
	 *
	 * @param requestInfo
	 * @param assesmentYears
	 * @return
	 */
	public MdmsCriteriaReq getFinancialYearRequest(RequestInfo requestInfo, Set<String> assesmentYears, String tenantId) {

		String assessmentYearStr = StringUtils.join(assesmentYears,",");
		MasterDetail mstrDetail = MasterDetail.builder().name(WSCalculationConstant.FINANCIAL_YEAR_MASTER)
				.filter("[?(@." + WSCalculationConstant.FINANCIAL_YEAR_RANGE_FEILD_NAME + " IN [" + assessmentYearStr + "]" +
						" && @.module== '"+WSCalculationConstant.SERVICE_FIELD_VALUE_WS+"')]")
				.build();
		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(WSCalculationConstant.FINANCIAL_MODULE)
				.masterDetails(Arrays.asList(mstrDetail)).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(moduleDetail)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}
	
	
	
	public MdmsCriteriaReq getBillingFrequency(RequestInfo requestInfo, String tenantId) {

		MasterDetail mstrDetail = MasterDetail.builder().name(WSCalculationConstant.BILLING_PERIOD)
				.filter("[?(@.active== "+true+")]")
				.build();
		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(WSCalculationConstant.WS_MODULE)
				.masterDetails(Arrays.asList(mstrDetail)).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(moduleDetail)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}
	
	/**
	 * 
	 * @param requestInfo
	 * @param connectionNo 
	 * @param tenantId
	 * @return WaterConnection based on parameters
	 */
    public WaterConnection getWaterConnection(RequestInfo requestInfo, String connectionNo, String tenantId){
        ObjectMapper mapper = new ObjectMapper();
        Object result =serviceRequestRepository.fetchResult(getWaterSearchURL(tenantId, connectionNo) ,RequestInfoWrapper.builder().
                requestInfo(requestInfo).build());

        WaterConnectionResponse response =null;
        try {
                response = mapper.convertValue(result, WaterConnectionResponse.class);
        }
        catch (IllegalArgumentException e){
            throw new CustomException("PARSING ERROR","Error while parsing response of Water Connection Search");
        }

        if(response==null || CollectionUtils.isEmpty(response.getWaterConnection()))
            return null;

		Collections.sort(response.getWaterConnection(), new Comparator<WaterConnection>() {
			@Override
			public int compare(WaterConnection wc1, WaterConnection wc2) {
				return wc1.getAuditDetails().getLastModifiedTime().compareTo(wc2.getAuditDetails().getLastModifiedTime());
			}
		});

		int size = response.getWaterConnection().size();

        return response.getWaterConnection().get(size-1);
    }
    
    
    /**
     * Creates waterConnection search url based on tenantId and connectionNumber
     * @return water search url
     */
	private StringBuilder getWaterSearchURL(String tenantId, String connectionNo) {
		StringBuilder url = new StringBuilder(calculationConfig.getWaterConnectionHost());
		url.append(calculationConfig.getWaterConnectionSearchEndPoint());
		url.append("?");
		url.append("tenantId=");
		url.append(tenantId);
		url.append("&");
		url.append("connectionNumber=");
		url.append(connectionNo);
		return url;
	}
	
	
	/**
	 * 
	 * @param requestInfo
	 * @param searchCriteria
	 * @param tenantId
	 * @return water connection
	 */
	public WaterConnection getWaterConnectionOnApplicationNO(RequestInfo requestInfo, SearchCriteria searchCriteria,
			String tenantId) {
		ObjectMapper mapper = new ObjectMapper();
		Object result = serviceRequestRepository.fetchResult(getWaterSearchURL(searchCriteria),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		try {
			WaterConnectionResponse response = null;
			response = mapper.convertValue(result, WaterConnectionResponse.class);
			if (CollectionUtils.isEmpty(response.getWaterConnection()))
				return null;
			return response.getWaterConnection().get(0);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Error while parsing response of Water Connection Search");
		}
	}
    
    
    /**
     * Creates waterConnection search url based on tenantId and connectionNumber
     * @return water search url
     */
	private StringBuilder getWaterSearchURL(SearchCriteria searchCriteria) {
		StringBuilder url = new StringBuilder(calculationConfig.getWaterConnectionHost());
		url.append(calculationConfig.getWaterConnectionSearchEndPoint());
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
		return url;
	}
	
	
	/**
	 * Methods provides all the usage category master for Water Service module
	 */
	public MdmsCriteriaReq getMdmsReqCriteria(RequestInfo requestInfo, String tenantId,
			ArrayList<String> masterDetails, String moduleName) {

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

		return new StringBuilder().append(calculationConfig.getBillingServiceHost())
				.append(calculationConfig.getFetchBillEndPoint()).append(WSCalculationConstant.URL_PARAMS_SEPARATER)
				.append(WSCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(tenantId)
				.append(WSCalculationConstant.SEPARATER).append(WSCalculationConstant.CONSUMER_CODE_SEARCH_FIELD_NAME)
				.append(consumerCode).append(WSCalculationConstant.SEPARATER)
				.append(WSCalculationConstant.BUSINESSSERVICE_FIELD_FOR_SEARCH_URL).append(WSCalculationConstant.WATER_TAX_SERVICE_CODE);
	}
	
	/**
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @return mdms request for master data
	 */
	public MdmsCriteriaReq getEstimationMasterCriteria(RequestInfo requestInfo, String tenantId) {
		List<MasterDetail> details = new ArrayList<>();
		details.add(MasterDetail.builder().name(WSCalculationConstant.WC_PLOTSLAB_MASTER).filter("[?(@.isActive== " + true + ")]").build());
		details.add(MasterDetail.builder().name(WSCalculationConstant.WC_PROPERTYUSAGETYPE_MASTER).filter("[?(@.isActive== "+ true +")]").build());
		details.add(MasterDetail.builder().name(WSCalculationConstant.WC_FEESLAB_MASTER).filter("[?(@.isActive== " + true + ")]").build());
		details.add(MasterDetail.builder().name(WSCalculationConstant.WC_ROADTYPE_MASTER).filter("[?(@.isActive== "+ true +")]").build());
		ModuleDetail mdDtl = ModuleDetail.builder().masterDetails(details)
				.moduleName(WSCalculationConstant.WS_TAX_MODULE).build();
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

		MasterDetail mstrDetail = MasterDetail.builder().name(WSCalculationConstant.BILLING_PERIOD)
				.filter("[?(@.active== " + true + " && @.connectionType== '" + WSCalculationConstant.nonMeterdConnection
						+ "')]")
				.build();
		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(WSCalculationConstant.WS_MODULE)
				.masterDetails(Arrays.asList(mstrDetail)).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(moduleDetail)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	/**
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @return Master For Billing Period
	 */
	public Map<String, Object> loadBillingFrequencyMasterData(RequestInfo requestInfo, String tenantId) {
		MdmsCriteriaReq mdmsCriteriaReq = getBillingFrequencyForScheduler(requestInfo, tenantId);
		Object res = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
		if (res == null) {
			throw new CustomException("MDMS_ERROR_FOR_BILLING_FREQUENCY", "ERROR IN FETCHING THE BILLING FREQUENCY");
		}
		List<Map<String, Object>> jsonOutput = JsonPath.read(res, WSCalculationConstant.JSONPATH_ROOT_FOR_BilingPeriod);
		return jsonOutput.get(0);
	}

	public Property getProperty(RequestInfo requestInfo,String tenantId,String propertyId){
		ObjectMapper mapper = new ObjectMapper();
		String propertySearchURL = getPropertySearchURL(propertyId,tenantId);
		Object propertyResult = serviceRequestRepository.fetchResult(new StringBuilder(propertySearchURL),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		PropertyResponse properties = null;

		try {
			properties = mapper.convertValue(propertyResult, PropertyResponse.class);
		}
		catch (IllegalArgumentException e){
			throw new CustomException("PARSING ERROR","Error while parsing response of Property Search");
		}

		if(properties==null || CollectionUtils.isEmpty(properties.getProperties()))
			return null;


		return properties.getProperties().get(0);
	}

	public String getPropertySearchURL(String propertyId,String tenantId){
		StringBuilder url = new StringBuilder(calculationConfig.getPropertyHost());
		url.append(calculationConfig.getSearchPropertyEndPoint()).append("?");
		url.append("tenantId=").append(tenantId).append("&");
		url.append("uuids=").append(propertyId);
		return url.toString();
	}


	public List<ProcessInstance> getWorkFlowProcessInstance(RequestInfo requestInfo, String tenantId, String businessIds){
		ObjectMapper mapper = new ObjectMapper();
		String workflowProcessInstanceSearchURL = getWorkflowProcessInstanceSearchURL(tenantId,businessIds);
		Object result = serviceRequestRepository.fetchResult(new StringBuilder(workflowProcessInstanceSearchURL),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		ProcessInstanceResponse processInstanceResponse = null;

		try {
			processInstanceResponse =mapper.convertValue(result, ProcessInstanceResponse.class);
		}
		catch (IllegalArgumentException e){
			throw new CustomException("PARSING ERROR","Error while parsing response of process Instance Search");
		}

		if(processInstanceResponse==null || CollectionUtils.isEmpty(processInstanceResponse.getProcessInstances()))
			return Collections.emptyList();

		return processInstanceResponse.getProcessInstances();
	}

	public String getWorkflowProcessInstanceSearchURL(String tenantId, String businessIds){
		StringBuilder url = new StringBuilder(calculationConfig.getWorkflowHost());
		url.append(calculationConfig.getSearchWorkflowProcessEndPoint()).append("?");
		url.append("tenantId=").append(tenantId).append("&");
		url.append("businessIds=").append(businessIds);
		return url.toString();
	}
}
