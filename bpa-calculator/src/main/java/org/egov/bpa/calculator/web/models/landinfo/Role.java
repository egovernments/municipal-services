package org.egov.bpa.calculator.web.models.landinfo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Role {
	@JsonProperty("name")
	private String name;

	@JsonProperty("code")
	private String code;

	@JsonProperty("description")
	private String description;
}
