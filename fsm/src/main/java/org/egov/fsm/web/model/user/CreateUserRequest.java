package org.egov.fsm.web.model.user;

import org.egov.fsm.web.model.OwnerInfo;
import org.egov.fsm.web.model.UserInfo;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class CreateUserRequest {

    public CreateUserRequest(@Valid RequestInfo requestInfo2, OwnerInfo owner) {
		// TODO Auto-generated constructor stub
	}

	@JsonProperty("requestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("user")
    private UserInfo user;

}
