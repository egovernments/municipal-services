package org.egov.bpa.web.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BPA {

	@JsonIgnore
	private ArrayList<String> docIds ;
	@JsonIgnore
	private ArrayList<String> unitIds;
	@JsonIgnore
	private ArrayList<String> ownerIds;
	@JsonIgnore
	private ArrayList<String> blockId;
	
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
	
	@Size(max = 64)
	@JsonProperty("occupancyType")
	private String occupancyType;
	
	@Size(max = 64)
	@JsonProperty("subOccupancyType")
	private String subOccupancyType;
	

	@Size(max = 64)
	@JsonProperty("usages")
	private String usages;
	
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
    private List<User> assignees;
	
	@Valid
	@JsonProperty("wfDocuments")
	private List<Document> wfDocuments;
	
	@JsonProperty("validityDate")
	private Long validityDate;
	
	@JsonProperty("orderGeneratedDate")
	private Long orderGeneratedDate;
	
	@JsonProperty("tradeType")
	private String tradeType;

	

	public BPA addOwnersItem(OwnerInfo ownersItem) {
		if (this.owners == null)
			this.owners = new ArrayList<>();
		
		if(this.ownerIds == null){
			this.ownerIds = new ArrayList<String>();
		}
		if(!this.ownerIds.contains(ownersItem.getUuid())){
			this.owners.add(ownersItem);
			this.ownerIds.add(ownersItem.getUuid());
		}
		
		return this;
	}

	public BPA addUnitsItem(Unit unitsItem) {
		if (this.units == null)
			this.units = new ArrayList<>();
		
		if(this.unitIds == null){
			this.unitIds = new ArrayList<String>();
		}
		if(!this.unitIds.contains(unitsItem.getId())){
			this.units.add(unitsItem);
			this.unitIds.add(unitsItem.getId());
		}
		return this;
	}

	public BPA addDocumentsItem(Document documentsItem) {
		if (this.documents == null) {
			this.documents = new ArrayList<>();
		}
		if(this.docIds == null){
			this.docIds = new ArrayList<String>();
		}
		
		if(!this.docIds.contains(documentsItem.getId())){
			this.documents.add(documentsItem);
			this.docIds.add(documentsItem.getId());
		}
			
		return this;
	}
	
	
	
}
