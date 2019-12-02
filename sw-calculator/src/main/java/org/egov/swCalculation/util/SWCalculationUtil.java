package org.egov.swCalculation.util;

import org.apache.commons.lang3.StringUtils;
import org.egov.swCalculation.config.SWCalculationConfiguration;
import org.egov.swCalculation.constants.SWCalculationConstant;
import org.egov.swCalculation.model.GetBillCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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
				.append(SWCalculationConstant.SERVICE_FIELD_VALUE_SW);
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
				.append(SWCalculationConstant.SERVICE_FIELD_VALUE_SW);
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
	
	/**
	 * method to create demandsearch url with demand criteria
	 *
	 * @param getBillCriteria
	 * @return
	 */
	public StringBuilder getDemandSearchUrl(GetBillCriteria getBillCriteria) {

		if (CollectionUtils.isEmpty(getBillCriteria.getConsumerCodes()))
			return new StringBuilder().append(configurations.getBillingServiceHost())
					.append(configurations.getDemandSearchEndPoint()).append(SWCalculationConstant.URL_PARAMS_SEPARATER)
					.append(SWCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(getBillCriteria.getTenantId())
					.append(SWCalculationConstant.SEPARATER)
					.append(SWCalculationConstant.CONSUMER_CODE_SEARCH_FIELD_NAME)
					.append(getBillCriteria.getConnectionId() + SWCalculationConstant.SW_CONSUMER_CODE_SEPARATOR
							+ getBillCriteria.getConnectionNumber());

		else
			return new StringBuilder().append(configurations.getBillingServiceHost())
					.append(configurations.getDemandSearchEndPoint()).append(SWCalculationConstant.URL_PARAMS_SEPARATER)
					.append(SWCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(getBillCriteria.getTenantId())
					.append(SWCalculationConstant.SEPARATER)
					.append(SWCalculationConstant.CONSUMER_CODE_SEARCH_FIELD_NAME)
					.append(StringUtils.join(getBillCriteria.getConsumerCodes(), ","));

	}
	
	/**
	 * Returns url for demand update Api
	 *
	 * @return
	 */
	public StringBuilder getUpdateDemandUrl() {
		return new StringBuilder().append(configurations.getBillingServiceHost())
				.append(configurations.getDemandUpdateEndPoint());
	}

}
