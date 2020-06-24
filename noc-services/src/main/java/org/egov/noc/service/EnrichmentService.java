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
import org.egov.noc.util.NOCUtil;
import org.egov.noc.web.model.AuditDetails;
import org.egov.noc.web.model.NOC;
import org.egov.noc.web.model.NOCRequest;
import org.egov.noc.web.model.idgen.IdResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EnrichmentService {
	

	@Autowired
	private NOCConfiguration config;

	@Autowired
	private NOCUtil nocUtil;

	@Autowired
	private IdGenRepository idGenRepository;
	
	public void enrichCreateRequest(NOCRequest nocRequest, Object mdmsData) {
		RequestInfo requestInfo = nocRequest.getRequestInfo();
		AuditDetails auditDetails = nocUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		nocRequest.getNoc().setAuditDetails(auditDetails);
		nocRequest.getNoc().setId(UUID.randomUUID().toString());	
		nocRequest.getNoc().setAccountId(nocRequest.getNoc().getAuditDetails().getCreatedBy());		
		setIdgenIds(nocRequest);
	}
	
	private void setIdgenIds(NOCRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getNoc().getTenantId();
		NOC noc = request.getNoc();

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

}
