package org.egov.echallan.expense.validator;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.expense.model.Expense;
import org.egov.echallan.expense.model.Expense.StatusEnum;
import org.egov.echallan.expense.model.ExpenseRequest;
import org.egov.echallan.expense.repository.ExpenseServiceRequestRepository;
import org.egov.echallan.model.RequestInfoWrapper;
import org.egov.echallan.web.models.vendor.Vendor;
import org.egov.echallan.web.models.vendor.VendorResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ExpenseValidator {

	@Autowired
	private ChallanConfiguration config;

	@Autowired
	private ExpenseServiceRequestRepository expenseServiceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	public void validateFields(ExpenseRequest request, Object mdmsData) {
		Expense expense = request.getExpense();
		Map<String, String> errorMap = new HashMap<>();

		if (expense.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0)
			errorMap.put("Zero amount", "Expense amount can not be zero amount");

		if (isBlank(expense.getTypeOfExpense()))
			errorMap.put("BLANK_TypeOfExpense", "TypeOfExpense is manadatory");

		if (isBlank(expense.getVendor()))
			errorMap.put("BLANK_Vendor", "Vendor is mandatory");
		else {
			Vendor vendor = (Vendor) validateVendor(expense.getVendor(), expense.getTenantId(),
					request.getRequestInfo());
			if (isNull(vendor))
				errorMap.put("INVALID_Vendor", "Vendor does not exists with id :" + expense.getVendor());
			else {
				expense.setAccountId(vendor.getOwnerId());
			}
		}
		Long currentTime = System.currentTimeMillis();
		if (isNull(expense.getBillDate()))
			errorMap.put("NULL_BillDate", "Bill date is mandatory");
		else if( expense.getBillDate()>currentTime)
			errorMap.put("BillDate_CurrentDate","Bill date should be before current date");
		else if (isNull(expense.getBillIssuedDate()) &&  expense.getBillIssuedDate()> expense.getBillDate()) 
				errorMap.put("BillIssuedDate_After_BillDate", " Party bill date should be before bill date.");
	
		if (expense.getIsBillPaid() && isNull(expense.getPaidDate()))
			errorMap.put("NULL_PaidDate","Paid date is mandatory");
	
		if (expense.getIsBillPaid() && (!isNull(expense.getPaidDate())) 
				&& (!Objects.isNull(expense.getBillDate()))  && expense.getPaidDate() < expense.getBillDate()) 
			errorMap.put("PaidDate_Before_BillDate","Paid date should be after billdate");
	
		if (expense.getIsBillPaid() && (!isNull(expense.getPaidDate())) && expense.getPaidDate()  >currentTime)	
			errorMap.put("PaidDate_CurrentDate"," Paid date should be before current date");
				
		
		if (isBlank(expense.getBusinessService()))
			errorMap.put("NULL_BusinessService", " Business Service cannot be null");

		if (!expense.getTenantId().equalsIgnoreCase(request.getRequestInfo().getUserInfo().getTenantId()))
			errorMap.put("Invalid Tenant", "Invalid tenant id");
		
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

	}

	private Object validateVendor(String vendor, String tenantId, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder(config.getVendorHost()).append(config.getVendorContextPath())
				.append(config.getVendorSearchEndpoint()).append("?tenantId=").append(tenantId);
		if (!isEmpty(vendor)) {
			uri.append("&ids=").append(vendor);
		}

		RequestInfoWrapper requestInfoWrpr = new RequestInfoWrapper();
		requestInfoWrpr.setRequestInfo(requestInfo);
		try {

			LinkedHashMap responseMap = (LinkedHashMap) expenseServiceRequestRepository.fetchResult(uri,
					requestInfoWrpr);
			VendorResponse vendorResponse = mapper.convertValue(responseMap, VendorResponse.class);
			if (!CollectionUtils.isEmpty(vendorResponse.getVendor())) {
				return vendorResponse.getVendor().get(0);
			} else {
				return null;
			}

		} catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException", "ObjectMapper convert to vendor");
		}

	}


	public void validateUpdateRequest(ExpenseRequest request, List<Expense> searchResult) {
		Expense expense = request.getExpense();
		Map<String, String> errorMap = new HashMap<>();
		if (searchResult.size() == 0)
			errorMap.put("INVALID_UPDATE_REQ_NOT_EXIST", "The Expense to be updated is not in database");
		Expense searchExpense = searchResult.get(0);
		if (!expense.getBusinessService().equalsIgnoreCase(searchExpense.getBusinessService()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_BSERVICE",
					"The business service is not matching with the Search result");
		if (!expense.getChallanNo().equalsIgnoreCase(searchExpense.getChallanNo()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_CHALLAN_NO",
					"The Challan Number is not matching with the Search result");
		if (!expense.getCitizen().getUuid().equalsIgnoreCase(searchExpense.getCitizen().getUuid()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_ADDRESS", "User Details not matching with the Search result");
		if (!expense.getCitizen().getName().equalsIgnoreCase(searchExpense.getCitizen().getName()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_NAME", "User Details not matching with the Search result");
		if (searchExpense.getApplicationStatus() != StatusEnum.ACTIVE)
			errorMap.put("INVALID_UPDATE_REQ_CHALLAN_INACTIVE", "Challan cannot be updated/cancelled");
		if (!expense.getTenantId().equalsIgnoreCase(request.getRequestInfo().getUserInfo().getTenantId()))
			errorMap.put("INVALID_UPDATE_REQ_INVALID_TENANTID", "Invalid tenant id");
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

	}
}
