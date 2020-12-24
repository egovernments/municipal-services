package org.egov.fsm.service;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.repository.FSMRepository;
import org.egov.fsm.repository.IdGenRepository;
import org.egov.fsm.util.FSMConstants;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.fsm.util.FSMUtil;
import org.egov.fsm.web.model.AuditDetails;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.FSMRequest;
import org.egov.fsm.web.model.idgen.IdResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EnrichmentService {
	@Autowired
	private FSMConfiguration config;

	@Autowired
	private IdGenRepository idGenRepository;
	@Autowired
	private BoundaryService boundaryService ;
	
	@Autowired
	private FSMRepository repository ;

	@Autowired
	private UserService userService;
	
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
		
		userService.manageApplicant(fsmRequest);
		boundaryService.getAreaType(fsmRequest, config.getHierarchyTypeCode());
		
		fsmRequest.getFsm().setApplicationStatus(FSMConstants.DRAFT);
		AuditDetails auditDetails = fsmUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		fsmRequest.getFsm().setAuditDetails(auditDetails);
		fsmRequest.getFsm().setId(UUID.randomUUID().toString());
		
		fsmRequest.getFsm().setAccountId(fsmRequest.getFsm().getCitizen().getUuid());
		
		if (fsmRequest.getFsm().getAddress() != null) {
			if (StringUtils.isEmpty(fsmRequest.getFsm().getAddress().getId()))
				fsmRequest.getFsm().getAddress().setId(UUID.randomUUID().toString());
			fsmRequest.getFsm().getAddress().setTenantId(fsmRequest.getFsm().getTenantId());
			fsmRequest.getFsm().getAddress().setAuditDetails(auditDetails);
			if (fsmRequest.getFsm().getAddress().getGeoLocation() != null
					&& StringUtils.isEmpty(fsmRequest.getFsm().getAddress().getGeoLocation().getId()))
				fsmRequest.getFsm().getAddress().getGeoLocation().setId(UUID.randomUUID().toString());
		}else {
			throw new CustomException(FSMErrorConstants.INVALID_ADDRES," Address is mandatory");
		}
		
		if(fsmRequest.getFsm().getPitDetail() != null) {
			if (StringUtils.isEmpty(fsmRequest.getFsm().getPitDetail().getId()))
				fsmRequest.getFsm().getPitDetail().setId(UUID.randomUUID().toString());
			fsmRequest.getFsm().getPitDetail().setTenantId(fsmRequest.getFsm().getTenantId());
			fsmRequest.getFsm().getPitDetail().setAuditDetails(auditDetails);
		}
		
		
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
		AuditDetails auditDetails = fsmUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
		fsmRequest.getFsm().setAuditDetails(auditDetails);


	}

	/**
	 * enrich the request with post workflow call based on the workflow response
	 * @param fsmRequest
	 */
	public void postStatusEnrichment(FSMRequest fsmRequest) {
		// TODO Auto-generated method stub
		
	}
}
