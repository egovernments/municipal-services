package org.egov.bpa.service;

import org.egov.bpa.repository.BPARepository;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.BPARepository;
//import org.egov.tl.service.CalculationService;
//import org.egov.tl.service.DiffService;
//import org.egov.tl.service.EnrichmentService;
//import org.egov.tl.service.UserService;
//import org.egov.tl.service.notification.EditNotificationService;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.validator.BPAValidator;
import org.egov.bpa.workflow.ActionValidator;
import org.egov.bpa.workflow.BPAWorkflowService;
import org.egov.bpa.workflow.WorkflowIntegrator;
import org.egov.bpa.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BPAService {
	
	@Autowired
	private BPARepository bpaRequestInfoDao;

	private WorkflowIntegrator wfIntegrator;

    private EnrichmentService enrichmentService;

    private UserService userService;

    private BPARepository repository;

    private ActionValidator actionValidator;

    private BPAValidator bpaValidator;

    private BPAWorkflowService TLWorkflowService;

    private BPAUtil util;

//    private DiffService diffService;

    private BPAConfiguration config;

    private WorkflowService workflowService;

//    private EditNotificationService  editNotificationService;
	public BPA create(BPARequest bpaRequest) {

		   Object mdmsData = util.mDMSCall(bpaRequest);
	        actionValidator.validateCreateRequest(bpaRequest);
	        enrichmentService.enrichBPACreateRequest(bpaRequest,mdmsData);
	        bpaValidator.validateCreate(bpaRequest,mdmsData);
	        userService.createUser(bpaRequest);
//	        calculationService.addCalculation(tradeLicenseRequest);
			
	        /*
			 * call workflow service if it's enable else uses internal workflow process
			 */
			if (config.getIsExternalWorkFlowEnabled())
				wfIntegrator.callWorkFlow(bpaRequest);
			repository.save(bpaRequest);
			return bpaRequest.getBPA();
	}
}