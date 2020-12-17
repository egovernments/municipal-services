package org.egov.fsm.validator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.util.FSMConstants;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.FSMRequest;
import org.egov.fsm.web.model.FSMSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
public class FSMValidator {

	@Autowired
	private MDMSValidator mdmsValidator;
	

	@Autowired
	private FSMConfiguration config;

	public void validateCreate(FSMRequest fsmRequest, Object mdmsData) {
		mdmsValidator.validateMdmsData(fsmRequest, mdmsData);
		mdmsValidator.validatePropertyType(fsmRequest.getFsm().getPropertyUsage());
	}

	/**
	 * Validates if the search parameters are valid
	 * 
	 * @param requestInfo
	 *            The requestInfo of the incoming request
	 * @param criteria
	 *            The FSMSearch Criteria
	 */
//TODO need to make the changes in the data
	public void validateSearch(RequestInfo requestInfo, FSMSearchCriteria criteria) {
		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase(FSMConstants.CITIZEN) && criteria.isEmpty())
			throw new CustomException(FSMErrorConstants.INVALID_SEARCH, "Search without any paramters is not allowed");

		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase(FSMConstants.CITIZEN) && !criteria.tenantIdOnly()
				&& criteria.getTenantId() == null)
			throw new CustomException(FSMErrorConstants.INVALID_SEARCH, "TenantId is mandatory in search");

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(FSMConstants.CITIZEN) && !criteria.isEmpty()
				&& !criteria.tenantIdOnly() && criteria.getTenantId() == null)
			throw new CustomException(FSMErrorConstants.INVALID_SEARCH, "TenantId is mandatory in search");

		String allowedParamStr = null;

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(FSMConstants.CITIZEN))
			allowedParamStr = config.getAllowedCitizenSearchParameters();
		else if (requestInfo.getUserInfo().getType().equalsIgnoreCase(FSMConstants.EMPLOYEE))
			allowedParamStr = config.getAllowedEmployeeSearchParameters();
		else
			throw new CustomException(FSMErrorConstants.INVALID_SEARCH,
					"The userType: " + requestInfo.getUserInfo().getType() + " does not have any search config");

		if (StringUtils.isEmpty(allowedParamStr) && !criteria.isEmpty())
			throw new CustomException(FSMErrorConstants.INVALID_SEARCH, "No search parameters are expected");
		else {
			List<String> allowedParams = Arrays.asList(allowedParamStr.split(","));
			validateSearchParams(criteria, allowedParams);
		}
	}

	/**
	 * Validates if the paramters coming in search are allowed
	 * 
	 * @param criteria
	 *            fsm search criteria
	 * @param allowedParams
	 *            Allowed Params for search
	 */
	private void validateSearchParams(FSMSearchCriteria criteria, List<String> allowedParams) {

		

		if (criteria.getMobileNumber() != null && !allowedParams.contains("mobileNumber"))
			throw new CustomException(FSMErrorConstants.INVALID_SEARCH, "Search on mobileNumber is not allowed");

		if (criteria.getOffset() != null && !allowedParams.contains("offset"))
			throw new CustomException(FSMErrorConstants.INVALID_SEARCH, "Search on offset is not allowed");

		if (criteria.getLimit() != null && !allowedParams.contains("limit"))
			throw new CustomException(FSMErrorConstants.INVALID_SEARCH, "Search on limit is not allowed");
		
	
	}

	public void validateUpdate(FSMRequest fsmRequest, List<FSM> searchResult, Object mdmsData, String currentState, Map<String, String> edcrResponse) {

		FSM fsm = fsmRequest.getFsm();
		
//		validateAllIds(searchResult, fsm);
		
		mdmsValidator.validateMdmsData(fsmRequest, mdmsData);
		
//		setFieldsFromSearch(fsmRequest, searchResult, mdmsData);

	}
	
	private void validateAllIds(List<FSM> searchResult, FSM fsm) {

		Map<String, FSM> idTofsmFromSearch = new HashMap<>();
		searchResult.forEach(fsms -> {
			idTofsmFromSearch.put(fsms.getId(), fsms);
		});

		Map<String, String> errorMap = new HashMap<>();
		FSM searchedfsm = idTofsmFromSearch.get(fsm.getId());

		if (!searchedfsm.getApplicationNo().equalsIgnoreCase(fsm.getApplicationNo()))
			errorMap.put("INVALID UPDATE", "The application number from search: " + searchedfsm.getApplicationNo()
					+ " and from update: " + fsm.getApplicationNo() + " does not match");

		if (!searchedfsm.getId().equalsIgnoreCase(fsm.getId()))
			errorMap.put("INVALID UPDATE", "The id " + fsm.getId() + " does not exist");


		// validate the pit id


		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}
	

	private void setFieldsFromSearch(FSMRequest fsmRequest, List<FSM> searchResult, Object mdmsData) {
		Map<String, FSM> idToFsmFromSearch = new HashMap<>();

		searchResult.forEach(fsm -> {
			idToFsmFromSearch.put(fsm.getId(), fsm);
		});

		fsmRequest.getFsm().getAuditDetails()
				.setCreatedBy(idToFsmFromSearch.get(fsmRequest.getFsm().getId()).getAuditDetails().getCreatedBy());
		fsmRequest.getFsm().getAuditDetails()
				.setCreatedTime(idToFsmFromSearch.get(fsmRequest.getFsm().getId()).getAuditDetails().getCreatedTime());
		fsmRequest.getFsm().setStatus(idToFsmFromSearch.get(fsmRequest.getFsm().getId()).getStatus());
	}
	
	public void validateWorkflowActions(FSMRequest fsmRequest) {
		FSM fsm = fsmRequest.getFsm();
		// TODO Validate the the current workflow action is valid according to business
	}
	

}