package org.egov.swService.model;

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
 * Property
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-10-24T10:29:25.253+05:30[Asia/Kolkata]")
public class Property extends PropertyInfo {
	@JsonProperty("acknowldgementNumber")
	private String acknowldgementNumber = null;

	@JsonProperty("propertyType")
	private String propertyType = null;

	@JsonProperty("ownershipCategory")
	private String ownershipCategory = null;

	@JsonProperty("owners")
	@Valid
	private List<OwnerInfo> owners = new ArrayList<OwnerInfo>();

	@JsonProperty("institution")
	private Institution institution = null;

	/**
	 * New property comes into system either property is newly constructed or
	 * existing property got sub divided. Here the reason for creation will be
	 * captured.
	 */
	public enum CreationReasonEnum {
		NEWPROPERTY("NEWPROPERTY"),

		SUBDIVISION("SUBDIVISION");

		private String value;

		CreationReasonEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static CreationReasonEnum fromValue(String text) {
			for (CreationReasonEnum b : CreationReasonEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("creationReason")
	private CreationReasonEnum creationReason = null;

	@JsonProperty("noOfFloors")
	private Long noOfFloors = null;

	@JsonProperty("landArea")
	private Float landArea = null;

	/**
	 * Source of a constructionDetail data. The constructionDetail will be
	 * created in a system based on the data avaialble in their manual records
	 * or during field survey. There can be more from client to client.
	 */
	public enum SourceEnum {
		MUNICIPAL_RECORDS("MUNICIPAL_RECORDS"),

		FIELD_SURVEY("FIELD_SURVEY");

		private String value;

		SourceEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static SourceEnum fromValue(String text) {
			for (SourceEnum b : SourceEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("source")
	private SourceEnum source = null;

	/**
	 * constructionDetail details can be created from different channels Eg.
	 * System (properties created by ULB officials), CFC Counter (From citizen
	 * faciliation counters) etc. Here we are defining some known channels,
	 * there can be more client to client.
	 */
	public enum ChannelEnum {
		SYSTEM("SYSTEM"),

		CFC_COUNTER("CFC_COUNTER"),

		CITIZEN("CITIZEN"),

		DATA_ENTRY("DATA_ENTRY"),

		MIGRATION("MIGRATION");

		private String value;

		ChannelEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static ChannelEnum fromValue(String text) {
			for (ChannelEnum b : ChannelEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("channel")
	private ChannelEnum channel = null;

	@JsonProperty("documents")
	@Valid
	private List<Document> documents = null;

	@JsonProperty("unit")
	private Unit unit = null;

	@JsonProperty("additionalDetails")
	private Object additionalDetails = null;

	public Property acknowldgementNumber(String acknowldgementNumber) {
		this.acknowldgementNumber = acknowldgementNumber;
		return this;
	}

	/**
	 * Acknowldgement number given to citizen on submitting the application for
	 * creation or modification of the property.
	 * 
	 * @return acknowldgementNumber
	 **/
	@ApiModelProperty(readOnly = true, value = "Acknowldgement number given to citizen on submitting the application for creation or modification of the property.")

	@Size(min = 1, max = 64)
	public String getAcknowldgementNumber() {
		return acknowldgementNumber;
	}

	public void setAcknowldgementNumber(String acknowldgementNumber) {
		this.acknowldgementNumber = acknowldgementNumber;
	}

	public Property propertyType(String propertyType) {
		this.propertyType = propertyType;
		return this;
	}

	/**
	 * Type of a property like Private, Vacant Land, State Government, Central
	 * Government etc.
	 * 
	 * @return propertyType
	 **/
	@ApiModelProperty(example = "VACANT", required = true, value = "Type of a property like Private, Vacant Land, State Government, Central Government etc.")
	@NotNull

	@Size(max = 64)
	public String getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	public Property ownershipCategory(String ownershipCategory) {
		this.ownershipCategory = ownershipCategory;
		return this;
	}

	/**
	 * The type of ownership of the property.
	 * 
	 * @return ownershipCategory
	 **/
	@ApiModelProperty(value = "The type of ownership of the property.")

	@Size(max = 64)
	public String getOwnershipCategory() {
		return ownershipCategory;
	}

	public void setOwnershipCategory(String ownershipCategory) {
		this.ownershipCategory = ownershipCategory;
	}

	public Property owners(List<OwnerInfo> owners) {
		this.owners = owners;
		return this;
	}

	public Property addOwnersItem(OwnerInfo ownersItem) {
		this.owners.add(ownersItem);
		return this;
	}

	/**
	 * Property owners, these will be citizen users in system.
	 * 
	 * @return owners
	 **/
	@ApiModelProperty(required = true, value = "Property owners, these will be citizen users in system.")
	@NotNull
	@Valid
	public List<OwnerInfo> getOwners() {
		return owners;
	}

	public void setOwners(List<OwnerInfo> owners) {
		this.owners = owners;
	}

	public Property institution(Institution institution) {
		this.institution = institution;
		return this;
	}

	/**
	 * Get institution
	 * 
	 * @return institution
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public Property creationReason(CreationReasonEnum creationReason) {
		this.creationReason = creationReason;
		return this;
	}

	/**
	 * New property comes into system either property is newly constructed or
	 * existing property got sub divided. Here the reason for creation will be
	 * captured.
	 * 
	 * @return creationReason
	 **/
	@ApiModelProperty(value = "New property comes into system either property is newly constructed or existing property got sub divided. Here the reason for creation will be captured.")

	public CreationReasonEnum getCreationReason() {
		return creationReason;
	}

	public void setCreationReason(CreationReasonEnum creationReason) {
		this.creationReason = creationReason;
	}

	public Property noOfFloors(Long noOfFloors) {
		this.noOfFloors = noOfFloors;
		return this;
	}

	/**
	 * no of floors in the property
	 * 
	 * @return noOfFloors
	 **/
	@ApiModelProperty(value = "no of floors in the property")

	public Long getNoOfFloors() {
		return noOfFloors;
	}

	public void setNoOfFloors(Long noOfFloors) {
		this.noOfFloors = noOfFloors;
	}

	public Property landArea(Float landArea) {
		this.landArea = landArea;
		return this;
	}

	/**
	 * Land area of the property in sq ft
	 * 
	 * @return landArea
	 **/
	@ApiModelProperty(value = "Land area of the property in sq ft")

	public Float getLandArea() {
		return landArea;
	}

	public void setLandArea(Float landArea) {
		this.landArea = landArea;
	}

	public Property source(SourceEnum source) {
		this.source = source;
		return this;
	}

	/**
	 * Source of a constructionDetail data. The constructionDetail will be
	 * created in a system based on the data avaialble in their manual records
	 * or during field survey. There can be more from client to client.
	 * 
	 * @return source
	 **/
	@ApiModelProperty(required = true, value = "Source of a constructionDetail data. The constructionDetail will be created in a system based on the data avaialble in their manual records or during field survey. There can be more from client to client.")
	@NotNull

	public SourceEnum getSource() {
		return source;
	}

	public void setSource(SourceEnum source) {
		this.source = source;
	}

	public Property channel(ChannelEnum channel) {
		this.channel = channel;
		return this;
	}

	/**
	 * constructionDetail details can be created from different channels Eg.
	 * System (properties created by ULB officials), CFC Counter (From citizen
	 * faciliation counters) etc. Here we are defining some known channels,
	 * there can be more client to client.
	 * 
	 * @return channel
	 **/
	@ApiModelProperty(value = "constructionDetail details can be created from different channels Eg. System (properties created by ULB officials), CFC Counter (From citizen faciliation counters) etc. Here we are defining some known channels, there can be more client to client.")

	public ChannelEnum getChannel() {
		return channel;
	}

	public void setChannel(ChannelEnum channel) {
		this.channel = channel;
	}

	public Property documents(List<Document> documents) {
		this.documents = documents;
		return this;
	}

	public Property addDocumentsItem(Document documentsItem) {
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
	@Valid
	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public Property unit(Unit unit) {
		this.unit = unit;
		return this;
	}

	/**
	 * Get unit
	 * 
	 * @return unit
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Property additionalDetails(Object additionalDetails) {
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
		Property property = (Property) o;
		return Objects.equals(this.acknowldgementNumber, property.acknowldgementNumber)
				&& Objects.equals(this.propertyType, property.propertyType)
				&& Objects.equals(this.ownershipCategory, property.ownershipCategory)
				&& Objects.equals(this.owners, property.owners)
				&& Objects.equals(this.institution, property.institution)
				&& Objects.equals(this.creationReason, property.creationReason)
				&& Objects.equals(this.noOfFloors, property.noOfFloors)
				&& Objects.equals(this.landArea, property.landArea) && Objects.equals(this.source, property.source)
				&& Objects.equals(this.channel, property.channel) && Objects.equals(this.documents, property.documents)
				&& Objects.equals(this.unit, property.unit)
				&& Objects.equals(this.additionalDetails, property.additionalDetails) && super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(acknowldgementNumber, propertyType, ownershipCategory, owners, institution, creationReason,
				noOfFloors, landArea, source, channel, documents, unit, additionalDetails, super.hashCode());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Property {\n");
		sb.append("    ").append(toIndentedString(super.toString())).append("\n");
		sb.append("    acknowldgementNumber: ").append(toIndentedString(acknowldgementNumber)).append("\n");
		sb.append("    propertyType: ").append(toIndentedString(propertyType)).append("\n");
		sb.append("    ownershipCategory: ").append(toIndentedString(ownershipCategory)).append("\n");
		sb.append("    owners: ").append(toIndentedString(owners)).append("\n");
		sb.append("    institution: ").append(toIndentedString(institution)).append("\n");
		sb.append("    creationReason: ").append(toIndentedString(creationReason)).append("\n");
		sb.append("    noOfFloors: ").append(toIndentedString(noOfFloors)).append("\n");
		sb.append("    landArea: ").append(toIndentedString(landArea)).append("\n");
		sb.append("    source: ").append(toIndentedString(source)).append("\n");
		sb.append("    channel: ").append(toIndentedString(channel)).append("\n");
		sb.append("    documents: ").append(toIndentedString(documents)).append("\n");
		sb.append("    unit: ").append(toIndentedString(unit)).append("\n");
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
