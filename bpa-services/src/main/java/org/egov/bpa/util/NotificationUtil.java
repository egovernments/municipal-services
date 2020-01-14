package org.egov.bpa.util;

import static org.egov.bpa.util.BPAConstants.ACTION_STATUS_INITIATED;
import static org.egov.bpa.util.BPAConstants.BILL_AMOUNT_JSONPATH;
import static org.egov.bpa.util.BPAConstants.BPA_MODULE_CODE;
import static org.egov.bpa.util.BPAConstants.DEFAULT_OBJECT_MODIFIED_MSG;

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

/*		String message = null, messageTemplate;

		String ACTION_STATUS = "INITIATED" + "_" + bpa.getStatus();
		switch (ACTION_STATUS) {

		case ACTION_STATUS_INITIATED:
			messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_INITIATED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate);
			break;
		}return message;*/

		
		
		String message = null, messageTemplate;
		String ACTION_STATUS = bpa.getAction() + "_" + bpa.getStatus();
		switch (ACTION_STATUS) {

		case BPAConstants.ACTION_STATUS_INITIATED:
			messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_INITIATED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate);
			break;

		case BPAConstants.ACTION_STATUS_PENDING_APPL_FEE:
			/*messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_INITIATED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate);*/
			message = "Dear <1>, Your application for BUILDING_PLAN_SCRUTINY is applied. Application is waiting for APPLICATION FEE Payment.";

			break;
			
		/*case BPAConstants.ACTION_STATUS_DOC_VERIFICATION:
			messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_INITIATED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate);
			message = "Dear <1>, The payment for you application with the application no as: " + bpa.getApplicationNo() + " is done Successfully. Waiting for Docverification.";
			break;*/
			
		case BPAConstants.ACTION_STATUS_FI_VERIFICATION:
			/*messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_INITIATED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate);*/
			message = "Dear <1>, Your application DOC verification for " + bpa.getApplicationNo() + " is done Successfully. Waiting for field inspection.";

			break;
			
		case BPAConstants.ACTION_STATUS_NOC_VERIFICATION:
			/*messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_INITIATED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate);*/
			message = "Dear <1>, Your application Field inspection for " + bpa.getApplicationNo() + " is done Successfully. Waiting for NOC verification.";

			break;
			
		case BPAConstants.ACTION_STATUS_PENDING_APPROVAL:
			/*messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_INITIATED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate);*/
			message = "Dear <1>, Your application NOC verification for " + bpa.getApplicationNo() + " is done Successfully. Waiting for Approval.";

			break;
		case BPAConstants.ACTION_STATUS_PENDING_SANC_FEE:
			/*messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_INITIATED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate);*/
			message = "Dear <1>, Your application with application no - " + bpa.getApplicationNo() + " is approved, waiting for sanction fee payment.";

			break;
			
		case BPAConstants.ACTION_STATUS_APPROVED:
			/*messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_INITIATED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate);*/
			message = "Dear <1>, Your application with application no - " + bpa.getApplicationNo() + " is approved successfully.";

			break;
		case BPAConstants.ACTION_STATUS_REJECTED:
			/*messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_INITIATED, localizationMessage);
			message = getInitiatedMsg(bpa, messageTemplate);*/
			message = "Dear <1>, Your application with application no - " + bpa.getApplicationNo() + " is Rejected.";

			break;
		/*case ACTION_STATUS_APPLIED:
			messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_APPLIED, localizationMessage);
			message = getAppliedMsg(bpa, messageTemplate);
			break;

		case ACTION_STATUS_PAID:
			messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_PAID, localizationMessage);
			message = getApprovalPendingMsg(bpa, messageTemplate);
			break;

		case ACTION_STATUS_DOCUMENTVERIFICATION:
			messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_DOCUMENT_VERIFICATION,
					localizationMessage);
			message = getDocumentVerificationMsg(bpa, messageTemplate);
			break;

		case ACTION_STATUS_FIELDINSPECTION:
			messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_FIELD_INSPECTION,
					localizationMessage);
			message = getFieldInspectionMsg(bpa, messageTemplate);
			break;

		case ACTION_STATUS_NOCUPDATION:
			messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_NOC_UPDATION, localizationMessage);
			message = getNOCUpdationMsg(bpa, messageTemplate);
			break;

		case ACTION_STATUS_APPROVED:
			BigDecimal amountToBePaid = getAmountToBePaid(requestInfo, bpa);
			messageTemplate = getMessageTemplate(
					BPAConstants.NOTIFICATION_APPROVED, localizationMessage);
			message = getApprovedMsg(bpa, amountToBePaid, messageTemplate);
			break;*/

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
			LinkedList data  = JsonPath.parse(localizationMessage).read(path);
			message = data.get(0).toString();
			System.out.println(data);
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
		String jsonString = new JSONObject(responseMap).toString();

		BigDecimal amountToBePaid = null;
		try {
			Object obj = JsonPath.parse(jsonString).read(BILL_AMOUNT_JSONPATH);
			amountToBePaid = new BigDecimal(obj.toString());
		} catch (Exception e) {
			throw new CustomException("PARSING ERROR",
					"Failed to parse the response using jsonPath: "
							+ BILL_AMOUNT_JSONPATH);
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
		StringBuilder builder = new StringBuilder(config.getCalculatorHost());
//		builder.append(config.getGetBillEndpoint());
		builder.append("?tenantId=");
		builder.append(bpa.getTenantId());
		builder.append("&consumerCode=");
		builder.append(bpa.getApplicationNo());
		builder.append("&businessService=");
		builder.append(BPA_MODULE_CODE);
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
				.append("rainmaker-tl");
		System.out.println("REquired Uri to test is: " + uri);
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
		System.out.println("message we are waiting for is: " + jsonString);
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
		message = message.replace("<2>", bpa.getApplicationType());
		message = message.replace("<3>", bpa.getApplicationNo());
		return message;
	}

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
			List<Map> users) {
		List<SMSRequest> smsRequest = new LinkedList<>();
		for(Map<String,String > mobileNumberToOwnerName: users){
			
		
		for (Map.Entry<String, String> entryset : mobileNumberToOwnerName
				.entrySet()) {
			String customizedMsg = message.replace("<1>", entryset.getValue());
			smsRequest.add(new SMSRequest(entryset.getKey(), customizedMsg));
		}
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
