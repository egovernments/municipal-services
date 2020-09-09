package org.egov.echallan.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.echallan.model.Amount;
import org.egov.echallan.model.Challan;
import org.egov.echallan.model.ChallanRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

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

         if (!errorMap.isEmpty())
        	 throw new CustomException(errorMap);
        
		
	}
}
