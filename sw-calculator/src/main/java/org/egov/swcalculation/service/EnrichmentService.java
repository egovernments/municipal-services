package org.egov.swcalculation.service;

import org.egov.swcalculation.web.models.AuditDetails;
import org.springframework.stereotype.Service;

@Service
public class EnrichmentService {

	/**
	 * Method to return auditDetails for create/update flows
	 *
	 * @param by
	 *            - UUID of the User
	 * @param isCreate
	 *            - TRUE in case of create scenario and FALSE for modify
	 *            scenario.
	 * @return AuditDetails
	 */
	public AuditDetails getAuditDetails(String by, Boolean isCreate) {
		Long time = System.currentTimeMillis();
		if (isCreate)
			return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time)
					.build();
		else
			return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
	}

}
