package org.egov.bpa.web.model;

import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class BPAResponse {
	 @JsonProperty("ResponseInfo")
	  private ResponseInfo responseInfo;

	  @JsonProperty("BPA")
	  @Valid
	  private List<BPA> BPA;

}
