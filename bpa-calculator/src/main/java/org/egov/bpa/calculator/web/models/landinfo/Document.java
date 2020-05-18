package org.egov.bpa.calculator.web.models.landinfo;

import org.egov.bpa.calculator.web.models.AuditDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Document {
	
	@JsonProperty("id")
	private String id;

	@JsonProperty("documentType")
	private String documentType;

	@JsonProperty("fileStore")
	private String fileStore;

	@JsonProperty("documentUid")
	private String documentUid;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;
}
