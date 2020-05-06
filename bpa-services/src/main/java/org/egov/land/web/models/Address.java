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
public class Address {
	  @JsonProperty("tenantId")
	  private String tenantId;

	  @JsonProperty("doorNo")
	  private String doorNo;

	  @JsonProperty("plotNo")
	  private String plotNo;

	  @JsonProperty("id")
	  private String id;

	  @JsonProperty("landmark")
	  private String landmark;

	  @JsonProperty("city")
	  private String city;

	  @JsonProperty("district")
	  private String district;

	  @JsonProperty("region")
	  private String region;

	  @JsonProperty("state")
	  private String state;

	  @JsonProperty("country")
	  private String country;

	  @JsonProperty("pincode")
	  private String pincode;

	  @JsonProperty("additionDetails")
	  private String additionDetails;

	  @JsonProperty("buildingName")
	  private String buildingName;

	  @JsonProperty("street")
	  private String street;

	  @JsonProperty("locality")
	  private Boundary locality;

	  @JsonProperty("geoLocation")
	  private GeoLocation geoLocation;

}
