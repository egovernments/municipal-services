package org.egov.bpa.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
	

    @Size(max=64)
    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
    
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("documentType")
	private String documentType;
	
	@JsonProperty("fileStoreId")
	private String fileStoreId;
	
	@JsonProperty("documentUid")
	private String documentUid;
	
	@JsonProperty("additionalDetails")
	private Object additionalDetails;


}
