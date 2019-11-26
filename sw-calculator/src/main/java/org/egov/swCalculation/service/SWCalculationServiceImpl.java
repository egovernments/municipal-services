package org.egov.swCalculation.service;

import org.egov.swCalculation.model.CalculationCriteria;
import org.egov.swCalculation.model.CalculationReq;
import org.egov.swCalculation.model.CalculationRes;
import org.springframework.beans.factory.annotation.Autowired;

public class SWCalculationServiceImpl implements SWCalculationService {
	
	@Autowired
	MasterDataService masterDataService;
	/**
	 * Get Calculation Request and return Calculated Response
	 */
	@Override
	public CalculationRes getTaxCalculation(CalculationReq calculationReq) {
		CalculationCriteria criteria = calculationReq.getCalculationCriteria().get(0);
		return null;
	}

}
