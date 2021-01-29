package org.egov.fsm.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.repository.ServiceRequestRepository;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.FSMRequest;
import org.egov.fsm.web.model.RequestInfoWrapper;
import org.egov.fsm.web.model.dso.Vendor;
import org.egov.fsm.web.model.dso.VendorResponse;
import org.egov.fsm.web.model.vehicle.Vehicle;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DSOService {

	@Autowired
	private FSMConfiguration config;
	
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
	VehicleService vehicleService;
	
	public Vendor getVendor(String dsoId, String tenantId, RequestInfo requestInfo) {
		
		StringBuilder uri  = new StringBuilder(config.getVendorHost()).append(config.getVendorContextPath())
				.append(config.getVendorSearchEndpoint()).append("?tenantId=").append(tenantId).append("&ids=").append(dsoId);
		
		RequestInfoWrapper requestInfoWrpr = new RequestInfoWrapper();
		requestInfoWrpr.setRequestInfo(requestInfo);
		try {
			
			LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, requestInfoWrpr);
			VendorResponse vendorResponse = mapper.convertValue(responseMap, VendorResponse.class);
			if(!CollectionUtils.isEmpty(vendorResponse.getVendor())) {
				return vendorResponse.getVendor().get(0);
			}else {
				return null;
			}
			
		} catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in validateDSO");
		}
		
	}
	
	public void validateDSO(FSMRequest fsmRequest) {
		FSM fsm = fsmRequest.getFsm();
		Vendor vendor = this.getVendor(fsm.getDsoId(), fsm.getTenantId(), fsmRequest.getRequestInfo());
		
		if(!StringUtils.isEmpty(fsm.getVehicleId())) {
			vehicleService.validateVehicle(fsmRequest);
			List<String> vehicleIds = vendor.getVehicles().stream().map(Vehicle::getId).collect(Collectors.toList());
			if(!CollectionUtils.isEmpty(vehicleIds) && vehicleIds.contains(fsm.getVehicleId())) {
				throw new CustomException(FSMErrorConstants.INVALID_DSO_VEHICLE," Vehicle Does not belong to DSO!");
			}
		}
		
	}
}
