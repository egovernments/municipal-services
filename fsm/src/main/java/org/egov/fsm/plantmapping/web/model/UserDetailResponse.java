package org.egov.fsm.plantmapping.web.model;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.fsm.web.model.user.User;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserDetailResponse {

	@JsonProperty("responseInfo")
    ResponseInfo responseInfo;
	
    @JsonProperty("user")
    List<User> user;
}
