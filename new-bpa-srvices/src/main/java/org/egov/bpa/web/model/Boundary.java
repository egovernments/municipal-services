package org.egov.bpa.web.model;

import java.util.List;

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
public class Boundary {
	@JsonProperty("code")
	  private String code = null;

	  @JsonProperty("name")
	  private String name = null;

	  @JsonProperty("label")
	  private String label = null;

	  @JsonProperty("latitude")
	  private String latitude = null;

	  @JsonProperty("longitude")
	  private String longitude = null;

	  @JsonProperty("children")
	  private List<Boundary> children = null;

	  @JsonProperty("materializedPath")
	  private String materializedPath = null;

}
