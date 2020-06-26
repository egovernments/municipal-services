package org.egov.noc.web.model;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class NocResponse {
	 
	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;

	@JsonProperty("Noc")
	private List<Noc> noc;

}
