package org.egov.swCalculation.config;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Component
public class SWCalculationConfiguration {

	// billing service
	@Value("${egov.billingservice.host}")
	private String billingServiceHost;

	@Value("${egov.taxhead.search.endpoint}")
	private String taxheadsSearchEndpoint;

	@Value("${egov.taxperiod.search.endpoint}")
	private String taxPeriodSearchEndpoint;

	// MDMS
	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndPoint;

	// water Registry
	@Value("${egov.ws.host}")
	private String sewerageConnectionHost;

	@Value("${egov.wc.search.endpoint}")
	private String sewerageConnectionSearchEndPoint;

	@Value("${sw.module.minpayable.amount}")
	private BigDecimal swMinAmountPayable;

	@Value("${egov.demand.businessservice}")
	private String businessService;
	
	@Value("${egov.demand.create.endpoint}")
	private String demandCreateEndPoint;
	
	@Value("${egov.demand.update.endpoint}")
	private String demandUpdateEndPoint;
	
	@Value("${egov.demand.search.endpoint}")
	private String demandSearchEndPoint;

	@Value("${egov.sewerageservice.pagination.default.limit}")
	private String limit;
	
	@Value("${egov.sewerageservice.pagination.default.offset}")
	private String offset;
	
	@Value("${egov.bill.gen.endpoint}")
	private String billGenEndPoint;

}
