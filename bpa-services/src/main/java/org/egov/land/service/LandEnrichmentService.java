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

@Service
public class LandEnrichmentService {
	
	@Autowired
	private LandUtil landUtil;
	
	@Autowired
	private BPAConfiguration config;
	
	@Autowired
	private LandBoundaryService boundaryService;
	
	public void enrichBPACreateRequest(LandRequest landRequest, Object mdmsData) {
		RequestInfo requestInfo = landRequest.getRequestInfo();
		AuditDetails auditDetails = landUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		landRequest.getLandInfo().setAuditDetails(auditDetails);
		landRequest.getLandInfo().setId(UUID.randomUUID().toString());

		// address
		landRequest.getLandInfo().getAddress().setId(UUID.randomUUID().toString());
		landRequest.getLandInfo().getAddress().setTenantId(landRequest.getLandInfo().getTenantId());

		// units
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getUnit())) {
			landRequest.getLandInfo().getUnit().forEach(unit -> {
				unit.setTenantId(landRequest.getLandInfo().getTenantId());
				unit.setId(UUID.randomUUID().toString());
			});
		}

		// BPA Documents
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getDocuments()))
			landRequest.getLandInfo().getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});

		// owners
//		landRequest.getLandInfo().getOwners().forEach(owner -> {
//
//			if (!CollectionUtils.isEmpty(owner.getDocuments()))
//				owner.getDocuments().forEach(document -> {
//					document.setId(UUID.randomUUID().toString());
//				});
//		});
		boundaryService.getAreaType(landRequest, config.getHierarchyTypeCode());
	}

}
