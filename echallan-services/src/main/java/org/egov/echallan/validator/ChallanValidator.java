package org.egov.echallan.validator;

import java.util.List;

import org.egov.echallan.model.Amount;
import org.egov.echallan.model.Challan;
import org.egov.echallan.model.ChallanRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

@Component
public class ChallanValidator {

	
	public void validateFields(ChallanRequest request) {
		 Challan challan = request.getChallan();
		 List<Amount> entAmount = challan.getAmount();
		 int totalAmt = 0;
		for (Amount amount : entAmount) {
			totalAmt+=amount.getAmount().intValue();
		}
		if(totalAmt <= 0) {
			throw new CustomException("Zero amount","Challan cannot be generated for zero amount");
		}
		
	}
}
