package org.egov.vehiclelog.service;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.vehicle.service.VehicleService;
import org.egov.vehicle.web.model.AuditDetails;
import org.egov.vehiclelog.config.VehicleLogConfiguration;
import org.egov.vehiclelog.repository.IdGenRepository;
import org.egov.vehiclelog.util.VehicleLogConstants;
import org.egov.vehiclelog.util.VehicleLogUtil;
import org.egov.vehiclelog.web.model.VehicleLog;
import org.egov.vehiclelog.web.model.VehicleLogRequest;
import org.egov.vehiclelog.web.model.idgen.IdResponse;
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
	private VehicleLogConfiguration config;
	
//	@Autowired
//	private DSOService dsoService;
	
	@Autowired
	private VehicleService vehicleService;
	
//	@Autowired
//	private FSMService fsmService;

	public void setInsertData(VehicleLogRequest request) {
		request.getVehicleLog().setId(UUID.randomUUID().toString());
		request.getVehicleLog().setStatus(VehicleLog.StatusEnum.ACTIVE);
		request.getVehicleLog().setApplicationStatus(VehicleLogConstants.VEHICLE_LOG_APPLICATION_CREATED_STATUS);
		setIdgenIds(request);
		AuditDetails auditDetails = vehicleLogUtil.getAuditDetails(request.getRequestInfo().getUserInfo().getUuid(),
				true);
		request.getVehicleLog().setAuditDetails(auditDetails);
	}

	public void setUpdateData(VehicleLogRequest request) {
		request.getVehicleLog().setApplicationStatus(VehicleLogConstants.VEHICLE_LOG_APPLICATION_UPDATED_STATUS);
		AuditDetails auditDetails = vehicleLogUtil.getAuditDetails(request.getRequestInfo().getUserInfo().getUuid(),
				false);
		request.getVehicleLog().setAuditDetails(auditDetails);
	}

	public void enrichSearch(List<VehicleLog> vehicleLogList, RequestInfo requestInfo) {
		vehicleLogList.forEach(vehicleLog -> {
//			addVehicle(vehicleLog, requestInfo);
//			addDSO(vehicleLog, requestInfo);	
//			addFSMList(vehicleLog, requestInfo);
		});
	}

//	private void addVehicle(VehicleLog vehicleLog, RequestInfo requestInfo) {
//		vehicleLog.setVehicle(vehicleService.getVehicle(vehicleLog.getVehicleId(), vehicleLog.getTenantId(), requestInfo));
//	}
//
//	private void addDSO(VehicleLog vehicleLog, RequestInfo requestInfo) {
//		vehicleLog.setDso(dsoService.getVendor(vehicleLog.getDsoId(),  vehicleLog.getTenantId(), requestInfo));
//	}
//	
//	private void addFSMList(VehicleLog vehicleLog, RequestInfo requestInfo) {
//		FSMSearchCriteria fsmCriteria = new FSMSearchCriteria();
//		fsmCriteria.setTenantId(vehicleLog.getTenantId());;
//		fsmCriteria.setIds(vehicleLog.getFsms().stream().map(FSM::getId).collect(Collectors.toList()));
//		vehicleLog.setFsms(fsmService.FSMsearch(fsmCriteria, requestInfo));
//	}

	/**
	 * generate the applicationNo using the idGen serivce and populate
	 * 
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
	 * 
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
			throw new CustomException(VehicleLogConstants.IDGEN_ERROR, "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}

}
