package org.egov.echallan.expense.service;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.expense.model.Expense;
import org.egov.echallan.expense.model.ExpenseRequest;
import org.egov.echallan.expense.model.SearchCriteria;
import org.egov.echallan.expense.repository.ExpenseRepository;
import org.egov.echallan.expense.util.ExpenseCommonUtils;
import org.egov.echallan.expense.validator.ExpenseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

	@Autowired
	private ExpenseEnrichmentService enrichmentService;

	private ExpenseUserService expenseUserService;

	private ExpenseRepository repository;

	private ExpenseCalculationService expenseCalculationService;

	private ExpenseValidator validator;

	private ExpenseCommonUtils utils;

	@Autowired
	public ExpenseService(ExpenseEnrichmentService enrichmentService, ExpenseUserService expenseUserService,
			ExpenseRepository repository, ExpenseCalculationService expenseCalculationService,
			ExpenseValidator validator, ExpenseCommonUtils utils) {
		this.enrichmentService = enrichmentService;
		this.expenseUserService = expenseUserService;
		this.repository = repository;
		this.expenseCalculationService = expenseCalculationService;
		this.validator = validator;
		this.utils = utils;
	}

	/**
	 * Enriches the Request and pushes to the Queue
	 *
	 * @param request
	 *            ExpenseRequest containing list of expenses to be created
	 * @return Expense successfully created
	 */
	public Expense create(ExpenseRequest request) {
		Object mdmsData = utils.mDMSCall(request);
		validator.validateFields(request, mdmsData);
		enrichmentService.enrichCreateRequest(request);
		expenseUserService.setAccountUser(request);   //TODO have to set challan account id as vendor userid?
		repository.save(request);
		return request.getExpense();
	}

	public List<Expense> search(SearchCriteria criteria, RequestInfo requestInfo) {
		List<Expense> expenses = null;
		// enrichmentService.enrichSearchCriteriaWithAccountId(requestInfo,criteria);
		/*
		 * if(criteria.getMobileNumber()!=null){ challans =
		 * getChallansFromMobileNumber(criteria,requestInfo); } else { challans
		 * = getChallansWithOwnerInfo(criteria,requestInfo); }
		 */
		return expenses;
	}

	/*
	 * public List<Challan> getChallansFromMobileNumber(SearchCriteria criteria,
	 * RequestInfo requestInfo){ List<Challan> challans = new LinkedList<>();
	 * UserDetailResponse userDetailResponse =
	 * userService.getUser(criteria,requestInfo);
	 * if(CollectionUtils.isEmpty(userDetailResponse.getUser())){ return
	 * Collections.emptyList(); }
	 * enrichmentService.enrichSearchCriteriaWithOwnerids(criteria,
	 * userDetailResponse); challans = repository.getChallans(criteria);
	 * 
	 * if(CollectionUtils.isEmpty(challans)){ return Collections.emptyList(); }
	 * 
	 * criteria=enrichmentService.getChallanCriteriaFromIds(challans); challans
	 * = getChallansWithOwnerInfo(criteria,requestInfo); return challans; }
	 * 
	 * public List<Challan> getChallansWithOwnerInfo(SearchCriteria
	 * criteria,RequestInfo requestInfo){ List<Challan> challans =
	 * repository.getChallans(criteria); if(challans.isEmpty()) return
	 * Collections.emptyList(); challans =
	 * enrichmentService.enrichChallanSearch(challans,criteria,requestInfo);
	 * return challans; }
	 */

	/*
	 * public List<Challan> searchChallans(ChallanRequest request){
	 * SearchCriteria criteria = new SearchCriteria(); List<String> ids = new
	 * LinkedList<>(); ids.add(request.getChallan().getId());
	 * 
	 * criteria.setTenantId(request.getChallan().getTenantId());
	 * criteria.setIds(ids);
	 * criteria.setBusinessService(request.getChallan().getBusinessService());
	 * 
	 * List<Challan> challans = repository.getChallans(criteria);
	 * 
	 * if(challans.isEmpty()) return Collections.emptyList(); challans =
	 * enrichmentService.enrichChallanSearch(challans,criteria,request.
	 * getRequestInfo()); return challans; }
	 */
	/*
	 * public Challan update(ChallanRequest request) { Object mdmsData =
	 * utils.mDMSCall(request); validator.validateFields(request, mdmsData);
	 * List<Challan> searchResult = searchChallans(request);
	 * validator.validateUpdateRequest(request,searchResult);
	 * enrichmentService.enrichUpdateRequest(request);
	 * calculationService.addCalculation(request); repository.update(request);
	 * return request.getChallan(); }
	 */

}