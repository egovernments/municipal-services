package org.egov.echallan.service;

import org.egov.echallan.model.Challan;
import org.egov.echallan.model.ChallanRequest;
import org.egov.echallan.repository.ChallanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ChallanService {

    @Autowired
    private EnrichmentService enrichmentService;

//    @Autowired
//    private ChallanValidator challanValidator;

    @Autowired
    private UserService userService;
    
    private ChallanRepository repository;
    
    private CalculationService calculationService;
    

//    @Autowired
//	private WorkflowService wfService;


    @Autowired
    public ChallanService(EnrichmentService enrichmentService, UserService userService,ChallanRepository repository,CalculationService calculationService ) {
        this.enrichmentService = enrichmentService;
        this.userService = userService;
        this.repository = repository;
        this.calculationService = calculationService;
    }
    
    
	/**
	 * Enriches the Request and pushes to the Queue
	 *
	 * @param request ChallanRequest containing list of challans to be created
	 * @return Challan successfully created
	 */
	public Challan create(ChallanRequest request) {

		//challanValidator.validateCreateRequest(request);
		enrichmentService.enrichCreateRequest(request);
		userService.createUser(request);
		calculationService.addCalculation(request);
		repository.save(request);
		return request.getChallan();
	}
	
	
}