package org.egov.land.service;

import java.util.List;

import javax.validation.Valid;

import org.egov.bpa.repository.BPARepository;
import org.egov.bpa.service.EnrichmentService;
import org.egov.bpa.service.UserService;
import org.egov.bpa.util.BPAUtil;
import org.egov.common.contract.request.RequestInfo;
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
	EnrichmentService enrichmentService;

	@Autowired
	UserService userService;

	@Autowired
	private BPARepository repository;

	@Autowired
	private BPAUtil util;

	public LandInfo create(@Valid LandRequest landRequest) {
		RequestInfo requestInfo = landRequest.getRequestInfo();
		String tenantId = landRequest.getLandInfo().getTenantId().split("\\.")[0];
		Object mdmsData = util.mDMSCall(requestInfo, tenantId);
		if (landRequest.getLandInfo().getTenantId().split("\\.").length == 1) {
			throw new CustomException("Invalid Tenant ", " land cannot be considered at StateLevel");
		}
		landValidator.validateCreate(landRequest, mdmsData);
		enrichmentService.enrichLandCreateRequest(landRequest, mdmsData);

		userService.createUser(landRequest);

		repository.saveLand(landRequest);
		return landRequest.getLandInfo();
	}

	public LandInfo update(@Valid LandRequest landRequest) {
		return null;
	}

	private List<LandInfo> getLandInfoWithOwnerInfo(@Valid LandRequest landRequest) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<LandInfo> search(@Valid LandSearchCriteria criteria, RequestInfo requestInfo) {
		return null;
	}

	private LandInfo getLandInfoFromMobileNumber(@Valid LandSearchCriteria criteria, RequestInfo requestInfo) {
		// TODO Auto-generated method stub
		return null;
	}
}
