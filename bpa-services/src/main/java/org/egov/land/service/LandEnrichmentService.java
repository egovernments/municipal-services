package org.egov.land.service;

import java.util.UUID;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.common.contract.request.RequestInfo;
import org.egov.land.util.LandUtil;
import org.egov.land.web.models.AuditDetails;
import org.egov.land.web.models.LandRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class LandEnrichmentService {

	@Autowired
	private LandUtil landUtil;
	
	@Autowired
	private LandBoundaryService boundaryService;
	
	@Autowired
	private BPAConfiguration config;

	public void enrichLandInfoRequest(LandRequest landRequest, boolean isUpdate) {
		RequestInfo requestInfo = landRequest.getRequestInfo();
		AuditDetails auditDetails = landUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		landRequest.getLandInfo().setAuditDetails(auditDetails);
		if (!isUpdate) {
			landRequest.getLandInfo().setId(UUID.randomUUID().toString());
			boundaryService.getAreaType(landRequest, config.getHierarchyTypeCode());
		}

		if (landRequest.getLandInfo().getInstitution() != null) {
			if (StringUtils.isEmpty(landRequest.getLandInfo().getInstitution().getId()))
				landRequest.getLandInfo().getInstitution().setId(UUID.randomUUID().toString());
			if (StringUtils.isEmpty(landRequest.getLandInfo().getInstitution().getTenantId()))
				landRequest.getLandInfo().getInstitution().setTenantId(landRequest.getLandInfo().getTenantId());
		}

		// address
		if (landRequest.getLandInfo().getAddress() != null) {
			if (StringUtils.isEmpty(landRequest.getLandInfo().getAddress().getId()))
				landRequest.getLandInfo().getAddress().setId(UUID.randomUUID().toString());
			landRequest.getLandInfo().getAddress().setTenantId(landRequest.getLandInfo().getTenantId());
			landRequest.getLandInfo().getAddress().setAuditDetails(auditDetails);
			if (StringUtils.isEmpty(landRequest.getLandInfo().getAddress().getGeoLocation().getId()))
				landRequest.getLandInfo().getAddress().getGeoLocation().setId(UUID.randomUUID().toString());
			if (StringUtils.isEmpty(landRequest.getLandInfo().getAddress().getLocality().getId()))
				landRequest.getLandInfo().getAddress().getLocality().setId(UUID.randomUUID().toString());
		}
		// units
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getUnits())) {
			landRequest.getLandInfo().getUnits().forEach(unit -> {
				if(StringUtils.isEmpty(unit.getId())) {
					unit.setId(UUID.randomUUID().toString());
				}
				unit.setTenantId(landRequest.getLandInfo().getTenantId());
				unit.setAuditDetails(auditDetails);
			});
		}

		// Documents
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getDocuments())) {
			landRequest.getLandInfo().getDocuments().forEach(document -> {
				if (StringUtils.isEmpty(document.getId())) {
					document.setId(UUID.randomUUID().toString());
				}
				document.setAuditDetails(auditDetails);
			});
		}

		// Owners
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getOwners())) {
			landRequest.getLandInfo().getOwners().forEach(owner -> {
				if(StringUtils.isEmpty(owner.getOwnerId()))
					owner.setOwnerId(UUID.randomUUID().toString());
				owner.setAuditDetails(auditDetails);
			});
		}
	}
}
