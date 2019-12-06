package org.egov.bpa.web.models;

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

}
