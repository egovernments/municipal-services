package org.egov.wsCalculation.model;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetBillCriteria {
	
	private String assessmentNumber;
	
	@Default
	private BigDecimal amountExpected = BigDecimal.ZERO;
	
	private String propertyId;
	
	private String assessmentYear;
	
	@NotNull
	private String tenantId;
	
	private String billId;

	private List<String> consumerCodes;
	
}
