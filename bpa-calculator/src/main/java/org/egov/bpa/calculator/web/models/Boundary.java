package org.egov.bpa.calculator.web.models;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Boundary {

	@JsonProperty("code")
	private String code;

	@JsonProperty("name")
	private String name;

	@JsonProperty("label")
	private String label;

	@JsonProperty("latitude")
	private String latitude;

	@JsonProperty("longitude")
	private String longitude;

	@JsonProperty("children")
	private Set<String> children;
}
