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
	
		@Valid
		@NotNull
        @JsonProperty("waterConnection")
        private WaterConnection waterConnection;

        @JsonProperty("assesmentNumber")
        private String assesmentNumber;

        @JsonProperty("assessmentYear")
        private String assessmentYear;

        @JsonProperty("oldAssessmentNumber")
        private String oldAssessmentNumber;

        @NotNull
        @JsonProperty("tenantId")
        private String tenantId;


}

