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
