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
import org.egov.noc.workflow.WorkflowIntegrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	
	public List<Noc> create(NocRequest nocRequest) {
		String tenantId = nocRequest.getNoc().getTenantId().split("\\.")[0];
		Object mdmsData = nocUtil.mDMSCall(nocRequest.getRequestInfo(), tenantId);
		nocValidator.validateCreate(nocRequest, mdmsData);		
		enrichmentService.enrichCreateRequest(nocRequest, mdmsData);
		wfIntegrator.callWorkFlow(nocRequest);
		nocRepository.save(nocRequest);
		return Arrays.asList(nocRequest.getNoc());
	}
	
	public List<Noc> search(NocSearchCriteria criteria) {
		List<Noc> noc = nocRepository.getNocData(criteria);
		return noc.isEmpty() ? Collections.emptyList() : noc;
	}
}
