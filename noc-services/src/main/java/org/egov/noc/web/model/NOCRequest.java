package org.egov.noc.web.model;

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
public class NOCRequest {
	
	  @JsonProperty("RequestInfo")
	  private RequestInfo requestInfo;

	  @JsonProperty("NOC")
	  private NOC noc;
	  
}
