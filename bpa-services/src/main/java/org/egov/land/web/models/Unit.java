package org.egov.land.web.models;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {
	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("floorNo")
	private String floorNo;

	@JsonProperty("unitType")
	private String unitType;

	@JsonProperty("usageCategory")
	private String usageCategory;

	@JsonProperty("occupancyType")
	private OccupancyType occupancyType;

	@JsonProperty("occupancyDate")
	private Long occupancyDate;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

}
