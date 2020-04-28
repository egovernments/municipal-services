package org.egov.bpa.web.models.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.egov.bpa.web.models.OwnerInfo;
import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class CreateUserRequest {

    @JsonProperty("requestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("user")
    private OwnerInfo user;

}
