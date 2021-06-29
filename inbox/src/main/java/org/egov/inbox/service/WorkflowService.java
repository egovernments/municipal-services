package org.egov.inbox.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.inbox.config.InboxConfiguration;
import org.egov.inbox.repository.ServiceRequestRepository;
import org.egov.inbox.util.ErrorConstants;
import org.egov.inbox.web.model.RequestInfoWrapper;
import org.egov.inbox.web.model.workflow.BusinessService;
import org.egov.inbox.web.model.workflow.BusinessServiceResponse;
import org.egov.inbox.web.model.workflow.ProcessInstanceResponse;
import org.egov.inbox.web.model.workflow.ProcessInstanceSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.egov.inbox.web.model.workflow.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WorkflowService {

	private InboxConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private ObjectMapper mapper;

	@Autowired
	public WorkflowService(InboxConfiguration config, ServiceRequestRepository serviceRequestRepository,
			ObjectMapper mapper) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.mapper = mapper;
	}

	public Integer getProcessCount(String tenantId, RequestInfo requestInfo, ProcessInstanceSearchCriteria criteria) {
		StringBuilder url = new StringBuilder(config.getWorkflowHost());
		url.append( config.getProcessCountPath());
		url = this.buildWorkflowUrl( criteria, url, Boolean.TRUE);
		
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = serviceRequestRepository.fetchIntResult(url, requestInfoWrapper);
		Integer response = null;
		try {
			response = mapper.convertValue(result, Integer.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorConstants.PARSING_ERROR, "Failed to parse response of ProcessInstance Count");
		}
		return response;
	}
	
	public List<HashMap<String, Object>> getProcessStatusCount( RequestInfo requestInfo, ProcessInstanceSearchCriteria criteria) {
		StringBuilder url = new StringBuilder(config.getWorkflowHost());
		url.append( config.getProcessStatusCountPath());
		url = this.buildWorkflowUrl(criteria, url, Boolean.FALSE);
		
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		List<HashMap<String, Object>> response = (List<HashMap<String, Object>>) serviceRequestRepository.fetchListResult(url, requestInfoWrapper);
		
		
		return response;
	}
	
	public ProcessInstanceResponse getProcessInstance(ProcessInstanceSearchCriteria criteria, RequestInfo requestInfo) {
		StringBuilder url = new StringBuilder(config.getWorkflowHost());
		url.append( config.getProcessSearchPath());
		url = this.buildWorkflowUrl(criteria, url, Boolean.FALSE);
		
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
		ProcessInstanceResponse resposne =null;
		try {
			resposne = mapper.convertValue(result, ProcessInstanceResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorConstants.PARSING_ERROR, "Failed to parse response of ProcessInstance");
		}
		return resposne;
	}
	/**
	 * Get the workflow config for the given tenant
	 * 
	 * @param tenantId
	 *           id of the tenant
	 * @param requestInfo
	 *            The RequestInfo object of the request
	 * @param businessServiceName
	 * 				businessService code
	 * @return BusinessService for the the given tenantId
	 */
	public BusinessService getBusinessService(String tenantId, RequestInfo requestInfo, String businessServceName) {
		StringBuilder url = getSearchURLWithParams(tenantId, businessServceName);
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
		BusinessServiceResponse response = null;
		try {
			response = mapper.convertValue(result, BusinessServiceResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorConstants.PARSING_ERROR, "Failed to parse response of Workflow");
		}
		return response.getBusinessServices().get(0);
	}
	
	private StringBuilder buildWorkflowUrl(ProcessInstanceSearchCriteria criteria, StringBuilder url,boolean noStatus) {
		url.append("?tenantId=").append(criteria.getTenantId());
		if(!CollectionUtils.isEmpty(criteria.getStatus()) && noStatus == Boolean.FALSE) {
			url.append("&status=").append(StringUtils.arrayToDelimitedString(criteria.getStatus().toArray(),","));
		}
		
		if(!CollectionUtils.isEmpty(criteria.getBusinessIds())) {
			url.append("&businessIds=").append(StringUtils.arrayToDelimitedString(criteria.getBusinessIds().toArray(),","));
		}
		
		if(!CollectionUtils.isEmpty(criteria.getIds())) {
			url.append("&ids=").append(StringUtils.arrayToDelimitedString(criteria.getIds().toArray(),","));
		}
		if(!StringUtils.isEmpty(criteria.getAssignee())) {
			url.append("&assignee=").append( criteria.getAssignee());
		}
		if(criteria.getHistory() != null) {
			url.append("&history=").append( criteria.getHistory());
		}
		if(criteria.getFromDate() != null) {
			url.append("&fromDate=").append( criteria.getFromDate());
		}
		if(criteria.getToDate() != null) {
			url.append("&toDate=").append( criteria.getToDate());
		}

		if(!StringUtils.isEmpty(criteria.getModuleName())) {
			url.append("&moduleName=").append( criteria.getModuleName());
		}
		if(!StringUtils.isEmpty(criteria.getBusinessService())) {
			url.append("&businessService=").append( StringUtils.arrayToDelimitedString(criteria.getBusinessService().toArray(),","));
		}
		if(!StringUtils.isEmpty(criteria.getLimit())) {
			url.append("&limit=").append( criteria.getLimit());
		}
		if(!StringUtils.isEmpty(criteria.getOffset())) {
			url.append("&offset=").append( criteria.getOffset());
		}
		
		return url;
	}

	/**
	 * Creates url for search based on given tenantId
	 *
	 * @param tenantId
	 *            The tenantId for which url is generated
	 * @return The search url
	 */
	private StringBuilder getSearchURLWithParams(String tenantId, String businessService) {
		StringBuilder url = new StringBuilder(config.getWorkflowHost());
		
		
		
		if (businessService != null) {
			url.append(config.getBusinessServiceSearchPath());
			url.append("?businessServices=");
			url.append(businessService);
		} 
		
		url.append("&tenantId=");
		url.append(tenantId);
		
		return url;
	}
	
	/**
     * Gets the list of status on which user from requestInfo can take action upon
     * @param requestInfo The RequestInfo Object of the request
     * @param businessServices List of all businessServices
     * @return List of status on which user from requestInfo can take action upon
     */

    public HashMap<String,String> getActionableStatusesForRole(RequestInfo requestInfo, List<BusinessService> businessServices,ProcessInstanceSearchCriteria criteria){

        String tenantId;
        List<String> userRoleCodes;
        Map<String,List<String>> tenantIdToUserRolesMap = getTenantIdToUserRolesMap(requestInfo);
        Map<String,List<BusinessService>> tenantIdToBuisnessSevicesMap =  getTenantIdToBuisnessSevicesMap(businessServices);
        Map<String,Set<String>> stateToRoleMap = getStateToRoleMap(businessServices);
        HashMap<String,String> actionableStatuses = new HashMap<>();
        
        for(Map.Entry<String,List<String>> entry : tenantIdToUserRolesMap.entrySet()){
            if(entry.getKey().equals(criteria.getTenantId())){
                List<BusinessService> businessServicesByTenantId = new ArrayList();
//                if(config.getIsStateLevel()){
//                    businessServicesByTenantId = tenantIdToBuisnessSevicesMap.get(entry.getKey().split("\\.")[0]);
//                }else{
                    businessServicesByTenantId = tenantIdToBuisnessSevicesMap.get(entry.getKey());
//                }
                if(businessServicesByTenantId != null ) {
                	 businessServicesByTenantId.forEach(service -> {
                         List<State> states = service.getStates();
                         states.forEach(state -> {
                             Set<String> stateRoles = stateToRoleMap.get(state.getUuid());
                             if(!CollectionUtils.isEmpty(stateRoles) && !Collections.disjoint(stateRoles,entry.getValue())){
                                 actionableStatuses.put(state.getUuid(), state.getApplicationStatus());
                             }

                         });
                     });
                }
               
            }         
        }
        return actionableStatuses;
    }
    
    /**
     * Gets the map of tenantId to roles the user is assigned
     * @param requestInfo RequestInfo of the request
     * @return Map of tenantId to roles for user in the requestInfo
     */
    public Map<String,List<String>> getTenantIdToUserRolesMap(RequestInfo requestInfo){
        Map<String,List<String>> tenantIdToUserRoles = new HashMap<>();
        requestInfo.getUserInfo().getRoles().forEach(role -> {
            if(tenantIdToUserRoles.containsKey(role.getTenantId())){
                tenantIdToUserRoles.get(role.getTenantId()).add(role.getCode());
            }
            else {
                List<String> roleCodes = new LinkedList<>();
                roleCodes.add(role.getCode());
                tenantIdToUserRoles.put(role.getTenantId(),roleCodes);
            }

        });
        return tenantIdToUserRoles;
    }
    
    public Map<String,List<BusinessService>> getTenantIdToBuisnessSevicesMap(List<BusinessService> businessServices){
        Map<String,List<BusinessService>> tenantIdToBuisnessSevicesMap = new HashMap<>();
        businessServices.forEach(businessService -> {
            if(tenantIdToBuisnessSevicesMap.containsKey(businessService.getTenantId())){
                tenantIdToBuisnessSevicesMap.get(businessService.getTenantId()).add(businessService);
            }
            else {
                List<BusinessService> businessServiceList = new LinkedList<>();
                businessServiceList.add(businessService);
                tenantIdToBuisnessSevicesMap.put(businessService.getTenantId(),businessServiceList);
            }
        });
        return tenantIdToBuisnessSevicesMap;
    }
    
    /**
     * Creates a map of status to roles who can take actions on it for all businessService
     * @param businessServices The list of businessServices
     * @return Map of status to roles which can take action on it for all businessService
     */
    public Map<String,Set<String>> getStateToRoleMap(List<BusinessService> businessServices){
        Map<String,Set<String>> stateToRolesMap = new HashMap<>();
        businessServices.forEach(businessService -> {
            for(State state : businessService.getStates()){
                HashSet<String> roles = new HashSet<>();
                if(!CollectionUtils.isEmpty(state.getActions())){
                    state.getActions().forEach(action -> {
                        roles.addAll(action.getRoles());
                    });
                }
                stateToRolesMap.put(state.getUuid(),roles);
            }
        });
        return stateToRolesMap;
    }

	
}
