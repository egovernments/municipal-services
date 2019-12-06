package org.egov.bpa.web.models;

import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;
@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {

	@JsonProperty("id")
	private String id;
	
	@Size(min = 2, max = 256)
	@JsonProperty("tenantId")
	private String tenantId;
	
	@Size(max = 64)
	@JsonProperty("usageCategory")
	private String usageCategory;
	
	@JsonProperty("additionalDetails")
	private Object additionalDetails;	
	
	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;
	
	@JsonProperty("active")
	private Boolean active;
}
