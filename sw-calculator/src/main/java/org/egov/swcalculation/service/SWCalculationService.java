package org.egov.swcalculation.service;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swcalculation.model.Calculation;
import org.egov.swcalculation.model.CalculationReq;

public interface SWCalculationService {
	
	public List<Calculation> getCalculation(CalculationReq request);
	
	public void generateDemandBasedOnTimePeriod(RequestInfo requestInfo);
	
	public List<Calculation> getEstimation(CalculationReq request);
}
