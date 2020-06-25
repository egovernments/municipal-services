package org.egov.wscalculation.model.users;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.wscalculation.model.OwnerInfo;

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
    List<OwnerInfo> user;
}
