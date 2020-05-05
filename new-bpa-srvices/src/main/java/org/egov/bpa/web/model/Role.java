package org.egov.bpa.web.model;

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
public class Role {
	 @JsonProperty("name")
	  private String name = null;

	  @JsonProperty("code")
	  private String code = null;

	  @JsonProperty("description")
	  private String description = null;

}
