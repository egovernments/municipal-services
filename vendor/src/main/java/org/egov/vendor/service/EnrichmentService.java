package org.egov.vendor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.vendor.config.VendorConfiguration;
import org.egov.vendor.repository.VendorRepository;
import org.egov.vendor.util.VendorErrorConstants;
import org.egov.vendor.util.VendorUtil;
import org.egov.vendor.web.model.AuditDetails;
import org.egov.vendor.web.model.Vendor;
import org.egov.vendor.web.model.VendorRequest;
import org.egov.vendor.web.model.VendorSearchCriteria;
import org.egov.vendor.web.model.user.UserDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EnrichmentService {

	@Autowired
	private VendorConfiguration config;

	@Autowired
	private VendorUtil vendorUtil;

	@Autowired
	private VendorRepository vendorRepository;
	
	@Autowired
	private VehicleService vehicleService;
	
	@Autowired
	private BoundaryService boundaryService;
	
	@Autowired
	private UserService userService;
	
	/**
	 * enriches the request object for create, assigns random ids for vedor, vehicles and drivers and audit details
	 * @param vendorRequest
	 */
	
	public void enrichCreate(VendorRequest vendorRequest) {
		Vendor vendor = vendorRequest.getVendor();
		RequestInfo requestInfo = vendorRequest.getRequestInfo();
		vendor.setStatus(Vendor.StatusEnum.ACTIVE);
		AuditDetails auditDetails = vendorUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		vendorRequest.getVendor().setAuditDetails(auditDetails);

		vendor.setId(UUID.randomUUID().toString());

		if (vendorRequest.getVendor().getAddress() != null) {
			if (StringUtils.isEmpty(vendorRequest.getVendor().getAddress().getId()))
				vendorRequest.getVendor().getAddress().setId(UUID.randomUUID().toString());
			vendorRequest.getVendor().getAddress().setTenantId(vendorRequest.getVendor().getTenantId());
			vendorRequest.getVendor().getAddress().setAuditDetails(auditDetails);
			if (vendorRequest.getVendor().getAddress().getGeoLocation() != null
					&& StringUtils.isEmpty(vendorRequest.getVendor().getAddress().getGeoLocation().getId()))
				vendorRequest.getVendor().getAddress().getGeoLocation().setId(UUID.randomUUID().toString());
		} else {
			throw new CustomException(VendorErrorConstants.INVALID_ADDRES, " Address is mandatory");
		}
		
		
		if(vendorRequest.getVendor().getVehicles() != null && vendorRequest.getVendor().getVehicles().size() >0) {
			vendorRequest.getVendor().getVehicles().forEach(vehicle->{
				if(StringUtils.isEmpty(vehicle.getId())) {
					vehicle.setId(UUID.randomUUID().toString());
					vehicle.setTenantId(vendorRequest.getVendor().getTenantId());
					vehicle.setAuditDetails(auditDetails);
				}
			});
		}
		
		
	}
	
	public void enrichVendorSearch(List<Vendor> vendorList, RequestInfo requestInfo, String tenantId) {
		
		vendorList.forEach(vendor -> {
			VendorSearchCriteria vendorDriverSearchCriteria = new VendorSearchCriteria();
			List<String> ownerIds = new ArrayList<String>();
			ownerIds.add(vendor.getOwnerId());
			vendorDriverSearchCriteria.setIds(ownerIds);
			vendorDriverSearchCriteria.setTenantId(tenantId);
			UserDetailResponse userResponse = userService.getUsers(vendorDriverSearchCriteria, requestInfo);
			if(userResponse != null && !CollectionUtils.isEmpty(userResponse.getUser())) {
				 vendor.setOwner(userResponse.getUser().get(0));
			}
			
			addDrivers(requestInfo, vendor, tenantId);
			addVehicles(requestInfo, vendor, tenantId);
			boundaryService.getAreaType(VendorRequest.builder().vendor(vendor).build(), config.getHierarchyTypeCode());
		});
	}
	
	private void addDrivers(RequestInfo requestInfo, Vendor vendor, String tenantId) {
		List<String> driverIds = vendorRepository.getDrivers(vendor.getId());
		
		if(!CollectionUtils.isEmpty(driverIds)) {
			VendorSearchCriteria vendorDriverSearchCriteria = new VendorSearchCriteria();
			vendorDriverSearchCriteria.setIds(driverIds);
			vendorDriverSearchCriteria.setTenantId(tenantId);
			UserDetailResponse  userDetailResponse = userService.getUsers(vendorDriverSearchCriteria, requestInfo);
			vendor.setDrivers(userDetailResponse.getUser());
		}
		
	}
	
	private void addVehicles(RequestInfo requestInfo, Vendor vendor, String tenantId) {
		VendorSearchCriteria vendorDriverSearchCriteria = new VendorSearchCriteria();
		List<String> vehicleIds = vendorRepository.getVehicles(vendor.getId());
		if(!CollectionUtils.isEmpty(vehicleIds)) {
			vendor.setVehicles(vehicleService.getVehicles(vehicleIds, null, null,requestInfo, tenantId));
		}
		
	}

}
