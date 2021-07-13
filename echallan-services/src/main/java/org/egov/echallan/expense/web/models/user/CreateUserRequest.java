package org.egov.echallan.expense.web.models.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.expense.model.UserInfo;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class CreateUserRequest {

    @JsonProperty("requestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("user")
    private UserInfo user;

}


