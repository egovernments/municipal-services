package org.egov.land.service;

import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.land.repository.LandRepository;
import org.egov.land.util.LandUtil;
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

	@Autowired
	private LandUtil util;

	public LandInfo create(@Valid LandRequest landRequest) {
		RequestInfo requestInfo = landRequest.getRequestInfo();
		String tenantId = landRequest.getLandInfo().getTenantId().split("\\.")[0];
		Object mdmsData = util.mDMSCall(requestInfo, tenantId);
		if (landRequest.getLandInfo().getTenantId().split("\\.").length == 1) {
			throw new CustomException(" Invalid Tenant ", " Application cannot be create at StateLevel");
		}

		landValidator.validateCreate(landRequest, mdmsData);
		enrichmentService.enrichLandInfoCreateRequest(landRequest, mdmsData);

		userService.createUser(landRequest);
		repository.save(landRequest);
		return landRequest.getLandInfo();
	}

	public LandInfo update(@Valid LandRequest landRequest) {
		return null;
	}

	public List<LandInfo> search(@Valid LandSearchCriteria criteria, RequestInfo requestInfo) {
		return null;
	}
}
