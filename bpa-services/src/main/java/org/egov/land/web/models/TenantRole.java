package org.egov.land.web.models;

import java.util.List;

import org.springframework.validation.annotation.Validated;

import com.google.gson.annotations.SerializedName;

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
public class TenantRole {
	  @SerializedName("tenantId")
	  private String tenantId;

	  @SerializedName("roles")
	  private List<Role> roles;

}
