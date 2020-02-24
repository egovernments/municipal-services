package org.egov.wsCalculation.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.wsCalculation.model.RequestInfoWrapper;
import org.egov.wsCalculation.model.SearchCriteria;
import org.egov.wsCalculation.model.WaterConnection;
import org.egov.wsCalculation.model.WaterConnectionResponse;
import org.egov.wsCalculation.repository.ServiceRequestRepository;
import org.egov.wsCalculation.config.WSCalculationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Component
@Getter
public class CalculatorUtil {

	@Autowired
	WSCalculationConfiguration calculationConfig;
	
	@Autowired
	ServiceRequestRepository serviceRequestRepository;
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

		MasterDetail mstrDetail = MasterDetail.builder().name(WSCalculationConstant.BillingPeriod)
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

        return response.getWaterConnection().get(0);
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
	 * @param connectionNo
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
	 * @param roadType
	 * @param usageType
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
}
