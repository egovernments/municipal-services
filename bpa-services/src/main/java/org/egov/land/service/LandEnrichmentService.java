package org.egov.land.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.web.model.user.UserDetailResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.land.util.LandUtil;
import org.egov.land.web.models.AuditDetails;
import org.egov.land.web.models.LandInfo;
import org.egov.land.web.models.LandRequest;
import org.egov.land.web.models.LandSearchCriteria;
import org.egov.land.web.models.OwnerInfo;
import org.egov.tracer.model.CustomException;
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

	@Autowired
	private LandUserService userService;

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
				if (StringUtils.isEmpty(unit.getId())) {
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
				if (StringUtils.isEmpty(owner.getOwnerId()))
					owner.setOwnerId(UUID.randomUUID().toString());
				owner.setAuditDetails(auditDetails);
			});
		}
	}

	/**
	 * Creates search criteria from list of bpa's
	 * 
	 * @param landInfo
	 *            's list The landInfo whose id's are added to search
	 * @return landSearch criteria on basis of bpa id
	 */
	public LandSearchCriteria getLandCriteriaFromIds(List<LandInfo> landInfo, Integer limit) {
		LandSearchCriteria criteria = new LandSearchCriteria();
		Set<String> landIds = new HashSet<>();
		landInfo.forEach(data -> landIds.add(data.getId()));
		criteria.setIds(new LinkedList<>(landIds));
		criteria.setTenantId(landInfo.get(0).getTenantId());
		criteria.setLimit(limit);
		return criteria;
	}

	public List<LandInfo> enrichLandInfoSearch(List<LandInfo> landInfos, LandSearchCriteria criteria,
			RequestInfo requestInfo) {

		List<LandRequest> landInfors = new ArrayList<LandRequest>();
		landInfos.forEach(bpa -> {
			landInfors.add(new LandRequest(requestInfo, bpa));
		});
		if (criteria.getLimit() == null || !criteria.getLimit().equals(-1)) {
			enrichBoundary(landInfors);
		}

		UserDetailResponse userDetailResponse = userService.getUsersForLandInfos(landInfos);
		enrichOwner(userDetailResponse, landInfos);
		return landInfos;
	}

	private void enrichBoundary(List<LandRequest> landRequests) {
		landRequests.forEach(landRequest -> {
			String code = null;
			if (landRequest.getLandInfo().getAddress() != null
					&& landRequest.getLandInfo().getAddress().getLocality() != null) {
				code = landRequest.getLandInfo().getAddress().getLocality().getCode() != null
						? landRequest.getLandInfo().getAddress().getLocality().getCode()
						: config.getHierarchyTypeCode();
				boundaryService.getAreaType(landRequest, code);
			}
		});
	}

	private void enrichOwner(UserDetailResponse userDetailResponse, List<LandInfo> landInfos) {

		List<OwnerInfo> users = userDetailResponse.getUser();
		Map<String, OwnerInfo> userIdToOwnerMap = new HashMap<>();
		users.forEach(user -> userIdToOwnerMap.put(user.getUuid(), user));
		landInfos.forEach(landInfo -> {
			landInfo.getOwners().forEach(owner -> {
				if (userIdToOwnerMap.get(owner.getUuid()) == null)
					throw new CustomException("OWNER SEARCH ERROR",
							"The owner of the landInfo " + landInfo.getId() + " is not coming in user search");
				else
					owner.addUserDetail(userIdToOwnerMap.get(owner.getUuid()));
			});
		});
	}

	public void enrichLandInfoCriteriaWithOwnerids(LandSearchCriteria criteria, UserDetailResponse userDetailResponse) {
		if (CollectionUtils.isEmpty(criteria.getOwnerIds())) {
			Set<String> ownerids = new HashSet<>();
			userDetailResponse.getUser().forEach(owner -> ownerids.add(owner.getUuid()));
			criteria.setOwnerIds(new ArrayList<>(ownerids));
		}
	}
}
