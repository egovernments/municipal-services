package org.egov.fsm.web.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * RequestHeader should be used to carry meta information about the requests to the server as described in the fields below. All eGov APIs will use requestHeader as a part of the request body to carry this meta information. Some of this information will be returned back from the server as part of the ResponseHeader in the response body to ensure correlation.
 */

@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-12-09T07:13:46.742Z[GMT]")


public class RequestHeader   {
  @JsonProperty("apiInfo")
  private String apiInfo = null;

  @JsonProperty("deviceDetail")
  private String deviceDetail = null;

  @JsonProperty("ts")
  private Long ts = null;

  @JsonProperty("action")
  private String action = null;

  @JsonProperty("key")
  private String key = null;

  @JsonProperty("msgId")
  private String msgId = null;

  @JsonProperty("requesterId")
  private String requesterId = null;

  @JsonProperty("authToken")
  private String authToken = null;

  @JsonProperty("userInfo")
  private String userInfo = null;

  @JsonProperty("correlationId")
  private String correlationId = null;

  @JsonProperty("signature")
  private String signature = null;

  public RequestHeader apiInfo(String apiInfo) {
    this.apiInfo = apiInfo;
    return this;
  }

  /**
   * Get apiInfo
   * @return apiInfo
   **/
  
  
    @Valid
    public String getApiInfo() {
    return apiInfo;
  }

  public void setApiInfo(String apiInfo) {
    this.apiInfo = apiInfo;
  }

  public RequestHeader deviceDetail(String deviceDetail) {
    this.deviceDetail = deviceDetail;
    return this;
  }

  /**
   * Get deviceDetail
   * @return deviceDetail
   **/
  
  
    @Valid
    public String getDeviceDetail() {
    return deviceDetail;
  }

  public void setDeviceDetail(String deviceDetail) {
    this.deviceDetail = deviceDetail;
  }

  public RequestHeader ts(Long ts) {
    this.ts = ts;
    return this;
  }

  /**
   * time in epoch
   * @return ts
   **/
  
      @NotNull

    public Long getTs() {
    return ts;
  }

  public void setTs(Long ts) {
    this.ts = ts;
  }

  public RequestHeader action(String action) {
    this.action = action;
    return this;
  }

  /**
   * API action to be performed like _create, _update, _search (denoting POST, PUT, GET) or _oauth etc
   * @return action
   **/
  
      @NotNull

  @Size(max=32)   public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public RequestHeader key(String key) {
    this.key = key;
    return this;
  }

  /**
   * API key (API key provided to the caller in case of server to server communication)
   * @return key
   **/
  
  
  @Size(max=256)   public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public RequestHeader msgId(String msgId) {
    this.msgId = msgId;
    return this;
  }

  /**
   * Unique request message id from the caller
   * @return msgId
   **/
  
      @NotNull

  @Size(max=256)   public String getMsgId() {
    return msgId;
  }

  public void setMsgId(String msgId) {
    this.msgId = msgId;
  }

  public RequestHeader requesterId(String requesterId) {
    this.requesterId = requesterId;
    return this;
  }

  /**
   * UserId of the user calling
   * @return requesterId
   **/
  
  
  @Size(max=256)   public String getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }

  public RequestHeader authToken(String authToken) {
    this.authToken = authToken;
    return this;
  }

  /**
   * //session/jwt/saml token/oauth token - the usual value that would go into HTTP bearer token
   * @return authToken
   **/
  
  
    public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public RequestHeader userInfo(String userInfo) {
    this.userInfo = userInfo;
    return this;
  }

  /**
   * Get userInfo
   * @return userInfo
   **/
  
  
    @Valid
    public String getUserInfo() {
    return userInfo;
  }

  public void setUserInfo(String userInfo) {
    this.userInfo = userInfo;
  }

  public RequestHeader correlationId(String correlationId) {
    this.correlationId = correlationId;
    return this;
  }

  /**
   * Get correlationId
   * @return correlationId
   **/
  
  
    public String getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  public RequestHeader signature(String signature) {
    this.signature = signature;
    return this;
  }

  /**
   * Hash describing the current RequestHeader
   * @return signature
   **/
  
  
    public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RequestHeader requestHeader = (RequestHeader) o;
    return Objects.equals(this.apiInfo, requestHeader.apiInfo) &&
        Objects.equals(this.deviceDetail, requestHeader.deviceDetail) &&
        Objects.equals(this.ts, requestHeader.ts) &&
        Objects.equals(this.action, requestHeader.action) &&
        Objects.equals(this.key, requestHeader.key) &&
        Objects.equals(this.msgId, requestHeader.msgId) &&
        Objects.equals(this.requesterId, requestHeader.requesterId) &&
        Objects.equals(this.authToken, requestHeader.authToken) &&
        Objects.equals(this.userInfo, requestHeader.userInfo) &&
        Objects.equals(this.correlationId, requestHeader.correlationId) &&
        Objects.equals(this.signature, requestHeader.signature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiInfo, deviceDetail, ts, action, key, msgId, requesterId, authToken, userInfo, correlationId, signature);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RequestHeader {\n");
    
    sb.append("    apiInfo: ").append(toIndentedString(apiInfo)).append("\n");
    sb.append("    deviceDetail: ").append(toIndentedString(deviceDetail)).append("\n");
    sb.append("    ts: ").append(toIndentedString(ts)).append("\n");
    sb.append("    action: ").append(toIndentedString(action)).append("\n");
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    msgId: ").append(toIndentedString(msgId)).append("\n");
    sb.append("    requesterId: ").append(toIndentedString(requesterId)).append("\n");
    sb.append("    authToken: ").append(toIndentedString(authToken)).append("\n");
    sb.append("    userInfo: ").append(toIndentedString(userInfo)).append("\n");
    sb.append("    correlationId: ").append(toIndentedString(correlationId)).append("\n");
    sb.append("    signature: ").append(toIndentedString(signature)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
