package org.egov.swCalculation.repository.builder;

import org.springframework.stereotype.Controller;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class sWCalculatorQueryBuilder {

	private static final String distinctTenantIdsCriteria = "select distinct(tenantid) from eg_sw_connection;";

	public String getDistinctTenantIds() {
		StringBuilder query = new StringBuilder(distinctTenantIdsCriteria);
		log.info("Query : " + query);
		return query.toString();
	}
}
