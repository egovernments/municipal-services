package org.egov.bpa.service;

import java.util.ArrayList;
import java.util.Calendar;
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
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.web.model.AuditDetails;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.egov.bpa.web.model.idgen.IdResponse;
import org.egov.bpa.web.model.user.UserDetailResponse;
import org.egov.bpa.web.model.workflow.BusinessService;
import org.egov.bpa.workflow.WorkflowService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.land.web.models.OwnerInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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
		AuditDetails auditDetails = bpaUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		bpaRequest.getBPA().setAuditDetails(auditDetails);
		bpaRequest.getBPA().setId(UUID.randomUUID().toString());
		bpaRequest.getBPA().setLandId(bpaRequest.getBPA().getLandInfo().getId()); 
		bpaRequest.getBPA().setAccountId(bpaRequest.getBPA().getAuditDetails().getCreatedBy());

		if(!bpaRequest.getBPA().getRiskType().equalsIgnoreCase(BPAConstants.LOW_RISKTYPE)){
			bpaRequest.getBPA().setBusinessService(BPAConstants.BPA_MODULE_CODE);
		}else{
			bpaRequest.getBPA().setBusinessService(BPAConstants.BPA_LOW_MODULE_CODE);
		}

		// BPA Documents
		if (!CollectionUtils.isEmpty(bpaRequest.getBPA().getDocuments()))
			bpaRequest.getBPA().getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		setIdgenIds(bpaRequest);
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

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNoIdgenName(),
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
	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}

	public void enrichBPAUpdateRequest(BPARequest bpaRequest, BusinessService businessService) {

		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		AuditDetails auditDetails = bpaUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
		auditDetails.setCreatedBy(bpaRequest.getBPA().getAuditDetails().getCreatedBy());
		auditDetails.setCreatedTime(bpaRequest.getBPA().getAuditDetails().getCreatedTime());
		bpaRequest.getBPA().getAuditDetails().setLastModifiedTime(auditDetails.getLastModifiedTime());
		
		// BPA Documents
		String state = workflowService.getCurrentState(bpaRequest.getBPA().getStatus(), businessService);
		if (!CollectionUtils.isEmpty(bpaRequest.getBPA().getDocuments()))
			bpaRequest.getBPA().getDocuments().forEach(document -> {
				if (document.getId() == null) {
//					document.setWfState(state);
					document.setId(UUID.randomUUID().toString());
				}
			});
		// BPA WfDocuments
		if (!CollectionUtils.isEmpty(bpaRequest.getBPA().getWorkflow().getVarificationDocuments())) {
			bpaRequest.getBPA().getWorkflow().getVarificationDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void postStatusEnrichment(BPARequest bpaRequest) {
		BPA bpa = bpaRequest.getBPA();

		BusinessService businessService = workflowService.getBusinessService(bpa, bpaRequest.getRequestInfo(),
				bpa.getApplicationNo());

		String state = workflowService.getCurrentState(bpa.getStatus(), businessService);

		if(state.equalsIgnoreCase(BPAConstants.DOCVERIFICATION_STATE)){
			bpa.setApplicationDate(Calendar.getInstance().getTimeInMillis());
		}
		if ((!bpa.getRiskType().toString().equalsIgnoreCase(BPAConstants.LOW_RISKTYPE)
				&& state.equalsIgnoreCase(BPAConstants.APPROVED_STATE))
				|| (state.equalsIgnoreCase(BPAConstants.DOCVERIFICATION_STATE)
						&& bpa.getRiskType().toString().equalsIgnoreCase(BPAConstants.LOW_RISKTYPE))) {
			int vailidityInMonths = config.getValidityInMonths();
			Calendar calendar = Calendar.getInstance();
			bpa.setApprovalDate(Calendar.getInstance().getTimeInMillis());

			// Adding 3years (36 months) to Current Date
			calendar.add(Calendar.MONTH, vailidityInMonths);
			Map<String, Object> additionalDetail = (Map) bpa.getAdditionalDetails();
			additionalDetail.put("validityDate", calendar.getTimeInMillis());
//			bpa.setValidityDate(calendar.getTimeInMillis());
			List<IdResponse> idResponses = idGenRepository.getId(bpaRequest.getRequestInfo(), bpa.getTenantId(),
					config.getPermitNoIdgenName(), config.getPermitNoIdgenFormat(), 1).getIdResponses();
//			bpa.setPermitOrderNo(idResponses.get(0).getId());
			if (state.equalsIgnoreCase(BPAConstants.DOCVERIFICATION_STATE)
					&& bpa.getRiskType().toString().equalsIgnoreCase(BPAConstants.LOW_RISKTYPE)) {
				
				Object mdmsData = bpaUtil.mDMSCall(bpaRequest.getRequestInfo(), bpaRequest.getBPA().getTenantId());
				String condeitionsPath = BPAConstants.CONDITIONS_MAP.replace("{1}", BPAConstants.PENDING_APPROVAL_STATE)
						.replace("{2}", bpa.getRiskType().toString())
						/*.replace("{3}", bpa.getServiceType())
						.replace("{4}", bpa.getApplicationType())*/;
				log.info(condeitionsPath);

				try {
					List<String> conditions = (List<String>) JsonPath.read(mdmsData, condeitionsPath);
					log.info(conditions.toString());
					if (bpa.getAdditionalDetails() == null) {
						bpa.setAdditionalDetails(new HashMap());
					}
					Map additionalDetails = (Map) bpa.getAdditionalDetails();
					additionalDetails.put(BPAConstants.PENDING_APPROVAL_STATE.toLowerCase(), conditions.get(0));

				} catch (Exception e) {
					log.warn("No approval conditions found for the application " + bpa.getApplicationNo());
				}
			}
		}
		
	}

	public List<BPA> enrichBPASearch(List<BPA> bpas, BPASearchCriteria criteria, RequestInfo requestInfo) {

		List<BPARequest> bprs = new ArrayList<BPARequest>();
		bpas.forEach(bpa -> {
			bprs.add(new BPARequest(requestInfo, bpa));
		});
//		if (criteria.getLimit() == null || !criteria.getLimit().equals(-1)) {
//			enrichBoundary(bprs);
//		}

		UserDetailResponse userDetailResponse = userService.getUsersForBpas(bpas);
		enrichOwner(userDetailResponse, bpas); // completed
		return bpas;
	}

	private void enrichOwner(UserDetailResponse userDetailResponse, List<BPA> bpas) {

		List<OwnerInfo> users = userDetailResponse.getUser();
		Map<String, OwnerInfo> userIdToOwnerMap = new HashMap<>();
		users.forEach(user -> userIdToOwnerMap.put(user.getUuid(), user));
		bpas.forEach(bpa -> {
			bpa.getLandInfo().getOwners().forEach(owner -> {
				if (userIdToOwnerMap.get(owner.getUuid()) == null)
					throw new CustomException("OWNER SEARCH ERROR",
							"The owner of the bpa " + bpa.getId() + " is not coming in user search");
				else
					owner.addUserDetail(userIdToOwnerMap.get(owner.getUuid()));
			});
		});

	}


	/**
	 * Adds accountId of the logged in user to search criteria
	 * 
	 * @param requestInfo
	 *            The requestInfo of search request
	 * @param criteria
	 *            The bpaSearch criteria
	 */

	public void enrichSearchCriteriaWithAccountId(RequestInfo requestInfo, BPASearchCriteria criteria) {

		if (criteria.isEmpty() && requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.CITIZEN)) {
//			criteria.setCreatedBy(requestInfo.getUserInfo().getUuid());
			criteria.setTenantId(requestInfo.getUserInfo().getTenantId());
		}

	}

	/**
	 * Creates search criteria from list of bpa's
	 * 
	 * @param bpa
	 *            's list The bpa whose id's are added to search
	 * @return bpaSearch criteria on basis of bpa id
	 */
	public BPASearchCriteria getBPACriteriaFromIds(List<BPA> bpa, Integer limit) {
		BPASearchCriteria criteria = new BPASearchCriteria();
		Set<String> bpaIds = new HashSet<>();
		bpa.forEach(data -> bpaIds.add(data.getId()));
		criteria.setIds(new LinkedList<>(bpaIds));
		criteria.setTenantId(bpa.get(0).getTenantId());
		criteria.setLimit(limit);
		return criteria;
	}

	/**
	 * Adds the ownerIds from userSearchReponse to search criteria
	 * 
	 * @param criteria
	 *            The BPA search Criteria
	 * @param userDetailResponse
	 *            The response of user search
	 */
	public void enrichBPACriteriaWithOwnerids(BPASearchCriteria criteria, UserDetailResponse userDetailResponse) {
	/*	if (CollectionUtils.isEmpty(criteria.getOwnerIds())) {
			Set<String> ownerids = new HashSet<>();
			userDetailResponse.getUser().forEach(owner -> ownerids.add(owner.getUuid()));
			criteria.setOwnerIds(new ArrayList<>(ownerids));
		}*/
	}



}
