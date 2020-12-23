package org.egov.fsm.service;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.repository.IdGenRepository;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.fsm.util.FSMUtil;
import org.egov.fsm.web.model.AuditDetails;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.FSMRequest;
import org.egov.fsm.web.model.IdResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EnrichmentService {
	@Autowired
	private FSMConfiguration config;

	@Autowired
	private IdGenRepository idGenRepository;
	@Autowired
	private FSMUtil fsmUtil;
	/**
	 * enrich the create FSM request with the required data
	 * @param fsmRequest
	 * @param mdmsData
	 */
	public void enrichFSMCreateRequest(FSMRequest fsmRequest, Object mdmsData) {
		//TODO add requied logic
		RequestInfo requestInfo = fsmRequest.getRequestInfo();
		AuditDetails auditDetails = fsmUtil.getAuditDetails(requestInfo.getUserInfo(), true);
		fsmRequest.getFsm().setAuditDetails(auditDetails);
		fsmRequest.getFsm().setId(UUID.randomUUID().toString());
		
		fsmRequest.getFsm().setAccountId(fsmRequest.getFsm().getAuditDetails().getCreatedBy());
		
		
		setIdgenIds(fsmRequest);
	}
	
	/**
	 *  generate the applicationNo using the idGen serivce and populate
	 * @param request
	 */
	private void setIdgenIds(FSMRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getFsm().getTenantId();
		FSM fsm = request.getFsm();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNoIdgenName(),
				config.getApplicationNoIdgenFormat(), 1);
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		fsm.setApplicationNo(itr.next());
	}
	
	/**
	 * Generate the id
	 * @param requestInfo
	 * @param tenantId
	 * @param idKey
	 * @param idformat
	 * @param count
	 * @return
	 */
	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException(FSMErrorConstants.IDGEN_ERROR, "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}
	
	/**
	 *  enrich the update request with the requied ata
	 * @param fsmRequest
	 * @param mdmsData
	 */
	public void enrichFSMUpdateRequest(FSMRequest fsmRequest, Object mdmsData) {
		//TODO add requied logic
		RequestInfo requestInfo = fsmRequest.getRequestInfo();
		AuditDetails auditDetails = fsmUtil.getAuditDetails(requestInfo.getUserInfo(), true);
		fsmRequest.getFsm().setAuditDetails(auditDetails);
		fsmRequest.getFsm().setId(UUID.randomUUID().toString());
		
		fsmRequest.getFsm().setAccountId(fsmRequest.getFsm().getAuditDetails().getCreatedBy());
		
		
		setIdgenIds(fsmRequest);
	}

	/**
	 * enrich the request with post workflow call based on the workflow response
	 * @param fsmRequest
	 */
	public void postStatusEnrichment(FSMRequest fsmRequest) {
		// TODO Auto-generated method stub
		
	}
}
