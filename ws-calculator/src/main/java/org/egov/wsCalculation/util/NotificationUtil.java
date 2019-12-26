package org.egov.wsCalculation.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.wsCalculation.config.WSCalculationConfiguration;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.DemandNotificationObj;
import org.egov.wsCalculation.model.NotificationReceiver;
import org.egov.wsCalculation.model.SMSRequest;
import org.egov.wsCalculation.producer.WSCalculationProducer;
import org.egov.wsCalculation.repository.ServiceRequestRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationUtil {

	@Autowired
	WSCalculationConfiguration config;
	
	@Autowired
	ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
	WSCalculationProducer producer;

	/**
	 * Returns the uri for the localization call
	 * 
	 * @param tenantId
	 *            TenantId demand Notification Obj
	 * @return The uri for localization search call
	 */
	public StringBuilder getUri(String tenantId, RequestInfo requestInfo) {

		if (config.getIsLocalizationStateLevel())
			tenantId = tenantId.split("\\.")[0];

		String locale = WSCalculationConstant.NOTIFICATION_LOCALE;
		if (!StringUtils.isEmpty(requestInfo.getMsgId()) && requestInfo.getMsgId().split("|").length >= 2)
			locale = requestInfo.getMsgId().split("\\|")[1];

		StringBuilder uri = new StringBuilder();
		uri.append(config.getLocalizationHost()).append(config.getLocalizationContextPath())
				.append(config.getLocalizationSearchEndpoint()).append("?").append("locale=").append(locale)
				.append("&tenantId=").append(tenantId).append("&module=").append(WSCalculationConstant.MODULE);

		return uri;
	}
	
	/**
	 * Fetches messages from localization service
	 * 
	 * @param tenantId
	 *            tenantId of the tradeLicense
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @return Localization messages for the module
	 */
	public String getLocalizationMessages(String tenantId, RequestInfo requestInfo) {
		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(getUri(tenantId, requestInfo),
				requestInfo);
		String jsonString = new JSONObject(responseMap).toString();
		return jsonString;
	}
	
	/**
	 * Extracts message for the specific code
	 * 
	 * @param notificationCode The code for which message is required
	 * @param localizationMessage The localization messages
	 * @return message for the specific code
	 */
	private String getMessageTemplate(String notificationCode, String localizationMessage) {
		String path = "$..messages[?(@.code==\"{}\")].message";
		path = path.replace("{}", notificationCode);
		String message = null;
		try {
			Object messageObj = JsonPath.parse(localizationMessage).read(path);
			message = ((ArrayList<String>) messageObj).get(0);
		} catch (Exception e) {
			log.warn("Fetching from localization failed", e);
		}
		return message;
	}
	
	public String getCustomizedMsg(String topic, String localizationMessage) {
		String messageString = null;
		if (topic.equalsIgnoreCase(config.getOnDemandsSaved())) {
			messageString = getMessageTemplate(WSCalculationConstant.DEMAND_SUCCESS_MESSAGE, localizationMessage);
		}
		if (topic.equalsIgnoreCase(config.getOnDemandsFailure())) {
			messageString = getMessageTemplate(WSCalculationConstant.DEMAND_FAILURE_MESSAGE, localizationMessage);
		}
		return messageString;
	}
	
	
	/**
	 * 
	 * @param license
	 * @param message
	 * @return
	 */
	public String getAppliedMsg(NotificationReceiver receiver, String message, DemandNotificationObj obj) {
		message = message.replace("<First Name>", receiver.getFirstName() == null ? "" : receiver.getFirstName());
		message = message.replace("<Last Name>", receiver.getLastName() == null ? "" : receiver.getLastName());
		message = message.replace("<service name>", receiver.getServiceName() == null ? "" : receiver.getServiceName());
		message = message.replace("<ULB Name>", receiver.getUlbName() == null ? "" : receiver.getUlbName());
		message = message.replace("<billing cycle>", obj.getBillingCycle() == null ? "" : obj.getBillingCycle());
		return message;
	}
	
	
	/**
	 * Send the SMSRequest on the SMSNotification kafka topic
	 * @param smsRequestList The list of SMSRequest to be sent
	 */
	public void sendSMS(List<SMSRequest> smsRequestList) {
		if (config.getIsSMSEnabled()) {
			if (CollectionUtils.isEmpty(smsRequestList))
				log.info("Messages from localization couldn't be fetched!");
			for (SMSRequest smsRequest : smsRequestList) {
				producer.push(config.getSmsNotifTopic(), smsRequest);
				log.info("MobileNumber: " + smsRequest.getMobileNumber() + " Messages: " + smsRequest.getMessage());
			}
		}
	}
}
