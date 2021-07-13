package org.egov.echallan.expense.web.controllers;

import java.util.Arrays;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.echallan.expense.model.Expense;
import org.egov.echallan.expense.model.ExpenseRequest;
import org.egov.echallan.expense.model.ExpenseResponse;
import org.egov.echallan.expense.service.ExpenseService;
import org.egov.echallan.expense.util.ExpenseResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("expense/v1")
public class ExpenseController {

	@Autowired
	private ExpenseService expenseService;

	@Autowired
	private ExpenseResponseInfoFactory responseInfoFactory;

	@PostMapping("/_create")
	public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseRequest expenseRequest) {

		Expense expense = expenseService.create(expenseRequest);
		ResponseInfo resInfo = responseInfoFactory.createResponseInfoFromRequestInfo(expenseRequest.getRequestInfo(),
				true);
		ExpenseResponse response = ExpenseResponse.builder().expenses(Arrays.asList(expense)).responseInfo(resInfo)
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
