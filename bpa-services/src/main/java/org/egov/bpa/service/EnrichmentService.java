package org.egov.bpa.service;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.egov.bpa.web.models.AuditDetails;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.OwnerInfo;
import org.egov.bpa.web.models.idgen.IdResponse;
import org.egov.bpa.web.models.user.UserDetailResponse;
import org.egov.bpa.web.models.user.UserSearchRequest;
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
		if(!CollectionUtils.isEmpty(bpaRequest.getBPA().getUnits())) {
			bpaRequest.getBPA().getUnits().forEach(unit -> {
				unit.setTenantId(bpaRequest.getBPA().getTenantId());
				unit.setId(UUID.randomUUID().toString());
				unit.setAuditDetails(auditDetails);
			});
		}
		
		
		// BPA Documents
		if (!CollectionUtils.isEmpty(bpaRequest.getBPA().getDocuments()))
			bpaRequest.getBPA().getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});

		// owners
		bpaRequest.getBPA().getOwners().forEach(owner -> {
		
				if (!CollectionUtils.isEmpty(owner.getDocuments()))
					owner.getDocuments().forEach(document -> {
						document.setId(UUID.randomUUID().toString());
					});
			});
		
//		if(bpaRequest.getBPA().getOwnershipCategory().contains(config.getInstitutional())){
//			bpaRequest.getBPA().getInstitution().setId(UUID.randomUUID().toString());
//			bpaRequest.getBPA().getInstitution().setActive(true);
//			bpaRequest.getBPA().getInstitution().setTenantId(tradeLicense.getTenantId());
//			bpaRequest.getBPA().getOwners().forEach(owner -> {
//                owner.setInstitutionId(bpaRequest.getBPA().getInstitution().getId());
//            });
//        }

		setIdgenIds(bpaRequest);
		setStatusForCreate(bpaRequest);
		boundaryService.getAreaType(bpaRequest,config.getHierarchyTypeCode());
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

			//BPA Units
			if(!CollectionUtils.isEmpty(bpaRequest.getBPA().getUnits())) {
				bpaRequest.getBPA().getUnits().forEach(unit -> {
					if (unit.getId() == null) {
						unit.setTenantId(bpaRequest.getBPA().getTenantId());
						unit.setId(UUID.randomUUID().toString());
					}
				});
			}
			
			//BPA Owner Documents
			if(!CollectionUtils.isEmpty(bpaRequest.getBPA().getOwners())) {
				bpaRequest.getBPA().getOwners().forEach(owner -> {
					if (!CollectionUtils.isEmpty(owner.getDocuments()))
						owner.getDocuments().forEach(document -> {
							if (document.getId() == null) {
								document.setId(UUID.randomUUID().toString());
							}
						});
				});
				
				
				
				//TODO as of now institution is out of scope
//				 if(bpaRequest.getBPA().getOwnershipCategory().contains(config.getInstitutional())
//	                        && bpaRequest.getBPA().getInstitution().getId()==null){
//					 bpaRequest.getBPA().getInstitution().setId(UUID.randomUUID().toString());
//					 bpaRequest.getBPA().getInstitution().setActive(true);
//					 bpaRequest.getBPA().getInstitution().setTenantId(tradeLicense.getTenantId());
//					 bpaRequest.getBPA().getOwners().forEach(owner -> {
//	                        owner.setInstitutionId(tradeLicense.getTradeLicenseDetail().getInstitution().getId());
//	                    });
//	                }
			}else {
				throw new CustomException("INVALID UPDATE",
						"Owners cannot be empty");
			}

			
		}
		// BPA Documents
		if (!CollectionUtils.isEmpty(bpaRequest.getBPA().getDocuments()))
			bpaRequest.getBPA().getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		// BPA WfDocuments
		if (!CollectionUtils.isEmpty(bpaRequest.getBPA().getWfDocuments())) {
			bpaRequest.getBPA().getWfDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		}

	}

	public void postStatusEnrichment(BPARequest bpaRequest) {
		// TODO Nothing as of now, logic like generating permit number and etc.. would be done here.
		BPA bpa = bpaRequest.getBPA();
		
		BusinessService businessService = workflowService.getBusinessService(
				bpa.getTenantId(), bpaRequest.getRequestInfo(),
				bpa.getApplicationNo());
		
		String state = workflowService.getCurrentState(bpa.getStatus(), businessService);
		if(state.equalsIgnoreCase(BPAConstants.APPROVED_STATE)) {
			List<IdResponse> idResponses =  idGenRepository.getId(bpaRequest.getRequestInfo(), bpa.getTenantId(), config.getPermitNoIdgenName(), config.getPermitNoIdgenFormat(), 1).getIdResponses();
			bpa.setPermitOrderNo(idResponses.get(0).getId());
		}

	}

	public List<BPA> enrichBPASearch(List<BPA> bpas, BPASearchCriteria criteria,
			RequestInfo requestInfo) {

		BPASearchCriteria searchCriteria = enrichBPASearchCriteriaWithOwnerids(
				criteria, bpas);
		List<BPARequest> bprs = new ArrayList<BPARequest>();
		bpas.forEach(bpa -> {
			bprs.add(new BPARequest( requestInfo,bpa));
		});
		
		 enrichBoundary(bprs); // some
	
		UserDetailResponse userDetailResponse  = userService.getUsersForBpas(bpas);
		enrichOwner(userDetailResponse, bpas); // completed
		return bpas;
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
									"The owner of the bpa "
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

	private void enrichBoundary(List<BPARequest> bpaRequests) {
		bpaRequests.forEach(bpaRequest->{
			BPARequest request = getRequestByTenantId(bpaRequest);
			boundaryService.getAreaType(bpaRequest, config.getHierarchyTypeCode());
		});
		
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


		return bpaRequest;
	}

	/**
	 * Adds accountId of the logged in user to search criteria
	 * 
	 * @param requestInfo
	 *            The requestInfo of search request
	 * @param criteria
	 *            The bpaSearch criteria
	 */

	public void enrichSearchCriteriaWithAccountId(RequestInfo requestInfo,
			BPASearchCriteria criteria) {

		if (criteria.isEmpty()
				&& requestInfo.getUserInfo().getType()
						.equalsIgnoreCase("CITIZEN")) {
			criteria.setCreatedBy(requestInfo.getUserInfo().getUuid());
//			criteria.setMobileNumber(requestInfo.getUserInfo().getUserName());
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
	public BPASearchCriteria getBPACriteriaFromIds(List<BPA> bpa) {
		BPASearchCriteria criteria = new BPASearchCriteria();
		Set<String> bpaIds = new HashSet<>();
		bpa.forEach(data -> bpaIds.add(data.getId()));
		criteria.setIds(new LinkedList<>(bpaIds));
		criteria.setTenantId(bpa.get(0).getTenantId());
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
	public void enrichBPACriteriaWithOwnerids(BPASearchCriteria criteria,
			UserDetailResponse userDetailResponse) {
		if (CollectionUtils.isEmpty(criteria.getOwnerIds())) {
			Set<String> ownerids = new HashSet<>();
			userDetailResponse.getUser().forEach(
					owner -> ownerids.add(owner.getUuid()));
			criteria.setOwnerIds(new ArrayList<>(ownerids));
		}

	}
	
	

}
