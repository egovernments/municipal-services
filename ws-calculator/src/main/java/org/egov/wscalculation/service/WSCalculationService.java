package org.egov.wscalculation.service;

import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wscalculation.model.Calculation;
import org.egov.wscalculation.model.CalculationCriteria;
import org.egov.wscalculation.model.CalculationReq;
import org.egov.wscalculation.model.CalculationRes;

public interface WSCalculationService {

	public List<Calculation> getCalculation(CalculationReq calculationReq);
	
	public void jobscheduler();
	
	public void generateDemandBasedOnTimePeriod();
}
