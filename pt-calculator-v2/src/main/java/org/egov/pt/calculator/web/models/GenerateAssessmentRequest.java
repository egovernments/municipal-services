package org.egov.pt.calculator.web.models;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateAssessmentRequest {

	@JsonProperty("RequestInfo")
	private RequestInfo  requestInfo;
	
	private String assessmentYear;
	
	private String locality;

	private String tenantId;
	
}
