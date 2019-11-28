package org.egov.waterConnection.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.egov.waterConnection.model.Connection;
import org.egov.waterConnection.model.Document;
import org.egov.waterConnection.model.Property;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * WaterConnection
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-10-24T10:29:25.253+05:30[Asia/Kolkata]")
public class WaterConnection extends Connection {
	@JsonProperty("connectionCategory")
	private String connectionCategory = null;

	@JsonProperty("rainWaterHarvesting")
	private Boolean rainWaterHarvesting = null;

	@JsonProperty("connectionType")
	private String connectionType = null;

	@JsonProperty("waterSource")
	private String waterSource = null;

	@JsonProperty("meterId")
	private String meterId = null;

	@JsonProperty("meterInstallationDate")
	private Long meterInstallationDate = null;
	
	@JsonProperty("pipeSize")
	private Double pipeSize;
	
	@JsonProperty("noOfTaps")
	private Integer noOfTaps;
	
	@JsonProperty("waterSubSource")
	private String waterSubSource = null;
	
	@JsonProperty("uom")
	private String uom = null;
	

	public Double getPipeSize() {
		return pipeSize;
	}

	public void setPipeSize(Double pipeSize) {
		this.pipeSize = pipeSize;
	}

	public void setNoOfTaps(Integer noOfTaps) {
		this.noOfTaps = noOfTaps;
	}

	public void setWaterSubSource(String waterSubSource) {
		this.waterSubSource = waterSubSource;
	}
	
	public Integer getNoOfTaps() {
		return noOfTaps;
	}

	public String getWaterSubSource() {
		return waterSubSource;
	}
	
	public void setUom(String uom) {
		this.uom = uom;
	}
	public String getUom() {
	return uom;
	}
	
	public WaterConnection pipeSize(Double pipeSize) {
		this.pipeSize = pipeSize;
		return this;
	}
	
	public WaterConnection noOfTaps(Integer noOfTaps) {
		this.noOfTaps = noOfTaps;
		return this;
	}
	
	public WaterConnection waterSubSource(String waterSubSource) {
		this.waterSubSource = waterSubSource;
		return this;
	}
	public WaterConnection uOM(String uom) {
		this.uom = uom;
		return this;
	}
	public WaterConnection connectionCategory(String connectionCategory) {
		this.connectionCategory = connectionCategory;
		return this;
	}

	/**
	 * It is a master data, defined in MDMS
	 * 
	 * @return connectionCategory
	 **/
	@ApiModelProperty(required = true, value = "It is a master data, defined in MDMS")
	@NotNull

	@Size(min = 2, max = 32)
	public String getConnectionCategory() {
		return connectionCategory;
	}

	public void setConnectionCategory(String connectionCategory) {
		this.connectionCategory = connectionCategory;
	}

	public WaterConnection rainWaterHarvesting(Boolean rainWaterHarvesting) {
		this.rainWaterHarvesting = rainWaterHarvesting;
		return this;
	}

	/**
	 * Get rainWaterHarvesting
	 * 
	 * @return rainWaterHarvesting
	 **/
	@ApiModelProperty(value = "")

	public Boolean isRainWaterHarvesting() {
		return rainWaterHarvesting;
	}

	public void setRainWaterHarvesting(Boolean rainWaterHarvesting) {
		this.rainWaterHarvesting = rainWaterHarvesting;
	}

	public WaterConnection connectionType(String connectionType) {
		this.connectionType = connectionType;
		return this;
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

	public WaterConnection waterSource(String waterSource) {
		this.waterSource = waterSource;
		return this;
	}

	/**
	 * It is a namespaced master data, defined in MDMS
	 * 
	 * @return waterSource
	 **/
	@ApiModelProperty(required = true, value = "It is a namespaced master data, defined in MDMS")
	@NotNull

	@Size(min = 2, max = 64)
	public String getWaterSource() {
		return waterSource;
	}

	public void setWaterSource(String waterSource) {
		this.waterSource = waterSource;
	}

	public WaterConnection meterId(String meterId) {
		this.meterId = meterId;
		return this;
	}

	/**
	 * Unique id of the meter.
	 * 
	 * @return meterId
	 **/
	@ApiModelProperty(value = "Unique id of the meter.")

	@Size(min = 2, max = 64)
	public String getMeterId() {
		return meterId;
	}

	public void setMeterId(String meterId) {
		this.meterId = meterId;
	}

	public WaterConnection meterInstallationDate(Long meterInstallationDate) {
		this.meterInstallationDate = meterInstallationDate;
		return this;
	}

	/**
	 * The date of meter installation date.
	 * 
	 * @return meterInstallationDate
	 **/
	@ApiModelProperty(value = "The date of meter installation date.")

	public Long getMeterInstallationDate() {
		return meterInstallationDate;
	}

	public void setMeterInstallationDate(Long meterInstallationDate) {
		this.meterInstallationDate = meterInstallationDate;
	}

	// public WaterConnection documents(List<Document> documents) {
	// this.documents = documents;
	// return this;
	// }
	//
	// public WaterConnection addDocumentsItem(Document documentsItem) {
	// if (this.documents == null) {
	// this.documents = new ArrayList<Document>();
	// }
	// this.documents.add(documentsItem);
	// return this;
	// }

	/**
	 * The documents attached by owner for exemption.
	 * 
	 * @return documents
	 **/
	// @ApiModelProperty(value = "The documents attached by owner for
	// exemption.")
	// @Valid
	// public List<Document> getDocuments() {
	// return documents;
	// }
	//
	// public void setDocuments(List<Document> documents) {
	// this.documents = documents;
	// }

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		WaterConnection waterConnection = (WaterConnection) o;
		return Objects.equals(this.connectionCategory, waterConnection.connectionCategory)
				&& Objects.equals(this.rainWaterHarvesting, waterConnection.rainWaterHarvesting)
				&& Objects.equals(this.connectionType, waterConnection.connectionType)
				&& Objects.equals(this.waterSource, waterConnection.waterSource)
				&& Objects.equals(this.meterId, waterConnection.meterId)
				&& Objects.equals(this.meterInstallationDate, waterConnection.meterInstallationDate) &&
				// Objects.equals(this.documents, waterConnection.documents) &&
				super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(connectionCategory, rainWaterHarvesting, connectionType, waterSource, meterId,
				meterInstallationDate, super.hashCode());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class WaterConnection {\n");
		sb.append("    ").append(toIndentedString(super.toString())).append("\n");
		sb.append("    connectionCategory: ").append(toIndentedString(connectionCategory)).append("\n");
		sb.append("    rainWaterHarvesting: ").append(toIndentedString(rainWaterHarvesting)).append("\n");
		sb.append("    connectionType: ").append(toIndentedString(connectionType)).append("\n");
		sb.append("    waterSource: ").append(toIndentedString(waterSource)).append("\n");
		sb.append("    meterId: ").append(toIndentedString(meterId)).append("\n");
		sb.append("    meterInstallationDate: ").append(toIndentedString(meterInstallationDate)).append("\n");
		// sb.append(" documents:
		// ").append(toIndentedString(documents)).append("\n");
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
