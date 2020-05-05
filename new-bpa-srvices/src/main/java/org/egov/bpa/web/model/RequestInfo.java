package org.egov.bpa.web.model;

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
public class RequestInfo {
	@SerializedName("apiId")
	  private String apiId = null;

	  @SerializedName("ver")
	  private String ver = null;

	  @SerializedName("ts")
	  private Long ts = null;

	  @SerializedName("action")
	  private String action = null;

	  @SerializedName("did")
	  private String did = null;

	  @SerializedName("key")
	  private String key = null;

	  @SerializedName("msgId")
	  private String msgId = null;

	  @SerializedName("requesterId")
	  private String requesterId = null;

	  @SerializedName("authToken")
	  private String authToken = null;

	  @SerializedName("userInfo")
	  private UserInfo userInfo = null;

	  @SerializedName("correlationId")
	  private String correlationId = null;

}
