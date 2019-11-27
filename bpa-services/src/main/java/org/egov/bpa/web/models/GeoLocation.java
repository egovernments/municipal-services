package org.egov.bpa.web.models;

import lombok.AllArgsConstructor;
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
public class GeoLocation {

	@JsonProperty("latitude")
	 private Double latitude;
	
	@JsonProperty("longitude")
	 private Double longitude;
	 
	 @JsonProperty("additionalDetails")
	 private Object additionalDetails;
}
