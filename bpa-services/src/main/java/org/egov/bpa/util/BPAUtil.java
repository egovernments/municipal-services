package org.egov.bpa.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.web.models.AuditDetails;
import org.egov.bpa.web.models.BPARequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

@Component
public class BPAUtil {

	private BPAConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	public BPAUtil(BPAConfiguration config, ServiceRequestRepository serviceRequestRepository) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
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
		if (isCreate)
			return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time)
					.build();
		else
			return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
	}

	/**
	 * Returns the URL for MDMS search end point
	 *
	 * @return URL for MDMS search end point
	 */
	public StringBuilder getMdmsSearchUrl() {
		return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
	}

	/**
	 * Creates request to search ApplicationType and etc from MDMS
	 * 
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @param tenantId
	 *            The tenantId of the BPA
	 * @return request to search ApplicationType and etc from MDMS
	 */
	public List<ModuleDetail> getBPAModuleRequest() {

		// master details for BPA module
		List<MasterDetail> bpaMasterDtls = new ArrayList<>();

		// filter to only get code field from master data
		final String filterCode = "$.[?(@.active==true)].code";

		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.APPLICATION_TYPE).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.SERVICE_TYPE).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.DOCUMENT_TYPE_MAPPING).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.RISKTYPE_COMPUTATION).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.OCCUPANCY_TYPE).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.SUB_OCCUPANCY_TYPE).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.USAGES).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.CalculationType).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.CHECKLIST_NAME).build());
		ModuleDetail bpaModuleDtls = ModuleDetail.builder().masterDetails(bpaMasterDtls)
				.moduleName(BPAConstants.BPA_MODULE).build();

		// master details for common-masters module
		List<MasterDetail> commonMasterDetails = new ArrayList<>();
		commonMasterDetails
				.add(MasterDetail.builder().name(BPAConstants.OWNERSHIP_CATEGORY).filter(filterCode).build());
		commonMasterDetails.add(MasterDetail.builder().name(BPAConstants.OWNER_TYPE).filter(filterCode).build());
		commonMasterDetails.add(MasterDetail.builder().name(BPAConstants.DOCUMENT_TYPE).filter(filterCode).build());
		ModuleDetail commonMasterMDtl = ModuleDetail.builder().masterDetails(commonMasterDetails)
				.moduleName(BPAConstants.COMMON_MASTERS_MODULE).build();

		return Arrays.asList(bpaModuleDtls, commonMasterMDtl);

	}

	private MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId) {
		List<ModuleDetail> moduleRequest = getBPAModuleRequest();

		List<ModuleDetail> moduleDetails = new LinkedList<>();
		moduleDetails.addAll(moduleRequest);

		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId).build();

		MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria).requestInfo(requestInfo)
				.build();
		return mdmsCriteriaReq;
	}

	public Object mDMSCall(BPARequest bpaRequest) {
		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		String tenantId = bpaRequest.getBPA().getTenantId().split("\\.")[0];
		MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId);
		Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
		return result;
	}

	public void defaultJsonPathConfig() {
		Configuration.setDefaults(new Configuration.Defaults() {

			private final JsonProvider jsonProvider = new JacksonJsonProvider();
			private final MappingProvider mappingProvider = new JacksonMappingProvider();

			@Override
			public JsonProvider jsonProvider() {
				return jsonProvider;
			}

			@Override
			public MappingProvider mappingProvider() {
				return mappingProvider;
			}

			@Override
			public Set<Option> options() {
				return EnumSet.noneOf(Option.class);
			}
		});
	}
}
