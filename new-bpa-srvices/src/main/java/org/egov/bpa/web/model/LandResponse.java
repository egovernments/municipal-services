package org.egov.bpa.web.model;

import org.egov.common.contract.response.ResponseInfo;
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
public class LandResponse {
	 @JsonProperty("ResponseInfo")
	  private ResponseInfo responseInfo;

	  @JsonProperty("LandInfo")
	  private LandInfo landInfo;
}
