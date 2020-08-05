package org.egov.pgr.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.User;
import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.repository.ServiceRequestRepository;
import org.egov.pgr.web.models.*;
import org.egov.pgr.web.models.workflow.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.pgr.util.PGRConstants.*;

@org.springframework.stereotype.Service
public class WorkflowService {

    private PGRConfiguration pgrConfiguration;

    private ServiceRequestRepository repository;

    private ObjectMapper mapper;


    @Autowired
    public WorkflowService(PGRConfiguration pgrConfiguration, ServiceRequestRepository repository, ObjectMapper mapper) {
        this.pgrConfiguration = pgrConfiguration;
        this.repository = repository;
        this.mapper = mapper;
    }

    /*
     *
     * Should return the applicable BusinessService for the given request
     *
     * */
    public BusinessService getBusinessService(ServiceRequest serviceRequest) {
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

        if (CollectionUtils.isEmpty(response.getBusinessServices()))
            throw new CustomException("BUSINESSSERVICE_NOT_FOUND", "The businessService " + PGR_BUSINESSSERVICE + " is not found");

        return response.getBusinessServices().get(0);
    }


    /*
     * Call the workflow service with the given action and update the status
     * return the updated status of the application
     *
     * */
    public String updateWorkflowStatus(ServiceRequest serviceRequest) {
        ProcessInstance processInstance = getProcessInstanceForPGR(serviceRequest);
        ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(serviceRequest.getRequestInfo(), Collections.singletonList(processInstance));
        State state = callWorkFlow(workflowRequest);
        serviceRequest.getPgrEntity().getService().setApplicationStatus(state.getApplicationStatus());
        return state.getApplicationStatus();
    }


    public void validateAssignee(ServiceRequest serviceRequest) {
        /*
         * Call HRMS service and validate of the assignee belongs to same department
         * as the employee assigning it
         *
         * */

    }

    /**
     * Creates url for search based on given tenantId and businessservices
     *
     * @param tenantId        The tenantId for which url is generated
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


    public void enrichmentForSendBackToCititzen() {
        /*
         * If send bac to citizen action is taken assignes should be set to accountId
         *
         * */
    }


    public void enrichWorkflow(ServiceRequest request) {

        String tenantId = request.getPgrEntity().getService().getTenantId();
        String serviceRequestId = request.getPgrEntity().getService().getServiceRequestId();
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(request.getRequestInfo()).build();

        StringBuilder searchUrl = getprocessInstanceSearchURL(tenantId, serviceRequestId);
        Object result = repository.fetchResult(searchUrl, requestInfoWrapper);


        List<ProcessInstance> processInstances = new ArrayList<>();
        try {
            processInstances = mapper.convertValue(result, new TypeReference<List<ProcessInstance>>(){});
        } catch (IllegalArgumentException e) {
            throw new CustomException("PARSING ERROR", "Failed to parse response of workflow processInstance search");
        }

        if (CollectionUtils.isEmpty(processInstances))
            throw new CustomException("WORKFLOW_NOT_FOUND", "The workflow for serviceRequestId:  " + serviceRequestId + " is not found");

        Workflow workflow = getWorkflow(processInstances.get(0));
        request.getPgrEntity().setWorkflow(workflow);

    }

    /**
     * Enriches ProcessInstance Object for workflow
     *
     * @param serviceRequest
     */
    public ProcessInstance getProcessInstanceForPGR(ServiceRequest serviceRequest) {

        Service service = serviceRequest.getPgrEntity().getService();

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setBusinessId(service.getServiceRequestId());
        processInstance.setAction(serviceRequest.getPgrEntity().getWorkflow().getAction());
        processInstance.setModuleName(PGR_MODULENAME);
        processInstance.setTenantId(service.getTenantId());
        processInstance.setBusinessService(getBusinessService(serviceRequest).getBusinessService());
        processInstance.setDocuments(serviceRequest.getPgrEntity().getWorkflow().getVerificationDocuments());

        return processInstance;
    }

    /**
     *
     * @param processInstance
     */
    public Workflow getWorkflow(ProcessInstance processInstance) {

        List<String> userIds = null;

        if(!CollectionUtils.isEmpty(processInstance.getAssignes())){
            userIds = processInstance.getAssignes().stream().map(User::getUuid).collect(Collectors.toList());
        }

        Workflow workflow = Workflow.builder()
                .action(processInstance.getAction())
                .assignes(userIds)
                .comments(processInstance.getComment())
                .verificationDocuments(processInstance.getDocuments())
                .build();

        return workflow;
    }

    /**
     * Method to integrate with workflow
     * <p>
     * take the ProcessInstanceRequest as paramerter to call wf-service
     * <p>
     * and return wf-response to sets the resultant status
     */
    public State callWorkFlow(ProcessInstanceRequest workflowReq) {

        ProcessInstanceResponse response = null;
        StringBuilder url = new StringBuilder(pgrConfiguration.getWfHost().concat(pgrConfiguration.getWfTransitionPath()));
        Object optional = repository.fetchResult(url, workflowReq);
        response = mapper.convertValue(optional, ProcessInstanceResponse.class);
        return response.getProcessInstances().get(0).getState();
    }


    private StringBuilder getprocessInstanceSearchURL(String tenantId, String serviceRequestId) {

        StringBuilder url = new StringBuilder(pgrConfiguration.getWfHost());
        url.append(pgrConfiguration.getWfProcessInstanceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&businessIds=");
        url.append(serviceRequestId);
        return url;

    }


}
