package org.egov.vendorregistory.service;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.tracer.model.CustomException;
import org.egov.vendorregistory.config.VendorConfiguration;
import org.egov.vendorregistory.util.VendorErrorConstants;
import org.egov.vendorregistory.util.VendorUtil;
import org.egov.vendorregistory.web.model.AuditDetails;
import org.egov.vendorregistory.web.model.VendorRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VendorCreateService {

	@Autowired
	private OwnerService ownerService;

	@Autowired
	private VendorConfiguration config;

	@Autowired
	private BoundaryService boundaryService;

	@Autowired
	private VendorUtil vendorUtil;

	// Unable to give name to java file created method name hear
	public void validateCreate(VendorRequest vendorRequest) {

		org.egov.common.contract.request.@NotNull @Valid RequestInfo requestInfo = vendorRequest.getRequestInfo();
		
		ownerService.manageOwner(vendorRequest);
		//boundaryService.getAreaType(vendorRequest, config.getHierarchyTypeCode());

		// fsmRequest.getFsm().setApplicationStatus(FSMConstants.DRAFT);
		AuditDetails auditDetails = vendorUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		vendorRequest.getVendor().setAuditDetails(auditDetails);
		//vendorRequest.getVendor().setId(UUID.randomUUID().toString());
		//vendorRequest.getVendor().setOwner(vendorRequest.getVendor().getOwner().getUuid());
		
		if (vendorRequest.getVendor().getAddress() != null) {
			if (StringUtils.isEmpty(vendorRequest.getVendor().getAddress().getId()))
				vendorRequest.getVendor().getAddress().setId(UUID.randomUUID().toString());
			vendorRequest.getVendor().getAddress().setTenantId(vendorRequest.getVendor().getTenantId());
			vendorRequest.getVendor().getAddress().setAuditDetails(auditDetails);
			if (vendorRequest.getVendor().getAddress().getGeoLocation() != null
					&& StringUtils.isEmpty(vendorRequest.getVendor().getAddress().getGeoLocation().getId()))
				vendorRequest.getVendor().getAddress().getGeoLocation().setId(UUID.randomUUID().toString());
		}else {
			throw new CustomException(VendorErrorConstants.INVALID_ADDRES," Address is mandatory");
		}
		
	
	}
}
