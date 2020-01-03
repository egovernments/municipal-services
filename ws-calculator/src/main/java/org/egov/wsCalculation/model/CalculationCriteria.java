package org.egov.wsCalculation.model;

import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * CalulationCriteria
 */
@Validated

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CalculationCriteria   {
	
        @JsonProperty("waterConnection")
        private WaterConnection waterConnection;
        
        @NotNull
        @JsonProperty("connectionNo")
        private String connectionNo;

        @JsonProperty("assessmentYear")
        private String assessmentYear;

        @NotNull
        @JsonProperty("tenantId")
        private String tenantId;
        
        @JsonProperty("meterStatus")
        private Integer meterStatus = null;
        
        @JsonProperty("lastReading")
        private Double lastReading = null;

        @JsonProperty("currentReading")
        private Double currentReading = null;
        
        @JsonProperty("billingPeriod")
        private String billingPeriod = null;
        
        
}

