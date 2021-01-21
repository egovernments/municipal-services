package org.egov.vendorregistory.web.model.owner;

import java.util.List;

import org.egov.common.contract.request.User;
import org.egov.common.contract.response.ResponseInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class VendorDetailResponse {

	@JsonProperty("responseInfo")
	ResponseInfo responseInfo;

	@JsonProperty("user")
	List<User> owner;

	public ResponseInfo getResponseInfo() {
		return responseInfo;
	}

	public void setResponseInfo(ResponseInfo responseInfo) {
		this.responseInfo = responseInfo;
	}

	public List<User> getOwner() {
		return owner;
	}

	public void setOwner(List<User> owner) {
		this.owner = owner;
	}
	
	
}
