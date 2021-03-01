package org.egov.echallancalculation.web.models.demand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxHeadEstimate {

    private String taxHeadCode;

    private BigDecimal estimateAmount;

    private Category category;
}
