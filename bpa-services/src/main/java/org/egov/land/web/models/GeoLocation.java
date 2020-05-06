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
public class GeoLocation {
	@JsonProperty("latitude")
	  private Double latitude;

	  @JsonProperty("longitude")
	  private Double longitude;

	  @JsonProperty("additionalDetails")
	  private Object additionalDetails;

}
