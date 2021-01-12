package org.egov.fsm.validator;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.web.model.user.User;
import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.util.FSMConstants;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.FSMRequest;
import org.egov.fsm.web.model.FSMSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.BeanUtils;
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
		FSM fsm = fsmRequest.getFsm();
		if( fsmRequest.getRequestInfo().getUserInfo().getType().equalsIgnoreCase(FSMConstants.CITIZEN)) {
			
			// when user is created for the citizen but name is mepty
			if( fsm.getCitizen() != null && StringUtils.isEmpty(fsm.getCitizen().getName()) && StringUtils.isEmpty(fsmRequest.getRequestInfo().getUserInfo().getName()) ) {
				throw new CustomException(FSMErrorConstants.INVALID_APPLICANT_ERROR,"Applicant Name and mobile number mandatory");
			}
			// ui does not pass citizen in fsm but userInfo in the request is citizen
			if(fsm.getCitizen() == null || StringUtils.isEmpty(fsm.getCitizen().getName()) || StringUtils.isEmpty(fsm.getCitizen().getMobileNumber() )) {
				User citzen = new User();
				BeanUtils.copyProperties(fsmRequest.getRequestInfo().getUserInfo(), citzen);
				fsm.setCitizen( citzen);
			}
			
			if(!StringUtils.isEmpty(fsm.getSource())) {
				mdmsValidator.validateApplicationChannel(fsm.getSource());
				
			}
			if(!StringUtils.isEmpty(fsm.getSanitationtype())) {
				mdmsValidator.validateOnSiteSanitationType(fsm.getSanitationtype());
			}
			
		}else if( fsmRequest.getRequestInfo().getUserInfo().getType().equalsIgnoreCase(FSMConstants.EMPLOYEE)) {
			User applicant = fsm.getCitizen();
			if( applicant == null ||  StringUtils.isEmpty(applicant.getName()) || StringUtils.isEmpty(applicant.getMobileNumber())) {
				throw new CustomException(FSMErrorConstants.INVALID_APPLICANT_ERROR,"Applicant Name and mobile number mandatory");
			}
			
			Map<String, String> additionalDetails = fsm.getadditionalDetails() != null ? (Map<String,String>)fsm.getadditionalDetails()
					: new HashMap<String, String>();
			if(config.getTripAmtRequired() &&  additionalDetails.get("tripAmount") == null   ) {
				throw new CustomException(FSMErrorConstants.INVALID_TRIP_AMOUNT," tripAmount is invalid");
			}else {
				try {
					BigDecimal tripAmt = BigDecimal.valueOf(Double.valueOf((String)additionalDetails.get("tripAmount")));
				}catch(Exception e) {
					throw new CustomException(FSMErrorConstants.INVALID_TRIP_AMOUNT," tripAmount is invalid");
				}
			}
			
			
			mdmsValidator.validateApplicationChannel(fsm.getSource());
			mdmsValidator.validateOnSiteSanitationType(fsm.getSanitationtype());
		}else {
			// incase of anonymous user, citizen is mandatory
			if(fsm.getCitizen() == null || StringUtils.isEmpty(fsm.getCitizen().getName()) || StringUtils.isEmpty(fsm.getCitizen().getMobileNumber() )) {
				throw new CustomException(FSMErrorConstants.INVALID_APPLICANT_ERROR,"Applicant Name and mobile number mandatory");
			}
			
			if(!StringUtils.isEmpty(fsm.getSource())) {
				mdmsValidator.validateApplicationChannel(fsm.getSource());
				
			}
			if(!StringUtils.isEmpty(fsm.getSanitationtype())) {
				mdmsValidator.validateOnSiteSanitationType(fsm.getSanitationtype());
			}
		}
		
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
		if(criteria.getTenantId() == null)
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
		
		if (criteria.getApplicationNumber() != null && !allowedParams.contains("applicationNo")) {
			System.out.println("app..... "+criteria.getApplicationNumber());
			throw new CustomException(FSMErrorConstants.INVALID_SEARCH, "Search on applicationNo is not allowed");
			}
		if (criteria.getFromDate() != null && !allowedParams.contains("fromDate") && 
				criteria.getToDate() != null && !allowedParams.contains("toDate") ) {
			throw new CustomException(FSMErrorConstants.INVALID_SEARCH, "Search on fromDate and toDate is not allowed");
		}else if( (criteria.getFromDate() != null && criteria.getToDate() == null ) ||
				criteria.getFromDate() == null && criteria.getToDate() != null) {
			throw new CustomException(FSMErrorConstants.INVALID_SEARCH, "Search only "+((criteria.getFromDate() == null) ? "fromDate" :"toDate" )+" is not allowed");
		}if(criteria.getFromDate() != null && criteria.getFromDate() > Calendar.getInstance().getTimeInMillis() ||
				criteria.getToDate() != null && criteria.getFromDate() > Calendar.getInstance().getTimeInMillis()	) {
			throw new CustomException(FSMErrorConstants.INVALID_SEARCH, "Search in futer dates is not allowed");
			
		}
		
		
			
	}

	public void validateUpdate(FSMRequest fsmRequest, List<FSM> searchResult, Object mdmsData, String currentState, Map<String, String> edcrResponse) {

		FSM fsm = fsmRequest.getFsm();
		
//		validateAllIds(searchResult, fsm);
		
		mdmsValidator.validateMdmsData(fsmRequest, mdmsData);
		
		if(!StringUtils.isEmpty(fsm.getSource())) {
			mdmsValidator.validateApplicationChannel(fsm.getSource());
			
		}
		if(!StringUtils.isEmpty(fsm.getSanitationtype())) {
			mdmsValidator.validateOnSiteSanitationType(fsm.getSanitationtype());
		}
		
		mdmsValidator.validatePropertyType(fsmRequest.getFsm().getPropertyUsage());

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