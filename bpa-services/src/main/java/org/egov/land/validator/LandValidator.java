package org.egov.land.validator;

import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;

import org.egov.bpa.util.BPAConstants;
import org.egov.common.contract.request.RequestInfo;
import org.egov.land.web.models.LandRequest;
import org.egov.land.web.models.LandSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LandValidator {

	// @Autowired
	// private MDMSValidator mdmsValidator;

	public void validateLandInfo(@Valid LandRequest landRequest) {
		// mdmsValidator.validateMdmsData(landRequest, mdmsData);
		validateApplicationDocuments(landRequest, null);
		validateUser(landRequest);
	}
	
	private void validateUser(@Valid LandRequest landRequest) {
		landRequest.getLandInfo().getOwners().forEach(owner -> {
			if (StringUtils.isEmpty(owner.getRelationship())) {
				throw new CustomException("BPA.CREATE.USER", " Owner relation ship is mandatory " + owner.toString());
			}
		});
	}

	private void validateApplicationDocuments(@Valid LandRequest landRequest, Object currentState) {
		if (landRequest.getLandInfo().getDocuments() != null) {
			List<String> documentFileStoreIds = new LinkedList<String>();
			landRequest.getLandInfo().getDocuments().forEach(document -> {
				if (documentFileStoreIds.contains(document.getFileStore()))
					throw new CustomException("BPA_DUPLICATE_DOCUMENT", "Same document cannot be used multiple times");
				else
					documentFileStoreIds.add(document.getFileStore());
			});
		}
	}

	/**
	 * Validates if the search parameters are valid
	 * 
	 * @param requestInfo
	 *            The requestInfo of the incoming request
	 * @param criteria
	 *            The LandSearch Criteria
	 */
	public void validateSearch(RequestInfo requestInfo, LandSearchCriteria criteria) {
		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.CITIZEN) && criteria.isEmpty())
			throw new CustomException(BPAConstants.INVALID_SEARCH, "Search without any paramters is not allowed");

		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.CITIZEN) && !criteria.tenantIdOnly()
				&& criteria.getTenantId() == null)
			throw new CustomException(BPAConstants.INVALID_SEARCH, "TenantId is mandatory in search");

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.CITIZEN) && !criteria.isEmpty()
				&& !criteria.tenantIdOnly() && criteria.getTenantId() == null)
			throw new CustomException(BPAConstants.INVALID_SEARCH, "TenantId is mandatory in search");
	}
}
