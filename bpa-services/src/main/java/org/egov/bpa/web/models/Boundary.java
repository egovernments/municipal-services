package org.egov.bpa.web.models;

import java.util.Set;

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
