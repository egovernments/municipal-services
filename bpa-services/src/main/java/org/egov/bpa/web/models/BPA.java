package org.egov.bpa.web.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.bpa.web.models.OwnerInfo.RelationshipEnum;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BPA {

	@Size(max = 64)
	@JsonProperty("id")
	private String id;

	@Size(max = 64)
	@JsonProperty("applicationNo")
	private String applicationNo;
	
	@Size(max = 64)
	@JsonProperty("permitOrderNo")
	private String permitOrderNo;

	@Size(max = 64)
	@NotNull
	@JsonProperty("applicationType")
	private String applicationType;
	
	public enum RiskTypeEnum {
		
		HIGH("HIGH"),

		LOW("LOW"),
		
		MEDIUM("MEDIUM");

		private String value;

		RiskTypeEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static RiskTypeEnum fromValue(String text) {
			for (RiskTypeEnum b : RiskTypeEnum.values()) {
				if (String.valueOf(b.value).equalsIgnoreCase(text)) {
					return b;
				}
			}
			return null;
		}
	}
	@Size(max = 64)
	@NotNull
	@JsonProperty("riskType")
	private RiskTypeEnum riskType;
	
	@Size(max = 64)
	@JsonProperty("edcrNumber")
	private String edcrNumber;

	@NotNull
	@Size(max = 256)
	@JsonProperty("tenantId")
	private String tenantId;

	@Size(max = 256)
	@NotNull
	@JsonProperty("serviceType")
	private String serviceType;


    @Size(max=64)
	@JsonProperty("status")
	private String status;
	
    @Size(max=500)
	@JsonProperty("remarks")
	private String remarks;
    
    @Size(max=500)
  	@JsonProperty("comment")
  	private String comment;
	
	@Size(max = 64)
	@JsonProperty("action")
	private String action;
	
	@Size(max = 64)
	@JsonProperty("holdingNo")
	private String holdingNo;
	
	@Size(max = 250)
	@JsonProperty("registrationDetails")
	private String registrationDetails;
	
	@Size(max = 250)
	@JsonProperty("govtOrQuasi")
	private String govtOrQuasi;

	@NotNull
	@JsonProperty("address")
	private Address address;

	@JsonProperty("ownershipCategory")
	private String ownershipCategory;

	@NotNull
	@JsonProperty("owners")
	private List<OwnerInfo> owners;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

	@JsonProperty("documents")
	private List<Document> documents;

	@JsonProperty("units")
	private List<Unit> units;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;
	
	@Size(max=64)
    @JsonProperty("assignees")
    private List<String> assignees = null;
	
	@Valid
	@JsonProperty("wfDocuments")
	private List<Document> wfDocuments;
	

	public BPA addOwnersItem(OwnerInfo ownersItem) {
		if (this.owners == null)
			this.owners = new ArrayList<>();
		if (!this.owners.contains(ownersItem))
			this.owners.add(ownersItem);
		return this;
	}

	public BPA addUnitsItem(Unit unitsItem) {
		if (this.units == null)
			this.units = new ArrayList<>();
		if (!this.units.contains(unitsItem))
			this.units.add(unitsItem);
		return this;
	}

	public BPA addDocumentsItem(Document documentsItem) {
		if (this.documents == null) {
			this.documents = new ArrayList<>();
		}
		if (!this.documents.contains(documentsItem))
			this.documents.add(documentsItem);
		return this;
	}
/*
	public String SECTION_CLERKSECTION_CLERK() {
		// TODO Auto-generated method stub
		return "INITIATED";
	}*/

	/*public void addAddressItem(Address addressItem) {
		if (!((List<Address>) this.address).contains(addressItem))
			((List<Address>) this.address).add(addressItem);
		return;
	}*/

}
