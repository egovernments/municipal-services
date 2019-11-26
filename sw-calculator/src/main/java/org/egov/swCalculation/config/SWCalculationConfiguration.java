package org.egov.swCalculation.config;

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
}
