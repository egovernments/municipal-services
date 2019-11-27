package org.egov.swCalculation.util;

import org.egov.swCalculation.config.SWCalculationConfiguration;
import org.egov.swCalculation.constants.SWCalculationConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Getter;
@Component
@Getter
public class SWCalculationUtil {
	
	@Autowired
	SWCalculationConfiguration configurations;
	/**
	 * Returns the tax head search Url with tenantId and SW service name
	 * parameters
	 *
	 * @param tenantId
	 * @return
	 */
	public StringBuilder getTaxPeriodSearchUrl(String tenantId) {

		return new StringBuilder().append(configurations.getBillingServiceHost())
				.append(configurations.getTaxPeriodSearchEndpoint()).append(SWCalculationConstant.URL_PARAMS_SEPARATER)
				.append(SWCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(tenantId)
				.append(SWCalculationConstant.SEPARATER).append(SWCalculationConstant.SERVICE_FIELD_FOR_SEARCH_URL)
				.append(SWCalculationConstant.SERVICE_FIELD_VALUE_WS);
	}
	
	/**
	 * Returns the tax head search Url with tenantId and SW service name
	 * parameters
	 *
	 * @param tenantId
	 * @return
	 */
	public StringBuilder getTaxHeadSearchUrl(String tenantId) {

		return new StringBuilder().append(configurations.getBillingServiceHost())
				.append(configurations.getTaxheadsSearchEndpoint()).append(SWCalculationConstant.URL_PARAMS_SEPARATER)
				.append(SWCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(tenantId)
				.append(SWCalculationConstant.SEPARATER).append(SWCalculationConstant.SERVICE_FIELD_FOR_SEARCH_URL)
				.append(SWCalculationConstant.SERVICE_FIELD_VALUE_WS);
	}
	
	/**
	 * Creates generate bill url using tenantId,consumerCode and businessService
	 * 
	 * @return Bill Generate url
	 */
	public String getBillGenerateURI() {
		StringBuilder url = new StringBuilder(configurations.getBillingServiceHost());
		url.append(configurations.getBillGenEndPoint());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("consumerCode=");
		url.append("{2}");
		url.append("&");
		url.append("businessService=");
		url.append("{3}");

		return url.toString();
	}

}
