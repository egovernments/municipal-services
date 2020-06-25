package org.egov.wscalculation.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxHeadEstimate {

	private String taxHeadCode;
	
	private BigDecimal estimateAmount;
	
	private Category category;
}
