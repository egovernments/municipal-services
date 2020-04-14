package org.egov.bpa.web.models;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
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
