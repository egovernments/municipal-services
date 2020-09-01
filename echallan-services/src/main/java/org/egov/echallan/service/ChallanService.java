package org.egov.echallan.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.model.Challan;
import org.egov.echallan.model.ChallanRequest;
import org.egov.echallan.model.SearchCriteria;
import org.egov.echallan.repository.ChallanRepository;
import org.egov.echallan.web.models.user.UserDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ChallanService {

    @Autowired
    private EnrichmentService enrichmentService;

    private UserService userService;
    
    private ChallanRepository repository;
    
    private CalculationService calculationService;
    
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
		enrichmentService.enrichCreateRequest(request);
		userService.createUser(request);
		calculationService.addCalculation(request);
		repository.save(request);
		return request.getChallan();
	}
	
	
	 public List<Challan> search(SearchCriteria criteria, RequestInfo requestInfo){
	        List<Challan> challans;
	        enrichmentService.enrichSearchCriteriaWithAccountId(requestInfo,criteria);
	         if(criteria.getMobileNumber()!=null){
	        	 challans = getChallansFromMobileNumber(criteria,requestInfo);
	         }
	         else {
	        	 challans = getChallansWithOwnerInfo(criteria,requestInfo);
	         }
	       return challans;
	    }
	 
	 public List<Challan> getChallansFromMobileNumber(SearchCriteria criteria, RequestInfo requestInfo){
		 List<Challan> challans = new LinkedList<>();
	        UserDetailResponse userDetailResponse = userService.getUser(criteria,requestInfo);
	        if(userDetailResponse.getUser().size()==0){
	            return Collections.emptyList();
	        }
	        enrichmentService.enrichSearchCriteriaWithOwnerids(criteria,userDetailResponse);
	        challans = repository.getChallans(criteria);

	        if(challans.size()==0){
	            return Collections.emptyList();
	        }

	        criteria=enrichmentService.getChallanCriteriaFromIds(challans);
	        challans = getChallansWithOwnerInfo(criteria,requestInfo);
	        return challans;
	    }
	 
	 public List<Challan> getChallansWithOwnerInfo(SearchCriteria criteria,RequestInfo requestInfo){
	        List<Challan> challans = repository.getChallans(criteria);
	        if(challans.isEmpty())
	            return Collections.emptyList();
	        challans = enrichmentService.enrichChallanSearch(challans,criteria,requestInfo);
	        return challans;
	    }

	
}