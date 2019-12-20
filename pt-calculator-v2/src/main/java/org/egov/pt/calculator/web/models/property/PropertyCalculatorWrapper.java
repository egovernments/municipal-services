package org.egov.pt.calculator.web.models.property;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PropertyCalculatorWrapper {
	
    @JsonProperty("property")
	private Property property;
	
    @JsonProperty("assessment")
	private Assessment assessment;

}
