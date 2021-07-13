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


	private ExpenseValidator validator;

	private ExpenseCommonUtils utils;

	@Autowired
	public ExpenseService(ExpenseEnrichmentService enrichmentService, ExpenseUserService expenseUserService,
			ExpenseRepository repository, 
			ExpenseValidator validator, ExpenseCommonUtils utils) {
		this.enrichmentService = enrichmentService;
		this.expenseUserService = expenseUserService;
		this.repository = repository;
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
		expenseUserService.setAccountUser(request);
		repository.save(request);
		return request.getExpense();
	}

	public List<Expense> search(SearchCriteria criteria, RequestInfo requestInfo) {
		List<Expense> expenses = null;

		return expenses;
	}

}