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
import org.egov.fsm.service.EnrichmentService;
import org.egov.fsm.util.FSMConstants;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.fsm.web.model.AuditDetails;
import org.egov.fsm.web.model.idgen.IdResponse;
import org.egov.tracer.model.CustomException;
import org.egov.vehiclelog.util.VehicleLogUtil;
import org.egov.vehiclelog.web.model.VehicleLog;
import org.egov.vehiclelog.web.model.VehicleLogRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VehicleLogEnrichmentService {
	
	@Autowired
	private IdGenRepository idGenRepository;
	
	@Autowired
	private VehicleLogUtil vehicleLogUtil;
	
	@Autowired
	private FSMConfiguration config;
	
	public static final String VEHICLE_LOG_APPLICATION_STATUS = "CREATED";
	
	public void setInsertData(VehicleLogRequest request) {
		request.getVehicleLog().setId(UUID.randomUUID().toString());
		request.getVehicleLog().setStatus(VehicleLog.StatusEnum.ACTIVE);
		request.getVehicleLog().setApplicationStatus(VEHICLE_LOG_APPLICATION_STATUS);
		setIdgenIds(request);
		AuditDetails auditDetails = vehicleLogUtil.getAuditDetails(request.getRequestInfo().getUserInfo().getUuid(), true);
		request.getVehicleLog().setAuditDetails(auditDetails);
	}
	
	/**
	 *  generate the applicationNo using the idGen serivce and populate
	 * @param request
	 */
	private void setIdgenIds(VehicleLogRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getVehicleLog().getTenantId();
		VehicleLog vehicleLog = request.getVehicleLog();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNoIdgenName(),
				config.getApplicationNoIdgenFormat(), 1);
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		vehicleLog.setApplicationNo(itr.next());
	}
	
	/**
	 * Generate the id
	 * @param requestInfo
	 * @param tenantId
	 * @param idKey
	 * @param idformat
	 * @param count
	 * @return
	 */
	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException(FSMErrorConstants.IDGEN_ERROR, "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}


}
