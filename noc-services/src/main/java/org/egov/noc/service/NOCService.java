package org.egov.noc.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.noc.repository.NOCRepository;
import org.egov.noc.util.NOCConstants;
import org.egov.noc.util.NOCUtil;
import org.egov.noc.validator.NOCValidator;
import org.egov.noc.web.model.Noc;
import org.egov.noc.web.model.NocRequest;
import org.egov.noc.web.model.NocSearchCriteria;
import org.egov.noc.web.model.workflow.BusinessService;
import org.egov.noc.workflow.WorkflowIntegrator;
import org.egov.noc.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

@Service
public class NOCService {
	
	@Autowired
	private NOCValidator nocValidator;
	
	@Autowired
	private WorkflowIntegrator wfIntegrator;
	
	@Autowired
	private NOCUtil nocUtil;
	
	@Autowired
	private NOCRepository nocRepository;
	
	@Autowired
	private EnrichmentService enrichmentService;
	
	@Autowired
	private WorkflowService workflowService;

	
	public List<Noc> create(NocRequest nocRequest) {
		String tenantId = nocRequest.getNoc().getTenantId().split("\\.")[0];
		Object mdmsData = nocUtil.mDMSCall(nocRequest.getRequestInfo(), tenantId);
		Map<String, String> additionalDetails = nocValidator.getOrValidateBussinessService(nocRequest.getNoc(), mdmsData);
		nocValidator.validateCreate(nocRequest,  mdmsData);
		enrichmentService.enrichCreateRequest(nocRequest, mdmsData);
		wfIntegrator.callWorkFlow(nocRequest, additionalDetails.get(NOCConstants.WORKFLOWCODE));
		nocRepository.save(nocRequest);
		return Arrays.asList(nocRequest.getNoc());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Noc> update(NocRequest nocRequest) {
		String tenantId = nocRequest.getNoc().getTenantId().split("\\.")[0];
		Object mdmsData = nocUtil.mDMSCall(nocRequest.getRequestInfo(), tenantId);
		Map<String, String> additionalDetails  ;
		if(!ObjectUtils.isEmpty(nocRequest.getNoc().getAdditionalDetails()))  {
			additionalDetails = (Map) nocRequest.getNoc().getAdditionalDetails();
		} else {
			additionalDetails = nocValidator.getOrValidateBussinessService(nocRequest.getNoc(), mdmsData);
		}
		BusinessService businessService = workflowService.getBusinessService(nocRequest.getNoc(), nocRequest.getRequestInfo(), additionalDetails.get(NOCConstants.WORKFLOWCODE));
		Noc searchResult = getNocForUpdate(nocRequest);
		nocValidator.validateUpdate(nocRequest, searchResult, additionalDetails.get(NOCConstants.MODE), mdmsData);
		enrichmentService.enrichNocUpdateRequest(nocRequest, searchResult);
		wfIntegrator.callWorkFlow(nocRequest, additionalDetails.get(NOCConstants.WORKFLOWCODE));
		enrichmentService.postStatusEnrichment(nocRequest, additionalDetails.get(NOCConstants.WORKFLOWCODE));
        nocRepository.update(nocRequest, workflowService.isStateUpdatable(nocRequest.getNoc().getApplicationStatus(), businessService));
		return Arrays.asList(nocRequest.getNoc());
	}
	
	public List<Noc> search(NocSearchCriteria criteria, RequestInfo requestInfo) {
		/*List<String> uuids = new ArrayList<String>();
		uuids.add(requestInfo.getUserInfo().getUuid());
		criteria.setAccountId(uuids);*/
		List<Noc> noc = nocRepository.getNocData(criteria);
		return noc.isEmpty() ? Collections.emptyList() : noc;
	}	
	
	public Noc getNocForUpdate(NocRequest nocRequest) {		
		List<String> ids = Arrays.asList(nocRequest.getNoc().getId());
		NocSearchCriteria criteria = new NocSearchCriteria();
		criteria.setIds(ids);
		List<Noc> nocList = search(criteria, nocRequest.getRequestInfo());
		if (CollectionUtils.isEmpty(nocList)) {
			StringBuilder builder = new StringBuilder();
			builder.append("Noc Application not found for: ").append(nocRequest.getNoc().getId()).append(" :ID");
			throw new CustomException("INVALID_NOC_SEARCH", builder.toString());
		}			
		return nocList.get(0);
	}
	
}
