package org.egov.land.web.models;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class LandInfo {

	@JsonProperty("id")
	private String id;

	@JsonProperty("landUid")
	private String landUid;

	@JsonProperty("landUniqueRegNo")
	private String landUniqueRegNo;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("status")
	private Status status;

	@JsonProperty("address")
	private Address address;

	@JsonProperty("ownershipCategory")
	private String ownershipCategory;

	@JsonProperty("owners")
	private List<OwnerInfo> owners;

	@JsonProperty("institution")
	private Institution institution;

	@JsonProperty("source")
	private Source source;

	@JsonProperty("channel")
	private Channel channel;

	@JsonProperty("documents")
	private List<Document> documents;

	@JsonProperty("unit")
	private List<Unit> unit;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;
	
	
	@JsonIgnore
	private ArrayList<String> docIds ;
	@JsonIgnore
	private ArrayList<String> unitIds;
	@JsonIgnore
	private ArrayList<String> ownerIds;
	@JsonIgnore
	private ArrayList<String> blockId;
	
	public LandInfo addOwnersItem(OwnerInfo ownersItem) {
		if (this.owners == null)
			this.owners = new ArrayList<>();
		
		if(this.ownerIds == null){
			this.ownerIds = new ArrayList<String>();
		}
		if(!this.ownerIds.contains(ownersItem.getUuid())){
			this.owners.add(ownersItem);
			this.ownerIds.add(ownersItem.getOwnerId());
		}
		
		return this;
	}

	public LandInfo addUnitsItem(Unit unitsItem) {
		if (this.unit == null)
			this.unit = new ArrayList<>();
		
		if(this.unitIds == null){
			this.unitIds = new ArrayList<String>();
		}
		if(!this.unitIds.contains(unitsItem.getId())){
			this.unit.add(unitsItem);
			this.unitIds.add(unitsItem.getId());			
		}
		return this;
	}

	public LandInfo addDocumentsItem(Document documentsItem) {
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
