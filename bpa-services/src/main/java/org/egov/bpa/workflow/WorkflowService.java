package org.egov.bpa.workflow;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.web.models.RequestInfoWrapper;
import org.egov.bpa.web.models.workflow.BusinessService;
import org.egov.bpa.web.models.workflow.BusinessServiceResponse;
import org.egov.bpa.web.models.workflow.ProcessInstance;
import org.egov.bpa.web.models.workflow.ProcessInstanceResponse;
import org.egov.bpa.web.models.workflow.State;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WorkflowService {


    private BPAConfiguration config;

    private ServiceRequestRepository serviceRequestRepository;

    private ObjectMapper mapper;

    @Autowired
    public WorkflowService(BPAConfiguration config, ServiceRequestRepository serviceRequestRepository, ObjectMapper mapper) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
        this.mapper = mapper;
    }

    /**
     * Get the workflow config for the given tenant
     * @param tenantId    The tenantId for which businessService is requested
     * @param requestInfo The RequestInfo object of the request
     * @return BusinessService for the the given tenantId
     */
    public BusinessService getBusinessService(String tenantId, RequestInfo requestInfo,String applicationNo) {
        StringBuilder url = getSearchURLWithParams(tenantId,true,null);
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
        BusinessServiceResponse response = null;
        try {
            response = mapper.convertValue(result,BusinessServiceResponse.class);
        } catch (IllegalArgumentException e) {
            throw new CustomException("PARSING ERROR", "Failed to parse response of calculate");
        }
        return response.getBusinessServices().get(0);
    }


    /**
     * Get the workflow processInstance for the given tenant
     * @param tenantId    The tenantId for which businessService is requested
     * @param requestInfo The RequestInfo object of the request
     * @return BusinessService for the the given tenantId
     */
    public ProcessInstance getProcessInstance(String tenantId, RequestInfo requestInfo,String applicationNo) {
        StringBuilder url = getSearchURLWithParams(tenantId,false,applicationNo);
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
        ProcessInstanceResponse response = null;
        try {
            response = mapper.convertValue(result,ProcessInstanceResponse.class);
        } catch (IllegalArgumentException e) {
            throw new CustomException("PARSING ERROR", "Failed to parse response of calculate");
        }
        return response.getProcessInstances().get(0);
    }

    /**
     * Creates url for search based on given tenantId
     *
     * @param tenantId The tenantId for which url is generated
     * @return The search url
     */
    private StringBuilder getSearchURLWithParams(String tenantId, boolean businessService,String applicationNo) {
        StringBuilder url = new StringBuilder(config.getWfHost());
        if(businessService) {
        		url.append(config.getWfBusinessServiceSearchPath());
        }else {
        		url.append(config.getWfProcessPath());
        }
//        url.append(config.getWfBusinessServiceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        if(businessService) {
        		url.append("&businessService=");
        		 url.append(config.getBusinessServiceValue());
	    }else {
	    		url.append("&businessIds=");
	    		 url.append(applicationNo);
	    }
        
       
        return url;
    }

    

    /**
     * Returns boolean value to specifying if the state is updatable
     * @param string The stateCode of the bpa
     * @param businessService The BusinessService of the application flow
     * @return State object to be fetched
     */
    public Boolean isStateUpdatable(String string, BusinessService businessService){
       for(State state : businessService.getStates()){
           if(state.getApplicationStatus()!=null && state.getApplicationStatus().equalsIgnoreCase(string.toString()))
               return state.getIsStateUpdatable();
       }
       return null;
    }
    /**
     * Returns boolean value to specifying if the state is updatable
     * @param string The stateCode of the bpa
     * @param businessService The BusinessService of the application flow
     * @return State object to be fetched
     */
    public String getCurrentState(String string, BusinessService businessService){
       for(State state : businessService.getStates()){
           if(state.getApplicationStatus()!=null && state.getApplicationStatus().equalsIgnoreCase(string.toString()))
               return state.getState();
       }
       return null;
    }



}
