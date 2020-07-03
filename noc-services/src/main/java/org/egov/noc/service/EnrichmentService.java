package org.egov.noc.service;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.noc.config.NOCConfiguration;
import org.egov.noc.repository.IdGenRepository;
import org.egov.noc.util.NOCConstants;
import org.egov.noc.util.NOCUtil;
import org.egov.noc.web.model.AuditDetails;
import org.egov.noc.web.model.Noc;
import org.egov.noc.web.model.NocRequest;
import org.egov.noc.web.model.idgen.IdResponse;
import org.egov.noc.web.model.workflow.BusinessService;
import org.egov.noc.web.model.workflow.State;
import org.egov.noc.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@Service
public class EnrichmentService {
	

	@Autowired
	private NOCConfiguration config;

	@Autowired
	private NOCUtil nocUtil;

	@Autowired
	private IdGenRepository idGenRepository;
	
	@Autowired
	private WorkflowService workflowService;
	
	public void enrichCreateRequest(NocRequest nocRequest, Object mdmsData) {
		RequestInfo requestInfo = nocRequest.getRequestInfo();
		AuditDetails auditDetails = nocUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		nocRequest.getNoc().setAuditDetails(auditDetails);
		nocRequest.getNoc().setId(UUID.randomUUID().toString());	
		nocRequest.getNoc().setAccountId(nocRequest.getNoc().getAuditDetails().getCreatedBy());		
		setIdgenIds(nocRequest);
		if (!CollectionUtils.isEmpty(nocRequest.getNoc().getDocuments()))
			nocRequest.getNoc().getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
		});
	}
	
	private void setIdgenIds(NocRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getNoc().getTenantId();
		Noc noc = request.getNoc();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNoIdgenName(), 1);
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		noc.setApplicationNo(itr.next());
	}
	
	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}	
	
	public void enrichNocUpdateRequest(NocRequest nocRequest, Noc searchResult) {

		RequestInfo requestInfo = nocRequest.getRequestInfo();
		AuditDetails auditDetails = nocUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);		
		nocRequest.getNoc().setAuditDetails(auditDetails);
		nocRequest.getNoc().getAuditDetails().setLastModifiedTime(auditDetails.getLastModifiedTime());
		
		// Noc Documents
		if (!CollectionUtils.isEmpty(nocRequest.getNoc().getDocuments()))
			nocRequest.getNoc().getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		if (!CollectionUtils.isEmpty(nocRequest.getNoc().getWorkflow().getDocuments())) {
			nocRequest.getNoc().getWorkflow().getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		}
		nocRequest.getNoc().setApplicationNo(searchResult.getApplicationNo());
		nocRequest.getNoc().getAuditDetails()
				.setCreatedBy(searchResult.getAuditDetails().getCreatedBy());
		nocRequest.getNoc().getAuditDetails()
				.setCreatedTime(searchResult.getAuditDetails().getCreatedTime());

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void postStatusEnrichment(NocRequest nocRequest, String businessServiceValue) {
		Noc noc = nocRequest.getNoc();

		BusinessService businessService = workflowService.getBusinessService(noc, nocRequest.getRequestInfo(), businessServiceValue);
		
		State stateObj = workflowService.getCurrentState(noc.getApplicationStatus(), businessService);
		String state = stateObj!=null ? stateObj.getState() : StringUtils.EMPTY;
		
		
		if (state.equalsIgnoreCase(NOCConstants.APPROVED_STATE) || state.equalsIgnoreCase(NOCConstants.AUTOAPPROVED_STATE)) {
			
			Map<String, Object> additionalDetail = null;
			if(noc.getAdditionalDetails() != null) {
				additionalDetail = (Map) noc.getAdditionalDetails();
			} else {
				additionalDetail = new HashMap<String, Object>();
				noc.setAdditionalDetails(additionalDetail);
			}

			List<IdResponse> idResponses = idGenRepository.getId(nocRequest.getRequestInfo(), noc.getTenantId(),
					config.getApplicationNoIdgenName(), 1).getIdResponses();
			noc.setNocNo(idResponses.get(0).getId());
		}
	  }
		

}