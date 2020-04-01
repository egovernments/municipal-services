package org.egov.wscalculation.service;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wscalculation.model.Calculation;
import org.egov.wscalculation.model.CalculationReq;

public interface WSCalculationService {

	public List<Calculation> getCalculation(CalculationReq calculationReq);
	
	public void jobscheduler();
	
	public void generateDemandBasedOnTimePeriod(RequestInfo requestInfo);
}
