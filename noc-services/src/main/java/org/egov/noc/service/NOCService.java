package org.egov.noc.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.egov.noc.repository.NOCRepository;
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
		nocValidator.validateCreate(nocRequest, mdmsData);		
		enrichmentService.enrichCreateRequest(nocRequest, mdmsData);
		wfIntegrator.callWorkFlow(nocRequest);
		nocRepository.save(nocRequest);
		return Arrays.asList(nocRequest.getNoc());
	}
	
	public List<Noc> update(NocRequest nocRequest) {
		String tenantId = nocRequest.getNoc().getTenantId().split("\\.")[0];
		Object mdmsData = nocUtil.mDMSCall(nocRequest.getRequestInfo(), tenantId);
		BusinessService businessService = workflowService.getBusinessService(nocRequest.getNoc(), nocRequest.getRequestInfo());
		Noc searchResult = getNocForUpdate(nocRequest);
		nocValidator.validateUpdate(nocRequest, searchResult, mdmsData);
		enrichmentService.enrichNocUpdateRequest(nocRequest, searchResult);
		wfIntegrator.callWorkFlow(nocRequest);
		enrichmentService.postStatusEnrichment(nocRequest);
        nocRepository.update(nocRequest, workflowService.isStateUpdatable(nocRequest.getNoc().getApplicationStatus(), businessService));
		return Arrays.asList(nocRequest.getNoc());
	}
	
	public List<Noc> search(NocSearchCriteria criteria) {
		List<Noc> noc = nocRepository.getNocData(criteria);
		return noc.isEmpty() ? Collections.emptyList() : noc;
	}	
	
	public Noc getNocForUpdate(NocRequest nocRequest) {		
		List<String> ids = Arrays.asList(nocRequest.getNoc().getId());
		NocSearchCriteria criteria = new NocSearchCriteria();
		criteria.setIds(ids);
		List<Noc> nocList = search(criteria);
		if (CollectionUtils.isEmpty(nocList)) {
			StringBuilder builder = new StringBuilder();
			builder.append("Noc Application not found for: ").append(nocRequest.getNoc().getId()).append(" :ID");
			throw new CustomException("INVALID_NOC_SEARCH", builder.toString());
		}			
		return nocList.get(0);
	}
	
}
