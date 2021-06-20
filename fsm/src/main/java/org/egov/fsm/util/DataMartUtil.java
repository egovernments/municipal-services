package org.egov.fsm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.repository.ServiceRequestRepository;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
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
		//final String filterCode = "$.[?(@.active==true)].code";
		final String activeFilter = "$.[?(@.active==true)]";
		// master details for FSM module
		List<MasterDetail> fsmMasterDtls = new ArrayList<>();

		fsmMasterDtls
				.add(MasterDetail.builder().name(FSMConstants.MDMS_APPLICATION_CHANNEL).build());
		fsmMasterDtls.add(MasterDetail.builder().name(FSMConstants.MDMS_SANITATION_TYPE).build());
		fsmMasterDtls.add(MasterDetail.builder().name(FSMConstants.MDMS_PROPERTY_TYPE).build());
		fsmMasterDtls.add(MasterDetail.builder().name(FSMConstants.MDMS_SLUM_NAME).build());
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
	
	public Map<String, JsonNode> groupMdmsDataByMater(Object mdmsData) {

		List<String> modulepaths = Arrays.asList(FSMConstants.FSM_JSONPATH_CODE);
		final Map<String, JsonNode> mdmsResMap = new HashMap<>();
		modulepaths.forEach(modulepath -> {
			try {
				mdmsResMap.putAll(JsonPath.read(mdmsData, modulepath));
			} catch (Exception e) {
				log.error("Error while fetvhing MDMS data", e);
				throw new CustomException(FSMErrorConstants.INVALID_TENANT_ID_MDMS_KEY,
						FSMErrorConstants.INVALID_TENANT_ID_MDMS_MSG);
			}
		});
		return mdmsResMap;
	}

}
