package org.egov.fsm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.repository.ServiceRequestRepository;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataMartUtil {
	
	 @Autowired
	 ServiceRequestRepository serviceRequestRepository;
	 
	@Autowired
	private FSMConfiguration config;
	

	public MdmsCriteriaReq getFSMMasterDataForDataMart(RequestInfo requestInfo, String tenantId) {
		List<ModuleDetail> moduleRequest = getFSMModuleRequest();

		List<ModuleDetail> moduleDetails = new LinkedList<>();
		moduleDetails.addAll(moduleRequest);

		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId).build();

		MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria).requestInfo(requestInfo)
				.build();
		return mdmsCriteriaReq;
	}

	public List<ModuleDetail> getFSMModuleRequest() {

		// filter to only get code field from master data
		final String filterCode = "$.[?(@.active==true)].code";
		final String activeFilter = "$.[?(@.active==true)]";
		// master details for FSM module
		List<MasterDetail> fsmMasterDtls = new ArrayList<>();

		fsmMasterDtls
				.add(MasterDetail.builder().name(FSMConstants.MDMS_APPLICATION_CHANNEL).filter(filterCode).build());
		fsmMasterDtls.add(MasterDetail.builder().name(FSMConstants.MDMS_SANITATION_TYPE).filter(filterCode).build());
		fsmMasterDtls.add(MasterDetail.builder().name(FSMConstants.MDMS_PROPERTY_TYPE).filter(filterCode).build());
		fsmMasterDtls.add(MasterDetail.builder().name(FSMConstants.MDMS_SLUM_NAME).filter(activeFilter).build());
		ModuleDetail fsmMasterMDtl = ModuleDetail.builder().masterDetails(fsmMasterDtls)
				.moduleName(FSMConstants.FSM_MODULE_CODE).build();

		return Arrays.asList(fsmMasterMDtl);

	}
	
	public Object mDMSCall(RequestInfo requestInfo, String tenantId) {
		MdmsCriteriaReq mdmsCriteriaReq = getFSMMasterDataForDataMart(requestInfo, tenantId);
		Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
		return result;
	}
	
	public StringBuilder getMdmsSearchUrl() {
		return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
	}

}
