package org.egov.wsCalculation.validator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReadingSearchCriteria;
import org.egov.wsCalculation.repository.WSCalculationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WSCalculationValidator {

	@Autowired
	WSCalculationDao wSCalculationDao;

	/**
	 * 
	 * @param meterReadingConnectionRequest
	 *            meterReadingConnectionRequest is request for create or update
	 *            meter reading connection
	 * @param isUpdate
	 *            True for update and false for create
	 */

	public void validateMeterReading(MeterConnectionRequest meterConnectionRequest, boolean isUpdate) {
		MeterReading meterReading = meterConnectionRequest.getMeterReading();
		Map<String, String> errorMap = new HashMap<>();
		if (isUpdate && (meterReading.getId() == null || meterReading.getId().isEmpty())) {
			errorMap.put("INVALID METER READING CONNECTION", "Meter Reading cannot be update without meter reading id");
		}
		if (isUpdate && meterReading.getId() != null && !meterReading.getId().isEmpty()) {
			int n = wSCalculationDao.isMeterReadingConnectionExist(Arrays.asList(meterReading.getId()));
			if (n == 0) {
				errorMap.put("INVALID METER READING CONNECTION", "Meter reading Id not present");
			}
		}
		if (meterReading.getBillingPeriod() == null || meterReading.getBillingPeriod().isEmpty()) {
			errorMap.put("INVALID METER READING CONNECTION", "Meter Reading cannot be updated without billing period");
		}

		if (!errorMap.isEmpty()) {
			throw new CustomException(errorMap);
		}
	}
	
	public void validateMeterReadingSearchCriteria(MeterReadingSearchCriteria criteria) {
		Map<String, String> errorMap = new HashMap<>();
		if(criteria.getConnectionNos() == null || criteria.getConnectionNos().isEmpty()) {
			errorMap.put("INVALID SEARCH CRITERIA ", " Search can not be done without connection no");
		}
		if (!errorMap.isEmpty()) {
			throw new CustomException(errorMap);
		}
	}

}
