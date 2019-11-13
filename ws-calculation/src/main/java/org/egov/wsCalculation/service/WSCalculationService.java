package org.egov.wsCalculation.service;



import org.egov.wsCalculation.model.CalculationReq;

import java.util.List;

import org.egov.wsCalculation.model.Calculation;

public interface WSCalculationService {
	
	public List<Calculation> getTaxCalculation(CalculationReq calculationReq);

}
