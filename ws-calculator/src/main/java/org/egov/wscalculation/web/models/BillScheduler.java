package org.egov.wscalculation.web.models;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillScheduler {

	@JsonProperty("id")
	private String id = null;

	private String transactionType;

	@NotNull
	private String locality;

	@NotNull
	private long billingcycleStartdate;

	@NotNull
	private long billingcycleEnddate;

	private BillStatus status;

	private AuditDetails auditDetails;

	@NotNull
	private String tenantId;
}
