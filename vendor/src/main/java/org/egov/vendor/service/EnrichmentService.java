package org.egov.vendor.service;

import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.vendor.config.VendorConfiguration;
import org.egov.vendor.util.VendorErrorConstants;
import org.egov.vendor.util.VendorUtil;
import org.egov.vendor.web.model.AuditDetails;
import org.egov.vendor.web.model.Vendor;
import org.egov.vendor.web.model.VendorRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EnrichmentService {

	@Autowired
	private VendorConfiguration config;

	@Autowired
	private VendorUtil vendorUtil;

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
		
//		if(vendorRequest.getVendor().getDrivers() != null && vendorRequest.getVendor().getDrivers().size() >0) {
//			vendorRequest.getVendor().getDrivers().forEach(driver->{
//				if(StringUtils.isEmpty(driver.getId())) {
//					driver.setId(Long.parseLong(UUID.randomUUID().toString()));
//					driver.setTenantId(vendorRequest.getVendor().getTenantId());
//				}
//			});
//		}
		
//		if(vendorRequest.getVendor().getOwner() != null ) {
//			if(StringUtils.isEmpty(vendorRequest.getVendor().getOwner().getId())) {
//				vendorRequest.getVendor().getOwner().setId(Long.parseLong(UUID.randomUUID().toString()));
//				vendorRequest.getVendor().getOwner().setTenantId(vendorRequest.getVendor().getTenantId());
//			}
//		}
		
		
	}

}
