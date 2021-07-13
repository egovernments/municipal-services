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
import org.egov.echallan.expense.model.Amount;
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
	private ExpenseServiceRequestRepository expenseserviceRequestRepository;

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
		if (Objects.isNull(expense.getBillDate()))
			errorMap.put("NULL_BillDate", "Bill date is mandatory");
		else if( expense.getBillDate()>currentTime)
			errorMap.put("BillDate_CurrentDate","Bill date should be before current date");
		else if (!Objects.isNull(expense.getBillIssuedDate()) &&  expense.getBillIssuedDate()> expense.getBillDate()) 
				errorMap.put("BillIssuedDate_After_BillDate", " Party bill date should be before bill date.");
	
		if (expense.getIsBillPaid() && Objects.isNull(expense.getPaidDate()))
			errorMap.put("NULL_PaidDate","Paid date is mandatory");
	
		if (expense.getIsBillPaid() && (!Objects.isNull(expense.getPaidDate())) 
				&& (!Objects.isNull(expense.getBillDate()))  && expense.getPaidDate() < expense.getBillDate()) 
			errorMap.put("PaidDate_Before_BillDate","Paid date should be after billdate");
	
		if (expense.getIsBillPaid() && (!Objects.isNull(expense.getPaidDate())) && expense.getPaidDate()  >currentTime)	
			errorMap.put("PaidDate_CurrentDate"," Paid date should be before current date");
				
		
		if (isBlank(expense.getBusinessService()))
			errorMap.put("NULL_BusinessService", " Business Service cannot be null");

		if (!expense.getTenantId().equalsIgnoreCase(request.getRequestInfo().getUserInfo().getTenantId()))
			errorMap.put("Invalid Tenant", "Invalid tenant id");
		
		

		// Not applicable for expense

	/*	if (expense.getTaxPeriodFrom() == null)
			errorMap.put("NULL_Fromdate", " From date cannot be null");
		if (expense.getTaxPeriodTo() == null)
			errorMap.put("NULL_Todate", " To date cannot be null");

		Boolean validFinancialYear = false;
		if (expense.getTaxPeriodTo() != null && expense.getTaxPeriodFrom() != null) {
			for (Map<String, Object> financialYearProperties : taxPeriods) {
				Long startDate = (Long) financialYearProperties.get(MDMS_STARTDATE);
				Long endDate = (Long) financialYearProperties.get(MDMS_ENDDATE);
				if (expense.getTaxPeriodFrom() < expense.getTaxPeriodTo() && expense.getTaxPeriodFrom() >= startDate
						&& expense.getTaxPeriodTo() <= endDate)
					validFinancialYear = true;
			}
		}

		if (!validFinancialYear)
			errorMap.put("Invalid TaxPeriod", "Tax period details are invalid");

		List<String> localityCodes = getLocalityCodes(expense.getTenantId(), request.getRequestInfo());

		if (!localityCodes.contains(expense.getAddress().getLocality().getCode()))
			errorMap.put("Invalid Locality", "Locality details are invalid");

		if (!currentTaxHeadCodes.isEmpty() && !requiredTaxHeadCodes.isEmpty()) {
			if (!currentTaxHeadCodes.containsAll(requiredTaxHeadCodes))
				errorMap.put("INAVLID_TAXHEAD_CODE_DETAILS",
						"Mandatory taxhead codes details are not present in request for provided business service");
		} else
			errorMap.put("INAVLID_TAXHEAD_CODE_DETAILS",
					"Taxhead codes details are not present in request or in mdms records for provided business service");
*/
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

			LinkedHashMap responseMap = (LinkedHashMap) expenseserviceRequestRepository.fetchResult(uri,
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

/*	public List<String> getLocalityCodes(String tenantId, RequestInfo requestInfo) {
		StringBuilder builder = new StringBuilder(config.getBoundaryHost());
		builder.append(config.getFetchBoundaryEndpoint());
		builder.append("?tenantId=");
		builder.append(tenantId);
		builder.append("&hierarchyTypeCode=");
		builder.append(HIERARCHY_CODE);
		builder.append("&boundaryType=");
		builder.append(BOUNDARY_TYPE);

		Object result = expenseserviceRequestRepository.fetchResult(builder, new RequestInfoWrapper(requestInfo));

		List<String> codes = JsonPath.read(result, LOCALITY_CODE_PATH);
		return codes;
	}*/

	public void validateUpdateRequest(ExpenseRequest request, List<Expense> searchResult) {
		Expense expense = request.getExpense();
		Map<String, String> errorMap = new HashMap<>();
		if (searchResult.size() == 0)
			errorMap.put("INVALID_UPDATE_REQ_NOT_EXIST", "The Challan to be updated is not in database");
		Expense searchchallan = searchResult.get(0);
		if (!expense.getBusinessService().equalsIgnoreCase(searchchallan.getBusinessService()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_BSERVICE",
					"The business service is not matching with the Search result");
		if (!expense.getChallanNo().equalsIgnoreCase(searchchallan.getChallanNo()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_CHALLAN_NO",
					"The Challan Number is not matching with the Search result");
		/*if (!expense.getAddress().getId().equalsIgnoreCase(searchchallan.getAddress().getId()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_ADDRESS", "Address is not matching with the Search result");*/
		if (!expense.getCitizen().getUuid().equalsIgnoreCase(searchchallan.getCitizen().getUuid()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_ADDRESS", "User Details not matching with the Search result");
		if (!expense.getCitizen().getName().equalsIgnoreCase(searchchallan.getCitizen().getName()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_NAME", "User Details not matching with the Search result");
		/*if (!expense.getCitizen().getMobileNumber().equalsIgnoreCase(searchchallan.getCitizen().getMobileNumber()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_MOBILENO", "User Details not matching with the Search result");*/
		if (searchchallan.getApplicationStatus() != StatusEnum.ACTIVE)
			errorMap.put("INVALID_UPDATE_REQ_CHALLAN_INACTIVE", "Challan cannot be updated/cancelled");
		if (!expense.getTenantId().equalsIgnoreCase(request.getRequestInfo().getUserInfo().getTenantId()))
			errorMap.put("INVALID_UPDATE_REQ_INVALID_TENANTID", "Invalid tenant id");
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

	}
}
