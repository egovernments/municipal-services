package org.egov.bpa.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.BPARepository;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.validator.BPAValidator;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.Difference;
import org.egov.bpa.web.models.user.UserDetailResponse;
import org.egov.bpa.web.models.workflow.BusinessService;
import org.egov.bpa.web.models.workflow.ProcessInstance;
import org.egov.bpa.workflow.ActionValidator;
import org.egov.bpa.workflow.BPAWorkflowService;
import org.egov.bpa.workflow.WorkflowIntegrator;
import org.egov.bpa.workflow.WorkflowService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class BPAService {

	@Autowired
	private BPARepository bpaRequestInfoDao;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	private EnrichmentService enrichmentService;

	@Autowired
	private EDCRService edcrService;

	@Autowired
	private UserService userService;

	@Autowired
	private BPARepository repository;

	@Autowired
	private ActionValidator actionValidator;

	@Autowired
	private BPAValidator bpaValidator;

	@Autowired
	private BPAUtil util;

	@Autowired
	private DiffService diffService;

	@Autowired
	private CalculationService calculationService;

	@Autowired
	private BPAConfiguration config;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private BPAWorkflowService bpaWorkflowService;

	public BPA create(BPARequest bpaRequest) {

		Object mdmsData = util.mDMSCall(bpaRequest);
		if (bpaRequest.getBPA().getTenantId().split("\\.").length == 1) {
			throw new CustomException(" Invalid Tenant ",
					" Application cannot be create at StateLevel");
		}
		edcrService.validateEdcrPlan(bpaRequest, mdmsData);
		bpaValidator.validateCreate(bpaRequest, mdmsData);
		enrichmentService.enrichBPACreateRequest(bpaRequest, mdmsData);

		userService.createUser(bpaRequest);

		wfIntegrator.callWorkFlow(bpaRequest);
		repository.save(bpaRequest);

		calculationService.addCalculation(bpaRequest,
				BPAConstants.APPLICATION_FEE_KEY);
		return bpaRequest.getBPA();
	}

	/**
	 * Searches the Bpa for the given criteria if search is on owner paramter
	 * then first user service is called followed by query to db
	 * 
	 * @param criteria
	 *            The object containing the paramters on which to search
	 * @param requestInfo
	 *            The search request's requestInfo
	 * @return List of bpa for the given criteria
	 */
	public List<BPA> search(BPASearchCriteria criteria, RequestInfo requestInfo) {
		List<BPA> bpa;
		bpaValidator.validateSearch(requestInfo, criteria);
		if (criteria.getMobileNumber() != null) {
			bpa = getBPAFromMobileNumber(criteria, requestInfo);
		} else {
			List<String> roles = new ArrayList<>();
			for (Role role : requestInfo.getUserInfo().getRoles()) {
				roles.add(role.getCode());
			}

			if ((criteria.tenantIdOnly() || criteria.isEmpty())
					&& roles.contains("CITIZEN")) {
				criteria.setCreatedBy(requestInfo.getUserInfo().getUuid());
			}

			bpa = getBPAWithOwnerInfo(criteria, requestInfo);
		}
		return bpa;
	}

	/**
	 * Returns the bpa with enrivhed owners from user servise
	 * 
	 * @param criteria
	 *            The object containing the paramters on which to search
	 * @param requestInfo
	 *            The search request's requestInfo
	 * @return List of bpa for the given criteria
	 */
	public List<BPA> getBPAWithOwnerInfo(BPASearchCriteria criteria,
			RequestInfo requestInfo) {
		List<BPA> bpa = repository.getBPAData(criteria);
		if (bpa.isEmpty())
			return Collections.emptyList();
		bpa = enrichmentService.enrichBPASearch(bpa, criteria, requestInfo);
		return bpa;
	}

	private List<BPA> getBPAFromMobileNumber(BPASearchCriteria criteria,
			RequestInfo requestInfo) {

		List<BPA> bpa = new LinkedList<>();
		UserDetailResponse userDetailResponse = userService.getUser(criteria,
				requestInfo);
		// If user not found with given user fields return empty list
		if (userDetailResponse.getUser().size() == 0) {
			return Collections.emptyList();
		}
		enrichmentService.enrichBPACriteriaWithOwnerids(criteria,
				userDetailResponse);
		bpa = repository.getBPAData(criteria);

		if (bpa.size() == 0) {
			return Collections.emptyList();
		}

		// Add bpaId of all bpa's owned by the user
		criteria = enrichmentService.getBPACriteriaFromIds(bpa);
		// Get all bpa with ownerInfo enriched from user service
		bpa = getBPAWithOwnerInfo(criteria, requestInfo);
		return bpa;
	}

	/**
	 * Updates the bpa
	 * 
	 * @param bpaRequest
	 *            The update Request
	 * @return Updated bpa
	 */
	public BPA update(BPARequest bpaRequest) {
		Object mdmsData = util.mDMSCall(bpaRequest);
		BPA bpa = bpaRequest.getBPA();

		if (bpa.getId() == null) {
			throw new CustomException("UPDATE ERROR",
					"Application Not found in the System" + bpa);
		}
		
		bpa.getOwners().forEach(owner -> {
			if(owner.getOwnerType() == null) {
				owner.setOwnerType("NONE");
			}
		});
		BusinessService businessService = workflowService.getBusinessService(
				bpa.getTenantId(), bpaRequest.getRequestInfo(),
				bpa.getApplicationNo());

		List<BPA> searchResult = getBPAWithOwnerInfo(bpaRequest);
		if (CollectionUtils.isEmpty(searchResult)) {
			throw new CustomException("UPDATE ERROR",
					"Failed to Update the Application");
		}
		Difference diffMap = diffService
				.getDifference(bpaRequest, searchResult);

		userService.createUser(bpaRequest);
		bpaValidator.validateUpdate(bpaRequest, searchResult, mdmsData,
				workflowService.getCurrentState(bpa.getStatus(),
						businessService));
		bpaRequest.getBPA().setAuditDetails(searchResult.get(0).getAuditDetails());
		enrichmentService.enrichBPAUpdateRequest(bpaRequest, businessService);
		actionValidator.validateUpdateRequest(bpaRequest, businessService);

		//
		//
		//
		// /*call workflow service if it's enable else uses internal workflow
		// process*/

		wfIntegrator.callWorkFlow(bpaRequest);

		enrichmentService.postStatusEnrichment(bpaRequest);
		userService.createUser(bpaRequest);
		repository.update(bpaRequest, workflowService.isStateUpdatable(
				bpa.getStatus(), businessService));

		// Generate the sanction Demand
		ProcessInstance processInstance = workflowService.getProcessInstance(
				bpa.getTenantId(), bpaRequest.getRequestInfo(),
				bpa.getApplicationNo());
		if (processInstance != null) {
			if (processInstance.getState().getState()
					.equalsIgnoreCase(BPAConstants.SANC_FEE_STATE)) {
				calculationService.addCalculation(bpaRequest,
						BPAConstants.SANCTION_FEE_KEY);
			}

		}

		return bpaRequest.getBPA();

	}

	/**
	 * Returns bpa from db for the update request
	 * 
	 * @param request
	 *            The update request
	 * @return List of bpas
	 */
	public List<BPA> getBPAWithOwnerInfo(BPARequest request) {
		BPASearchCriteria criteria = new BPASearchCriteria();
		List<String> ids = new LinkedList<>();
		ids.add(request.getBPA().getId());

		criteria.setTenantId(request.getBPA().getTenantId());
		criteria.setIds(ids);

		List<BPA> bpa = repository.getBPAData(criteria);

		bpa = enrichmentService.enrichBPASearch(bpa, criteria,
				request.getRequestInfo());
		return bpa;
	}
}