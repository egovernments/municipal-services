package org.egov.bpa.service;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.IdGenRepository;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.web.models.AuditDetails;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.idgen.IdResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EnrichmentService {

	@Autowired
	private BPAConfiguration config;

	@Autowired
	private BPAUtil bpaUtil;

	@Autowired
	private IdGenRepository idGenRepository;

	@Autowired
	private BoundaryService boundaryService;

	public void enrichBPACreateRequest(BPARequest bpaRequest, Object mdmsData) {
		// TODO Auto-generated method stub
		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		AuditDetails auditDetails = bpaUtil.getAuditDetails(requestInfo
				.getUserInfo().getUuid(), true);
		bpaRequest.getBPA().setAuditDetails(auditDetails);
		bpaRequest.getBPA().setId(UUID.randomUUID().toString());

		// address
		bpaRequest.getBPA().getAddress()
				.setTenantId(bpaRequest.getBPA().getTenantId());
		bpaRequest.getBPA().getAddress().setId(UUID.randomUUID().toString());

		// units
		bpaRequest.getBPA().getUnits().forEach(unit -> {
			unit.setTenantId(bpaRequest.getBPA().getTenantId());
			unit.setId(UUID.randomUUID().toString());
			unit.setAuditDetails(auditDetails);
			// unit.setActive(true);
			});

		// owners
		bpaRequest.getBPA().getOwners().forEach(owner -> {
//			owner.setId(UUID.randomUUID().toString());
//			owner.setTenantId(bpaRequest.getBPA().getTenantId());
			owner.setAuditDetails(auditDetails);
			owner.setInstitutionId(UUID.randomUUID().toString());
			if (!CollectionUtils.isEmpty(owner.getDocuments()))
				owner.getDocuments().forEach(document -> {
					document.setId(UUID.randomUUID().toString());
				});
		});

		setIdgenIds(bpaRequest);
		setStatusForCreate(bpaRequest);
//		boundaryService.getAreaType(bpaRequest, config.getHierarchyTypeCode());
	}

	/**
	 * Sets the ApplicationNumber for given TradeLicenseRequest
	 *
	 * @param request
	 *            TradeLicenseRequest which is to be created
	 */
	private void setIdgenIds(BPARequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getBPA().getTenantId();
		BPA bpa = request.getBPA();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId,
				config.getApplicationNoIdgenName(),
				config.getApplicationNoIdgenFormat(), 1);
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		bpa.setApplicationNo(itr.next());
	}

	/**
	 * Returns a list of numbers generated from idgen
	 *
	 * @param requestInfo
	 *            RequestInfo from the request
	 * @param tenantId
	 *            tenantId of the city
	 * @param idKey
	 *            code of the field defined in application properties for which
	 *            ids are generated for
	 * @param idformat
	 *            format in which ids are to be generated
	 * @param count
	 *            Number of ids to be generated
	 * @return List of ids generated using idGen service
	 */
	private List<String> getIdList(RequestInfo requestInfo, String tenantId,
			String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo,
				tenantId, idKey, idformat, count).getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN ERROR",
					"No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId)
				.collect(Collectors.toList());
	}

	/**
	 * Sets status for create request
	 * 
	 * @param tradeLicenseRequest
	 *            The create request
	 */
	private void setStatusForCreate(BPARequest tradeLicenseRequest) {

	}
}
