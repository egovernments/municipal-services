package org.egov.pt.calculator.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.pt.calculator.repository.DefaultersRepository;
import org.egov.pt.calculator.repository.Repository;
import org.egov.pt.calculator.util.CalculatorConstants;
import org.egov.pt.calculator.util.CalculatorUtils;
import org.egov.pt.calculator.util.Configurations;
import org.egov.pt.calculator.web.models.DefaultersInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DefaultersService {

	@Autowired
	private CalculatorUtils utils;

	@Autowired
	private Repository mdmsRepository;

	@Autowired
	private Configurations configs;

	@Autowired
	private DefaultersRepository defaultersRepository;

	@Autowired
	private NotificationService notificationService;

	public List<String> fetchAllDefaulterDetails(RequestInfo request) {
		List<DefaultersInfo> defaulterDetails = null;
		List<String> notifiedTenants = new ArrayList<>();
		Map<String, Object> config = fetchDefultersConfig(request);
		List<String> tenants = (List<String>) config.get("tenants");
		for (String tenant : tenants) {

			defaulterDetails = defaultersRepository.fetchAllDefaulterDetailsForFY((String) config.get("financialyear"),
					tenant);
			if (defaulterDetails.isEmpty()) {
				log.info("No properties with due in the city " + tenant);
			} else {
				defaulterDetails.forEach(d -> {
					d.setTenantId(tenant);
					d.setRebateEndDate((String) config.get("rebateDate"));
				});
				notificationService.prepareAndSendSMS(defaulterDetails);
				notifiedTenants.add(tenant);
			}

		}
		return notifiedTenants;
	}

	private Map<String, Object> fetchDefultersConfig(RequestInfo request) {

		StringBuilder mdmsURL = utils.getMdmsSearchUrl();

		MdmsCriteriaReq mdmsConfig = utils.getDefaultersConfigRequest(request, configs.getStateLevelTenantId());
		try {
			Object response = mdmsRepository.fetchResult(mdmsURL, mdmsConfig);
			List<Map<String, Object>> jsonOutput = JsonPath.read(response,
					CalculatorConstants.MDMS_DEFAULTERS_CONFIG_PATH);
			Map<String, Object> tenantConfig = new HashMap<>();
			for (Map<String, Object> config : jsonOutput) {

				tenantConfig.put("rebateDate", config.get("rebatedate"));
				tenantConfig.put(CalculatorConstants.FINANCIALYEAR_KEY,
						config.get(CalculatorConstants.FINANCIALYEAR_KEY));
				tenantConfig.put("tenants", config.get("tenant"));
			}
			return tenantConfig;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
