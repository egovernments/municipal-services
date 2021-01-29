package org.egov.fsm.calculator.services;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.calculator.config.CalculatorConfig;
import org.egov.fsm.calculator.repository.ServiceRequestRepository;
import org.egov.fsm.calculator.web.models.CalculationReq;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MDMSService {

	@Autowired
	 private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private CalculatorConfig config;

	
    public Object mDMSCall(CalculationReq calculationReq,String tenantId){
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(calculationReq,tenantId);
        StringBuilder url = getMdmsSearchUrl();
        Object result = serviceRequestRepository.fetchResult(url , mdmsCriteriaReq);
        return result;
    }

    /**
     * Creates and returns the url for mdms search endpoint
     *
     * @return MDMS Search URL
     */
    private StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsSearchEndpoint());
    }

    /**
     * Creates MDMS request
     * @param requestInfo The RequestInfo of the calculationRequest
     * @param tenantId The tenantId of the tradeLicense
     * @return MDMSCriteria Request
     */
    private MdmsCriteriaReq getMDMSRequest(CalculationReq calculationReq, String tenantId) {
    	RequestInfo requestInfo = 	calculationReq.getRequestInfo();
        List<ModuleDetail> moduleDetails = new ArrayList<>();
        // TODO for product there is no need to query mdms data
        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
    }




}
