package org.egov.swCalculation.service;

import java.util.Collections;
import java.util.Map;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.swCalculation.model.CalculationCriteria;
import org.egov.swCalculation.model.CalculationReq;
import org.egov.swCalculation.model.CalculationRes;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Authorization;

public class SWCalculationServiceImpl implements SWCalculationService {
	
	@Autowired
	MasterDataService mDataService;
	
	@Autowired
	EstimationService estimationService;
	/**
	 * Get Calculation Request and return Calculated Response
	 */
	@Override
	public CalculationRes getTaxCalculation(CalculationReq request) {
		CalculationCriteria criteria = request.getCalculationCriteria().get(0);
		Map<String, Object> masterMap = mDataService.getMasterMap(request);
		return new CalculationRes(new ResponseInfo(), Collections.singletonList(getCalculation(request.getRequestInfo(),
				criteria, estimationService.getEstimationMap(criteria, request.getRequestInfo()), masterMap)));
	}

}
