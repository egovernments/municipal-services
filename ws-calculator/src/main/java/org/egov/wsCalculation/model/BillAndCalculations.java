package org.egov.wsCalculation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BillAndCalculations {

    @JsonProperty("billResponse")
    private BillResponse billResponse;

    @JsonProperty("billingSlabIds")
    private BillingSlabIds billingSlabIds;
}
