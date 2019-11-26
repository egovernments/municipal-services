package org.egov.bpa.util;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.web.models.AuditDetails;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.workflow.WorkflowService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.bpa.web.models.workflow.BusinessService;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BPAUtil {

	 private BPAConfiguration config;

	    private ServiceRequestRepository serviceRequestRepository;

	    private WorkflowService workflowService;

	    @Autowired
	    public BPAUtil(BPAConfiguration config, ServiceRequestRepository serviceRequestRepository,
	                     WorkflowService workflowService) {
	        this.config = config;
	        this.serviceRequestRepository = serviceRequestRepository;
	        this.workflowService = workflowService;
	    }



	    /**
	     * Method to return auditDetails for create/update flows
	     *
	     * @param by
	     * @param isCreate
	     * @return AuditDetails
	     */
	    public AuditDetails getAuditDetails(String by, Boolean isCreate) {
	        Long time = System.currentTimeMillis();
	        if(isCreate)
	            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
	        else
	            return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
	    }
	    
	    /**
	     * Returns the url for mdms search endpoint
	     *
	     * @return url for mdms search endpoint
	     */
	    public StringBuilder getMdmsSearchUrl() {
	        return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
	    }
	    

	    /**
	     * Creates request to search financialYear in mdms
	     * @return MDMS request for financialYear
	     */
	    private ModuleDetail getFinancialYearRequest() {

	        // master details for TL module
	        List<MasterDetail> masterDetails = new ArrayList<>();

	        // filter to only get code field from master data

	        final String filterCodeForUom = "$.[?(@.active==true && @.module=='TL')]";

	        masterDetails.add(MasterDetail.builder().name(BPAConstants.MDMS_FINANCIALYEAR).filter(filterCodeForUom).build());

	        ModuleDetail masterDetail = ModuleDetail.builder().masterDetails(masterDetails)
	                .moduleName(BPAConstants.MDMS_EGF_MASTER).build();



	        return masterDetail;
	    }
	    
	    /**
	     * Creates request to search ApplicationType and etc from MDMS
	     * @param requestInfo The requestInfo of the request
	     * @param tenantId The tenantId of the BPA
	     * @return request to search ApplicationType and etc from MDMS
	     */
	    public List<ModuleDetail> getBPAModuleRequest() {

	        // master details for BPA module
	        List<MasterDetail> bpaMasterDtls = new ArrayList<>();

	        // filter to only get code field from master data
	        final String filterCode = "$.[?(@.active==true)].code";

	        bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.APPLICATION_TYPE).build());

	        ModuleDetail bpaModuleDtls = ModuleDetail.builder().masterDetails(bpaMasterDtls)
	                .moduleName(BPAConstants.BPA_MODULE).build();


	        return Arrays.asList(bpaModuleDtls);

	    }
	    
	    private MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo,String tenantId){
	        ModuleDetail financialYearRequest = getFinancialYearRequest();
	        List<ModuleDetail> tradeModuleRequest = getBPAModuleRequest();

	        List<ModuleDetail> moduleDetails = new LinkedList<>();
	        moduleDetails.add(financialYearRequest);
	        moduleDetails.addAll(tradeModuleRequest);

	        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
	                .build();

	        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
	                .requestInfo(requestInfo).build();
	        return mdmsCriteriaReq;
	    }



	    public Object mDMSCall(BPARequest bpaRequest){
	        RequestInfo requestInfo = bpaRequest.getRequestInfo();
	        String tenantId = bpaRequest.getBPA().getTenantId();
	        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo,tenantId);
	        Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
	        return result;
	    }
	    

	    /**
	     * Creates a map of id to isStateUpdatable
	     * @param searchresult Application from DB
	     * @param businessService The businessService configuration
	     * @return Map of is to isStateUpdatable
	     */
	    public Map<String,Boolean> getIdToIsStateUpdatableMap(BusinessService businessService,List<BPA> searchresult){
	        Map<String ,Boolean> idToIsStateUpdatableMap = new HashMap<>();
	        searchresult.forEach(result -> {
	            idToIsStateUpdatableMap.put(result.getId(),workflowService.isStateUpdatable(result.getStatus(), businessService));
	        });
	        return idToIsStateUpdatableMap;
	    }

}
