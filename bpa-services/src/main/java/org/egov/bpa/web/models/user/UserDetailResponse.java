package org.egov.bpa.web.models.user;

import java.util.List;

import org.egov.bpa.web.models.OwnerInfo;
import org.egov.common.contract.response.ResponseInfo;

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
