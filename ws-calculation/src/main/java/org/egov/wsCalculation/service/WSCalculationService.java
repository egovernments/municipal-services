package org.egov.wsCalculation.service;



import org.egov.wsCalculation.model.CalculationReq;
import org.egov.wsCalculation.model.CalculationRes;

import java.util.List;

import org.egov.wsCalculation.model.Calculation;

public interface WSCalculationService {
	
	public CalculationRes getTaxCalculation(CalculationReq calculationReq);

}
