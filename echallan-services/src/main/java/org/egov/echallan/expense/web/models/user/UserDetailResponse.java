package org.egov.echallan.expense.web.models.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.echallan.expense.model.UserInfo;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserDetailResponse {
    @JsonProperty("responseInfo")
    ResponseInfo responseInfo;

    @JsonProperty("user")
    List<UserInfo> user;
}
