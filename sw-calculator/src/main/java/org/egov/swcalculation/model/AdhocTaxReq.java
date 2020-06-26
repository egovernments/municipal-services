package org.egov.swcalculation.model;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Validated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class AdhocTaxReq {

	@JsonProperty("RequestInfo")
	@NotNull
	private RequestInfo requestInfo;

	@JsonProperty("demandId")
	@NotNull
	private String demandId;

	@JsonProperty("adhocrebate")
	private BigDecimal adhocrebate = null;

	@JsonProperty("adhocpenalty")
	private BigDecimal adhocpenalty = null;

}
