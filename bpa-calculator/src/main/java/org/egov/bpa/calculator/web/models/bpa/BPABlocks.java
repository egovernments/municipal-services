package org.egov.bpa.calculator.web.models.bpa;

import javax.validation.constraints.Size;

import org.egov.bpa.calculator.web.models.AuditDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BPABlocks {

	@Size(max = 64)
	@JsonProperty("id")
	private String id;
	
	@Size(max = 64)
	@JsonProperty("subOccupancyType")
	private String subOccupancyType;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;
}
