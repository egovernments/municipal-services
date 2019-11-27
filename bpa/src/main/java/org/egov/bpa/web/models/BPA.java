package org.egov.bpa.web.models;

import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
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
public class BPA {

	@Size(max=64)
	@JsonProperty("id")
	private String id;
	
	@Size(max=64)
	@JsonProperty("applicationNo")
	private String applicationNo;
	 
	@Size(max=64)
	@JsonProperty("edcrNumber")
	private String edcrNumber;

	@NotNull
	@Size(max=256)
	@JsonProperty("tenantId")
	private String tenantId;

	@Size(max=256)
	@JsonProperty("serviceType")
	private String serviceType;
	
	@JsonProperty("status")
	private Status status;
	
	@NotNull
	@JsonProperty("address")
	private Address address;
	
	@JsonProperty("ownershipCategory")
	private String ownershipCategory;
	
	@NotNull
	@JsonProperty("owners")
	private Set<OwnerInfo> owners;
	
	@JsonProperty("additionalDetails")
	private Object additionalDetails;
	
	@JsonProperty("documents")
	private List<Document> documents;
	
	@JsonProperty("units")
	private List<Unit> units;
	
	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;
	
}
