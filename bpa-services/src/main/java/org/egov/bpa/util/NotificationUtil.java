package org.egov.bpa.util;

import static org.egov.bpa.util.BPAConstants.BILL_AMOUNT;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.producer.Producer;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.service.EDCRService;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.EventRequest;
import org.egov.bpa.web.model.RequestInfoWrapper;
import org.egov.bpa.web.model.SMSRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationUtil {

	private BPAConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private Producer producer;
	
	private EDCRService edcrService;
	
	private BPAUtil bpaUtil;

	@Autowired
	public NotificationUtil(BPAConfiguration config, ServiceRequestRepository serviceRequestRepository,
			Producer producer, EDCRService edcrService, BPAUtil bpaUtil) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.producer = producer;
		this.edcrService = edcrService;
		this.bpaUtil = bpaUtil;
	}

	final String receiptNumberKey = "receiptNumber";

	final String amountPaidKey = "amountPaid";

	/**
	 * Creates customized message based on bpa
	 * 
	 * @param bpa
	 *            The bpa for which message is to be sent
	 * @param localizationMessage
	 *            The messages from localization
	 * @return customized message based on bpa
	 */
	@SuppressWarnings("unchecked")
	public String getCustomizedMsg(RequestInfo requestInfo, BPA bpa, String localizationMessage) {
		String message = null, messageTemplate;
		Map<String, String> edcrResponse = edcrService.getEDCRDetails(requestInfo, bpa);
		
		String applicationType = edcrResponse.get(BPAConstants.APPLICATIONTYPE);
		String serviceType = edcrResponse.get(BPAConstants.SERVICETYPE);

		if (bpa.getStatus().toString().toUpperCase().equals(BPAConstants.STATUS_REJECTED)) {
			messageTemplate = getMessageTemplate(
					applicationType + "_" + serviceType + "_" + BPAConstants.STATUS_REJECTED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate, serviceType);
		} else {

			String messageCode = applicationType + "_" + serviceType + "_" + bpa.getWorkflow().getAction() + "_"
					+ bpa.getStatus();

			messageTemplate = getMessageTemplate(messageCode, localizationMessage);
			if (!StringUtils.isEmpty(messageTemplate)) {
				message = getInitiatedMsg(bpa, messageTemplate, serviceType);

				if (message.contains("<AMOUNT_TO_BE_PAID>")) {
					BigDecimal amount = getAmountToBePaid(requestInfo, bpa);
					message = message.replace("<AMOUNT_TO_BE_PAID>", amount.toString());
				}
			}
		}
		return message;
	}

	@SuppressWarnings("unchecked")
	// As per OAP-304, keeping the same messages for Events and SMS, so removed
	// "M_" prefix for the localization codes.
	// so it will be same as the getCustomizedMsg
	public String getEventsCustomizedMsg(RequestInfo requestInfo, BPA bpa, String localizationMessage) {
		String message = null, messageTemplate;
		Map<String, String> edcrResponse = edcrService.getEDCRDetails(requestInfo, bpa);		
		String applicationType = edcrResponse.get(BPAConstants.APPLICATIONTYPE);
		String serviceType = edcrResponse.get(BPAConstants.SERVICETYPE);
		
		if (bpa.getStatus().toString().toUpperCase().equals(BPAConstants.STATUS_REJECTED)) {
			messageTemplate = getMessageTemplate(BPAConstants.M_APP_REJECTED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate, serviceType);
		} else {
			String messageCode = applicationType + "_" + serviceType + "_" + bpa.getWorkflow().getAction()
					+ "_" + bpa.getStatus();
			messageTemplate = getMessageTemplate(messageCode, localizationMessage);
			if (!StringUtils.isEmpty(messageTemplate)) {
				message = getInitiatedMsg(bpa, messageTemplate, serviceType);
				if (message.contains("<AMOUNT_TO_BE_PAID>")) {
					BigDecimal amount = getAmountToBePaid(requestInfo, bpa);
					message = message.replace("<AMOUNT_TO_BE_PAID>", amount.toString());
				}
			}
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
	@SuppressWarnings("rawtypes")
	public String getMessageTemplate(String notificationCode, String localizationMessage) {
		String path = "$..messages[?(@.code==\"{}\")].message";
		path = path.replace("{}", notificationCode);
		String message = null;
		try {
			List data = JsonPath.parse(localizationMessage).read(path);
			if (!CollectionUtils.isEmpty(data))
				message = data.get(0).toString();
			else
				log.error("Fetching from localization failed with code " + notificationCode);
		} catch (Exception e) {
			log.warn("Fetching from localization failed", e);
		}
		return message;
	}

	/**
	 * Fetches the amount to be paid from getBill API
	 * 
	 * @param requestInfo
	 *            The RequestInfo of the request
	 * @param license
	 *            The TradeLicense object for which
	 * @return
	 */
	private BigDecimal getAmountToBePaid(RequestInfo requestInfo, BPA bpa) {

		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(bpaUtil.getBillUri(bpa),
				new RequestInfoWrapper(requestInfo));
		JSONObject jsonObject = new JSONObject(responseMap);
		BigDecimal amountToBePaid;
		double amount = 0.0;
		try {
			JSONArray demandArray = (JSONArray) jsonObject.get("Demands");
			if (demandArray != null) {
				JSONObject firstElement = (JSONObject) demandArray.get(0);
				if (firstElement != null) {
					JSONArray demandDetails = (JSONArray) firstElement.get("demandDetails");
					if (demandDetails != null) {
						for (int i = 0; i < demandDetails.length(); i++) {
							JSONObject object = (JSONObject) demandDetails.get(i);
							Double taxAmt = Double.valueOf((object.get("taxAmount").toString()));
							amount = amount + taxAmt;
						}
					}
				}
			}
			amountToBePaid = BigDecimal.valueOf(amount);
		} catch (Exception e) {
			throw new CustomException("PARSING ERROR",
					"Failed to parse the response using jsonPath: "
							+ BILL_AMOUNT);
		}
		return amountToBePaid;
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
				.append("&tenantId=").append(tenantId).append("&module=").append(BPAConstants.SEARCH_MODULE);
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
	@SuppressWarnings("rawtypes")
	public String getLocalizationMessages(String tenantId, RequestInfo requestInfo) {

		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(getUri(tenantId, requestInfo),
				requestInfo);
		String jsonString = new JSONObject(responseMap).toString();
		return jsonString;
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
	@SuppressWarnings("unchecked")
	private String getInitiatedMsg(BPA bpa, String message, String serviceType) {
		message = message.replace("<2>", serviceType);
		message = message.replace("<3>", bpa.getApplicationNo());
		return message;
	}


	/**
	 * Send the SMSRequest on the SMSNotification kafka topic
	 * 
	 * @param smsRequestList
	 *            The list of SMSRequest to be sent
	 */
	public void sendSMS(List<org.egov.bpa.web.model.SMSRequest> smsRequestList, boolean isSMSEnabled) {
		if (isSMSEnabled) {
			if (CollectionUtils.isEmpty(smsRequestList))
				log.debug("Messages from localization couldn't be fetched!");
			for (SMSRequest smsRequest : smsRequestList) {
				producer.push(config.getSmsNotifTopic(), smsRequest);
				log.debug("MobileNumber: " + smsRequest.getMobileNumber() + " Messages: " + smsRequest.getMessage());
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
	public List<SMSRequest> createSMSRequest(String message, Map<String, String> mobileNumberToOwner) {
		List<SMSRequest> smsRequest = new LinkedList<>();

		for (Map.Entry<String, String> entryset : mobileNumberToOwner.entrySet()) {
			String customizedMsg = message.replace("<1>", entryset.getValue());
			smsRequest.add(new SMSRequest(entryset.getKey(), customizedMsg));
		}
		return smsRequest;
	}
	
	
	/**
	 * Pushes the event request to Kafka Queue.
	 * 
	 * @param request
	 */
	public void sendEventNotification(EventRequest request) {
		producer.push(config.getSaveUserEventsTopic(), request);

		log.debug("STAKEHOLDER:: " + request.getEvents().get(0).getDescription());
	}
	
	


}
