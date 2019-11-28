package org.egov.swCalculation.service;

import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swCalculation.model.Calculation;
import org.egov.swCalculation.model.CalculationCriteria;
import org.egov.swCalculation.model.CalculationReq;
import org.egov.swCalculation.model.CalculationRes;

public interface SWCalculationService {

	public CalculationRes getTaxCalculation(CalculationReq calculationReq);
	
	public Calculation getCalculation(RequestInfo requestInfo, CalculationCriteria criteria,
			Map<String, List> estimatesAndBillingSlabs, Map<String, Object> masterMap); 
}
