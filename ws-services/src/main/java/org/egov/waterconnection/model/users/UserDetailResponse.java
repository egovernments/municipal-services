package org.egov.waterconnection.model.users;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.waterconnection.model.OwnerInfo;
import org.egov.waterconnection.model.workflow.ConnectionHolderInfo;

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
    List<ConnectionHolderInfo> user;
}
