package org.egov.swCalculation.util;

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
import org.egov.swCalculation.config.SWCalculationConfiguration;
import org.egov.swCalculation.constants.SWCalculationConstant;
import org.egov.swCalculation.model.RequestInfoWrapper;
import org.egov.swCalculation.repository.ServiceRequestRepository;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Component
@Getter
public class CalculatorUtils {
	
	@Autowired
	SWCalculationConfiguration configurations;
	
	@Autowired
	ServiceRequestRepository serviceRequestRepository;
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
     * @param requestInfo The RequestInfo of the incoming request
     * @param connectionNo The connectionNumber whose sewerage connection has to be fetched
     * @param tenantId The tenantId of the sewerage connection
     * @return The water connection fo the particular connection no
     */
    public SewerageConnection getSewerageConnection(RequestInfo requestInfo, String connectionNo, String tenantId){
        ObjectMapper mapper = new ObjectMapper();
    	String url = getSewerageSearchURL();
        url = url.replace("{1}",tenantId).replace("{2}",connectionNo);
        Object result =serviceRequestRepository.fetchResult(new StringBuilder(url),RequestInfoWrapper.builder().
                requestInfo(requestInfo).build());

        SewerageConnectionResponse response =null;
        try {
                response = mapper.convertValue(result, SewerageConnectionResponse.class);
        }
        catch (IllegalArgumentException e){
            throw new CustomException("PARSING ERROR","Error while parsing response of Sewerage Search");
        }

        if(response==null || CollectionUtils.isEmpty(response.getSewerageConnections()))
            return null;

        return response.getSewerageConnections().get(0);
    }
    
    /**
     * Creates tradeLicense search url based on tenantId and applicationNumber
     * @return water search url
     */
	private String getSewerageSearchURL() {
		StringBuilder url = new StringBuilder(configurations.getSewerageConnectionHost());
		url.append(configurations.getSewerageConnectionSearchEndPoint());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("connectionNumber=");
		url.append("{2}");
		return url.toString();
	}
	
	
	/**
	 * Methods provides all the usage category master for Sewerage Service module
	 */
	public MdmsCriteriaReq getWaterConnectionModuleRequest(RequestInfo requestInfo, String tenantId) {
		List<MasterDetail> details = new ArrayList<>();
		details.add(MasterDetail.builder().name(SWCalculationConstant.SW_REBATE_MASTER).build());
		details.add(MasterDetail.builder().name(SWCalculationConstant.SW_PENANLTY_MASTER).build());
		details.add(MasterDetail.builder().name(SWCalculationConstant.SW_INTEREST_MASTER).build());
		details.add(MasterDetail.builder().name(SWCalculationConstant.SW_BILLING_SLAB_MASTER).build());
		ModuleDetail mdDtl = ModuleDetail.builder().masterDetails(details)
				.moduleName(SWCalculationConstant.SW_TAX_MODULE).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(mdDtl)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}
}
