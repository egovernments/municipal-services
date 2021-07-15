package org.egov.echallan.expense.validator;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.model.Challan;
import org.egov.echallan.model.ChallanRequest;
import org.egov.echallan.model.RequestInfoWrapper;
import org.egov.echallan.repository.ServiceRequestRepository;
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
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	public void validateFields(ChallanRequest request, Object mdmsData) {
		Challan challan = request.getChallan();
		Map<String, String> errorMap = new HashMap<>();

		if (isBlank(challan.getTypeOfExpense()))
			errorMap.put("BLANK_TypeOfExpense", "TypeOfExpense is manadatory");

		if (isBlank(challan.getVendor()))
			errorMap.put("BLANK_Vendor", "Vendor is mandatory");
		else {
			Vendor vendor = (Vendor) validateVendor(challan.getVendor(), challan.getTenantId(),
					request.getRequestInfo());
			if (isNull(vendor))
				errorMap.put("INVALID_Vendor", "Vendor does not exists with id :" + challan.getVendor());
			else {
				challan.setAccountId(vendor.getOwnerId());
			}
		}
		Long currentTime = System.currentTimeMillis();
		if (isNull(challan.getBillDate()))
			errorMap.put("NULL_BillDate", "Bill date is mandatory");
		else if( challan.getBillDate()>currentTime)
			errorMap.put("BillDate_CurrentDate","Bill date should be before current date");
		else if (isNull(challan.getBillIssuedDate()) &&  challan.getBillIssuedDate()> challan.getBillDate()) 
				errorMap.put("BillIssuedDate_After_BillDate", " Party bill date should be before bill date.");
	
		if (challan.getIsBillPaid() && isNull(challan.getPaidDate()))
			errorMap.put("NULL_PaidDate","Paid date is mandatory");
	
		if (challan.getIsBillPaid() && (!isNull(challan.getPaidDate())) 
				&& (!Objects.isNull(challan.getBillDate()))  && challan.getPaidDate() < challan.getBillDate()) 
			errorMap.put("PaidDate_Before_BillDate","Paid date should be after billdate");
	
		if (challan.getIsBillPaid() && (!isNull(challan.getPaidDate())) && challan.getPaidDate()  >currentTime)	
			errorMap.put("PaidDate_CurrentDate"," Paid date should be before current date");

		
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
		else {
			challan.setTaxPeriodTo(challan.getBillIssuedDate());
			challan.setTaxPeriodFrom(challan.getBillIssuedDate());
		}

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

			LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
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

}
