package org.egov.bpa.web.model.landInfo;

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
	  private String name;

	  @JsonProperty("code")
	  private String code;

	  @JsonProperty("description")
	  private String description;

}
