package org.egov.waterconnection.service;

import java.util.HashMap;
import java.util.Map;

import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.web.models.ValidatorResult;
import org.egov.waterconnection.web.models.WaterConnectionRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WaterFieldValidator implements WaterActionValidator {

	@Override
	public ValidatorResult validate(WaterConnectionRequest waterConnectionRequest, int reqType) {
		Map<String, String> errorMap = new HashMap<>();
		if (reqType == WCConstants.UPDATE_APPLICATION) {
			handleUpdateApplicationRequest(waterConnectionRequest, errorMap);
		}
		if(reqType == WCConstants.MODIFY_CONNECTION){
			handleModifyConnectionRequest(waterConnectionRequest, errorMap);
		}
		if (!errorMap.isEmpty())
			return new ValidatorResult(false, errorMap);
		return new ValidatorResult(true, errorMap);
	}

	private void handleUpdateApplicationRequest(WaterConnectionRequest waterConnectionRequest,
			Map<String, String> errorMap) {
		if (WCConstants.ACTIVATE_CONNECTION_CONST
				.equalsIgnoreCase(waterConnectionRequest.getWaterConnection().getProcessInstance().getAction())) {
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionType())) {
				errorMap.put("INVALID_WATER_CONNECTION_TYPE", "Connection type should not be empty");
			}
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getWaterSource())) {
				errorMap.put("INVALID_WATER_SOURCE", "WaterConnection cannot be created  without water source");
			}
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getRoadType())) {
				errorMap.put("INVALID_ROAD_TYPE", "Road type should not be empty");
			}
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionExecutionDate())) {
				errorMap.put("INVALID_CONNECTION_EXECUTION_DATE", "Connection execution date should not be empty");
			}

		}
		if (WCConstants.APPROVE_CONNECTION_CONST
				.equalsIgnoreCase(waterConnectionRequest.getWaterConnection().getProcessInstance().getAction())) {
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getRoadType())) {
				errorMap.put("INVALID_ROAD_TYPE", "Road type should not be empty");
			}
			if (waterConnectionRequest.getWaterConnection().getRoadCuttingArea() == null) {
				errorMap.put("INVALID_ROAD_CUTTING_AREA", "Road cutting area should not be empty");
			}
		}
	}
	
	private void handleModifyConnectionRequest(WaterConnectionRequest waterConnectionRequest, Map<String, String> errorMap){
		if (WCConstants.APPROVE_CONNECTION
				.equalsIgnoreCase(waterConnectionRequest.getWaterConnection().getProcessInstance().getAction())) {
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionType())) {
				errorMap.put("INVALID_WATER_CONNECTION_TYPE", "Connection type should not be empty");
			}
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getWaterSource())) {
				errorMap.put("INVALID_WATER_SOURCE", "WaterConnection cannot be created  without water source");
			}
			if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionExecutionDate())) {
				errorMap.put("INVALID_CONNECTION_EXECUTION_DATE", "Connection execution date should not be empty");
			}

		}
	}
}
