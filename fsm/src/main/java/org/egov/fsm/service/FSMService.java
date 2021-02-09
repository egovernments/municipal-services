package org.egov.fsm.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.repository.FSMRepository;
import org.egov.fsm.util.FSMAuditUtil;
import org.egov.fsm.util.FSMConstants;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.fsm.util.FSMUtil;
import org.egov.fsm.validator.FSMValidator;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.FSMAudit;
import org.egov.fsm.web.model.FSMAuditSearchCriteria;
import org.egov.fsm.web.model.FSMRequest;
import org.egov.fsm.web.model.FSMSearchCriteria;
import org.egov.fsm.web.model.Workflow;
import org.egov.fsm.web.model.dso.Vendor;
import org.egov.fsm.web.model.user.User;
import org.egov.fsm.web.model.user.UserDetailResponse;
import org.egov.fsm.web.model.vehicle.Vehicle;
import org.egov.fsm.web.model.workflow.BusinessService;
import org.egov.fsm.workflow.ActionValidator;
import org.egov.fsm.workflow.WorkflowIntegrator;
import org.egov.fsm.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
	private CalculationService calculationService;
	
	@Autowired
	private DSOService dsoService;
	
	@Autowired
	private FSMConfiguration config;
	

	@Autowired
	VehicleService vehicleService;
	
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
		
		wfIntegrator.callWorkFlow(fsmRequest);
		repository.save(fsmRequest);
		if(requestInfo.getUserInfo().getType().equalsIgnoreCase(FSMConstants.EMPLOYEE)) {
			calculationService.addCalculation(fsmRequest, FSMConstants.APPLICATION_FEE);
		}
		
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

		List<String> ids = new ArrayList<String>();
		ids.add( fsm.getId());
		FSMSearchCriteria criteria = FSMSearchCriteria.builder().ids(ids).tenantId(fsm.getTenantId()).build();
		List<FSM> fsms = repository.getFSMData(criteria);
		
		fsmValidator.validateUpdate(fsmRequest, fsms, mdmsData);
		
		BusinessService businessService = workflowService.getBusinessService(fsm, fsmRequest.getRequestInfo(),
				FSMConstants.FSM_BusinessService,null);
		actionValidator.validateUpdateRequest(fsmRequest, businessService);
		FSM oldFSM = fsms.get(0);
		
		if( fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_SUBMIT) ) {
			handleApplicationSubmit(fsmRequest,oldFSM);
		}
		
		if( fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_ASSIGN_DSO) ) {
			handleAssignDSO(fsmRequest);
		}
		
		if( fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_DSO_ACCEPT) ) {
			handleDSOAccept(fsmRequest,oldFSM);
		}
		
		if( fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_DSO_REJECT) ) {
			handleDSOReject(fsmRequest,oldFSM);
		}
		
		if( fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_COMPLETE) ) {
			handleFSMComplete(fsmRequest,oldFSM);
		}
		
		if( fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_SUBMIT_FEEDBACK) ) {
			handleFSMSubmitFeeback(fsmRequest,oldFSM, mdmsData);
		}
		
		if( fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_ADDITIONAL_PAY_REQUEST) ) {
			handleAdditionalPayRequest(fsmRequest,oldFSM);
		}
		
		if( fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_REJECT) || 
				fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_CANCEL)) {
			handleRejectCancel(fsmRequest,oldFSM);
		}
		
		if( fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_SEND_BACK) ) {
			handleSendBack(fsmRequest,oldFSM);
		}
		
		enrichmentService.enrichFSMUpdateRequest(fsmRequest, mdmsData,oldFSM);
		
		wfIntegrator.callWorkFlow(fsmRequest);

		enrichmentService.postStatusEnrichment(fsmRequest);
		
		repository.update(fsmRequest, workflowService.isStateUpdatable(fsm.getApplicationStatus(), businessService)); 
		return fsmRequest.getFsm();
	}
	
	
	/**
	 * 
	 * @param fsmRequest
	 */
	private void handleAssignDSO(FSMRequest fsmRequest) {
		
		FSM fsm = fsmRequest.getFsm();
		if(!StringUtils.hasLength(fsm.getDsoId())) {
			throw new CustomException(FSMErrorConstants.INVALID_DSO," DSO is invalid");
		}
		
		if(fsm.getPossibleServiceDate() != null) {
			Calendar psd = Calendar.getInstance();
			psd.setTimeInMillis(fsm.getPossibleServiceDate());
			if(Calendar.getInstance().compareTo(psd) >0) {
				throw new CustomException(FSMErrorConstants.INVALID_POSSIBLE_DATE," Possible service Date  is invalid");
			}
		}
		dsoService.validateDSO(fsmRequest);
		
		
	}
	
	private void handleDSOAccept(FSMRequest fsmRequest, FSM oldFSM) {
		FSM fsm = fsmRequest.getFsm();
		org.egov.common.contract.request.User dsoUser = fsmRequest.getRequestInfo().getUserInfo();
		Vendor vendor = dsoService.getVendor(oldFSM.getDsoId(),fsm.getTenantId(), dsoUser.getUuid(),fsmRequest.getRequestInfo());
		if(vendor == null) {
			throw new CustomException(FSMErrorConstants.INVALID_DSO," DSO is invalid, cannot take an action, Application is not assigned to current logged in user !");
		}
		
		if(!StringUtils.hasLength(fsm.getVehicleId())) {
			throw new CustomException(FSMErrorConstants.INVALID_DSO_VEHICLE,"Vehicle should be assigned to accept the Request !");
			
		}else {
			
			vehicleService.validateVehicle(fsmRequest);
			Map<String, Vehicle> vehilceIdMap = vendor.getVehicles().stream().collect(Collectors.toMap(Vehicle::getId,Function.identity()));
			if(!CollectionUtils.isEmpty(vehilceIdMap) && vehilceIdMap.get(fsm.getVehicleId()) == null ) {
				throw new CustomException(FSMErrorConstants.INVALID_DSO_VEHICLE," Vehicle Does not belong to DSO!");
			}else {
				Vehicle vehicle = vehilceIdMap.get(fsm.getVehicleId());
				if(!vehicle.getType().equalsIgnoreCase(fsm.getVehicleType())) {
					throw new CustomException(FSMErrorConstants.INVALID_DSO_VEHICLE," Vehilce Type of FSM and vehilceType of the assigned vehicle does not match !");
				}
			}
		}
	}
	
	private void handleDSOReject(FSMRequest fsmRequest, FSM oldFSM) {
		FSM fsm = fsmRequest.getFsm();
		org.egov.common.contract.request.User dsoUser = fsmRequest.getRequestInfo().getUserInfo();
		fsm.setDsoId(null);
		fsm.setVehicleId(null);
		Workflow workflow = fsmRequest.getWorkflow();
		if(!StringUtils.hasLength(workflow.getComments())) {
			throw new CustomException(FSMErrorConstants.INVALID_COMMENT_CANCEL_REJECT," Comment is mandatory to reject or cancel the application !.");
		}
	}
	private void handleFSMComplete(FSMRequest fsmRequest, FSM oldFSM) {
		FSM fsm = fsmRequest.getFsm();
		org.egov.common.contract.request.User dsoUser = fsmRequest.getRequestInfo().getUserInfo();
		//TODO on complete mark the vehicleLog as Ready for Disposal
		
//		if(!dsoUser.getUuid().equalsIgnoreCase(oldFSM.getDsoId())) {
//			throw new CustomException(FSMErrorConstants.INVALID_DSO," DSO is invalid, cannot take an action as it is not assigned to the current user");
//		}
	}
	
	private void handleFSMSubmitFeeback(FSMRequest fsmRequest, FSM oldFSM,Object mdmsData) {
		FSM fsm = fsmRequest.getFsm();
		org.egov.common.contract.request.User citizen = fsmRequest.getRequestInfo().getUserInfo();
		if(!citizen.getUuid().equalsIgnoreCase(fsmRequest.getRequestInfo().getUserInfo().getUuid())) {
			throw new CustomException(FSMErrorConstants.INVALID_UPDATE," Only owner of the application can submit the feedback !.");
		}
		if(fsmRequest.getWorkflow().getRating() == null) {
			throw new CustomException(FSMErrorConstants.INVALID_UPDATE," Rating has to be provided!");
		}else if(config.getAverageRatingCommentMandatory() !=null && Integer.parseInt(config.getAverageRatingCommentMandatory() ) > fsmRequest.getWorkflow().getRating()) {
			if(!StringUtils.hasLength(fsmRequest.getWorkflow().getComments())) {
				throw new CustomException(FSMErrorConstants.INVALID_UPDATE," Comment mandatory for rating "+ fsmRequest.getWorkflow().getRating());
			}
		}
		fsmValidator.validateCheckList(fsmRequest, mdmsData);
			
		//TODO handle the citizen rating and checklist.
	}
	
	private void handleAdditionalPayRequest(FSMRequest fsmRequest, FSM oldFSM) {
		FSM fsm = fsmRequest.getFsm();
		//TODO if additionalcharge is allowed then allow this action and then call calculator
	}
	
	private void handleRejectCancel(FSMRequest fsmRequest, FSM oldFSM) {
		FSM fsm = fsmRequest.getFsm();
		Workflow workflow = fsmRequest.getWorkflow();
		if(!StringUtils.hasLength(workflow.getComments())) {
			throw new CustomException(FSMErrorConstants.INVALID_COMMENT_CANCEL_REJECT," Comment is mandatory to reject or cancel the application !.");
		}
	}

	private void handleSendBack(FSMRequest fsmRequest, FSM oldFSM) {
		FSM fsm = fsmRequest.getFsm();
		//TODO based on the old application Status DSO or vehicle has to removed
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
		
		if(criteria.tenantIdOnly() && 
				requestInfo.getUserInfo().getType().equalsIgnoreCase(FSMConstants.CITIZEN) ) {
			criteria.setMobileNumber(requestInfo.getUserInfo().getMobileNumber());
		}
		
		if( criteria.getMobileNumber() !=null && StringUtils.hasText(criteria.getMobileNumber() )) {
			usersRespnse = userService.getUser(criteria,requestInfo);
			if(usersRespnse !=null && usersRespnse.getUser() != null && usersRespnse.getUser().size() >0) {
				uuids = usersRespnse.getUser().stream().map(User::getUuid).collect(Collectors.toList());
				if(CollectionUtils.isEmpty(criteria.getOwnerIds())) {
					criteria.setOwnerIds(uuids);
				}else {
					criteria.getOwnerIds().addAll(uuids);
				}
				
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
	
	/**
	 * hanles the application submit, identify the tripAmount and call calculation service.
	 * @param fsmRequest
	 * @param oldFSM
	 */
	public void handleApplicationSubmit(FSMRequest fsmRequest,FSM oldFSM) {
		
		FSM fsm = fsmRequest.getFsm();
		Map<String, String> newAdditionalDetails = fsm.getAdditionalDetails() != null ? (Map<String, String>)fsm.getAdditionalDetails()
				: new HashMap<String, String>();
		BigDecimal newTripAmount  = new BigDecimal(0);
		try {
			if( newAdditionalDetails != null || newAdditionalDetails.get("tripAmount") != null) {
				 newTripAmount  = BigDecimal.valueOf(Double.valueOf((String)newAdditionalDetails.get("tripAmount")));
			}
		}catch( Exception e) {
			throw new CustomException(FSMErrorConstants.INVALID_TRIP_AMOUNT," tripAmount is invalid");
		}
		
		
		BigDecimal oldTripAmount  = new BigDecimal(0);
		try {
			Map<String, String> oldAdditionalDetails = oldFSM.getAdditionalDetails() != null ? (Map<String, String>)oldFSM.getAdditionalDetails()
					: new HashMap<String, String>();
			if(  oldAdditionalDetails != null || oldAdditionalDetails.get("tripAmount") != null) {
				 oldTripAmount  = BigDecimal.valueOf(Double.valueOf((String)oldAdditionalDetails.get("tripAmount")));
			}
		}catch( Exception e) {
			 oldTripAmount  = new BigDecimal(0);
		}
		
		 
		if( oldTripAmount.compareTo(newTripAmount) != 0) {
			calculationService.addCalculation(fsmRequest, FSMConstants.APPLICATION_FEE);
		}
	}
	
	public List<FSMAudit> auditSearch(FSMAuditSearchCriteria criteria, RequestInfo requestInfo) {
		fsmValidator.validateAudit(criteria);
		List<FSMAudit> auditList = null;
		List<FSMAuditUtil> sourceObjects = repository.getFSMActualData(criteria);
		if (!CollectionUtils.isEmpty(sourceObjects)) {
			FSMAuditUtil sourceObject = repository.getFSMActualData(criteria).get(NumberUtils.INTEGER_ZERO);
			List<FSMAuditUtil> targetObjects = repository.getFSMAuditData(criteria);
			auditList = enrichmentService.enrichFSMAudit(sourceObject, targetObjects);
		}
		return auditList;

	}
}