package org.egov.swCalculation.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Property extends PropertyInfo {

	@JsonProperty("acknowldgementNumber")
	private String acknowldgementNumber;

	@JsonProperty("propertyType")
	private String propertyType;

	@JsonProperty("usageCategory")
	private String usageCategory;

	@JsonProperty("ownershipCategory")
	private String ownershipCategory;

	@JsonProperty("owners")
	@Valid
	@NotNull
	private List<OwnerInfo> owners;

	@JsonProperty("institution")
	@Valid
	private Institution institution;

	@JsonProperty("creationReason")
	private CreationReason creationReason;

	@JsonProperty("occupancyDate")
	private Long occupancyDate;

	@JsonProperty("constructionDate")
	private Long constructionDate;

	@JsonProperty("noOfFloors")
	private Long noOfFloors;

	@JsonProperty("landArea")
	private Float landArea;

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
	private List<Document> documents;

	@JsonProperty("unit")
	private Unit unit = null;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	public enum Source {

		PT("PT"),

		TL("TL"),

		WAS("WAS"),

		DATA_MIGRATION("DATA_MIGRATION");

		private String value;

		Source(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static Source fromValue(String text) {
			for (Source b : Source.values()) {
				if (String.valueOf(b.value).equalsIgnoreCase(text)) {
					return b;
				}
			}
			return null;
		}
	}

	public Property addOwnersItem(OwnerInfo ownersItem) {
		if (this.owners == null) {
			this.owners = new ArrayList<>();
		}

		if (null != ownersItem)
			this.owners.add(ownersItem);
		return this;
	}

	public Property addDocumentsItem(Document documentsItem) {
		if (this.documents == null) {
			this.documents = new ArrayList<>();
		}

		if (null != documentsItem)
			this.documents.add(documentsItem);
		return this;
	}

	@Builder
	public Property(String id, String propertyId, String tenantId, String accountId, String oldPropertyId,
			Status status, Address address, List<String> parentProperties, String acknowldgementNumber,
			String propertyType, String usageCategory, String ownershipCategory, List<OwnerInfo> owners,
			Institution institution, CreationReason creationReason, Long occupancyDate, Long constructionDate,
			Long noOfFloors, Float landArea, SourceEnum source, List<Document> documents, Object additionalDetails,
			AuditDetails auditDetails) {
		super(id, propertyId, tenantId, accountId, oldPropertyId, status, address, parentProperties);
		this.acknowldgementNumber = acknowldgementNumber;
		this.propertyType = propertyType;
		this.usageCategory = usageCategory;
		this.ownershipCategory = ownershipCategory;
		this.owners = owners;
		this.institution = institution;
		this.creationReason = creationReason;
		this.occupancyDate = occupancyDate;
		this.constructionDate = constructionDate;
		this.noOfFloors = noOfFloors;
		this.landArea = landArea;
		this.source = source;
		this.documents = documents;
		this.additionalDetails = additionalDetails;
		this.auditDetails = auditDetails;
	}

}
