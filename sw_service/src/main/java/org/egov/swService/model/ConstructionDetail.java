package org.egov.swService.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.egov.waterConnection.model.AuditDetails;
import org.egov.waterConnection.model.Dimensions;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Construction/constructionDetail details are captured here. Detail information
 * of the constructionDetail including floor wise usage and area are saved as
 * seperate units .For each financial year construction details may change.
 * constructionDetail object is required for tax calculation
 */
@ApiModel(description = "Construction/constructionDetail details are captured here. Detail information of the constructionDetail including floor wise usage and area are saved as seperate units .For each financial year construction details may change. constructionDetail object is required for tax calculation")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-10-24T10:29:25.253+05:30[Asia/Kolkata]")
public class ConstructionDetail {
	@JsonProperty("id")
	private String id = null;

	@JsonProperty("totalUnitArea")
	private Float totalUnitArea = null;

	@JsonProperty("builtUpArea")
	private Float builtUpArea = null;

	@JsonProperty("carpetArea")
	private Float carpetArea = null;

	@JsonProperty("superBuiltUpArea")
	private Float superBuiltUpArea = null;

	@JsonProperty("constructionType")
	private String constructionType = null;

	@JsonProperty("constructionDate")
	private Long constructionDate = null;

	@JsonProperty("dimensions")
	private Dimensions dimensions = null;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

	@JsonProperty("additionalDetails")
	private Object additionalDetails = null;

	public ConstructionDetail id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * id of the property with which the constructionDetail is associated.
	 * 
	 * @return id
	 **/
	@ApiModelProperty(readOnly = true, value = "id of the property with which the constructionDetail is associated.")

	@Size(max = 64)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ConstructionDetail totalUnitArea(Float totalUnitArea) {
		this.totalUnitArea = totalUnitArea;
		return this;
	}

	/**
	 * Area of the defined Unit.
	 * 
	 * @return totalUnitArea
	 **/
	@ApiModelProperty(value = "Area of the defined Unit.")

	public Float getTotalUnitArea() {
		return totalUnitArea;
	}

	public void setTotalUnitArea(Float totalUnitArea) {
		this.totalUnitArea = totalUnitArea;
	}

	public ConstructionDetail builtUpArea(Float builtUpArea) {
		this.builtUpArea = builtUpArea;
		return this;
	}

	/**
	 * Total built up area in sq ft(built-up area = carpet area + areas covered
	 * by walls)
	 * 
	 * @return builtUpArea
	 **/
	@ApiModelProperty(value = "Total built up area in sq ft(built-up area = carpet area + areas covered by walls)")

	public Float getBuiltUpArea() {
		return builtUpArea;
	}

	public void setBuiltUpArea(Float builtUpArea) {
		this.builtUpArea = builtUpArea;
	}

	public ConstructionDetail carpetArea(Float carpetArea) {
		this.carpetArea = carpetArea;
		return this;
	}

	/**
	 * Total built up area in sq ft(built-up area = carpet area + areas covered
	 * by walls)
	 * 
	 * @return carpetArea
	 **/
	@ApiModelProperty(value = "Total built up area in sq ft(built-up area = carpet area + areas covered by walls)")

	public Float getCarpetArea() {
		return carpetArea;
	}

	public void setCarpetArea(Float carpetArea) {
		this.carpetArea = carpetArea;
	}

	public ConstructionDetail superBuiltUpArea(Float superBuiltUpArea) {
		this.superBuiltUpArea = superBuiltUpArea;
		return this;
	}

	/**
	 * Total built up area in sq ft(built-up area + Common area = Super built-up
	 * area)
	 * 
	 * @return superBuiltUpArea
	 **/
	@ApiModelProperty(value = "Total built up area in sq ft(built-up area + Common area = Super built-up area)")

	public Float getSuperBuiltUpArea() {
		return superBuiltUpArea;
	}

	public void setSuperBuiltUpArea(Float superBuiltUpArea) {
		this.superBuiltUpArea = superBuiltUpArea;
	}

