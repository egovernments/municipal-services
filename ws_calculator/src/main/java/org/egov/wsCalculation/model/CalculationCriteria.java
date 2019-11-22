package org.egov.wsCalculation.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.egov.waterConnection.model.WaterConnection;
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

        @NotNull
        @JsonProperty("tenantId")
        private String tenantId;
        
        @JsonProperty("meterStatus")
        private Integer meterStatus = null;
        
        @JsonProperty("lastReading")
        private Integer lastReading = null;

        @JsonProperty("currentReading")
        private Integer currentReading = null;
        
        @JsonProperty("assessmentYear")
        private String assessmentYear;
        
}

