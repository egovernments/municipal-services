package org.egov.bpa.web.models;

import java.util.List;

import javax.validation.Valid;

import lombok.Builder;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

@Builder
public class BPAResponse {
	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;

	@JsonProperty("Bpa")
	@Valid
	private List<BPA> BPA;

	/*public BPAResponse addBuildingPlanItem(BPA bpaReq) {
		if (this.BPA == null) {
			this.BPA = new ArrayList<>();
		}
		this.BPA.add(bpaReq);
		return this;
	}*/

}
