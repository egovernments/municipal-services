package org.egov.fsm.web.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import org.springframework.validation.annotation.Validated;

/**
 * Info of the API being called
 */

@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-12-09T07:13:46.742Z[GMT]")


public class APIInfo   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("version")
  private String version = null;

  @JsonProperty("path")
  private String path = null;

  public APIInfo id(String id) {
    this.id = id;
    return this;
  }

  /**
   * The id of the API being called
   * @return id
   **/
    public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public APIInfo version(String version) {
    this.version = version;
    return this;
  }

  /**
   * The version of the API
   * @return version
   **/
     public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public APIInfo path(String path) {
    this.path = path;
    return this;
  }

  /**
   * The URI of the API
   * @return path
   **/
  
  
    public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIInfo apIInfo = (APIInfo) o;
    return Objects.equals(this.id, apIInfo.id) &&
        Objects.equals(this.version, apIInfo.version) &&
        Objects.equals(this.path, apIInfo.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, version, path);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIInfo {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
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
