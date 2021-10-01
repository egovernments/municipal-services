package org.egov.pt.calculator.web.models;

import org.egov.pt.calculator.web.models.property.AuditDetails;
import org.egov.pt.calculator.web.models.property.Channel;
import org.egov.pt.calculator.web.models.propertyV2.AssessmentV2.Source;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assessment {
	
	private String id;

	private String uuid;
	
	private String assessmentNumber;
	
	private String propertyId;
	
	private String assessmentYear;
	
	private String demandId;
	
	private String status;
	
	private String tenantId;
	
	@JsonProperty("source")
	private Source source;
	
	@JsonProperty("channel")
	private Channel channel ;
	
	@JsonProperty("financialYear")
	private String financialYear;
	
	@JsonProperty("assessmentDate")
	private Long assessmentDate;
	
	private AuditDetails auditDetails;
	
}
