package org.egov.waterConnection.validator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.repository.WaterDao;
import org.egov.waterConnection.util.WCConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WaterConnectionValidator {
	@Autowired
	WaterDao waterDao;

	/**
	 * 
	 * @param waterConnectionRequest
	 *            WaterConnectionRequest is request for create or update water
	 *            connection
	 * @param isUpdate
	 *            True for update and false for create
	 */
	public void validateWaterConnection(WaterConnectionRequest waterConnectionRequest, boolean isUpdate) {
		WaterConnection waterConnection = waterConnectionRequest.getWaterConnection();
		Map<String, String> errorMap = new HashMap<>();
		if (isUpdate && (waterConnection.getId() == null || waterConnection.getId().isEmpty())) {
			errorMap.put("INVALID WATER CONNECTION", "WaterConnection cannot be update without connection id");
		}
		if (waterConnection.getConnectionType() == WCConstants.METER_STATUS) {
			if (waterConnection.getMeterId() == null) {
				errorMap.put("INVALID WATER CONNECTION", "Meter Id cannot be null !!");
			}
			if (waterConnection.getMeterInstallationDate() < 0 || waterConnection.getMeterInstallationDate() == null
					|| waterConnection.getMeterInstallationDate() == 0) {
				errorMap.put("INVALID WATER CONNECTION", "Meter Installation date cannot be null or negative !!");
			}
		}
			
	 
		if (isUpdate && waterConnection.getId() != null && !waterConnection.getId().isEmpty()) {
			int n = waterDao.isWaterConnectionExist(Arrays.asList(waterConnection.getId()));
			if (n == 0) {
				errorMap.put("INVALID WATER CONNECTION", "Water Id not present");
			}
		}
		if (waterConnection.getConnectionType() == null || waterConnection.getConnectionType().isEmpty()) {
			errorMap.put("INVALID WATER CONNECTION", "WaterConnection cannot be created  without connection type");
		}
		if (waterConnection.getConnectionCategory() == null || waterConnection.getConnectionCategory().isEmpty()) {
			errorMap.put("INVALID WATER CONNECTION", "WaterConnection cannot be created without connection category");
		}
		if (waterConnection.getWaterSource() == null || waterConnection.getWaterSource().isEmpty()) {
			errorMap.put("INVALID WATER CONNECTION", "WaterConnection cannot be created without water source");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

}
