package org.egov.swService.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.util.List;
import org.egov.swService.model.Connection;
import org.egov.swService.model.Document;
import org.egov.swService.model.Property;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * SewerageConnection
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-10-24T10:29:25.253+05:30[Asia/Kolkata]")
public class SewerageConnection extends Connection {
	@JsonProperty("connectionExecutionDate")
	private BigDecimal connectionExecutionDate = null;
	
	@JsonProperty("noOfWaterClosets")
	private Integer noOfWaterClosets;
	
	@JsonProperty("noOfToilets")
	private Integer noOfToilets;
	
	@JsonProperty("uom")
	private String uom = null;
	
	@JsonProperty("calculationAttribute")
	private String calculationAttribute = null;
	
	@JsonProperty("connectionType")
	private String connectionType = null;
	
	
	public void setNoOfToilets(Integer noOfToilets) {
		this.noOfToilets = noOfToilets;
	}
	
	public Integer getNoOfToilets() {
	return noOfToilets;
	}
	
	public void setNoOfWaterClosets(Integer noOfWaterClosets) {
		this.noOfWaterClosets = noOfWaterClosets;
	}
	
	public Integer getNoOfWaterClosets() {
	return noOfWaterClosets;
	}
	
	public void setUom(String uom) {
		this.uom = uom;
	}
	
	public String getUom() {
	return uom;
	}
	
	/**
	 * It is a master data, defined in MDMS.
	 * 
	 * @return connectionType
	 **/
	@ApiModelProperty(required = true, value = "It is a master data, defined in MDMS.")
	@NotNull

	@Size(min = 2, max = 32)
	public String getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}
	
	public void setCalculationAttribute(String calculationAttribute) {
		this.calculationAttribute = calculationAttribute;
	}
	
	public String getCalculationAttribute() {
		return calculationAttribute;
	}
	
	public SewerageConnection noOfToilets(Integer noOfToilets) {
		this.noOfToilets = noOfToilets;
		return this;
	}
	
	
	public SewerageConnection noOfWaterClosets(Integer noOfWaterClosets) {
		this.noOfWaterClosets = noOfWaterClosets;
		return this;
	}
	
	public SewerageConnection calculationAttribute(String calculationAttribute) {
		this.calculationAttribute = calculationAttribute;
		return this;
	}
	public SewerageConnection uOM(String uom) {
		this.uom = uom;
		return this;
	}
	
	public SewerageConnection connectionType(String connectionType) {
		this.connectionType = connectionType;
		return this;
	}
	
	public SewerageConnection connectionExecutionDate(BigDecimal connectionExecutionDate) {
		this.connectionExecutionDate = connectionExecutionDate;
		return this;
	}
	

	/**
	 * Get connectionExecutionDate
	 * 
	 * @return connectionExecutionDate
	 **/
	@ApiModelProperty(required = true, readOnly = true, value = "")
	@NotNull

	@Valid
	public BigDecimal getConnectionExecutionDate() {
		return connectionExecutionDate;
	}

	public void setConnectionExecutionDate(BigDecimal connectionExecutionDate) {
		this.connectionExecutionDate = connectionExecutionDate;
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
		return Objects.equals(this.connectionExecutionDate, sewerageConnection.connectionExecutionDate)
				&& super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(connectionExecutionDate, super.hashCode());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class SewerageConnection {\n");
		sb.append("    ").append(toIndentedString(super.toString())).append("\n");
		sb.append("    connectionExecutionDate: ").append(toIndentedString(connectionExecutionDate)).append("\n");
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
