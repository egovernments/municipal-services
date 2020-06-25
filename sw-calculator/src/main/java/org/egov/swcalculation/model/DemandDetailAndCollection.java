package org.egov.swcalculation.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DemandDetailAndCollection {

    private String taxHeadCode;

    private DemandDetail latestDemandDetail;

    private BigDecimal taxAmountForTaxHead;

    private BigDecimal collectionAmountForTaxHead;

}
