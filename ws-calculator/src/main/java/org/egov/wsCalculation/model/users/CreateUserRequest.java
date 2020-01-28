package org.egov.wsCalculation.model.users;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wsCalculation.model.OwnerInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
@Setter
public class CreateUserRequest {

    @JsonProperty("requestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("user")
    private OwnerInfo user;

    
}


