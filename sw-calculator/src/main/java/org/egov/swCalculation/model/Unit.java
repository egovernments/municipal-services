package org.egov.swCalculation.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Unit
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-10-24T10:29:25.253+05:30[Asia/Kolkata]")
public class Unit {
	@JsonProperty("id")
	private String id = null;

	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("floorNo")
	private String floorNo = null;

	@JsonProperty("unitType")
	private String unitType = null;

	@JsonProperty("usageCategory")
	private String usageCategory = null;

	/**
	 * Value denoting if the unit is rented or occupied by owner
	 */
	public enum OccupancyTypeEnum {
		OWNER("OWNER"),

		TENANT("TENANT");

		private String value;

		OccupancyTypeEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static OccupancyTypeEnum fromValue(String text) {
			for (OccupancyTypeEnum b : OccupancyTypeEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("occupancyType")
	private OccupancyTypeEnum occupancyType = null;

	@JsonProperty("occupancyDate")
	private BigDecimal occupancyDate = null;

	@JsonProperty("constructionDetail")
	private ConstructionDetail constructionDetail = null;

	@JsonProperty("additionalDetails")
	private Object additionalDetails = null;

	public Unit id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Unique Identifier of the Unit(UUID).
	 * 
	 * @return id
	 **/
	@ApiModelProperty(value = "Unique Identifier of the Unit(UUID).")

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Unit tenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}

	/**
	 * tenant id of the Property
	 * 
	 * @return tenantId
	 **/
	@ApiModelProperty(value = "tenant id of the Property")

	@Size(min = 2, max = 256)
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public Unit floorNo(String floorNo) {
		this.floorNo = floorNo;
		return this;
	}

	/**
	 * floor number of the Unit
	 * 
	 * @return floorNo
	 **/
	@ApiModelProperty(value = "floor number of the Unit")

	@Size(min = 1, max = 64)
	public String getFloorNo() {
		return floorNo;
	}

	public void setFloorNo(String floorNo) {
		this.floorNo = floorNo;
	}

	public Unit unitType(String unitType) {
		this.unitType = unitType;
		return this;
	}

	/**
	 * Unit type is master data.
	 * 
	 * @return unitType
	 **/
	@ApiModelProperty(example = "Building, Room, Kitchen etc.", value = "Unit type is master data. ")

	public String getUnitType() {
		return unitType;
	}

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

	public Unit usageCategory(String usageCategory) {
		this.usageCategory = usageCategory;
		return this;
	}

	/**
	 * This is about the usage of the property like Residential,
	 * Non-residential, Mixed(Property witch is gettiong used for Residential,
	 * Non-residential purpose)
	 * 
	 * @return usageCategory
	 **/
	@ApiModelProperty(value = "This is about the usage of the property like Residential, Non-residential, Mixed(Property witch is gettiong used for Residential, Non-residential purpose)")

	@Size(min = 1, max = 64)
	public String getUsageCategory() {
		return usageCategory;
	}

	public void setUsageCategory(String usageCategory) {
		this.usageCategory = usageCategory;
	}

	public Unit occupancyType(OccupancyTypeEnum occupancyType) {
		this.occupancyType = occupancyType;
		return this;
	}

	/**
	 * Value denoting if the unit is rented or occupied by owner
	 * 
	 * @return occupancyType
	 **/
	@ApiModelProperty(value = "Value denoting if the unit is rented or occupied by owner")

	public OccupancyTypeEnum getOccupancyType() {
		return occupancyType;
	}

	public void setOccupancyType(OccupancyTypeEnum occupancyType) {
		this.occupancyType = occupancyType;
	}

	public Unit occupancyDate(BigDecimal occupancyDate) {
		this.occupancyDate = occupancyDate;
		return this;
	}

	/**
	 * Date on which unit is occupied.
	 * 
	 * @return occupancyDate
	 **/
	@ApiModelProperty(value = "Date on which unit is occupied.")

	@Valid
	public BigDecimal getOccupancyDate() {
		return occupancyDate;
	}

	public void setOccupancyDate(BigDecimal occupancyDate) {
		this.occupancyDate = occupancyDate;
	}

	public Unit constructionDetail(ConstructionDetail constructionDetail) {
		this.constructionDetail = constructionDetail;
		return this;
	}

	/**
	 * Get constructionDetail
	 * 
	 * @return constructionDetail
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public ConstructionDetail getConstructionDetail() {
		return constructionDetail;
	}

	public void setConstructionDetail(ConstructionDetail constructionDetail) {
		this.constructionDetail = constructionDetail;
	}

	public Unit additionalDetails(Object additionalDetails) {
		this.additionalDetails = additionalDetails;
		return this;
	}

	/**
	 * Json object to capture any extra information which is not accommodated by
	 * model
	 * 
	 * @return additionalDetails
	 **/
	@ApiModelProperty(value = "Json object to capture any extra information which is not accommodated by model")

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
		Unit unit = (Unit) o;
		return Objects.equals(this.id, unit.id) && Objects.equals(this.tenantId, unit.tenantId)
				&& Objects.equals(this.floorNo, unit.floorNo) && Objects.equals(this.unitType, unit.unitType)
				&& Objects.equals(this.usageCategory, unit.usageCategory)
				&& Objects.equals(this.occupancyType, unit.occupancyType)
				&& Objects.equals(this.occupancyDate, unit.occupancyDate)
				&& Objects.equals(this.constructionDetail, unit.constructionDetail)
				&& Objects.equals(this.additionalDetails, unit.additionalDetails);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, tenantId, floorNo, unitType, usageCategory, occupancyType, occupancyDate,
				constructionDetail, additionalDetails);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Unit {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
		sb.append("    floorNo: ").append(toIndentedString(floorNo)).append("\n");
		sb.append("    unitType: ").append(toIndentedString(unitType)).append("\n");
		sb.append("    usageCategory: ").append(toIndentedString(usageCategory)).append("\n");
		sb.append("    occupancyType: ").append(toIndentedString(occupancyType)).append("\n");
		sb.append("    occupancyDate: ").append(toIndentedString(occupancyDate)).append("\n");
		sb.append("    constructionDetail: ").append(toIndentedString(constructionDetail)).append("\n");
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
