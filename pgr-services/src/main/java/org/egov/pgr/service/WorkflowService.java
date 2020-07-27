package org.egov.pgr.service;

import org.egov.pgr.web.models.ServiceRequest;
import org.egov.pgr.web.models.workflow.BusinessService;

@org.springframework.stereotype.Service
public class WorkflowService {




    public BusinessService getBusinessService(ServiceRequest serviceRequest){
        /*
        *
        * Should return the applicable BusinessService for the given request
        *
        * */
        return null;
    }



    public String updateWorkflowStatus(ServiceRequest serviceRequest){

        /*
        * Call the workflow service with the given action and update the status
        * return the updated status of the application
        *
        * */
        return null;
    }


    public void validateAssignee(ServiceRequest serviceRequest){
        /*
        * Call HRMS service and validate of the assignee belongs to same department
        * as the employee assigning it
        *
        * */

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




}
