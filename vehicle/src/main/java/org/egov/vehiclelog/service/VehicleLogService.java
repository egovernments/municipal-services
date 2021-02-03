package org.egov.vehiclelog.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.vehiclelog.repository.VehicleLogRepository;
import org.egov.vehiclelog.util.VehicleLogConstants;
import org.egov.vehiclelog.util.VehicleLogUtil;
import org.egov.vehiclelog.validator.VehicleLogValidator;
import org.egov.vehiclelog.web.model.VehicleLog;
import org.egov.vehiclelog.web.model.VehicleLogRequest;
import org.egov.vehiclelog.web.model.VehicleLogSearchCriteria;
import org.egov.vehiclelog.web.model.idgen.IdResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VehicleLogService {
	
	@Autowired
	private VehicleLogRepository vehicleLogRepository;
	
	@Autowired
	private VehicleLogEnrichmentService vehicleLogEnrichmentService;
	
	@Autowired
	private VehicleLogValidator validator;
	
	public VehicleLog create(VehicleLogRequest request) {
		if (request.getVehicleLog() == null) {
			throw new CustomException(VehicleLogConstants.CREATE_VEHICLELOG_ERROR, "vehicleLog not found in the Request" + request.getVehicleLog());
		}
		validator.validateCreateOrUpdateRequest(request);
		validator.validateVehicleLogExists(request.getVehicleLog().getVehicleId());
		vehicleLogEnrichmentService.setInsertData(request);
		vehicleLogRepository.save(request);
		return request.getVehicleLog();
	}
	
	public VehicleLog update(VehicleLogRequest request) {
		if (request.getVehicleLog() == null || StringUtils.isEmpty(request.getVehicleLog().getId())) {
			throw new CustomException(VehicleLogConstants.UPDATE_VEHICLELOG_ERROR, "vehicleLogId not found in the Request" + request.getVehicleLog());
		}
		validator.validateCreateOrUpdateRequest(request);
		validator.validateUpdateRecord(request);
		vehicleLogEnrichmentService.setUpdateData(request);
		vehicleLogRepository.update(request);
		return request.getVehicleLog();
	}
	
	public List<VehicleLog> search(VehicleLogSearchCriteria criteria, RequestInfo requestInfo) {
		validator.validateSearch(requestInfo, criteria);
		List<VehicleLog> vehicleLogList = new LinkedList<>();
		vehicleLogList = vehicleLogRepository.getVehicleLogData(criteria);
		vehicleLogEnrichmentService.enrichSearch(vehicleLogList, requestInfo);
		return vehicleLogList;
	}
}
