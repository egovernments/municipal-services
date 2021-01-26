package org.egov.vendor.web.model.hrms;

import lombok.*;

import org.egov.vendor.web.model.AuditDetails;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
@EqualsAndHashCode(exclude = {"auditDetails"})
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
@ToString
@Builder
public class ReactivationDetails {
	
	private String id;

	@NotNull
	private String reasonForReactivation;
	
	private String orderNo;

	private String remarks;

	@NotNull
	private Long effectiveFrom;

	private String tenantId;

	private AuditDetails auditDetails;




}

