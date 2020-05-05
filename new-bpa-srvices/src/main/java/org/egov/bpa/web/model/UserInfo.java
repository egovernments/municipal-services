package org.egov.bpa.web.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
	  @JsonProperty("tenantId")
	  private String tenantId = null;

	  @JsonProperty("uuid")
	  private String uuid = null;

	  @JsonProperty("userName")
	  private String userName = null;

	  @JsonProperty("password")
	  private String password = null;

	  @JsonProperty("idToken")
	  private String idToken = null;

	  @JsonProperty("mobile")
	  private String mobile = null;

	  @JsonProperty("email")
	  private String email = null;

	  @JsonProperty("primaryrole")
	  private List<Role> primaryrole = new ArrayList<Role>();

	  @JsonProperty("additionalroles")
	  private List<TenantRole> additionalroles = null;

}
