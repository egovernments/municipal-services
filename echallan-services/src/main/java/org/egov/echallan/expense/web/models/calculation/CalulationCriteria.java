package org.egov.echallan.expense.web.models.calculation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.echallan.expense.model.Expense;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-09-27T14:56:03.454+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalulationCriteria {
        @JsonProperty("challan")
        @Valid
        private Expense expense = null;

        @JsonProperty("challanNo")
        @Size(min=2,max=64) 
        private String challanNo = null;

        @JsonProperty("tenantId")
        @NotNull@Size(min=2,max=256) 
        private String tenantId = null;


}

