package org.egov.echallan.expense.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.expense.model.AuditDetails;
import org.egov.echallan.expense.model.ExpenseRequest;
import org.egov.echallan.expense.repository.ExpenseServiceRequestRepository;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Component
@Getter
public class ExpenseCommonUtils {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ChallanConfiguration configs;

	@Autowired
	private ExpenseServiceRequestRepository expenseServiceRequestRepository;

	private ExpenseConstants expenseConstants;

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

	public Object mDMSCall(ExpenseRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getExpense().getTenantId();
		String service = request.getExpense().getBusinessService();
		MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId, service);
		return expenseServiceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
	}

	public StringBuilder getMdmsSearchUrl() {
		return new StringBuilder().append(configs.getMdmsHost()).append(configs.getMdmsEndPoint());
	}

	private MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId, String service) {
		ModuleDetail moduleDeatilRequest = getModuleDetailRequest(service);
		List<ModuleDetail> moduleDetails = new LinkedList<>();
		moduleDetails.add(moduleDeatilRequest);

		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId).build();

		return MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria).requestInfo(requestInfo)
				.build();
	}

	private ModuleDetail getModuleDetailRequest(String service) {
		List<MasterDetail> masterDetails = new ArrayList<>();

		// filter to only get code field from master data
		final String filterCode = "$.[?(@.service=='" + service + "')]";

		masterDetails.add(MasterDetail.builder().name(expenseConstants.TAXPERIOD_MASTER).filter(filterCode).build());
		masterDetails.add(MasterDetail.builder().name(expenseConstants.TAXPHEADCODE_MASTER).filter(filterCode).build());

		return ModuleDetail.builder().masterDetails(masterDetails)
				.moduleName(expenseConstants.BILLING_SERVICE).build();

	}

}
