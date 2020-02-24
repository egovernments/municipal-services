package org.egov.waterConnection.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.config.WSConfiguration;
import org.egov.waterConnection.model.RequestInfoWrapper;
import org.egov.waterConnection.model.workflow.BusinessService;
import org.egov.waterConnection.model.workflow.BusinessServiceResponse;
import org.egov.waterConnection.model.workflow.State;
import org.egov.waterConnection.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class WorkflowService {

	@Autowired
    private WSConfiguration config;
	
	@Autowired
    private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
    private ObjectMapper mapper;

    /**
     * Get the workflow-config for the given tenant
     * @param tenantId    The tenantId for which businessService is requested
     * @param requestInfo The RequestInfo object of the request
     * @return BusinessService for the the given tenantId
     */
    public BusinessService getBusinessService(String tenantId, RequestInfo requestInfo) {
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        Object result = serviceRequestRepository.fetchResult(getSearchURLWithParams(tenantId), requestInfoWrapper);
        BusinessServiceResponse response = null;
        try {
            response = mapper.convertValue(result,BusinessServiceResponse.class);
        } catch (IllegalArgumentException e) {
            throw new CustomException("PARSING ERROR", "Failed to parse response of calculate");
        }
        return response.getBusinessServices().get(0);
    }


    /**
     * Creates url for search based on given tenantId
     *
     * @param tenantId The tenantId for which url is generated
     * @return The search url
     */
    private StringBuilder getSearchURLWithParams(String tenantId) {
        StringBuilder url = new StringBuilder(config.getWfHost());
        url.append(config.getWfBusinessServiceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&businessservices=");
        url.append(config.getBusinessServiceValue());
        return url;
    }


    /**
     * Returns boolean value to specifying if the state is updatable
     * @param stateCode The stateCode of the license
     * @param businessService The BusinessService of the application flow
     * @return State object to be fetched
     */
    public Boolean isStateUpdatable(String stateCode, BusinessService businessService){
       for(State state : businessService.getStates()){
           if(state.getApplicationStatus()!=null && state.getApplicationStatus().equalsIgnoreCase(stateCode))
               return state.getIsStateUpdatable();
       }
       return null;
    }




}
