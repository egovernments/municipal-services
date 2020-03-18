package org.egov.wscalculation.validator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.egov.tracer.model.CustomException;
import org.egov.wscalculation.constants.WSCalculationConstant;
import org.egov.wscalculation.model.MeterConnectionRequest;
import org.egov.wscalculation.model.MeterReading;
import org.egov.wscalculation.model.MeterReadingSearchCriteria;
import org.egov.wscalculation.model.WaterConnection;
import org.egov.wscalculation.repository.WSCalculationDao;
import org.egov.wscalculation.service.MasterDataService;
import org.egov.wscalculation.util.CalculatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class WSCalculationValidator {

	@Autowired
	private WSCalculationDao wSCalculationDao;
	
	@Autowired
	private CalculatorUtil calculationUtil;
	
	@Autowired
	private MasterDataService masterDataService;

	/**
	 * 
	 * @param meterReadingConnectionRequest
	 *            meterReadingConnectionRequest is request for create or update
	 *            meter reading connection
	 * @param isUpdate
	 *            True for create
	 */

	public void validateMeterReading(MeterConnectionRequest meterConnectionRequest, boolean isUpdate) {
		MeterReading meterReading = meterConnectionRequest.getMeterReading();
		Map<String, String> errorMap = new HashMap<>();

		// Future Billing Period Check
		if (!StringUtils.isEmpty(meterReading.getBillingPeriod()))
			masterDataService.getDemandStartAndEndValue(meterReading.getBillingPeriod());
		WaterConnection connection = calculationUtil.getWaterConnection(meterConnectionRequest.getRequestInfo(),
				meterReading.getConnectionNo(), meterConnectionRequest.getRequestInfo().getUserInfo().getTenantId());
		if (meterConnectionRequest.getMeterReading().getGenerateDemand() && connection == null) {
			errorMap.put("INVALID_METER_READING_CONNECTION_NUMBER", "Invalid water connection number");
		}
		if (connection != null
				&& !WSCalculationConstant.meteredConnectionType.equalsIgnoreCase(connection.getConnectionType())) {
			errorMap.put("INVALID_WATER_CONNECTION_TYPE",
					"Meter reading can not be create for : " + connection.getConnectionType() + " connection");
		}
		MeterReadingSearchCriteria criteria = new MeterReadingSearchCriteria();
		Set<String> connectionNos = new HashSet<>();
		connectionNos.add(meterReading.getConnectionNo());
		criteria.setConnectionNos(connectionNos);
		List<MeterReading> previousMeterReading = wSCalculationDao.searchCurrentMeterReadings(criteria);
		if (!CollectionUtils.isEmpty(previousMeterReading)) {
			Double currentMeterReading = wSCalculationDao.searchCurrentMeterReadings(criteria).get(0)
					.getCurrentReading();
			if (meterReading.getCurrentReading() < currentMeterReading) {
				errorMap.put("INVALID_METER_READING_CONNECTION_NUMBER",
						"Current meter reading has to be greater than the past last readings in the meter reading!");
			}
		}

		if (meterReading.getCurrentReading() < meterReading.getLastReading()) {
			errorMap.put("INVALID_METER_READING_LAST_READING",
					"Current Meter Reading cannot be less than last meter reading");
		}

		if (StringUtils.isEmpty(meterReading.getMeterStatus())) {
			errorMap.put("INVALID_METER_READING_STATUS", "Meter status can not be null");
		}

		if (isUpdate && (meterReading.getCurrentReading() == null)) {
			errorMap.put("INVALID_CURRENT_METER_READING",
					"Current Meter Reading cannot be update without current meter reading");
		}

		if (isUpdate && !StringUtils.isEmpty(meterReading.getId())) {
			int n = wSCalculationDao.isMeterReadingConnectionExist(Arrays.asList(meterReading.getId()));
			if (n > 0) {
				errorMap.put("INVALID_METER_READING_CONNECTION", "Meter reading Id already present");
			}
		}
		if (StringUtils.isEmpty(meterReading.getBillingPeriod())) {
			errorMap.put("INVALID_BILLING_PERIOD", "Meter Reading cannot be updated without billing period");
		}

		int billingPeriodNumber = wSCalculationDao.isBillingPeriodExists(meterReading.getConnectionNo(),
				meterReading.getBillingPeriod());
		if (billingPeriodNumber > 0)
			errorMap.put("INVALID_METER_READING_BILLING_PERIOD", "Billing Period Already Exists");

		if (!errorMap.isEmpty()) {
			throw new CustomException(errorMap);
		}
	}

	/**
	 * validates for the required information needed to do the
	 * calculation/estimation
	 * 
	 * @param detail
	 *            property detail
	 */
	public void validateWaterConnectionForCalculation(WaterConnection waterConnection) {

		Map<String, String> error = new HashMap<>();

		// boolean isVacantLand =
		// PT_TYPE_VACANT_LAND.equalsIgnoreCase(detail.getPropertyType());
		//
		// if(null == detail.getLandArea() && null == detail.getBuildUpArea())
		// error.put(PT_ESTIMATE_AREA_NULL, PT_ESTIMATE_AREA_NULL_MSG);
		//
		// if (isVacantLand && null == detail.getLandArea())
		// error.put(PT_ESTIMATE_VACANT_LAND_NULL,
		// PT_ESTIMATE_VACANT_LAND_NULL_MSG);
		//
		// if (!isVacantLand && CollectionUtils.isEmpty(detail.getUnits()))
		// error.put(PT_ESTIMATE_NON_VACANT_LAND_UNITS,
		// PT_ESTIMATE_NON_VACANT_LAND_UNITS_MSG);

		if (!CollectionUtils.isEmpty(error))
			throw new CustomException(error);
	}

	public void validateMeterReadingSearchCriteria(MeterReadingSearchCriteria criteria) {
		if (criteria.getConnectionNos() == null || criteria.getConnectionNos().isEmpty()) {
			throw new CustomException("INVALID SEARCH CRITERIA ", " Search can not be done without connection no");
		}
	}

}
