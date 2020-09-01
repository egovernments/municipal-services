package org.egov.echallan.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.model.Challan;
import org.egov.echallan.model.ChallanRequest;
import org.egov.echallan.producer.Producer;
import org.egov.echallan.util.NotificationUtil;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class NotificationService {
	private ChallanConfiguration config;

	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsUrl;
	
	private RestTemplate restTemplate;
	
	private NotificationUtil util;
	
	private Producer producer;
	
	private static final String BUSINESSSERVICE_MDMS_MODULE = "BillingService";
	public static final String BUSINESSSERVICE_MDMS_MASTER = "BusinessService";
	public static final String BUSINESSSERVICE_CODES_FILTER = "$.[?(@.type=='Adhoc')].code";
	public static final String BUSINESSSERVICE_CODES_JSONPATH = "$.MdmsRes.BillingService.BusinessService";
	
	@Autowired
	public NotificationService(ChallanConfiguration config,RestTemplate restTemplate,NotificationUtil util,Producer producer) {
		this.config = config;
		this.restTemplate = restTemplate;
		this.util = util;
		this.producer = producer;
	}
	
	public void sendChallanNotification(ChallanRequest challanRequest) {
		if(null != config.getIsSMSEnabled()) {
			if(config.getIsSMSEnabled()) {
				String message = null;
				String tenantId = challanRequest.getChallan().getTenantId();
				List<String> businessServiceAllowed = fetchBusinessServiceFromMDMS(challanRequest.getRequestInfo(), tenantId);
				if(!CollectionUtils.isEmpty(businessServiceAllowed)) {
					Challan challan = challanRequest.getChallan();
						if (businessServiceAllowed.contains(challan.getBusinessService())) {
							String mobilenumber = challan.getCitizen().getMobileNumber();
							String localizationMessages = util.getLocalizationMessages(tenantId, challanRequest.getRequestInfo());
							message = util.getCustomizedMsg(challanRequest.getRequestInfo(), challan, localizationMessages);
							if (!StringUtils.isEmpty(message)) {
								Map<String, Object> request = new HashMap<>();
								request.put("mobileNumber", mobilenumber);
								request.put("message", message);
								producer.push(config.getSmsNotifTopic(), request);
							} else {
								log.error("No message configured! Notification will not be sent.");
							}
						} else {
							log.info("Notification not configured for this business service!");
						}

				}else {
					log.info("Business services to which notifs are to be sent, couldn't be retrieved! Notification will not be sent.");
				}
			}
		}
	}
	
	private List<String> fetchBusinessServiceFromMDMS(RequestInfo requestInfo, String tenantId){
		List<String> masterData = new ArrayList<>();
		StringBuilder uri = new StringBuilder();
		uri.append(mdmsHost).append(mdmsUrl);
		if(StringUtils.isEmpty(tenantId))
			return masterData;
		MdmsCriteriaReq request = getRequestForEvents(requestInfo, tenantId.split("\\.")[0]);
		try {
			Object response = restTemplate.postForObject(uri.toString(), request, Map.class);
			masterData = JsonPath.read(response, BUSINESSSERVICE_CODES_JSONPATH);
		}catch(Exception e) {
			log.error("Exception while fetching business service codes: ",e);
		}
		return masterData;
	}
	
	
	private MdmsCriteriaReq getRequestForEvents(RequestInfo requestInfo, String tenantId) {
		MasterDetail masterDetail = org.egov.mdms.model.MasterDetail.builder()
				.name(BUSINESSSERVICE_MDMS_MASTER).filter(BUSINESSSERVICE_CODES_FILTER).build();
		List<MasterDetail> masterDetails = new ArrayList<>();
		masterDetails.add(masterDetail);
		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(BUSINESSSERVICE_MDMS_MODULE)
				.masterDetails(masterDetails).build();
		List<ModuleDetail> moduleDetails = new ArrayList<>();
		moduleDetails.add(moduleDetail);
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}
	

	
}