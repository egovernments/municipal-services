package org.egov.vendorregistory.web.model.owner;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class CreateOwnerRequest {

	@JsonProperty("requestInfo")
	private RequestInfo requestInfo;

	@JsonProperty("owner")
	private User owner;
}
