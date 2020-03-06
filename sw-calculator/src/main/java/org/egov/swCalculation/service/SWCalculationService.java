package org.egov.swCalculation.service;

import java.util.List;

import org.egov.swCalculation.model.Calculation;
import org.egov.swCalculation.model.CalculationReq;

public interface SWCalculationService {
	
	public List<Calculation> getCalculation(CalculationReq request);
	
	public void generateDemandBasedOnTimePeriod();
	
	public List<Calculation> getEstimation(CalculationReq request);
}
