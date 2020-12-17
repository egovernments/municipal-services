package org.egov.fsm.web.model;

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
 * Contains information about the device used to access the api
 */

@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-12-09T07:13:46.742Z[GMT]")


public class DeviceDetail   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("signature")
  private String signature = null;

  public DeviceDetail id(String id) {
    this.id = id;
    return this;
  }

  /**
   * The id of the device which is trying to accessed
   * @return id
   **/

  
    public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public DeviceDetail signature(String signature) {
    this.signature = signature;
    return this;
  }

  /**
   * The electronic signature of the device
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
    DeviceDetail deviceDetail = (DeviceDetail) o;
    return Objects.equals(this.id, deviceDetail.id) &&
        Objects.equals(this.signature, deviceDetail.signature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, signature);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeviceDetail {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
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
