package org.egov.bpa.workflow;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.producer.Producer;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import static org.egov.bpa.util.BPAConstants.*;


@Service
public class BPAWorkflowService {

    private ActionValidator actionValidator;
    private Producer producer;
    private BPAConfiguration config;
    private WorkflowConfig workflowConfig;

    @Autowired
    public BPAWorkflowService(ActionValidator actionValidator, Producer producer, BPAConfiguration config,WorkflowConfig workflowConfig) {
        this.actionValidator = actionValidator;
        this.producer = producer;
        this.config = config;
        this.workflowConfig = workflowConfig;
    }


    /**
     * Validates and updates the status
     * @param request The update tradeLicense Request
     */
    public void updateStatus(BPARequest request){
        actionValidator.validateUpdateRequest(request,null);
        changeStatus(request);
    }


    /**
     * Changes the status of the tradeLicense according to action status mapping
     * @param request The update tradeLicenseRequest
     */
    private void changeStatus(BPARequest request){
       Map<String,String> actionToStatus =  workflowConfig.getActionStatusMap();
//       request.getLicenses().forEach(license -> {
//             license.setStatus(actionToStatus.get(license.getAction()));
//             if(license.getAction().equalsIgnoreCase(ACTION_APPROVE)){
//                 Long time = System.currentTimeMillis();
//                 license.setIssuedDate(time);
//                 license.setValidFrom(time);
//             }
//       });
    }

}
