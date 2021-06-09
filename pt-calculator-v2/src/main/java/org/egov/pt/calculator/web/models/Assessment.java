package org.egov.pt.calculator.web.models;

import org.egov.pt.calculator.web.models.property.AuditDetails;
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

	private String uuid;
	
	private String assessmentNumber;
	
	private String propertyId;
	
	private String assessmentYear;
	
	private String demandId;
	
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
	
	public enum Channel {
		
		  SYSTEM("SYSTEM"),
		  
		  CFC_COUNTER("CFC_COUNTER"),
		  
		  CITIZEN("CITIZEN"),
		  
		  DATA_ENTRY("DATA_ENTRY"),
		  
		  LEGACY_MIGRATION("LEGACY_MIGRATION"),
			
		  MIGRATION("MIGRATION");

		  private String value;

		  Channel(String value) {
		    this.value = value;
		  }

		  @Override
		  @JsonValue
		  public String toString() {
		    return String.valueOf(value);
		  }

		  @JsonCreator
		  public static Channel fromValue(String text) {
		    for (Channel b : Channel.values()) {
		      if (String.valueOf(b.value).equalsIgnoreCase(text)) {
		        return b;
		      }
		    }
		    return null;
		  }
		}
}
