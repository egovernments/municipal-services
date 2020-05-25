package org.egov.bpa.util;

import static org.egov.bpa.util.BPAConstants.ACTION_STATUS_INITIATED;
import static org.egov.bpa.util.BPAConstants.BILL_AMOUNT;
import static org.egov.bpa.util.BPAConstants.BPA_MODULE_CODE;
import static org.egov.bpa.util.BPAConstants.DEFAULT_OBJECT_MODIFIED_MSG;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import static org.egov.bpa.util.BPAConstants.*;
@Component
@Slf4j
public class NotificationUtil {

	private BPAConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private Producer producer;

	@Autowired
	public NotificationUtil(BPAConfiguration config,
			ServiceRequestRepository serviceRequestRepository, Producer producer) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.producer = producer;
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
	public String getCustomizedMsg(RequestInfo requestInfo, BPA bpa,
			String localizationMessage) {
		String message = null, messageTemplate;
		if(bpa.getStatus().toUpperCase().equals(BPAConstants.STATUS_REJECTED)) {
			messageTemplate = getMessageTemplate(
					BPAConstants.APP_REJECTED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate);
		} else {
			String ACTION_STATUS = bpa.getAction() + "_" + bpa.getStatus();
			switch (ACTION_STATUS) {

			case BPAConstants.ACTION_STATUS_INITIATED:
				messageTemplate = getMessageTemplate(
						BPAConstants.APP_CREATE, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);
				break;


			case BPAConstants.ACTION_STATUS_SEND_TO_CITIZEN:
				messageTemplate = getMessageTemplate(
				 
						BPAConstants.SEND_TO_CITIZEN, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);
				break;
				

			case BPAConstants.ACTION_STATUS_SEND_TO_ARCHITECT:
				messageTemplate = getMessageTemplate(
						BPAConstants.SEND_TO_ARCHITECT, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);
				break;
				

			case BPAConstants.ACTION_STATUS_CITIZEN_APPROVE:
				messageTemplate = getMessageTemplate(
						BPAConstants.CITIZEN_APPROVED, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);
				break;
				
			case BPAConstants.ACTION_STATUS_PENDING_APPL_FEE:
				messageTemplate = getMessageTemplate(
						BPAConstants.APP_FEE_PENDNG, localizationMessage);
//				message = getPaymentMsg(requestInfo,bpa, messageTemplate);
				message = getInitiatedMsg(bpa, messageTemplate);

				break;
				
			case BPAConstants.ACTION_STATUS_DOC_VERIFICATION:
				messageTemplate = getMessageTemplate(
						BPAConstants.PAYMENT_RECEIVE, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);
				break;
				
			case BPAConstants.ACTION_STATUS_FI_VERIFICATION:
				messageTemplate = getMessageTemplate(
						BPAConstants.DOC_VERIFICATION, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);

				break;
				
			case BPAConstants.ACTION_STATUS_NOC_VERIFICATION:
				messageTemplate = getMessageTemplate(
						BPAConstants.NOC_VERIFICATION, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);

				break;
				
			case BPAConstants.ACTION_STATUS_PENDING_APPROVAL:
				messageTemplate = getMessageTemplate(
						BPAConstants.NOC_APPROVE, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);

				break;
			case BPAConstants.ACTION_STATUS_PENDING_SANC_FEE:
				messageTemplate = getMessageTemplate(
						BPAConstants.PERMIT_FEE_GENERATED, localizationMessage);
//				message = getPaymentMsg(requestInfo,bpa, messageTemplate);
				message = getInitiatedMsg(bpa, messageTemplate);
				break;
				
			case BPAConstants.ACTION_STATUS_APPROVED:
				messageTemplate = getMessageTemplate(
						BPAConstants.APPROVE_PERMIT_GENERATED, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);

				break;
		}
			
			if(message.contains("<4>")) {
				StringBuilder paymentUrl = new StringBuilder();
				BigDecimal amount= getAmountToBePaid(requestInfo, bpa);
				message = message.replace("<4>", amount.toString());
			}
		}
		return message;

	}
	public String getEventsCustomizedMsg(RequestInfo requestInfo, BPA bpa,
			String localizationMessage) {
		String message = null, messageTemplate;
		if(bpa.getStatus().toUpperCase().equals(BPAConstants.STATUS_REJECTED)) {
			messageTemplate = getMessageTemplate(
					BPAConstants.M_APP_REJECTED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate);
		} else {
			String ACTION_STATUS = bpa.getAction() + "_" + bpa.getStatus();
			switch (ACTION_STATUS) {

			case BPAConstants.ACTION_STATUS_INITIATED:
				messageTemplate = getMessageTemplate(
						BPAConstants.M_APP_CREATE, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);
				break;


			case BPAConstants.ACTION_STATUS_SEND_TO_CITIZEN:
				messageTemplate = getMessageTemplate(
				 
						BPAConstants.M_SEND_TO_CITIZEN, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);
				break;
				

			case BPAConstants.ACTION_STATUS_SEND_TO_ARCHITECT:
				messageTemplate = getMessageTemplate(
						BPAConstants.M_SEND_TO_ARCHITECT, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);
				break;
				

			case BPAConstants.ACTION_STATUS_CITIZEN_APPROVE:
				messageTemplate = getMessageTemplate(
						BPAConstants.M_CITIZEN_APPROVED, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);
				break;
				
			case BPAConstants.ACTION_STATUS_PENDING_APPL_FEE:
				messageTemplate = getMessageTemplate(
						BPAConstants.M_APP_FEE_PENDNG, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);

				break;
				
			case BPAConstants.ACTION_STATUS_DOC_VERIFICATION:
				messageTemplate = getMessageTemplate(
						BPAConstants.M_PAYMENT_RECEIVE, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);
				break;
				
			case BPAConstants.ACTION_STATUS_FI_VERIFICATION:
				messageTemplate = getMessageTemplate(
						BPAConstants.M_DOC_VERIFICATION, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);

				break;
				
			case BPAConstants.ACTION_STATUS_NOC_VERIFICATION:
				messageTemplate = getMessageTemplate(
						BPAConstants.M_NOC_VERIFICATION, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);

				break;
				
			case BPAConstants.ACTION_STATUS_PENDING_APPROVAL:
				messageTemplate = getMessageTemplate(
						BPAConstants.M_NOC_APPROVE, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);

				break;
			case BPAConstants.ACTION_STATUS_PENDING_SANC_FEE:
				messageTemplate = getMessageTemplate(
						BPAConstants.M_PERMIT_FEE_GENERATED, localizationMessage);
//				message = getPaymentMsg(requestInfo,bpa, messageTemplate);
				message = getInitiatedMsg(bpa, messageTemplate);
				break;
				
			case BPAConstants.ACTION_STATUS_APPROVED:
				messageTemplate = getMessageTemplate(
						BPAConstants.M_APPROVE_PERMIT_GENERATED, localizationMessage);
				message = getInitiatedMsg(bpa, messageTemplate);

				break;
		}
			
			if(message.contains("<4>")) {
				StringBuilder paymentUrl = new StringBuilder();
				BigDecimal amount= getAmountToBePaid(requestInfo, bpa);
				message = message.replace("<4>", amount.toString());
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
	public String getMessageTemplate(String notificationCode,
			String localizationMessage) {
		String path = "$..messages[?(@.code==\"{}\")].message";
		path = path.replace("{}", notificationCode);
		String message = null;
		try {
			List data  = JsonPath.parse(localizationMessage).read(path);
			message = data.get(0).toString();
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

		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository
				.fetchResult(getBillUri(bpa), new RequestInfoWrapper(
						requestInfo));
		JSONObject jsonObject = new JSONObject(responseMap);
		BigDecimal amountToBePaid;
		double amount = 0.0;
		try {
		    /* Object obj = JsonPath.parse(jsonString).read(BILL_AMOUNT);  */
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
	 * Creates the uri for getBill by adding query params from the license
	 * 
	 * @param license
	 *            The TradeLicense for which getBill has to be called
	 * @return The uri for the getBill
	 */
	private StringBuilder getBillUri(BPA bpa) {
		String status = bpa.getStatus();
		String code = null;
		if(status.equalsIgnoreCase("PENDING_APPL_FEE")){
			code= "BPA.NC_APP_FEE";
		}else{
			code= "BPA.NC_SAN_FEE";
		}
		StringBuilder builder = new StringBuilder(config.getBillingHost());
		builder.append(config.getDemandSearchEndpoint());
		builder.append("?tenantId=");
		builder.append(bpa.getTenantId());
		builder.append("&consumerCode=");
		builder.append(bpa.getApplicationNo());
		builder.append("&businessService=");
		builder.append(code);
		return builder;
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
		if (!StringUtils.isEmpty(requestInfo.getMsgId())
				&& requestInfo.getMsgId().split("|").length >= 2)
			locale = requestInfo.getMsgId().split("\\|")[1];

		StringBuilder uri = new StringBuilder();
		uri.append(config.getLocalizationHost())
				.append(config.getLocalizationContextPath())
				.append(config.getLocalizationSearchEndpoint()).append("?")
				.append("locale=").append(locale).append("&tenantId=")
				.append(tenantId).append("&module=")
				.append(BPAConstants.SEARCH_MODULE);
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
	public String getLocalizationMessages(String tenantId,
			RequestInfo requestInfo) {

		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository
				.fetchResult(getUri(tenantId, requestInfo), requestInfo);
		String jsonString = new JSONObject(responseMap).toString();
		return jsonString;
		// return "User creation is successfull";
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
		message = message.replace("<2>", bpa.getServiceType());
		message = message.replace("<3>", bpa.getApplicationNo());
		return message;
	}
	
	/*private String getPaymentMsg(RequestInfo requestInfo, BPA bpa, String message) {
		message = message.replace("<2>", bpa.getServiceType());
		message = message.replace("<3>", bpa.getApplicationNo());
		StringBuilder paymentUrl = new StringBuilder();
		BigDecimal amount= getAmountToBePaid(requestInfo, bpa);
		message = message.replace("<4>", amount.toString());
		return message;
	}*/

	/**
	 * Send the SMSRequest on the SMSNotification kafka topic
	 * 
	 * @param smsRequestList
	 *            The list of SMSRequest to be sent
	 */
	public void sendSMS(List<SMSRequest> smsRequestList, boolean isSMSEnabled) {
		if (isSMSEnabled) {
			if (CollectionUtils.isEmpty(smsRequestList))
				log.info("Messages from localization couldn't be fetched!");
			for (SMSRequest smsRequest : smsRequestList) {
				producer.push(config.getSmsNotifTopic(), smsRequest);
				log.info("MobileNumber: " + smsRequest.getMobileNumber()
						+ " Messages: " + smsRequest.getMessage());
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
	public List<SMSRequest> createSMSRequest(String message,
			Map<String, String> mobileNumberToOwner) {
		List<SMSRequest> smsRequest = new LinkedList<>();
		
		for (Map.Entry<String, String> entryset : mobileNumberToOwner
				.entrySet()) {
			String customizedMsg = message.replace("<1>", entryset.getValue());
			smsRequest.add(new SMSRequest(entryset.getKey(), customizedMsg));
		}
		return smsRequest;
	}

	public String getCustomizedMsg(Difference diff, BPA bpa,
			String localizationMessage) {
		String message = null, messageTemplate;

		if (!CollectionUtils.isEmpty(diff.getFieldsChanged())
				|| !CollectionUtils.isEmpty(diff.getClassesAdded())
				|| !CollectionUtils.isEmpty(diff.getClassesRemoved())) {
			messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_OBJECT_MODIFIED,
					localizationMessage);
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
		message = message.replace("<APPLICATION_NUMBER>",
				bpa.getApplicationNo());
		return message;
	}

	/**
	 * Pushes the event request to Kafka Queue.
	 * 
	 * @param request
	 */
	public void sendEventNotification(EventRequest request) {
		producer.push(config.getSaveUserEventsTopic(), request);
		
		log.info("STAKEHOLDER:: " + request.getEvents().get(0).getDescription());
	/*	if(request.getEvents().get(1) != null){
			log.info(" USER::  " + request.getEvents().get(1).getDescription());
		}*/
	}

	/**
	 * Creates customized message for apply
	 * 
	 * @param license
	 *            tenantId of the tradeLicense
	 * @param message
	 *            Message from localization for apply
	 * @return customized message for apply
	 */
	private String getAppliedMsg(BPA bpa, String message) {
		// message = message.replace("<1>",);
		// message = message.replace("<2>", bpa.getTradeName());
		message = message.replace("<3>", bpa.getApplicationNo());

		return message;
	}

	/**
	 * Creates customized message for submitted
	 * 
	 * @param license
	 *            tenantId of the tradeLicense
	 * @param message
	 *            Message from localization for submitted
	 * @return customized message for submitted
	 */
	private String getApprovalPendingMsg(BPA bpa, String message) {
		// message = message.replace("<1>",);
		message = message.replace("<2>", bpa.getApplicationType()); // name
																	// replaced
																	// with
																	// application
																	// type

		return message;
	}

	/**
	 * Creates customized message for approved
	 * 
	 * @param license
	 *            tenantId of the tradeLicense
	 * @param message
	 *            Message from localization for approved
	 * @return customized message for approved
	 */
	private String getApprovedMsg(BPA bpa, BigDecimal amountToBePaid,
			String message) {
		message = message.replace("<2>", bpa.getApplicationType());
		message = message.replace("<3>", amountToBePaid.toString());
		return message;
	}

	/**
	 * Creates customized message for DocumentVerification
	 * 
	 * @param license
	 *            tenantId of the bpa
	 * @param message
	 *            Message from localization for rejected
	 * @return customized message for DocumentVerification
	 */
	private String getDocumentVerificationMsg(BPA bpa, String message) {
		message = message.replace("<2>", bpa.getApplicationType());
		return message;
	}

	/**
	 * Creates customized message for FieldInspection
	 * 
	 * @param license
	 *            tenantId of the bpa
	 * @param message
	 *            Message from localization for FieldInspection
	 * @return customized message for rejected
	 */
	private String getFieldInspectionMsg(BPA bpa, String message) {
		message = message.replace("<2>", bpa.getApplicationType());
		return message;
	}

	/**
	 * Creates customized message for NOCUpdation
	 * 
	 * @param license
	 *            tenantId of the bpa
	 * @param message
	 *            Message from localization for NOCUpdation
	 * @return customized message for NOCUpdation
	 */
	private String getNOCUpdationMsg(BPA bpa, String message) {
		message = message.replace("<2>", bpa.getApplicationType());
		return message;
	}

	/**
	 * Creates message for completed payment for owners
	 * 
	 * @param valMap
	 *            The map containing required values from receipt
	 * @param localizationMessages
	 *            Message from localization
	 * @return message for completed payment for owners
	 */
	public String getOwnerPaymentMsg(BPA bpa, Map<String, String> valMap, String localizationMessages) {
		String messageTemplate = getMessageTemplate(BPAConstants.NOTIFICATION_PAYMENT_OWNER, localizationMessages);
		messageTemplate = messageTemplate.replace("<2>", valMap.get(amountPaidKey));
		messageTemplate = messageTemplate.replace("<3>", bpa.getApplicationType());
		messageTemplate = messageTemplate.replace("<4>", valMap.get(receiptNumberKey));
		return messageTemplate;
	}

	/**
	 * Creates message for completed payment for payer
	 * 
	 * @param valMap
	 *            The map containing required values from receipt
	 * @param localizationMessages
	 *            Message from localization
	 * @return message for completed payment for payer
	 */
	public String getPayerPaymentMsg(BPA bpa, Map<String, String> valMap, String localizationMessages) {
		String messageTemplate = getMessageTemplate(BPAConstants.NOTIFICATION_PAYMENT_PAYER, localizationMessages);
		messageTemplate = messageTemplate.replace("<2>", valMap.get(amountPaidKey));
		messageTemplate = messageTemplate.replace("<3>", bpa.getApplicationType());
		messageTemplate = messageTemplate.replace("<4>", valMap.get(receiptNumberKey));
		return messageTemplate;
	}
	
}
