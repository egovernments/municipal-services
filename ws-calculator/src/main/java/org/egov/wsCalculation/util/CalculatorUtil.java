package org.egov.wsCalculation.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionResponse;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.RequestInfoWrapper;
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
	 /**
     * Call WS-services to get waterConnection for the given applicationNumber and tenantID
     * @param requestInfo The RequestInfo of the incoming request
     * @param applicationNumber The applicationNumber whose water connection has to be fetched
     * @param tenantId The tenantId of the water connection
     * @return The water connection fo the particular applicationNumber
     */
    public WaterConnection getWaterConnection(RequestInfo requestInfo, String connectionNo, String tenantId){
        ObjectMapper mapper = new ObjectMapper();
    	String url = getWaterSearchURL();
        url = url.replace("{1}",tenantId).replace("{2}",connectionNo);
        Object result =serviceRequestRepository.fetchResult(new StringBuilder(url),RequestInfoWrapper.builder().
                requestInfo(requestInfo).build());

        WaterConnectionResponse response =null;
        try {
                response = mapper.convertValue(result, WaterConnectionResponse.class);
        }
        catch (IllegalArgumentException e){
            throw new CustomException("PARSING ERROR","Error while parsing response of TradeLicense Search");
        }

        if(response==null || CollectionUtils.isEmpty(response.getWaterConnection()))
            return null;

        return response.getWaterConnection().get(0);
    }
    
    
    /**
     * Creates tradeLicense search url based on tenantId and applicationNumber
     * @return water search url
     */
	private String getWaterSearchURL() {
		StringBuilder url = new StringBuilder(calculationConfig.getWaterConnectionHost());
		url.append(calculationConfig.getWaterConnectionSearchEndPoint());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("connectionNumber=");
		url.append("{2}");
		return url.toString();
	}

}
