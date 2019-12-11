package org.egov.bpa.web.models;

import javax.validation.constraints.NotNull;
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

public class Address {
	@JsonProperty("id")
	private String id;
	
	@NotNull
	@JsonProperty("tenantId")
	private String tenantId;
	
	@JsonProperty("doorNo")
	private String doorNo;
	
	@JsonProperty("plotNo")
	private String plotNo;
	
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
	
	@Size(max = 64, min=2)
	@JsonProperty("buildingName")
	private String buildingName;
	
	@Size(max = 64, min=2)
	@JsonProperty("street")
	private String street;
	
	@NotNull
	@JsonProperty("locality")
	private Boundary locality;
	
	@JsonProperty("geoLocation")
	private GeoLocation geoLocation;
}
