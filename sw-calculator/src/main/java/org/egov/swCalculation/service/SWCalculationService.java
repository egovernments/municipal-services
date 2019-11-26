package org.egov.swCalculation.service;

import org.egov.swCalculation.model.CalculationReq;
import org.egov.swCalculation.model.CalculationRes;

public interface SWCalculationService {

	public CalculationRes getTaxCalculation(CalculationReq calculationReq);
}
