package org.egov.land.service;

import java.util.UUID;

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

	public void enrichLandInfoCreateRequest(LandRequest landRequest, Object mdmsData) {
		RequestInfo requestInfo = landRequest.getRequestInfo();
		AuditDetails auditDetails = landUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		landRequest.getLandInfo().setAuditDetails(auditDetails);
		landRequest.getLandInfo().setId(UUID.randomUUID().toString());
		landRequest.getLandInfo().getInstitution().setId(UUID.randomUUID().toString());
		if(StringUtils.isEmpty(landRequest.getLandInfo().getInstitution().getTenantId())){
			landRequest.getLandInfo().getInstitution().setTenantId( landRequest.getLandInfo().getTenantId());
		}

		// address
		landRequest.getLandInfo().getAddress().setId(UUID.randomUUID().toString());
		landRequest.getLandInfo().getAddress().setTenantId(landRequest.getLandInfo().getTenantId());
		landRequest.getLandInfo().getAddress().setAuditDetails(auditDetails);
		landRequest.getLandInfo().getAddress().getGeoLocation().setId(UUID.randomUUID().toString());
		landRequest.getLandInfo().getAddress().getLocality().setId(UUID.randomUUID().toString());

		// units
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getUnits())) {
			landRequest.getLandInfo().getUnits().forEach(unit -> {
				unit.setTenantId(landRequest.getLandInfo().getTenantId());
				unit.setId(UUID.randomUUID().toString());
				unit.setAuditDetails(auditDetails);
				unit.getConstructionDetail().setAuditDetails(auditDetails);
			});
		}

		//Documents
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getDocuments())) {
			landRequest.getLandInfo().getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
				document.setAuditDetails(auditDetails);
			});
		}
		
		//Owners
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getOwners())) {
			landRequest.getLandInfo().getOwners().forEach(owner -> {
				owner.setAuditDetails(auditDetails);
			});
		}
	}
}
