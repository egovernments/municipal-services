package org.egov.bpa.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.BPARepository;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.validator.BPAValidator;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.Difference;
import org.egov.bpa.workflow.ActionValidator;
import org.egov.bpa.workflow.BPAWorkflowService;
import org.egov.bpa.workflow.WorkflowIntegrator;
import org.egov.bpa.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BPAService {
	
	@Autowired
	private BPARepository bpaRequestInfoDao;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
    private EnrichmentService enrichmentService;

	@Autowired
    private EDCRService edcrService;

	@Autowired
    private UserService userService;

	@Autowired
    private BPARepository repository;

    @Autowired
    private ActionValidator actionValidator;

    @Autowired
    private BPAValidator bpaValidator;

    @Autowired
    private BPAWorkflowService BPAWorkflowService;

    @Autowired
    private BPAUtil util;

    @Autowired
    private DiffService diffService;

    @Autowired
    private BPAConfiguration config;

    @Autowired
    private WorkflowService workflowService;
    
	public BPA create(BPARequest bpaRequest) {

		   Object mdmsData = util.mDMSCall(bpaRequest);
		   /*if( !edcrService.validateEdcrPlan(bpaRequest)) {
			   throw new CustomException("INVALID EDCR NUMBER",
						"The Scrutiny is not accepted for the EDCR Number "
								+ bpaRequest.getBPA().getEdcrNumber() );
		   }*/
		    bpaValidator.validateCreate(bpaRequest,mdmsData);
	        actionValidator.validateCreateRequest(bpaRequest);
	        enrichmentService.enrichBPACreateRequest(bpaRequest,mdmsData);
	       
	        userService.createUser(bpaRequest);
			
	        /*
			 * call workflow service if it's enable else uses internal workflow process
			 */
			if (config.getIsExternalWorkFlowEnabled())
				wfIntegrator.callWorkFlow(bpaRequest);
			repository.save(bpaRequest);
			return bpaRequest.getBPA();
	}
	

	
	
	
	  /**
     * Updates the bpa
     * @param bpaRequest The update Request
     * @return Updated bpa
     */
    public BPA update(BPARequest bpaRequest){
        Object mdmsData = util.mDMSCall(bpaRequest);
     
        List<BPA> searchResult = getBPAWithOwnerInfo(bpaRequest); 
        bpaValidator.validateUpdate(bpaRequest,searchResult,mdmsData);
        Map<String,Difference> diffMap = diffService.getDifference(bpaRequest,searchResult);
        userService.createUser(bpaRequest);
        repository.update(bpaRequest);
        return bpaRequest.getBPA();
    }
    
    
    /**
     * Returns bpa from db for the update request
     * @param request The update request
     * @return List of bpas
     */
    public List<BPA> getBPAWithOwnerInfo(BPARequest request){
       BPASearchCriteria criteria = new BPASearchCriteria();
        List<String> ids = new LinkedList<>();
        	ids.add( request.getBPA().getId());

        criteria.setTenantId(request.getBPA().getTenantId());
        criteria.setIds(ids);
        
        List<BPA> bpa = repository.getBPAData(criteria);
        
        bpa = enrichmentService.enrichBPASearch(bpa,criteria,request.getRequestInfo());
        return bpa;
    }
}