	public ConstructionDetail constructionType(String constructionType) {
		this.constructionType = constructionType;
		return this;
	}

	/**
	 * Construction type is defined in MDMS ConstructionTypeMaster.
	 * 
	 * @return constructionType
	 **/
	@ApiModelProperty(value = "Construction type is defined in MDMS ConstructionTypeMaster.")

	@Size(min = 1, max = 64)
	public String getConstructionType() {
		return constructionType;
	}

	public void setConstructionType(String constructionType) {
		this.constructionType = constructionType;
	}

	public ConstructionDetail constructionDate(Long constructionDate) {
		this.constructionDate = constructionDate;
		return this;
	}

	/**
	 * The date when the property was constructed
	 * 
	 * @return constructionDate
	 **/
	@ApiModelProperty(value = "The date when the property was constructed")

	public Long getConstructionDate() {
		return constructionDate;
	}

	public void setConstructionDate(Long constructionDate) {
		this.constructionDate = constructionDate;
	}

	public ConstructionDetail dimensions(Dimensions dimensions) {
		this.dimensions = dimensions;
		return this;
	}

	/**
	 * Get dimensions
	 * 
	 * @return dimensions
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public Dimensions getDimensions() {
		return dimensions;
	}

	public void setDimensions(Dimensions dimensions) {
		this.dimensions = dimensions;
	}

	public ConstructionDetail auditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
		return this;
	}

	/**
	 * Get auditDetails
	 * 
	 * @return auditDetails
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public AuditDetails getAuditDetails() {
		return auditDetails;
	}

	public void setAuditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
	}

	public ConstructionDetail additionalDetails(Object additionalDetails) {
		this.additionalDetails = additionalDetails;
		return this;
	}

	/**
	 * The json (array of '#/definitions/Factor')
	 * 
	 * @return additionalDetails
	 **/
	@ApiModelProperty(value = "The json (array of '#/definitions/Factor')")

	public Object getAdditionalDetails() {
		return additionalDetails;
	}

	public void setAdditionalDetails(Object additionalDetails) {
		this.additionalDetails = additionalDetails;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ConstructionDetail constructionDetail = (ConstructionDetail) o;
		return Objects.equals(this.id, constructionDetail.id)
				&& Objects.equals(this.totalUnitArea, constructionDetail.totalUnitArea)
				&& Objects.equals(this.builtUpArea, constructionDetail.builtUpArea)
				&& Objects.equals(this.carpetArea, constructionDetail.carpetArea)
				&& Objects.equals(this.superBuiltUpArea, constructionDetail.superBuiltUpArea)
				&& Objects.equals(this.constructionType, constructionDetail.constructionType)
				&& Objects.equals(this.constructionDate, constructionDetail.constructionDate)
				&& Objects.equals(this.dimensions, constructionDetail.dimensions)
				&& Objects.equals(this.auditDetails, constructionDetail.auditDetails)
				&& Objects.equals(this.additionalDetails, constructionDetail.additionalDetails);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, totalUnitArea, builtUpArea, carpetArea, superBuiltUpArea, constructionType,
				constructionDate, dimensions, auditDetails, additionalDetails);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ConstructionDetail {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    totalUnitArea: ").append(toIndentedString(totalUnitArea)).append("\n");
		sb.append("    builtUpArea: ").append(toIndentedString(builtUpArea)).append("\n");
		sb.append("    carpetArea: ").append(toIndentedString(carpetArea)).append("\n");
		sb.append("    superBuiltUpArea: ").append(toIndentedString(superBuiltUpArea)).append("\n");
		sb.append("    constructionType: ").append(toIndentedString(constructionType)).append("\n");
		sb.append("    constructionDate: ").append(toIndentedString(constructionDate)).append("\n");
		sb.append("    dimensions: ").append(toIndentedString(dimensions)).append("\n");
		sb.append("    auditDetails: ").append(toIndentedString(auditDetails)).append("\n");
		sb.append("    additionalDetails: ").append(toIndentedString(additionalDetails)).append("\n");
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
