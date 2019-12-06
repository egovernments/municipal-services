package org.egov.bpa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.IdGenRepository;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.web.models.AuditDetails;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.OwnerInfo;
import org.egov.bpa.web.models.idgen.IdResponse;
import org.egov.bpa.web.models.user.UserDetailResponse;
import org.egov.bpa.web.models.workflow.BusinessService;
import org.egov.bpa.workflow.WorkflowService;
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

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private UserService userService;

	public void enrichBPACreateRequest(BPARequest bpaRequest, Object mdmsData) {
		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		AuditDetails auditDetails = bpaUtil.getAuditDetails(requestInfo
				.getUserInfo().getUuid(), true);
		bpaRequest.getBPA().setAuditDetails(auditDetails);
		bpaRequest.getBPA().setId(UUID.randomUUID().toString());

		// address
		bpaRequest.getBPA().getAddress().setId(UUID.randomUUID().toString());
		bpaRequest.getBPA().getAddress()
				.setTenantId(bpaRequest.getBPA().getTenantId());

		// units
		bpaRequest.getBPA().getUnits().forEach(unit -> {
			unit.setTenantId(bpaRequest.getBPA().getTenantId());
			unit.setId(UUID.randomUUID().toString());
			unit.setAuditDetails(auditDetails);
		});

		// owners
		bpaRequest.getBPA().getOwners().forEach(owner -> {
			// owner.setUuid(UUID.randomUUID().toString());

				owner.setUserActive(true);
				/*
				 * owner.setTenantId(bpaRequest.getBPA().getTenantId());
				 * owner.setAuditDetails(auditDetails);
				 * owner.setInstitutionId(UUID.randomUUID().toString());
				 */
				if (!CollectionUtils.isEmpty(owner.getDocuments()))
					owner.getDocuments().forEach(document -> {
						document.setId(UUID.randomUUID().toString());
					});
			});

		setIdgenIds(bpaRequest);
		setStatusForCreate(bpaRequest);
	}

	/**
	 * Sets the ApplicationNumber for given bpaRequest
	 *
	 * @param request
	 *            bpaRequest which is to be created
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
	 * @param bpaRequest
	 *            The create request
	 */
	private void setStatusForCreate(BPARequest bpaRequest) {
		/*
		 * if(bpaRequest.getBPA().getAction().equalsIgnoreCase(ACTION_INITIATE))
		 * bpaRequest.getBPA().setStatus(STATUS_INITIATED);
		 * if(bpaRequest.getBPA().getAction().equalsIgnoreCase(ACTION_APPLY))
		 * bpaRequest.getBPA().setStatus(STATUS_APPLIED);
		 */
	}

	public void enrichBPAUpdateRequest(BPARequest bpaRequest,
			BusinessService businessService) {

		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		AuditDetails auditDetails = bpaUtil.getAuditDetails(requestInfo
				.getUserInfo().getUuid(), false);
		bpaRequest.getBPA().setAuditDetails(auditDetails);
		if (workflowService.isStateUpdatable(bpaRequest.getBPA().getStatus(),
				businessService)) {
			bpaRequest.getBPA().setAuditDetails(auditDetails);

			bpaRequest.getBPA().getUnits().forEach(unit -> {
				if (unit.getId() == null) {
					unit.setTenantId(bpaRequest.getBPA().getTenantId());
					unit.setId(UUID.randomUUID().toString());
				}
			});

			bpaRequest.getBPA().getOwners().forEach(owner -> {
				if (owner.getUuid() == null || owner.getUserActive() == null)
					owner.setUserActive(true);
				if (!CollectionUtils.isEmpty(owner.getDocuments()))
					owner.getDocuments().forEach(document -> {
						if (document.getId() == null) {
							document.setId(UUID.randomUUID().toString());
						}
					});
			});

			if (!CollectionUtils.isEmpty(bpaRequest.getBPA().getDocuments())) {
				bpaRequest.getBPA().getDocuments().forEach(document -> {
					if (document.getId() == null) {
						document.setId(UUID.randomUUID().toString());
					}
				});
			}
		}

	}

	public void postStatusEnrichment(BPARequest bpaRequest) {
		// TODO Auto-generated method stub

	}

	public List<BPA> enrichBPASearch(List<BPA> bpa, BPASearchCriteria criteria,
			RequestInfo requestInfo) {

		BPASearchCriteria searchCriteria = enrichBPASearchCriteriaWithOwnerids(
				criteria, bpa);
//		enrichBoundary(new BPARequest(requestInfo, (BPA) bpa)); // some pending
		UserDetailResponse userDetailResponse = userService.getUser(
				searchCriteria, requestInfo);
		enrichOwner(userDetailResponse, bpa); // completed
		return bpa;
	}

	private void enrichOwner(UserDetailResponse userDetailResponse,
			List<BPA> bpas) {

		List<OwnerInfo> users = userDetailResponse.getUser();
		Map<String, OwnerInfo> userIdToOwnerMap = new HashMap<>();
		users.forEach(user -> userIdToOwnerMap.put(user.getUuid(), user));
		bpas.forEach(bpa -> {
			bpa.getOwners().forEach(
					owner -> {
						if (userIdToOwnerMap.get(owner.getUuid()) == null)
							throw new CustomException("OWNER SEARCH ERROR",
									"The owner of the bpaCategoryDetail "
											+ bpa.getId()
											+ " is not coming in user search");
						else
							owner.addUserDetail(userIdToOwnerMap.get(owner
									.getUuid()));
					});
		});

	}

	private BPASearchCriteria enrichBPASearchCriteriaWithOwnerids(
			BPASearchCriteria criteria, List<BPA> bpas) {
		BPASearchCriteria searchCriteria = new BPASearchCriteria();
		searchCriteria.setTenantId(criteria.getTenantId());
		Set<String> ownerids = new HashSet<>();
		bpas.forEach(bpa -> {
			bpa.getOwners().forEach(owner -> ownerids.add(owner.getUuid()));
		});
		

		searchCriteria.setOwnerIds(new ArrayList<>(ownerids));
		return searchCriteria;
	}

	private void enrichBoundary(BPARequest bpaRequest) {
		BPARequest request = getRequestByTenantId(bpaRequest); 
		boundaryService.getAreaType(request, config.getHierarchyTypeCode());
	}

	private BPARequest getRequestByTenantId(BPARequest bpaRequest) {
		BPA bpa = bpaRequest.getBPA();
		RequestInfo requestInfo = bpaRequest.getRequestInfo();

		Map<String, List<BPA>> tenantIdToProperties = new HashMap<>();
		if (bpa != null) {
			if (tenantIdToProperties.containsKey(bpa.getTenantId()))
				tenantIdToProperties.get(bpa.getTenantId()).add(bpa);
			else {
				List<BPA> list = new ArrayList<>();
				list.add(bpa);
				tenantIdToProperties.put(bpa.getTenantId(), list);
			}
		}
		List<BPARequest> requests = new LinkedList<>();

		/*
		 * tenantIdToProperties.forEach((key,value)-> { requests.add(new
		 * BPARequest(requestInfo,bpa)); }); return requests;
		 */

		return bpaRequest;
	}
}
