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
public class AuditDetails {
	 @JsonProperty("createdBy")
	  private String createdBy = null;

	  @JsonProperty("lastModifiedBy")
	  private String lastModifiedBy = null;

	  @JsonProperty("createdTime")
	  private Long createdTime = null;

	  @JsonProperty("lastModifiedTime")
	  private Long lastModifiedTime = null;

}
