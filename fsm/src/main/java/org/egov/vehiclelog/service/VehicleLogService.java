package org.egov.vehiclelog.service;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.repository.IdGenRepository;
import org.egov.fsm.service.FSMService;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.fsm.web.model.AuditDetails;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.FSMRequest;
import org.egov.fsm.web.model.idgen.IdResponse;
import org.egov.tracer.model.CustomException;
import org.egov.vehiclelog.repository.VehicleLogRepository;
import org.egov.vehiclelog.util.VehicleLogUtil;
import org.egov.vehiclelog.validator.VehicleLogValidator;
import org.egov.vehiclelog.web.model.VehicleLog;
import org.egov.vehiclelog.web.model.VehicleLogRequest;
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
		validator.validateCreateRequest(request);
		vehicleLogEnrichmentService.setInsertData(request);
		vehicleLogRepository.save(request);
		return request.getVehicleLog();
	}
	
	
}
