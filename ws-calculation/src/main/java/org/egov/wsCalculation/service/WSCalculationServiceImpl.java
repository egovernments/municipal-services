package org.egov.wsCalculation.service;

import java.awt.List;
import java.util.Collections;
import java.util.Map;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.wsCalculation.model.CalculationCriteria;
import org.egov.wsCalculation.model.CalculationReq;

public class WSCalculationServiceImpl implements WSCalculationService {
	
	

	/**
	 * Method to estimate the tax to be paid for given waterConnection
	 * will be called by estimate api
	 *
	 * @param request incoming calculation request containing the criteria.
	 * @return CalculationRes calculation object containing all the tax for the given criteria.
	 */
    public List<Calculation> getTaxCalculation(CalculationReq request) {

        CalculationCriteria criteria = request.getCalculationCriteria().get(0);
        WaterConnection waterConnection = criteria.getWaterConnection();
        calcValidator.validatePropertyForCalculation(detail);
        Map<String,Object> masterMap = mDataService.getMasterMap(request);
        return new CalculationRes(new ResponseInfo(), Collections.singletonList(getCalculation(request.getRequestInfo(), criteria,
                getEstimationMap(criteria, request.getRequestInfo()),masterMap)));
    }

}
