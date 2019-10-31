package org.egov.waterConnection.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.repository.ServiceRequestRepository;
import org.egov.waterConnection.repository.WaterDao;
import org.egov.waterConnection.util.WCConstants;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WaterConnectionValidator {

	@Autowired
	private WaterServicesUtil waterServicesUtil;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	WaterDao waterDao;

	public void validateWaterConnection(WaterConnectionRequest waterConnectionRequest, boolean isUpdate) {
		WaterConnection waterConnection = waterConnectionRequest.getWaterConnection();
		Map<String, String> errorMap = new HashMap<>();
		if (isUpdate && (waterConnection.getId() == null || waterConnection.getId().isEmpty())) {
			errorMap.put("INVALID WATER CONNECTION", "WaterConnection cannot be update without connection id");
		}
		if (isUpdate && waterConnection.getId() != null && !waterConnection.getId().isEmpty()) {
			int n = waterDao.isWaterConnectionExist(Arrays.asList(waterConnection.getId()));
			if (n == 0) {
				errorMap.put("INVALID WATER CONNECTION", "Water Id not present");
			}
		}
		if (waterConnection.getConnectionType() == null || waterConnection.getConnectionType().isEmpty()) {
			errorMap.put("INVALID WATER CONNECTION", "WaterConnection cannot be updated without connection type");
		}
		if (waterConnection.getConnectionCategory() == null || waterConnection.getConnectionCategory().isEmpty()) {
			errorMap.put("INVALID WATER CONNECTION", "WaterConnection cannot be update without connection category");
		}
		if (waterConnection.getWaterSource() == null || waterConnection.getWaterSource().isEmpty()) {
			errorMap.put("INVALID WATER CONNECTION", "WaterConnection cannot be created without water source");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

}
