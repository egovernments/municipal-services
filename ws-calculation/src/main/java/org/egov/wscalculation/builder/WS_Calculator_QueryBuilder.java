package org.egov.wscalculation.builder;

import org.egov.wscalculation.config.WS_Calculation_Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WS_Calculator_QueryBuilder {

	@Autowired
	WS_Calculation_Configuration configuration;
}
