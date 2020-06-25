package org.egov.wscalculation.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.egov.tracer.model.CustomException;
import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.egov.wscalculation.model.DemandNotificationObj;
import org.egov.wscalculation.model.EmailRequest;
import org.egov.wscalculation.model.NotificationReceiver;
import org.egov.wscalculation.model.SMSRequest;
import org.egov.wscalculation.util.NotificationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONArray;

@Service
public class DemandNotificationService {

	@Autowired
	private NotificationUtil util;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private MasterDataService masterDataService;

	@Autowired
	private WSCalculationConfiguration config;

	public void process(DemandNotificationObj noiticationObj, String topic) {
		if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {
			List<SMSRequest> smsRequests = new LinkedList<>();
			enrichSMSRequest(noiticationObj, smsRequests, topic);
			if (!CollectionUtils.isEmpty(smsRequests))
				util.sendSMS(smsRequests);
		}
		if (config.getIsEmailEnabled() != null && config.getIsEmailEnabled()) {
			List<EmailRequest> emailRequests = new LinkedList<>();
			enrichEmailRequest(noiticationObj, emailRequests, topic);
		}
	}

	@SuppressWarnings("unused")
	private void enrichSMSRequest(DemandNotificationObj notificationObj, List<SMSRequest> smsRequest, String topic) {
		String tenantId = notificationObj.getTenantId();
		String loclizationMessage = util.getLocalizationMessages(tenantId, notificationObj.getRequestInfo());
		String messageTemplate = util.getCustomizedMsgForSMS(topic, loclizationMessage);
		List<NotificationReceiver> receiverList = new ArrayList<>();
		enrichNotificationReceivers(receiverList, notificationObj);
		receiverList.forEach(receiver -> {
			String message = util.getAppliedMsg(receiver, messageTemplate, notificationObj);
			SMSRequest sms = new SMSRequest(receiver.getMobileNumber(), message);
			smsRequest.add(sms);
		});
	}
	
	@SuppressWarnings("unused")
	private void enrichNotificationReceivers(List<NotificationReceiver> receiverList,
			DemandNotificationObj notificationObj) {
		try {
			JSONArray receiver = masterDataService.getMasterListOfReceiver(notificationObj.getRequestInfo(),
					notificationObj.getTenantId());
			receiverList.addAll(mapper.readValue(receiver.toJSONString(),
					mapper.getTypeFactory().constructCollectionType(List.class, NotificationReceiver.class)));
		} catch (IOException e) {
			throw new CustomException("Parsing Exception", " Notification Receiver List Can Not Be Parsed!!");
		}
	}
	
	@SuppressWarnings("unused")
	private void enrichEmailRequest(DemandNotificationObj notificationObj, List<EmailRequest> emailRequest, String topic) {
//		String tenantId = notificationObj.getTenantId();
//		String loclizationMessage = util.getLocalizationMessages(tenantId, notificationObj.getRequestInfo());
//		String emailBody = util.getCustomizedMsg(topic, loclizationMessage);
//		List<NotificationReceiver> receiverList = new ArrayList<>();
//		enrichNotificationReceivers(receiverList, notificationObj);
//		receiverList.forEach(receiver -> {
//			String message = util.getAppliedMsg(receiver, messageTemplate, notificationObj);
//			SMSRequest sms = new SMSRequest(receiver.getMobileNumber(), message);
//			smsRequest.add(sms);
//		});
	}

}
