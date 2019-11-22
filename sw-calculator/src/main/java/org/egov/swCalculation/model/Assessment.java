package org.egov.swCalculation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assessment {

	private String uuid;

	private String assessmentNumber;

	private String connectionId;

	private String assessmentYear;

	private String demandId;

	private String tenantId;

	private AuditDetails auditDetails;

}
