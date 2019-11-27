package org.egov.swCalculation.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.swService.model.SewerageConnection;
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
        @JsonProperty("sewerageConnection")
        private SewerageConnection sewerageConnection;

        @JsonProperty("connectionNo")
        private String connectionNo;

        @JsonProperty("assessmentYear")
        private String assessmentYear;

        @NotNull
        @JsonProperty("tenantId")
        private String tenantId;


}

