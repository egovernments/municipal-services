package org.egov.swCalculation.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * SewerageConnection
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-02T14:54:15.890+05:30[Asia/Kolkata]")
public class SewerageConnection extends Connection  {
  @JsonProperty("connectionExecutionDate")
  private BigDecimal connectionExecutionDate = null;
  
  @JsonProperty("proposedWaterClosets")
  private Integer proposedWaterClosets = null;

  @JsonProperty("proposedToilets")
  private Integer proposedToilets = null;

  @JsonProperty("noOfWaterClosets")
  private Integer noOfWaterClosets = null;

  @JsonProperty("noOfToilets")
  private Integer noOfToilets = null;

  @JsonProperty("uom")
  private String uom = null;

  @JsonProperty("calculationAttribute")
  private String calculationAttribute = null;

  @JsonProperty("connectionType")
  private String connectionType = null;

  public SewerageConnection connectionExecutionDate(BigDecimal connectionExecutionDate) {
    this.connectionExecutionDate = connectionExecutionDate;
    return this;
  }

  /**
   * Get connectionExecutionDate
   * @return connectionExecutionDate
  **/
  @ApiModelProperty(required = true, readOnly = true, value = "")
  @Valid
  public BigDecimal getConnectionExecutionDate() {
    return connectionExecutionDate;
  }

  public void setConnectionExecutionDate(BigDecimal connectionExecutionDate) {
    this.connectionExecutionDate = connectionExecutionDate;
  }

  public SewerageConnection noOfWaterClosets(Integer noOfWaterClosets) {
    this.noOfWaterClosets = noOfWaterClosets;
    return this;
  }

  /**
   * No of taps for water closets of calculation attribute.
   * @return noOfWaterClosets
  **/
  @ApiModelProperty(value = "No of taps for water closets of calculation attribute.")

  public Integer getNoOfWaterClosets() {
    return noOfWaterClosets;
  }

  public void setNoOfWaterClosets(Integer noOfWaterClosets) {
    this.noOfWaterClosets = noOfWaterClosets;
  }

  public SewerageConnection noOfToilets(Integer noOfToilets) {
    this.noOfToilets = noOfToilets;
    return this;
  }

  /**
   * No of taps for toilets of calculation attribute.
   * @return noOfToilets
  **/
  @ApiModelProperty(value = "No of taps for toilets of calculation attribute.")

  public Integer getNoOfToilets() {
    return noOfToilets;
  }

  public void setNoOfToilets(Integer noOfToilets) {
    this.noOfToilets = noOfToilets;
  }

  public SewerageConnection uom(String uom) {
    this.uom = uom;
    return this;
  }

  
  @ApiModelProperty(value = "No of proposed water closets")

  public Integer getProposedWaterClosets() {
    return proposedWaterClosets;
  }

  public void setProposedWaterClosets(Integer proposedWaterClosets) {
    this.proposedWaterClosets = proposedWaterClosets;
  }

  public SewerageConnection proposedWaterClosets(Integer proposedWaterClosets) {
    this.proposedWaterClosets = proposedWaterClosets;
    return this;
  }

  
  @ApiModelProperty(value = "No of proposed toilets")

  public Integer getProposedToilets() {
    return proposedToilets;
  }

  public void setProposedToilets(Integer proposedToilets) {
    this.proposedToilets = proposedToilets;
  }

  public SewerageConnection proposedToilets(Integer proposedToilets) {
    this.proposedToilets = proposedToilets;
    return this;
  }

  /**
   * The Unit of measurement for sewarage connection.
   * @return uom
  **/
  @ApiModelProperty(required = true, value = "The Unit of measurement for sewarage connection.")
@Size(min=2,max=32)   public String getUom() {
    return uom;
  }

  public void setUom(String uom) {
    this.uom = uom;
  }

  public SewerageConnection calculationAttribute(String calculationAttribute) {
    this.calculationAttribute = calculationAttribute;
    return this;
  }

  /**
   * The calculation attribute of sewarage connection.
   * @return calculationAttribute
  **/
  @ApiModelProperty(required = true, value = "The calculation attribute of sewarage connection.")

@Size(min=2,max=32)   public String getCalculationAttribute() {
    return calculationAttribute;
  }

  public void setCalculationAttribute(String calculationAttribute) {
    this.calculationAttribute = calculationAttribute;
  }

  public SewerageConnection connectionType(String connectionType) {
    this.connectionType = connectionType;
    return this;
  }

  /**
   * It is a master data, defined in MDMS.
   * @return connectionType
  **/
  @ApiModelProperty(required = true, value = "It is a master data, defined in MDMS.")

@Size(min=2,max=32)   public String getConnectionType() {
    return connectionType;
  }

  public void setConnectionType(String connectionType) {
    this.connectionType = connectionType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SewerageConnection sewerageConnection = (SewerageConnection) o;
    return Objects.equals(this.connectionExecutionDate, sewerageConnection.connectionExecutionDate) &&
        Objects.equals(this.noOfWaterClosets, sewerageConnection.noOfWaterClosets) &&
        Objects.equals(this.noOfToilets, sewerageConnection.noOfToilets) &&
        Objects.equals(this.uom, sewerageConnection.uom) &&
        Objects.equals(this.calculationAttribute, sewerageConnection.calculationAttribute) &&
        Objects.equals(this.connectionType, sewerageConnection.connectionType) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(connectionExecutionDate, noOfWaterClosets, noOfToilets, uom, calculationAttribute, connectionType, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SewerageConnection {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    connectionExecutionDate: ").append(toIndentedString(connectionExecutionDate)).append("\n");
    sb.append("    noOfWaterClosets: ").append(toIndentedString(noOfWaterClosets)).append("\n");
    sb.append("    noOfToilets: ").append(toIndentedString(noOfToilets)).append("\n");
    sb.append("    uom: ").append(toIndentedString(uom)).append("\n");
    sb.append("    calculationAttribute: ").append(toIndentedString(calculationAttribute)).append("\n");
    sb.append("    connectionType: ").append(toIndentedString(connectionType)).append("\n");
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
