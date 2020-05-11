package org.egov.land.service;

import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.land.repository.LandRepository;
import org.egov.land.validator.LandValidator;
import org.egov.land.web.models.LandInfo;
import org.egov.land.web.models.LandRequest;
import org.egov.land.web.models.LandSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LandService {

	@Autowired
	LandValidator landValidator;

	@Autowired
	private LandEnrichmentService enrichmentService;

	@Autowired
	private LandUserService userService;

	@Autowired
	private LandRepository repository;

	public LandInfo create(@Valid LandRequest landRequest) {
		if (landRequest.getLandInfo().getTenantId().split("\\.").length == 1) {
			throw new CustomException(" Invalid Tenant ", " Application cannot be create at StateLevel");
		}
		
		landValidator.validateLandInfo(landRequest);
		enrichmentService.enrichLandInfoRequest(landRequest, false);

		userService.createUser(landRequest);
		repository.save(landRequest);
		return landRequest.getLandInfo();
	}

	public LandInfo update(@Valid LandRequest landRequest) {
		LandInfo landInfo = landRequest.getLandInfo();

		if (landInfo.getId() == null) {
			throw new CustomException("UPDATE ERROR", "Id is mandatory to update ");
		}

		landInfo.getOwners().forEach(owner -> {
			if (owner.getOwnerType() == null) {
				owner.setOwnerType("NONE");
			}
		});
		landValidator.validateLandInfo(landRequest);
		// landRequest.getLandInfo().setAuditDetails(searchResult.get(0).getAuditDetails());
		enrichmentService.enrichLandInfoRequest(landRequest, true);
		repository.update(landRequest);

		return landRequest.getLandInfo();
	}
	
	public List<LandInfo> search(@Valid LandSearchCriteria criteria, RequestInfo requestInfo) {
		return null;
	}
}
