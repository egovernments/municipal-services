package org.egov.vendorregistory.validator;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.vendorregistory.config.VendorConfiguration;
import org.egov.vendorregistory.service.BoundaryService;
import org.egov.vendorregistory.service.OwnerService;
import org.egov.vendorregistory.util.VendorConstants;
import org.egov.vendorregistory.util.VendorErrorConstants;
import org.egov.vendorregistory.util.VendorUtil;
import org.egov.vendorregistory.web.model.AuditDetails;
import org.egov.vendorregistory.web.model.VendorRequest;
import org.egov.vendorregistory.web.model.VendorSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VendorValidator {

	@Autowired
	private VendorConfiguration config;

	@Autowired
	private VendorUtil vendorUtil;

	@Autowired
	private OwnerService ownerService;

	@Autowired
	private BoundaryService boundaryService;

	public void validateSearch(RequestInfo requestInfo, VendorSearchCriteria criteria) {

		// Coz hear employee will be logged in to create vendor so..
		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase(VendorConstants.EMPLOYEE) && criteria.isEmpty())
			throw new CustomException(VendorErrorConstants.INVALID_SEARCH,
					"Search without any paramters is not allowed");
		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase(VendorConstants.EMPLOYEE) && !criteria.tenantIdOnly()
				&& criteria.getTenantId() == null)
			throw new CustomException(VendorErrorConstants.INVALID_SEARCH, "TenantId is mandatory in search");

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(VendorConstants.EMPLOYEE) && !criteria.isEmpty()
				&& !criteria.tenantIdOnly() && criteria.getTenantId() == null)
			throw new CustomException(VendorErrorConstants.INVALID_SEARCH, "TenantId is mandatory in search");
		if (criteria.getTenantId() == null)
			throw new CustomException(VendorErrorConstants.INVALID_SEARCH, "TenantId is mandatory in search");

		String allowedParamStr = null;

		// I am in doute
		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(VendorConstants.EMPLOYEE))
			allowedParamStr = config.getAllowedEmployeeSearchParameters();
		else if (requestInfo.getUserInfo().getType().equalsIgnoreCase(VendorConstants.EMPLOYEE))
			allowedParamStr = config.getAllowedEmployeeSearchParameters();
		else
			throw new CustomException(VendorErrorConstants.INVALID_SEARCH,
					"The userType: " + requestInfo.getUserInfo().getType() + " does not have any search config");
		if (StringUtils.isEmpty(allowedParamStr) && !criteria.isEmpty())
			throw new CustomException(VendorErrorConstants.INVALID_SEARCH, "No search parameters are expected");
		else {
			List<String> allowedParams = Arrays.asList(allowedParamStr.split(","));
			validateSearchParams(criteria, allowedParams);
		}

	}

	private void validateSearchParams(VendorSearchCriteria criteria, List<String> allowedParams) {
		if (criteria.getOwnerIds() != null && !allowedParams.contains("ownerIds"))
			throw new CustomException(VendorErrorConstants.INVALID_SEARCH, "Search on ownerIds is not allowed");
		if (criteria.getOffset() != null && !allowedParams.contains("offset"))
			throw new CustomException(VendorErrorConstants.INVALID_SEARCH, "Search on offset is not allowed");
		if (criteria.getLimit() != null && !allowedParams.contains("limit"))
			throw new CustomException(VendorErrorConstants.INVALID_SEARCH, "Search on limit is not allowed");
		if (criteria.getOwnerName() != null && !allowedParams.contains("ownerName")) {
			throw new CustomException(VendorErrorConstants.INVALID_SEARCH, "Search on ownerName is not allowed");
		}
		if (criteria.getTenantId() != null && !allowedParams.contains("tenantId")) {
			throw new CustomException(VendorErrorConstants.INVALID_SEARCH, "Search on tenantid is not allowed");
		}
		if (criteria.getIds() != null && !allowedParams.contains("ids"))
			throw new CustomException(VendorErrorConstants.INVALID_SEARCH, "Search on ids is not allowed");

	}

	public void validateCreate(VendorRequest vendorRequest) {

		RequestInfo requestInfo = vendorRequest.getRequestInfo();

		ownerService.manageOwner(vendorRequest);
		//boundaryService.getAreaType(vendorRequest, config.getHierarchyTypeCode());

		// fsmRequest.getFsm().setApplicationStatus(FSMConstants.DRAFT);
		AuditDetails auditDetails = vendorUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		vendorRequest.getVendor().setAuditDetails(auditDetails);

		// vendorRequest.getVendor().setId(UUID.randomUUID().toString());
		// vendorRequest.getVendor().setOwner(vendorRequest.getVendor().getOwner().getUuid());

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

	}
}
