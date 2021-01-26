package org.egov.vendor.web.model.hrms;


import lombok.*;

import javax.validation.constraints.NotNull;

import org.egov.vendor.web.model.AuditDetails;
import org.springframework.validation.annotation.Validated;

@Validated
@EqualsAndHashCode(exclude = {"auditDetails"})
@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
@ToString
public class DepartmentalTest {

	private String id;

	@NotNull
	private String test;

	@NotNull
	private Long yearOfPassing;

	private String remarks;
	
	private String tenantId;
	
	private AuditDetails auditDetails;

	private Boolean isActive;

}
