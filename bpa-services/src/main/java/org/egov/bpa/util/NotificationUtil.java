package org.egov.bpa.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.producer.Producer;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.Difference;
import org.egov.bpa.web.models.EventRequest;
import org.egov.bpa.web.models.RequestInfoWrapper;
import org.egov.bpa.web.models.SMSRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import static org.egov.bpa.util.BPAConstants.*;
@Component
@Slf4j
public class NotificationUtil {


	private BPAConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private Producer producer;

	@Autowired
	public NotificationUtil(BPAConfiguration config, ServiceRequestRepository serviceRequestRepository,
			Producer producer) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.producer = producer;
	}

	/**
	 * Creates customized message based on bpa
	 * 
	 * @param bpa
	 *            The bpa for which message is to be sent
	 * @param localizationMessage
	 *            The messages from localization
	 * @return customized message based on bpa
	 */
	public String getCustomizedMsg(RequestInfo requestInfo, BPA bpa, String localizationMessage) {
		String message = null, messageTemplate;
		
		
		String ACTION_STATUS ="INITIATED" +"_"+ bpa.getStatus();
		switch (ACTION_STATUS) {

		case ACTION_STATUS_INITIATED:
			messageTemplate = getMessageTemplate(BPAConstants.NOTIFICATION_INITIATED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate);
			break;
		}
		
		return message;
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
		String message = null;
		try {
			Object messageObj = JsonPath.parse(localizationMessage).read(path);
			message = ((ArrayList<String>) messageObj).get(0);
		} catch (Exception e) {
			log.warn("Fetching from localization failed", e);
		}
		return message;
	}

	/**
	 * Returns the uri for the localization call
	 * 
	 * @param tenantId
	 *            TenantId of the propertyRequest
	 * @return The uri for localization search call
	 */
	public StringBuilder getUri(String tenantId, RequestInfo requestInfo) {

		if (config.getIsLocalizationStateLevel())
			tenantId = tenantId.split("\\.")[0];

		String locale = "en_IN";
		if (!StringUtils.isEmpty(requestInfo.getMsgId()) && requestInfo.getMsgId().split("|").length >= 2)
			locale = requestInfo.getMsgId().split("\\|")[1];

		StringBuilder uri = new StringBuilder();
		uri.append(config.getLocalizationHost()).append(config.getLocalizationContextPath())
				.append(config.getLocalizationSearchEndpoint()).append("?").append("locale=").append(locale)
				.append("&tenantId=").append(tenantId).append("&module=").append(BPAConstants.BPA_MODULE);

		return uri;
	}

	/**
	 * Fetches messages from localization service
	 * 
	 * @param tenantId
	 *            tenantId of the BPA
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @return Localization messages for the module
	 */
	public String getLocalizationMessages(String tenantId, RequestInfo requestInfo) {
		/*LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(getUri(tenantId, requestInfo),
				requestInfo);
		String jsonString = new JSONObject(responseMap).toString();*/ /* -- Localization service changes are required*/
		
		return "User creation is successfull";
	}

	/**
	 * Creates customized message for initiate
	 * 
	 * @param bpa
	 *            tenantId of the bpa
	 * @param message
	 *            Message from localization for initiate
	 * @return customized message for initiate
	 */
	private String getInitiatedMsg(BPA bpa, String message) {
		message = message.replace("<3>", bpa.getApplicationNo());
		return message;
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
				log.info("MobileNumber: " + smsRequest.getMobileNumber() + " Messages: " + smsRequest.getMessage());
			}
		}
	}

	

	/**
	 * Creates sms request for the each owners
	 * 
	 * @param message
	 *            The message for the specific bpa
	 * @param mobileNumberToOwnerName
	 *            Map of mobileNumber to OwnerName
	 * @return List of SMSRequest
	 */
	public List<SMSRequest> createSMSRequest(String message, Map<String, String> mobileNumberToOwnerName) {
		List<SMSRequest> smsRequest = new LinkedList<>();
		for (Map.Entry<String, String> entryset : mobileNumberToOwnerName.entrySet()) {
			String customizedMsg = message.replace("<1>", entryset.getValue());
			smsRequest.add(new SMSRequest(entryset.getKey(), customizedMsg));
		}
		return smsRequest;
	}

	public String getCustomizedMsg(Difference diff, BPA bpa, String localizationMessage) {
		String message = null, messageTemplate;
		
		if (!CollectionUtils.isEmpty(diff.getFieldsChanged()) || !CollectionUtils.isEmpty(diff.getClassesAdded())
				|| !CollectionUtils.isEmpty(diff.getClassesRemoved())) {
			messageTemplate = getMessageTemplate(BPAConstants.NOTIFICATION_OBJECT_MODIFIED, localizationMessage);
			if (messageTemplate == null)
				messageTemplate = DEFAULT_OBJECT_MODIFIED_MSG;
			message = getEditMsg(bpa, messageTemplate);
		}

		return message;
	}

	/**
	 * Creates customized message for field chnaged
	 * 
	 * @param message
	 *            Message from localization for field change
	 * @return customized message for field change
	 */

	private String getEditMsg(BPA bpa, String message) {
		message = message.replace("<APPLICATION_NUMBER>", bpa.getApplicationNo());
		return message;
	}

	/**
	 * Pushes the event request to Kafka Queue.
	 * 
	 * @param request
	 */
	public void sendEventNotification(EventRequest request) {
		producer.push(config.getSaveUserEventsTopic(), request);
	}

}
