package org.egov.vehiclelog.validator;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.egov.fsm.repository.querybuilder.FSMQueryBuilder;
import org.egov.fsm.service.DSOService;
import org.egov.fsm.service.VehicleService;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.fsm.validator.FSMValidator;
import org.egov.fsm.web.model.FSM;
import org.egov.tracer.model.CustomException;
import org.egov.vehiclelog.repository.VehicleLogRepository;
import org.egov.vehiclelog.web.model.VehicleLog;
import org.egov.vehiclelog.web.model.VehicleLogRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VehicleLogValidator {
	public static final String INVALID_APPLICANT_ERROR = "INVALID_APPLICANT_ERROR";
	public static final String INVALID_TENANT = "INVALID TENANT";
	public static final String INVALID_DSO = "INVALID DSO";
	public static final String INVALID_VEHICLE = "INVALID VEHICLE";
	public static final String INVALID_APPLICATION_NOS = "INVALID APPLICATION NUMBER/NUMBERS";

	@Autowired
	private DSOService dsoService;

	@Autowired
	private VehicleService vehicleService;

	@Autowired
	private VehicleLogRepository vehicleLogRepository;

	@Autowired
	private FSMQueryBuilder fsmQueryBuilder;

	public void validateCreateRequest(VehicleLogRequest request) {
		if (StringUtils.isEmpty(request.getVehicleLog().getTenantId())) {
			throw new CustomException(INVALID_APPLICANT_ERROR, "TenantId is mandatory");
		}
		if (request.getVehicleLog().getTenantId().split("\\.").length == 1) {
			throw new CustomException(INVALID_TENANT, " Application cannot be create at StateLevel");
		}
		if (StringUtils.isEmpty(request.getVehicleLog().getDsoId())) {
			throw new CustomException(INVALID_APPLICANT_ERROR, "dsoId is mandatory");
		}
		if (StringUtils.isEmpty(request.getVehicleLog().getVehicleId())) {
			throw new CustomException(INVALID_APPLICANT_ERROR, "vehicleId is mandatory");
		}
		if (dsoService.getVendor(request.getVehicleLog().getDsoId(), request.getVehicleLog().getTenantId(),
				request.getRequestInfo()) == null) {
			throw new CustomException(INVALID_DSO, "Invalid DSO");
		}
		if (vehicleService.getVehicle(request.getVehicleLog().getVehicleId(), request.getVehicleLog().getTenantId(),
				request.getRequestInfo()) == null) {
			throw new CustomException(INVALID_VEHICLE, "Invalid Vehicle");
		}
		validateFSMApplicationNumbers(
				request.getVehicleLog().getFsmList().stream().map(FSM::getId).collect(Collectors.toList()));
	}

	public void validateFSMApplicationNumbers (List<String> applicationsNos ) {
		if( !CollectionUtils.isEmpty(applicationsNos)) {
			 if(!(vehicleLogRepository.getFSMApplicationCount(fsmQueryBuilder.getFSMApplicationCountQuery(applicationsNos)).equals(applicationsNos.size()))) {
				 throw new CustomException(INVALID_VEHICLE, "Invalid Application Number/Numbers");
			 }
		} 
    }

	

}
