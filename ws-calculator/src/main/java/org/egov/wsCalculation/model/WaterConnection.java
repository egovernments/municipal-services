package org.egov.wsCalculation.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * WaterConnection
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-02T14:30:33.286+05:30[Asia/Kolkata]")
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
	private Double pipeSize = null;

	@JsonProperty("noOfTaps")
	private Integer noOfTaps = null;

	@JsonProperty("waterSubSource")
	private String waterSubSource = null;

	@JsonProperty("uom")
	private String uom = null;

	@JsonProperty("calculationAttribute")
	private String calculationAttribute = null;

	@JsonProperty("documents")
	@Valid
	private List<Document> documents = null;

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

	public WaterConnection pipeSize(Double pipeSize) {
		this.pipeSize = pipeSize;
		return this;
	}

	/**
	 * Pipe size for non-metered calulation attribute.
	 * 
	 * @return pipeSize
	 **/
	@ApiModelProperty(value = "Pipe size for non-metered calulation attribute.")

	public Double getPipeSize() {
		return pipeSize;
	}

	public void setPipeSize(Double pipeSize) {
		this.pipeSize = pipeSize;
	}

	public WaterConnection noOfTaps(Integer noOfTaps) {
		this.noOfTaps = noOfTaps;
		return this;
	}

	/**
	 * No of taps for non-metered calculation attribute.
	 * 
	 * @return noOfTaps
	 **/
	@ApiModelProperty(value = "No of taps for non-metered calculation attribute.")

	public Integer getNoOfTaps() {
		return noOfTaps;
	}

	public void setNoOfTaps(Integer noOfTaps) {
		this.noOfTaps = noOfTaps;
	}

	public WaterConnection waterSubSource(String waterSubSource) {
		this.waterSubSource = waterSubSource;
		return this;
	}

	/**
	 * It is a namespaced master data, defined in MDMS.
	 * 
	 * @return waterSubSource
	 **/
	@ApiModelProperty(value = "It is a namespaced master data, defined in MDMS.")

	@Size(min = 2, max = 64)
	public String getWaterSubSource() {
		return waterSubSource;
	}

	public void setWaterSubSource(String waterSubSource) {
		this.waterSubSource = waterSubSource;
	}

	public WaterConnection uom(String uom) {
		this.uom = uom;
		return this;
	}

	/**
	 * The Unit of measurement for water connection.
	 * 
	 * @return uom
	 **/
	@ApiModelProperty(required = true, value = "The Unit of measurement for water connection.")
	@NotNull

	@Size(min = 2, max = 32)
	public String getUom() {
		return uom;
	}

	public void setUom(String uom) {
		this.uom = uom;
	}

	public WaterConnection calculationAttribute(String calculationAttribute) {
		this.calculationAttribute = calculationAttribute;
		return this;
	}

	/**
	 * The calculation attribute of water connection.
	 * 
	 * @return calculationAttribute
	 **/
	@ApiModelProperty(required = true, value = "The calculation attribute of water connection.")
	@NotNull

	@Size(min = 2, max = 32)
	public String getCalculationAttribute() {
		return calculationAttribute;
	}

	public void setCalculationAttribute(String calculationAttribute) {
		this.calculationAttribute = calculationAttribute;
	}

	public WaterConnection documents(List<Document> documents) {
		this.documents = documents;
		return this;
	}

	public WaterConnection addDocumentsItem(Document documentsItem) {
		if (this.documents == null) {
			this.documents = new ArrayList<Document>();
		}
		this.documents.add(documentsItem);
		return this;
	}

	/**
	 * The documents attached by owner for exemption.
	 * 
	 * @return documents
	 **/
	@ApiModelProperty(value = "The documents attached by owner for exemption.")
	
	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

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
				&& Objects.equals(this.meterInstallationDate, waterConnection.meterInstallationDate)
				&& Objects.equals(this.pipeSize, waterConnection.pipeSize)
				&& Objects.equals(this.noOfTaps, waterConnection.noOfTaps)
				&& Objects.equals(this.waterSubSource, waterConnection.waterSubSource)
				&& Objects.equals(this.uom, waterConnection.uom)
				&& Objects.equals(this.calculationAttribute, waterConnection.calculationAttribute)
				&& Objects.equals(this.documents, waterConnection.documents) && super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(connectionCategory, rainWaterHarvesting, connectionType, waterSource, meterId,
				meterInstallationDate, pipeSize, noOfTaps, waterSubSource, uom, calculationAttribute, documents,
				super.hashCode());
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
		sb.append("    pipeSize: ").append(toIndentedString(pipeSize)).append("\n");
		sb.append("    noOfTaps: ").append(toIndentedString(noOfTaps)).append("\n");
		sb.append("    waterSubSource: ").append(toIndentedString(waterSubSource)).append("\n");
		sb.append("    uom: ").append(toIndentedString(uom)).append("\n");
		sb.append("    calculationAttribute: ").append(toIndentedString(calculationAttribute)).append("\n");
		sb.append("    documents: ").append(toIndentedString(documents)).append("\n");
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
