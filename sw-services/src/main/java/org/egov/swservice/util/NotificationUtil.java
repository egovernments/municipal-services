package org.egov.swservice.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.model.EventRequest;
import org.egov.swservice.model.SMSRequest;
import org.egov.swservice.producer.SewarageConnectionProducer;
import org.egov.swservice.repository.ServiceRequestRepository;
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
	private SWConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private SewarageConnectionProducer producer;

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

		String locale = SWConstants.NOTIFICATION_LOCALE;
		if (!StringUtils.isEmpty(requestInfo.getMsgId()) && requestInfo.getMsgId().split("|").length >= 2)
			locale = requestInfo.getMsgId().split("\\|")[1];
		StringBuilder uri = new StringBuilder();
		uri.append(config.getLocalizationHost()).append(config.getLocalizationContextPath())
				.append(config.getLocalizationSearchEndpoint()).append("?").append("locale=").append(locale)
				.append("&tenantId=").append(tenantId).append("&module=").append(SWConstants.MODULE);

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
		@SuppressWarnings("rawtypes")
		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(getUri(tenantId, requestInfo),
				requestInfo);
		return new JSONObject(responseMap).toString();
	}

	/**
	 * Extracts message for the specific code
	 * 
	 * @param notificationCode
	 *            The code for which message is required
	 * @param localizationMessage
	 *            The localization messages
	 * @return message for the specific code
	 */
	private String getMessageTemplate(String notificationCode, String localizationMessage) {
		String path = "$..messages[?(@.code==\"{}\")].message";
		path = path.replace("{}", notificationCode);
		try {
			Object messageObj = JsonPath.parse(localizationMessage).read(path);
			return ((ArrayList<String>) messageObj).get(0);
		} catch (Exception e) {
			log.warn("Fetching from localization failed", e);
		}
		return null;
	}

	/**
	 * Send the SMSRequest on the SMSNotification kafka topic
	 * 
	 * @param smsRequestList
	 *            The list of SMSRequest to be sent
	 */
	public void sendSMS(List<SMSRequest> smsRequestList) {
		if (config.getIsSMSEnabled()) {
			if (CollectionUtils.isEmpty(smsRequestList))
				log.info("Messages from localization couldn't be fetched!");
			for (SMSRequest smsRequest : smsRequestList) {
				producer.push(config.getSmsNotifTopic(), smsRequest);
				StringBuilder builder = new StringBuilder();
				builder.append("MobileNumber: ").append(smsRequest.getMobileNumber()).append(" Messages: ")
						.append(smsRequest.getMessage());
				log.info(builder.toString());
			}
		}
	}

	/**
	 * 
	 * @param applicationStatus
	 * @param localizationMessage
	 * @return message template
	 */
	public String getCustomizedMsgForSMS(String action, String applicationStatus, String localizationMessage) {
		StringBuilder notificationCode = new StringBuilder("SW_").append(action.toUpperCase()).append("_").append(applicationStatus.toUpperCase()).append("_SMS_MESSAGE");
		return getMessageTemplate(notificationCode.toString(), localizationMessage);
	}

	/**
	 * 
	 * @param applicationStatus
	 * @param localizationMessage
	 * @return In app message template
	 */
	public String getCustomizedMsgForInApp(String action, String applicationStatus, String localizationMessage) {
		
		StringBuilder notificationCode = new StringBuilder("SW_").append(action.toUpperCase()).append("_").append(applicationStatus.toUpperCase()).append("_APP_MESSAGE");
		return getMessageTemplate(notificationCode.toString(), localizationMessage);
	}
	/**
	 * Pushes the event request to Kafka Queue.
	 * 
	 * @param request
	 */
	public void sendEventNotification(EventRequest request) {
		producer.push(config.getSaveUserEventsTopic(), request);
	}
	
	/**
	 * 
	 * @param applicationStatus
	 * @param localizationMessage
	 * @return In app message template
	 */
	public String getCustomizedMsg(String code, String localizationMessage) {
		return getMessageTemplate(code, localizationMessage);
	}


}
