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
public class ServiceHistory {

	private String id;

	private String serviceStatus;

	private Long serviceFrom;

	private Long serviceTo;

	private String orderNo;
	
	private String location;
	
	private String tenantId;	

	private  Boolean isCurrentPosition;

	private AuditDetails auditDetails;



}
