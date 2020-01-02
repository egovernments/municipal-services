package org.egov.wsCalculation.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.mdms.model.MdmsResponse;
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.config.WSCalculationConfiguration;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.BillingSlab;
import org.egov.wsCalculation.model.DemandNotificationObj;
import org.egov.wsCalculation.model.EmailRequest;
import org.egov.wsCalculation.model.NotificationReceiver;
import org.egov.wsCalculation.model.SMSRequest;
import org.egov.wsCalculation.util.NotificationUtil;
import org.egov.wsCalculation.util.WSCalculationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class DemandNotificationService {

	@Autowired
	NotificationUtil util;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	MasterDataService masterDataService;

	@Autowired
	WSCalculationConfiguration config;

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
		String messageTemplate = util.getCustomizedMsg(topic, loclizationMessage);
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
