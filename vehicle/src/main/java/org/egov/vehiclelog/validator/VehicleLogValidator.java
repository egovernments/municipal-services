package org.egov.vehiclelog.validator;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.vehicle.service.VehicleService;
import org.egov.vehiclelog.config.VehicleLogConfiguration;
import org.egov.vehiclelog.querybuilder.VehicleLogQueryBuilder;
import org.egov.vehiclelog.repository.VehicleLogRepository;
import org.egov.vehiclelog.util.VehicleLogConstants;
import org.egov.vehiclelog.web.model.VehicleLog;
import org.egov.vehiclelog.web.model.VehicleLogRequest;
import org.egov.vehiclelog.web.model.VehicleLogSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VehicleLogValidator {

//	@Autowired
//	private DSOService dsoService;

	@Autowired
	private VehicleService vehicleService;

	@Autowired
	private VehicleLogRepository vehicleLogRepository;

	@Autowired
	private VehicleLogQueryBuilder queryBuilder;
	
	@Autowired
	private VehicleLogConfiguration config;

	public void validateCreateOrUpdateRequest(VehicleLogRequest request) {
		if (StringUtils.isEmpty(request.getVehicleLog().getTenantId())) {
			throw new CustomException(VehicleLogConstants.INVALID_VEHICLELOG_ERROR, "TenantId is mandatory");
		}
		if (request.getVehicleLog().getTenantId().split("\\.").length == 1) {
			throw new CustomException(VehicleLogConstants.INVALID_TENANT, " Invalid TenantId");
		}
//		if (StringUtils.isEmpty(request.getVehicleLog().getDsoId())) {
//			throw new CustomException(VehicleLogConstants.INVALID_VEHICLELOG_ERROR, "dsoId is mandatory");
//		}
		if (StringUtils.isEmpty(request.getVehicleLog().getVehicleId())) {
			throw new CustomException(VehicleLogConstants.INVALID_VEHICLELOG_ERROR, "vehicleId is mandatory");
		}
//		if (dsoService.getVendor(request.getVehicleLog().getDsoId(), request.getVehicleLog().getTenantId(),
//				request.getRequestInfo()) == null) {
//			throw new CustomException(VehicleLogConstants.INVALID_DSO, "Invalid DSO");
//		}
//		if (vehicleService.getVehicle(request.getVehicleLog().getVehicleId(), request.getVehicleLog().getTenantId(),
//				request.getRequestInfo()) == null) {
//			throw new CustomException(VehicleLogConstants.INVALID_VEHICLE, "Invalid Vehicle");
//		}
//		validateFSMApplicationNumbers(
//				request.getVehicleLog().getFsms().stream().map(FSM::getId).collect(Collectors.toList()));
	}

	public void validateFSMApplicationNumbers (List<String> applicationsNos ) {
		if( CollectionUtils.isEmpty(applicationsNos)) {
			throw new CustomException(VehicleLogConstants.INVALID_APPLICATION_NOS, "FSM Application ID not found");
		} 
		if(!(vehicleLogRepository.getDataCount(queryBuilder.getFSMApplicationCountQuery(applicationsNos)).equals(applicationsNos.size()))) {
			 throw new CustomException(VehicleLogConstants.INVALID_APPLICATION_NOS, "Invalid Application Number/Numbers");
		 }
    }
	
	public void validateUpdateRecord(VehicleLogRequest request) {
		int vehicleLogCount = vehicleLogRepository.getDataCount(queryBuilder.getVehicleLogExistQuery(request.getVehicleLog().getId()));
		if(vehicleLogCount <= 0) {
			throw new CustomException(VehicleLogConstants.UPDATE_VEHICLELOG_ERROR, "VehicleLog Not found in the System" + request.getVehicleLog());
		}
	}

	public void validateVehicleLogExists(String vehicleId) {
		if(vehicleLogRepository.getDataCount(queryBuilder.getVehicleLogExistQueryForVehicle(vehicleId))>=1) {
			 throw new CustomException(VehicleLogConstants.CREATE_VEHICLELOG_ERROR, "VehicleLog already exists for the given vehicleId");
		 }
	}
	
	public void validateSearch(RequestInfo requestInfo, VehicleLogSearchCriteria criteria) {
		if(StringUtils.isEmpty(criteria.getTenantId())) {
			throw new CustomException(VehicleLogConstants.INVALID_SEARCH, "TenantId is mandatory in search");
		}
		String allowedParamStr = config.getAllowedVehicleLogSearchParameters();
		if (StringUtils.isEmpty(allowedParamStr) && !criteria.isEmpty())
			throw new CustomException(VehicleLogConstants.INVALID_SEARCH, "No search parameters are expected");
		else {
			List<String> allowedParams = Arrays.asList(allowedParamStr.split(","));
			validateSearchParams(criteria, allowedParams);
		}
	}
	
	
	private void validateSearchParams(VehicleLogSearchCriteria criteria, List<String> allowedParams) {

		if (criteria.getOffset() != null && !allowedParams.contains("offset"))
			throw new CustomException(VehicleLogConstants.INVALID_SEARCH, "Search on offset is not allowed");

		if (criteria.getLimit() != null && !allowedParams.contains("limit"))
			throw new CustomException(VehicleLogConstants.INVALID_SEARCH, "Search on limit is not allowed");
		
		if (criteria.getFromDate() != null && !allowedParams.contains("fromDate") && 
				criteria.getToDate() != null && !allowedParams.contains("toDate") ) {
			throw new CustomException(VehicleLogConstants.INVALID_SEARCH, "Search on fromDate and toDate is not allowed");
		}else if( (criteria.getFromDate() != null && criteria.getToDate() == null ) ||
				criteria.getFromDate() == null && criteria.getToDate() != null) {
			throw new CustomException(VehicleLogConstants.INVALID_SEARCH, "Search only "+((criteria.getFromDate() == null) ? "fromDate" :"toDate" )+" is not allowed");
		}if(criteria.getFromDate() != null && criteria.getFromDate() > Calendar.getInstance().getTimeInMillis() ||
				criteria.getToDate() != null && criteria.getFromDate() > Calendar.getInstance().getTimeInMillis()	) {
			throw new CustomException(VehicleLogConstants.INVALID_SEARCH, "Search in futer dates is not allowed");
			
		}
		
		if (CollectionUtils.isEmpty(criteria.getIds())&& !allowedParams.contains("ids")) {
			throw new CustomException(VehicleLogConstants.INVALID_SEARCH, "Search on ids is not allowed");
		}
		
		if (CollectionUtils.isEmpty(criteria.getVehicleIds())&& !allowedParams.contains("vehicleIds")) {
			throw new CustomException(VehicleLogConstants.INVALID_SEARCH, "Search on vehicleIds is not allowed");
		}
		
		if (CollectionUtils.isEmpty(criteria.getDsoIds())&& !allowedParams.contains("dsoIds")) {
			throw new CustomException(VehicleLogConstants.INVALID_SEARCH, "Search on dsoIds is not allowed");
		}
		
		if (CollectionUtils.isEmpty(criteria.getApplicationStatus())&& !allowedParams.contains("applicationStatus")) {
			throw new CustomException(VehicleLogConstants.INVALID_SEARCH, "Search on applicationStatus is not allowed");
		}
		
		if (CollectionUtils.isEmpty(criteria.getFsmIds()) && !allowedParams.contains("fsmIds")) {
			throw new CustomException(VehicleLogConstants.INVALID_SEARCH, "Search on fsmIds is not allowed");
		}	
	}

}
