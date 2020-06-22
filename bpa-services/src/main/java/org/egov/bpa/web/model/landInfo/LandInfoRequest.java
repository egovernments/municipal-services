package org.egov.bpa.web.model.landInfo;

import org.egov.common.contract.request.RequestInfo;
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
public class LandInfoRequest {
	@JsonProperty("RequestInfo")
	  private RequestInfo requestInfo;

	  @JsonProperty("landInfo")
	  private LandInfo landInfo;
}
