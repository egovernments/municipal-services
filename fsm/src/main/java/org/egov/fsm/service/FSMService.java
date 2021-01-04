package org.egov.fsm.service;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.fsm.repository.FSMRepository;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.fsm.util.FSMUtil;
import org.egov.fsm.validator.FSMValidator;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.FSMRequest;
import org.egov.fsm.web.model.FSMSearchCriteria;
import org.egov.fsm.web.model.user.UserDetailResponse;
import org.egov.fsm.web.model.workflow.BusinessService;
import org.egov.fsm.workflow.ActionValidator;
import org.egov.fsm.workflow.WorkflowIntegrator;
import org.egov.fsm.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FSMService {
	/**
	 * does all the validations required to create fsm Record in the system
	 * @param fsmRequest
	 * @return
	 */
	@Autowired
	private FSMUtil util;
	
	@Autowired
	private EnrichmentService enrichmentService;
	
	@Autowired
	private FSMValidator fsmValidator;
	
	@Autowired
	private WorkflowIntegrator wfIntegrator;
	
	@Autowired
	private ActionValidator actionValidator;
	
	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private FSMRepository repository;
	public FSM create(FSMRequest fsmRequest) {
		RequestInfo requestInfo = fsmRequest.getRequestInfo();
		String tenantId = fsmRequest.getFsm().getTenantId().split("\\.")[0];
		Object mdmsData = util.mDMSCall(requestInfo, tenantId);
		if (fsmRequest.getFsm().getTenantId().split("\\.").length == 1) {
			throw new CustomException(FSMErrorConstants.INVALID_TENANT, " Application cannot be create at StateLevel");
		}
		fsmValidator.validateCreate(fsmRequest, mdmsData);
		enrichmentService.enrichFSMCreateRequest(fsmRequest, mdmsData);
		
		//wfIntegrator.callWorkFlow(fsmRequest);
		
		repository.save(fsmRequest);
		return fsmRequest.getFsm();
	}
	
	/**
	 * Updates the FSM
	 * 
	 * @param fsmRequest
	 *            The update Request
	 * @return Updated FSM
	 */
	@SuppressWarnings("unchecked")
	public FSM update(FSMRequest fsmRequest) {
		
		RequestInfo requestInfo = fsmRequest.getRequestInfo();
		String tenantId = fsmRequest.getFsm().getTenantId().split("\\.")[0];
		Object mdmsData = util.mDMSCall(requestInfo, tenantId);
		FSM fsm = fsmRequest.getFsm();

		if (fsm.getId() == null) {
			throw new CustomException(FSMErrorConstants.UPDATE_ERROR, "Application Not found in the System" + fsm);
		}
		
		//TODO get the FSM object with ID
		// not find throw error
		
		BusinessService businessService = workflowService.getBusinessService(fsm, fsmRequest.getRequestInfo(),
				fsm.getApplicationNo());
		
		// TODO write business logic 
		// fill the audit details
		
		enrichmentService.enrichFSMUpdateRequest(fsmRequest, businessService);
		
		wfIntegrator.callWorkFlow(fsmRequest);

		enrichmentService.postStatusEnrichment(fsmRequest);
		
		fsmValidator.validateWorkflowActions(fsmRequest);
		
		repository.update(fsmRequest, workflowService.isStateUpdatable(fsm.getApplicationStatus(), businessService));
		return fsmRequest.getFsm();
	}
	
	/**
	 * search the fsm applications based on the search criteria
	 * @param criteria
	 * @param requestInfo
	 * @return
	 */
	public List<FSM> FSMsearch(FSMSearchCriteria criteria, RequestInfo requestInfo) {
		
		List<FSM> fsmList = new LinkedList<>();
		List<String> uuids = new ArrayList<String>();
		UserDetailResponse usersRespnse;
		
		fsmValidator.validateSearch(requestInfo, criteria);
		if( criteria.getMobileNumber() !=null) {
			usersRespnse = userService.getUser(criteria,requestInfo);
			if(usersRespnse !=null && usersRespnse.getUser() != null && usersRespnse.getUser().size() >0) {
				uuids = usersRespnse.getUser().stream().map(User::getUuid).collect(Collectors.toList());
				criteria.setOwnerIds(uuids);
			}
		}
		fsmList = repository.getFSMData(criteria);
		if (!fsmList.isEmpty()) {
			enrichmentService.enrichFSMSearch(fsmList, requestInfo, criteria.getTenantId());
		}

		if (fsmList.isEmpty()) {
			return Collections.emptyList();
		}
		return fsmList;
	}
}