package org.egov.swcalculation.service;

import java.util.List;

import org.egov.swcalculation.model.Calculation;
import org.egov.swcalculation.model.CalculationReq;
import org.egov.swcalculation.model.CalculationRes;

public interface SWCalculationService {
	

	public List<Calculation> getCalculation(CalculationReq request);
	
	public void generateDemandBasedOnTimePeriod();
	
	public List<Calculation> getEstimation(CalculationReq request);
}
