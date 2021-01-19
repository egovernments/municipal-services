package org.egov.fsm.calculator.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import org.egov.common.contract.request.User;
import org.egov.fsm.calculator.web.models.AuditDetails;
import org.egov.fsm.calculator.web.models.Address;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * cature the pit details 
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-12-23T12:08:13.326Z[GMT]")


public class PitDetail   {
  @JsonProperty("citizen")
  private User citizen = null;

  @JsonProperty("id")
  private String id = null;

  @JsonProperty("tenantId")
  private String tenantId = null;
  
  @JsonProperty("height")
  private Double height = null;

  @JsonProperty("length")
  private Double length = null;

  @JsonProperty("width")
  private Double width = null;

  @JsonProperty("distanceFromRoad")
  private Double distanceFromRoad = null;

  @JsonProperty("auditDetails")
  private AuditDetails auditDetails = null;

  public PitDetail citizen(User citizen) {
    this.citizen = citizen;
    return this;
  }

  /**
   * Get citizen
   * @return citizen
   **/
  
    @Valid
    public User getCitizen() {
    return citizen;
  }

  public void setCitizen(User citizen) {
    this.citizen = citizen;
  }

  public PitDetail id(String id) {
    this.id = id;
    return this;
  }

  /**
   * The server generated unique ID(UUID).
   * @return id
   **/
  
  @Size(min=2,max=64)   public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public PitDetail height(Double height) {
    this.height = height;
    return this;
  }

  

  /**
   * Unique Identifier of the tenant to which user primarily belongs
   * @return tenantId
   **/
  
      

    public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }


  /**
   * Hight of the PIT in meter
   * @return height
   **/
      @NotNull

    public Double getHeight() {
    return height;
  }

  public void setHeight(Double height) {
    this.height = height;
  }

  public PitDetail length(Double length) {
    this.length = length;
    return this;
  }

  /**
   * Length of the PIT in meter
   * @return length
   **/
      @NotNull

    public Double getLength() {
    return length;
  }

  public void setLength(Double length) {
    this.length = length;
  }

  public PitDetail width(Double width) {
    this.width = width;
    return this;
  }

  /**
   * Width of the PIT in meter
   * @return width
   **/
      @NotNull

    public Double getWidth() {
    return width;
  }

  public void setWidth(Double width) {
    this.width = width;
  }

  public PitDetail distanceFromRoad(Double distanceFromRoad) {
    this.distanceFromRoad = distanceFromRoad;
    return this;
  }

  /**
   * Distance of the PIT from road in meter
   * @return distanceFromRoad
   **/
      @NotNull

    public Double getDistanceFromRoad() {
    return distanceFromRoad;
  }

  public void setDistanceFromRoad(Double distanceFromRoad) {
    this.distanceFromRoad = distanceFromRoad;
  }

  public PitDetail auditDetails(AuditDetails auditDetails) {
    this.auditDetails = auditDetails;
    return this;
  }

  /**
   * Get auditDetails
   * @return auditDetails
   **/
  
    @Valid
    public AuditDetails getAuditDetails() {
    return auditDetails;
  }

  public void setAuditDetails(AuditDetails auditDetails) {
    this.auditDetails = auditDetails;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PitDetail pitDetail = (PitDetail) o;
    return Objects.equals(this.citizen, pitDetail.citizen) &&
        Objects.equals(this.id, pitDetail.id) &&
        Objects.equals(this.height, pitDetail.height) &&
        Objects.equals(this.length, pitDetail.length) &&
        Objects.equals(this.width, pitDetail.width) &&
        Objects.equals(this.distanceFromRoad, pitDetail.distanceFromRoad) &&
        Objects.equals(this.auditDetails, pitDetail.auditDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(citizen, id, height, length, width, distanceFromRoad, auditDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PitDetail {\n");
    
    sb.append("    citizen: ").append(toIndentedString(citizen)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    height: ").append(toIndentedString(height)).append("\n");
    sb.append("    length: ").append(toIndentedString(length)).append("\n");
    sb.append("    width: ").append(toIndentedString(width)).append("\n");
    sb.append("    distanceFromRoad: ").append(toIndentedString(distanceFromRoad)).append("\n");
    sb.append("    auditDetails: ").append(toIndentedString(auditDetails)).append("\n");
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
