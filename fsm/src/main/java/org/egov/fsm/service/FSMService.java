package org.egov.fsm.service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang3.math.NumberUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
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
import org.egov.fsm.web.model.FSMResponse;
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
import org.javers.common.collections.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.jayway.jsonpath.JsonPath;

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
	private VehicleTripService vehicleTripService;
	
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
//		String tenantId = fsmRequest.getFsm().getTenantId().split("\\.")[0];
		Object mdmsData = util.mDMSCall(requestInfo, fsmRequest.getFsm().getTenantId());
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
//		String tenantId = fsmRequest.getFsm().getTenantId().split("\\.")[0];
		Object mdmsData = util.mDMSCall(requestInfo, fsmRequest.getFsm().getTenantId());
		FSM fsm = fsmRequest.getFsm();
		
		
		if (fsm.getId() == null) {
			throw new CustomException(FSMErrorConstants.UPDATE_ERROR, "Application Not found in the System" + fsm);
		}
		
		if (fsmRequest.getWorkflow() == null || fsmRequest.getWorkflow().getAction() == null) {
			throw new CustomException(FSMErrorConstants.UPDATE_ERROR, "Workflow action cannot be null." + String.format("{Workflow:%s}", fsmRequest.getWorkflow())) ;
		}
		
		

		List<String> ids = new ArrayList<String>();
		ids.add( fsm.getId());
		FSMSearchCriteria criteria = FSMSearchCriteria.builder().ids(ids).tenantId(fsm.getTenantId()).build();
		FSMResponse fsmResponse = repository.getFSMData(criteria, null);
		List<FSM> fsms = fsmResponse.getFsm();
		
		
		BusinessService businessService = workflowService.getBusinessService(fsm, fsmRequest.getRequestInfo(),
				FSMConstants.FSM_BusinessService,null);
		actionValidator.validateUpdateRequest(fsmRequest, businessService);
		FSM oldFSM = fsms.get(0);
		
		if( fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_REJECT) || 
				fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_CANCEL)) {
			handleRejectCancel(fsmRequest,oldFSM);
		}else {
			fsmValidator.validateUpdate(fsmRequest, fsms, mdmsData); 
		}
		
		if( fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_SUBMIT) ) {
			handleApplicationSubmit(fsmRequest,oldFSM);
		}
		
		if( fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_ASSIGN_DSO) || 
				fsmRequest.getWorkflow().getAction().equalsIgnoreCase(FSMConstants.WF_ACTION_REASSIGN_DSO) ) {
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
			Calendar psd = Calendar.getInstance(TimeZone.getDefault());
			psd.setTimeInMillis(fsm.getPossibleServiceDate());
			Calendar today = Calendar.getInstance(TimeZone.getDefault());
			today.set(Calendar.HOUR_OF_DAY,0);
			today.set(Calendar.MINUTE,0);
			today.set(Calendar.SECOND,0);
			today.set(Calendar.MILLISECOND,0);
			psd.set(Calendar.HOUR_OF_DAY,0);
			psd.set(Calendar.MINUTE,0);
			psd.set(Calendar.SECOND,0);
			psd.set(Calendar.MILLISECOND,0);
			if(today.after(psd) ) {
				throw new CustomException(FSMErrorConstants.INVALID_POSSIBLE_DATE," Possible service Date  is invalid");
			}
		}else {
			throw new CustomException(FSMErrorConstants.INVALID_POSSIBLE_DATE," Possible service Date  is invalid");
		}
		dsoService.validateDSO(fsmRequest);
		
		
	}
	
	private void handleDSOAccept(FSMRequest fsmRequest, FSM oldFSM) {
		FSM fsm = fsmRequest.getFsm();
		org.egov.common.contract.request.User dsoUser = fsmRequest.getRequestInfo().getUserInfo();
		Vendor vendor = dsoService.getVendor(oldFSM.getDsoId(),fsm.getTenantId(), dsoUser.getUuid(),null,null, fsmRequest.getRequestInfo());
		if(vendor == null) {
			throw new CustomException(FSMErrorConstants.INVALID_DSO," DSO is invalid, cannot take an action, Application is not assigned to current logged in user !");
		}
		fsm.setDso(vendor);
		
		if(!StringUtils.hasLength(fsm.getVehicleId())) {
			throw new CustomException(FSMErrorConstants.INVALID_DSO_VEHICLE,"Vehicle should be assigned to accept the Request !");
			
		}else {
			
			Vehicle vehicle = vehicleService.validateVehicle(fsmRequest);
			Map<String, Vehicle> vehilceIdMap = vendor.getVehicles().stream().collect(Collectors.toMap(Vehicle::getId,Function.identity()));
			if(!CollectionUtils.isEmpty(vehilceIdMap) && vehilceIdMap.get(fsm.getVehicleId()) == null ) {
				throw new CustomException(FSMErrorConstants.INVALID_DSO_VEHICLE," Vehicle Does not belong to DSO!");
			}else {
				 vehicle = vehilceIdMap.get(fsm.getVehicleId());
				 fsm.setVehicle(vehicle);
				if(!vehicle.getType().equalsIgnoreCase(fsm.getVehicleType())) {
					throw new CustomException(FSMErrorConstants.INVALID_DSO_VEHICLE," Vehilce Type of FSM and vehilceType of the assigned vehicle does not match !");
				}
			}
		}
		ArrayList uuids = new ArrayList<String>();
		uuids.add(fsm.getDso().getOwner().getUuid());
		fsmRequest.getWorkflow().setAssignes(uuids);
		vehicleTripService.scheduleVehicleTrip(fsmRequest);
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
		Vehicle vehicle = vehicleService.getVehicle(fsm.getVehicleId(), fsm.getTenantId(), fsmRequest.getRequestInfo());
		if(fsm.getWasteCollected() == null  || fsm.getWasteCollected() <=0 ||
				(vehicle != null && vehicle.getTankCapicity() != null && fsm.getWasteCollected() > vehicle.getTankCapicity()) ) {
			throw new CustomException(FSMErrorConstants.INVALID_WASTER_COLLECTED," Wastecollected is invalid to complete the application !.");
		}
		
		if(fsm.getCompletedOn() != null) {
			Calendar cmpOn = Calendar.getInstance(TimeZone.getDefault());
			cmpOn.setTimeInMillis(fsm.getCompletedOn());
			Calendar strtDt = Calendar.getInstance(TimeZone.getDefault());
			strtDt.setTimeInMillis(fsm.getAuditDetails().getCreatedTime());
			Calendar today = Calendar.getInstance(TimeZone.getDefault());
			strtDt.set(Calendar.HOUR_OF_DAY,0);
			strtDt.set(Calendar.MINUTE,0);
			strtDt.set(Calendar.SECOND,0);
			strtDt.set(Calendar.MILLISECOND,0);
			today.set(Calendar.HOUR_OF_DAY,0);
			today.set(Calendar.MINUTE,0);
			today.set(Calendar.SECOND,0);
			today.set(Calendar.MILLISECOND,0);
			cmpOn.set(Calendar.HOUR_OF_DAY,0);
			cmpOn.set(Calendar.MINUTE,0);
			cmpOn.set(Calendar.SECOND,0);
			cmpOn.set(Calendar.MILLISECOND,0);
			cmpOn.add(Calendar.DAY_OF_YEAR, 1);
			today.add(Calendar.DAY_OF_YEAR, 2);
			if( !(cmpOn.after(strtDt) && cmpOn.before(today) )) {
				throw new CustomException(FSMErrorConstants.INVALID_COMPLETED_DATE," Completed  Date  is invalid");
			}
		}else {
			throw new CustomException(FSMErrorConstants.COMPLETED_DATE_NOT_NULL," Completed On Cannot be empty !");
		}
		
		ArrayList assignes = new ArrayList<String>();
		assignes.add(fsm.getAccountId());
		FSMSearchCriteria fsmsearch = new FSMSearchCriteria();
		ArrayList accountIds = new ArrayList<String>();
		accountIds.add(fsm.getAccountId());
		fsmsearch.setTenantId(fsm.getTenantId());
		fsmsearch.setOwnerIds(accountIds);
		UserDetailResponse userDetailResponse = userService.getUser(fsmsearch, fsmRequest.getRequestInfo());
		fsmsearch.setMobileNumber(userDetailResponse.getUser().get(0).getMobileNumber());
		fsmsearch.setOwnerIds(null);
		userDetailResponse = userService.getUser(fsmsearch, null);
		fsmRequest.getWorkflow().setAssignes(userDetailResponse.getUser().stream().map(User::getUuid).collect(Collectors.toList()));
		vehicleTripService.vehicleTripReadyForDisposal(fsmRequest);

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
	public FSMResponse FSMsearch(FSMSearchCriteria criteria, RequestInfo requestInfo) {
		
		List<FSM> fsmList = new LinkedList<>();
		FSMResponse fsmResponse =null;
		List<String> uuids = new ArrayList<String>();
		UserDetailResponse usersRespnse;
		String dsoId = null;
		
		fsmValidator.validateSearch(requestInfo, criteria);
		
		if(requestInfo.getUserInfo().getType().equalsIgnoreCase(FSMConstants.CITIZEN) ) {
			List<Role> roles = requestInfo.getUserInfo().getRoles();
			if( roles.stream().anyMatch(role -> Objects.equals(role.getCode(), FSMConstants.ROLE_FSM_DSO))) {
			  	Vendor dso = dsoService.getVendor(null, criteria.getTenantId(), null, requestInfo.getUserInfo().getMobileNumber(),null, requestInfo);
			  	if(dso!=null && org.apache.commons.lang3.StringUtils.isNotEmpty(dso.getId())) {
			  		dsoId = dso.getId();
			  	}
			}else if(criteria.tenantIdOnly() ){
				criteria.setMobileNumber(requestInfo.getUserInfo().getMobileNumber());
			}
			
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
				
			}else {
				return FSMResponse.builder().fsm(fsmList).build();
			}
		}
		

		
		fsmResponse = repository.getFSMData(criteria, dsoId);
		fsmList = fsmResponse.getFsm();
		if (!fsmList.isEmpty()) {
			enrichmentService.enrichFSMSearch(fsmList, requestInfo, criteria.getTenantId());
		}

		
		return fsmResponse;
	}
	
	/**
	 * hanles the application submit, identify the tripAmount and call calculation service.
	 * @param fsmRequest
	 * @param oldFSM
	 */
	public void handleApplicationSubmit(FSMRequest fsmRequest,FSM oldFSM) {
		
		FSM fsm = fsmRequest.getFsm();
		
		calculationService.addCalculation(fsmRequest, FSMConstants.APPLICATION_FEE);
	}
	
	public List<FSMAudit> auditSearch(FSMAuditSearchCriteria criteria, RequestInfo requestInfo) {
		fsmValidator.validateAudit(criteria);
		List<FSMAudit> auditList = null;
		List<FSMAuditUtil> sourceObjects = repository.getFSMActualData(criteria);
		if (!CollectionUtils.isEmpty(sourceObjects)) {
			FSMAuditUtil sourceObject = sourceObjects.get(NumberUtils.INTEGER_ZERO);
			List<FSMAuditUtil> targetObjects = repository.getFSMAuditData(criteria);
			auditList = enrichmentService.enrichFSMAudit(sourceObject, targetObjects);
		}
		return auditList;

	}
	
	public List<FSM> searchFSMPlainSearch(@Valid FSMSearchCriteria criteria, RequestInfo requestInfo) {
		List<FSM> fsmList = getFsmPlainSearch(criteria, requestInfo);
		if (!fsmList.isEmpty()) {
			enrichmentService.enrichFSMSearch(fsmList, requestInfo, criteria.getTenantId());
		}
		return fsmList;
	}


	private List<FSM> getFsmPlainSearch(@Valid FSMSearchCriteria criteria, RequestInfo requestInfo) {
		if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit())
            criteria.setLimit(config.getMaxSearchLimit());

        List<String> ids = null;

        if(criteria.getIds() != null && !criteria.getIds().isEmpty())
            ids = criteria.getIds();
        else
            ids = repository.fetchFSMIds(criteria);

        if(ids.isEmpty())
            return Collections.emptyList();

        FSMSearchCriteria FSMcriteria = FSMSearchCriteria.builder().ids(ids).build();

        List<FSM> listFSM = repository.getFsmPlainSearch(FSMcriteria);
        return listFSM;
	}
	
}
