package org.egov.echallan.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.echallan.model.Amount;
import org.egov.echallan.model.Challan;
import org.egov.echallan.model.ChallanRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import static org.egov.echallan.util.ChallanConstants.*;

@Component
public class ChallanValidator {

	
	public void validateFields(ChallanRequest request) {
		 Challan challan = request.getChallan();
         Map<String, String> errorMap = new HashMap<>();

		 List<Amount> entAmount = challan.getAmount();
		 int totalAmt = 0;
		for (Amount amount : entAmount) {
			totalAmt+=amount.getAmount().intValue();
		}
		if(totalAmt <= 0) {
			errorMap.put("Zero amount","Challan cannot be generated for zero amount");
		}
		 if (challan.getCitizen().getMobileNumber() == null)
            errorMap.put("NULL_Mobile Number", " Mobile Number cannot be null");
         if (challan.getBusinessService() == null)
            errorMap.put("NULL_BusinessService", " Business Service cannot be null");
         if (challan.getTaxPeriodFrom() == null)
            errorMap.put("NULL_Fromdate", " From date cannot be null");
         if (challan.getTaxPeriodTo() == null)
            errorMap.put("NULL_Todate", " To date cannot be null");
         if(!challan.getTenantId().equalsIgnoreCase(request.getRequestInfo().getUserInfo().getTenantId()))
        	 errorMap.put("Invalid Tenant", "Invalid tenant id");
         if (!errorMap.isEmpty())
        	 throw new CustomException(errorMap);
        
		
	}

	public void validateUpdateRequest(ChallanRequest request, List<Challan> searchResult) {
		Challan challan = request.getChallan();
		Map<String, String> errorMap = new HashMap<>();
		if (searchResult.size() == 0)
			errorMap.put("INVALID UPDATE", "The Challan to be updated is not in database");
		Challan searchchallan = searchResult.get(0);
		System.out.println("searchchallan.getApplicationStatus()==="+searchchallan.getApplicationStatus());
		if(!challan.getBusinessService().equalsIgnoreCase(searchchallan.getBusinessService()))
			errorMap.put("INVALID UPDATE", "The business service is not matching with the Search result");
		if(!challan.getChallanNo().equalsIgnoreCase(searchchallan.getChallanNo()))
			errorMap.put("INVALID UPDATE", "The Challan Number is not matching with the Search result");
		if(!challan.getAddress().getId().equalsIgnoreCase(searchchallan.getAddress().getId()))
			errorMap.put("INVALID UPDATE", "Address is not matching with the Search result");
		if(!challan.getCitizen().getUuid().equalsIgnoreCase(searchchallan.getCitizen().getUuid()))
			errorMap.put("INVALID UPDATE", "User Details not matching with the Search result");
		if(!challan.getCitizen().getName().equalsIgnoreCase(searchchallan.getCitizen().getName()))
			errorMap.put("INVALID UPDATE", "User Details not matching with the Search result");
		if(!challan.getCitizen().getMobileNumber().equalsIgnoreCase(searchchallan.getCitizen().getMobileNumber()))
			errorMap.put("INVALID UPDATE", "User Details not matching with the Search result");
		if(!searchchallan.getApplicationStatus().equalsIgnoreCase(STATUS_ACTIVE))
			errorMap.put("INVALID UPDATE", "Challan cannot be updated/cancelled");
		if(!challan.getTenantId().equalsIgnoreCase(request.getRequestInfo().getUserInfo().getTenantId()))
       	 	errorMap.put("Invalid Tenant", "Invalid tenant id");
		if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
		
	}
}
