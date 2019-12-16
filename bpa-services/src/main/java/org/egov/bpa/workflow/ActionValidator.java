package org.egov.bpa.workflow;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAConstants.*;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.workflow.BusinessService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class ActionValidator {


    private WorkflowConfig workflowConfig;

    private WorkflowService workflowService;

    @Autowired
    public ActionValidator(WorkflowConfig workflowConfig, WorkflowService workflowService) {
        this.workflowConfig = workflowConfig;
        this.workflowService = workflowService;
    }




    /**
     * Validates create request
     * @param request The BPA Create request
     */
	public void validateCreateRequest(BPARequest request){
        Map<String,String> errorMap = new HashMap<>();

        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


    /**
     * Validates the update request
     * @param request The BPA update request
     */
    public void validateUpdateRequest(BPARequest request,BusinessService businessService){
        validateDocumentsForUpdate(request);
//        validateRole(request);
        validateAction(request);
        validateIds(request,businessService);
    }


    /**
     * Validates the applicationDocument
     * @param request The bpa create or update request
     */
    private void validateDocumentsForUpdate(BPARequest request){
        Map<String,String> errorMap = new HashMap<>();
        BPA bpa = request.getBPA();
        if(BPAConstants.ACTION_INITIATE.equalsIgnoreCase(bpa.getAction())){
            if(bpa.getDocuments()!=null)
                errorMap.put("INVALID STATUS","Status cannot be INITIATE when application document are provided");
        }
        if(BPAConstants.ACTION_APPLY.equalsIgnoreCase(bpa.getAction())){
            if(bpa.getDocuments()==null)
                errorMap.put("INVALID STATUS","Status cannot be APPLY when application document are not provided");
        }

        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


    /**
     * Validates if the role of the logged in user can perform the given action
     * @param request The bpa create or update request
     */
    private void validateRole(BPARequest request){
       Map<String,List<String>> roleActionMap = workflowConfig.getRoleActionMap();
       Map<String,String> errorMap = new HashMap<>();
       BPA bpa = request.getBPA();
       RequestInfo requestInfo = request.getRequestInfo();
       List<Role> roles = requestInfo.getUserInfo().getRoles();

       List<String> actions = new LinkedList<>();
       roles.forEach(role -> {
           if(!CollectionUtils.isEmpty(roleActionMap.get(role.getCode())))
           {
               actions.addAll(roleActionMap.get(role.getCode()));}
       });


       if(!errorMap.isEmpty())
           throw new CustomException(errorMap);
    }


    /**
     * Validate if the action can be performed on the current status
     * @param request The bpa update request
     */
    private void validateAction(BPARequest request){
       Map<String,List<String>> actionStatusMap = workflowConfig.getActionCurrentStatusMap();
        Map<String,String> errorMap = new HashMap<>();
        BPA bpa = request.getBPA();
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


    /**
     * Validates if the any new object is added in the request
     * @param request The bpa update request
     */
    private void validateIds(BPARequest request,BusinessService businessService){
        Map<String,String> errorMap = new HashMap<>();
        BPA bpa = request.getBPA();
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }



}
