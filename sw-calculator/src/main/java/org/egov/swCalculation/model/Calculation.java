package org.egov.swCalculation.model;

import java.math.BigDecimal;
import java.util.List;

import org.egov.swService.model.SewerageConnection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Calculation
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder({ "tenantId", "totalAmount", "taxAmount", "exemption", "rebate", "exemption", "penalty" })
public class Calculation {

	@JsonProperty("applicationNo")
	private String applicationNO;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("totalAmount")
	private BigDecimal totalAmount;

	@JsonProperty("taxAmount")
	private BigDecimal taxAmount;

	@JsonProperty("penalty")
	private BigDecimal penalty;

	@JsonProperty("exemption")
	private BigDecimal exemption;

	@JsonProperty("rebate")
	private BigDecimal rebate;

	List<TaxHeadEstimate> taxHeadEstimates;

	@JsonProperty("billingSlabIds")
	private List<String> billingSlabIds;
	
	@JsonProperty("sewerageConnection")
	private SewerageConnection sewerageConnection = null;
	
	@JsonProperty("connectionNo")
	private String connectionNo;
}
