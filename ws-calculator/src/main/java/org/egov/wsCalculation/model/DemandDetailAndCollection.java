package org.egov.wsCalculation.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DemandDetailAndCollection {

    private String taxHeadCode;

    private DemandDetail latestDemandDetail;

    private BigDecimal taxAmountForTaxHead;

    private BigDecimal collectionAmountForTaxHead;

}
