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
 * minimal representation of the Roles in the system to be carried along in UserInfo with RequestHeader meta data. Actual authorization service to extend this to have more role related attributes 
 */

@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-12-09T07:13:46.742Z[GMT]")


public class Role   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("code")
  private String code = null;

  @JsonProperty("tenantId")
  private String tenantId = null;

  @JsonProperty("description")
  private String description = null;

  public Role name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Unique name of the role
   * @return name
   **/
  
  
  @Size(max=64)   public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Role code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Unique code of the role
   * @return code
   **/
  
      @NotNull

  @Size(max=64)   public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Role tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * The tenantId for which the role is available
   * @return tenantId
   **/
  
      @NotNull

  @Size(max=64)   public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Role description(String description) {
    this.description = description;
    return this;
  }

  /**
   * brief description of the role
   * @return description
   **/
  
  
    public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Role role = (Role) o;
    return Objects.equals(this.name, role.name) &&
        Objects.equals(this.code, role.code) &&
        Objects.equals(this.tenantId, role.tenantId) &&
        Objects.equals(this.description, role.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, code, tenantId, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Role {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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
