package org.egov.bpa.workflow;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.workflow.BusinessService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.egov.bpa.util.BPAConstants.*;


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
     * @param request The tradeLicense Create request
     */
	public void validateCreateRequest(BPARequest request){
        Map<String,String> errorMap = new HashMap<>();

//        BPA bpa = request.getBPA();
//        if(ACTION_INITIATE.equalsIgnoreCase(bpa.getAction())){
//            if(license.getTradeLicenseDetail().getApplicationDocuments()!=null)
//                errorMap.put("INVALID ACTION","Action should be APPLY when application document are provided");
//        }
//        if(ACTION_APPLY.equalsIgnoreCase(bpa.getAction())){
//            if(license.getTradeLicenseDetail().getApplicationDocuments()==null)
//                errorMap.put("INVALID ACTION","Action cannot be changed to APPLY. Application document are not provided");
//        }
//        if(!ACTION_APPLY.equalsIgnoreCase(bpa.getAction()) &&
//                !ACTION_INITIATE.equalsIgnoreCase(bpa.getAction())){
//            errorMap.put("INVALID ACTION","Action can only be APPLY or INITIATE during create");
//        }

        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


    /**
     * Validates the update request
     * @param request The BPA update request
     */
    public void validateUpdateRequest(BPARequest request,BusinessService businessService){
        validateDocumentsForUpdate(request);
       // validateRole(request);
       // validateAction(request);
        validateIds(request,businessService);
    }


    /**
     * Validates the applicationDocument
     * @param request The tradeLciense create or update request
     */
    private void validateDocumentsForUpdate(BPARequest request){
        Map<String,String> errorMap = new HashMap<>();
        BPA bpa = request.getBPA();
//        if(ACTION_INITIATE.equalsIgnoreCase(bpa.getAction())){
//            if(license.getTradeLicenseDetail().getApplicationDocuments()!=null)
//                errorMap.put("INVALID STATUS","Status cannot be INITIATE when application document are provided");
//        }
//        if(ACTION_APPLY.equalsIgnoreCase(bpa.getAction())){
//            if(license.getTradeLicenseDetail().getApplicationDocuments()==null)
//                errorMap.put("INVALID STATUS","Status cannot be APPLY when application document are not provided");
//        }

        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


    /**
     * Validates if the role of the logged in user can perform the given action
     * @param request The tradeLciense create or update request
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


//          if(!actions.contains(bpa.getAction().toString()))
//              errorMap.put("UNAUTHORIZED UPDATE","The action cannot be performed by this user");

       if(!errorMap.isEmpty())
           throw new CustomException(errorMap);
    }


    /**
     * Validate if the action can be performed on the current status
     * @param request The tradeLciense update request
     */
    private void validateAction(BPARequest request){
       Map<String,List<String>> actionStatusMap = workflowConfig.getActionCurrentStatusMap();
        Map<String,String> errorMap = new HashMap<>();
        BPA bpa = request.getBPA();
//        request.getLicenses().forEach(license -> {
//           if(actionStatusMap.get(bpa.getStatus().toString())!=null){
//               if(!actionStatusMap.get(bpa.getStatus().toString()).contains(bpa.getAction().toString()))
//                   errorMap.put("UNAUTHORIZED ACTION","The action "+bpa.getAction() +" cannot be applied on the status "+license.getStatus());
//               }
//       });
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


    /**
     * Validates if the any new object is added in the request
     * @param request The tradeLciense update request
     */
    private void validateIds(BPARequest request,BusinessService businessService){
        Map<String,String> errorMap = new HashMap<>();
        BPA bpa = request.getBPA();
//        request.getLicenses().forEach(license -> {
//            if( !workflowService.isStateUpdatable(license.getStatus(), businessService)) {
//                if (license.getId() == null)
//                    errorMap.put("INVALID UPDATE", "Id of tradeLicense cannot be null");
//                if(license.getTradeLicenseDetail().getId()==null)
//                    errorMap.put("INVALID UPDATE", "Id of tradeLicenseDetail cannot be null");
//                if(license.getTradeLicenseDetail().getAddress()==null)
//                    errorMap.put("INVALID UPDATE", "Id of address cannot be null");
//                license.getTradeLicenseDetail().getOwners().forEach(owner -> {
//                    if(owner.getUuid()==null)
//                        errorMap.put("INVALID UPDATE", "Id of owner cannot be null");
//                    if(!CollectionUtils.isEmpty(owner.getDocuments())){
//                        owner.getDocuments().forEach(document -> {
//                            if(document.getId()==null)
//                                errorMap.put("INVALID UPDATE", "Id of owner document cannot be null");
//                        });
//                      }
//                    });
//                license.getTradeLicenseDetail().getTradeUnits().forEach(tradeUnit -> {
//                    if(tradeUnit.getId()==null)
//                        errorMap.put("INVALID UPDATE", "Id of tradeUnit cannot be null");
//                });
//                if(!CollectionUtils.isEmpty(license.getTradeLicenseDetail().getAccessories())){
//                    license.getTradeLicenseDetail().getAccessories().forEach(accessory -> {
//                        if(accessory.getId()==null)
//                            errorMap.put("INVALID UPDATE", "Id of accessory cannot be null");
//                    });
//                }
//                if(!CollectionUtils.isEmpty(license.getTradeLicenseDetail().getApplicationDocuments())){
//                    license.getTradeLicenseDetail().getApplicationDocuments().forEach(document -> {
//                        if(document.getId()==null)
//                            errorMap.put("INVALID UPDATE", "Id of applicationDocument cannot be null");
//                    });
//                }
//            }
//        });
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }





}
