package org.egov.pgr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.repository.ServiceRequestRepository;
import org.egov.pgr.web.models.RequestInfoWrapper;
import org.egov.pgr.web.models.Service;
import org.egov.pgr.web.models.ServiceRequest;
import org.egov.pgr.web.models.Status;
import org.egov.pgr.web.models.workflow.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.egov.pgr.util.PGRConstants.*;

@org.springframework.stereotype.Service
public class WorkflowService {

    @Autowired
    private PGRConfiguration pgrConfiguration;

    @Autowired
    private ServiceRequestRepository repository;

    @Autowired
    private ObjectMapper mapper;



    /*
     *
     * Should return the applicable BusinessService for the given request
     *
     * */
    public BusinessService getBusinessService(ServiceRequest serviceRequest){
        String tenantId = serviceRequest.getPgrEntity().getService().getTenantId();
        StringBuilder url = getSearchURLWithParams(tenantId, PGR_BUSINESSSERVICE);
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(serviceRequest.getRequestInfo()).build();
        Object result = repository.fetchResult(url, requestInfoWrapper);
        BusinessServiceResponse response = null;
        try {
            response = mapper.convertValue(result, BusinessServiceResponse.class);
        } catch (IllegalArgumentException e) {
            throw new CustomException("PARSING ERROR", "Failed to parse response of workflow business service search");
        }

        if(CollectionUtils.isEmpty(response.getBusinessServices()))
            throw new CustomException("BUSINESSSERVICE_NOT_FOUND","The businessService "+PGR_BUSINESSSERVICE+" is not found");

        return response.getBusinessServices().get(0);
    }


    /*
     * Call the workflow service with the given action and update the status
     * return the updated status of the application
     *
     * */
    public String updateWorkflowStatus(ServiceRequest serviceRequest){
        ProcessInstance processInstance = getProcessInstanceForPGR(serviceRequest);
        ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(serviceRequest.getRequestInfo(), Collections.singletonList(processInstance));
        State state = callWorkFlow(workflowRequest);
        return state.getApplicationStatus();
    }


    public void validateAssignee(ServiceRequest serviceRequest){
        /*
        * Call HRMS service and validate of the assignee belongs to same department
        * as the employee assigning it
        *
        * */

    }

    /**
     * Creates url for search based on given tenantId and businessservices
     *
     * @param tenantId The tenantId for which url is generated
     * @param businessService The businessService for which url is generated
     * @return The search url
     */
    private StringBuilder getSearchURLWithParams(String tenantId, String businessService) {

        StringBuilder url = new StringBuilder(pgrConfiguration.getWfHost());
        url.append(pgrConfiguration.getWfBusinessServiceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&businessservices=");
        url.append(businessService);
        return url;
    }


    public void enrichmentForSendBackToCititzen(){
        /*
        * If send bac to citizen action is taken assignes should be set to accountId
        *
        * */
    }


    public void enrichWorkflow(ServiceRequest request){
        /*
        * Should enrich the workflow object in PGREntity Object
        * */
    }

   /**
     * Enriches ProcessInstance Object for workflow
     * @param serviceRequest
     */
    public ProcessInstance getProcessInstanceForPGR(ServiceRequest serviceRequest){

        Service service = serviceRequest.getPgrEntity().getService();

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setBusinessId(service.getServiceRequestId());
        processInstance.setAction(serviceRequest.getPgrEntity().getWorkflow().getAction());
        processInstance.setModuleName(PGR_MODULENAME);
        processInstance.setTenantId(service.getTenantId());
        processInstance.setBusinessService(getBusinessService(serviceRequest).getBusinessService());

        return processInstance;
    }
    
    /**
     * Method to integrate with workflow
     *
     * take the ProcessInstanceRequest as paramerter to call wf-service
     *
     * and return wf-response to sets the resultant status
     */
    public State callWorkFlow(ProcessInstanceRequest workflowReq) {

        ProcessInstanceResponse response = null;
        StringBuilder url = new StringBuilder(pgrConfiguration.getWfHost().concat(pgrConfiguration.getWfTransitionPath()));
        Object optional = repository.fetchResult(url, workflowReq);
        response = mapper.convertValue(optional, ProcessInstanceResponse.class);
        return response.getProcessInstances().get(0).getState();
    }
}
