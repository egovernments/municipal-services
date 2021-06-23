package org.egov.swcalculation.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.egov.swcalculation.config.SWCalculationConfiguration;
import org.egov.swcalculation.util.SWCalculationUtil;
import org.egov.swcalculation.web.models.Category;
import org.egov.swcalculation.web.models.DemandNotificationObj;
import org.egov.swcalculation.web.models.EmailRequest;
import org.egov.swcalculation.web.models.NotificationReceiver;
import org.egov.swcalculation.web.models.SMSRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONArray;

@Service
public class SewerageDemandNotificationService {

	@Autowired
	SWCalculationUtil util;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	MasterDataService masterDataService;

	@Autowired
	SWCalculationConfiguration config;

	public void process(DemandNotificationObj notificationObj, String topic) {
		if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {
			List<SMSRequest> smsRequests = new LinkedList<>();
			enrichSMSRequest(notificationObj, smsRequests, topic);
			if (!CollectionUtils.isEmpty(smsRequests))
				util.sendSMS(smsRequests);
		}
		if (config.getIsMailEnabled() != null && config.getIsMailEnabled()) {
			List<EmailRequest> emailRequests = new LinkedList<>();
			enrichEmailRequest(notificationObj, emailRequests, topic);
		}
	}

	@SuppressWarnings("unused")
	private void enrichSMSRequest(DemandNotificationObj notificationObj, List<SMSRequest> smsRequest, String topic) {
		String tenantId = notificationObj.getTenantId();
		String localizationMessage = util.getLocalizationMessages(tenantId, notificationObj.getRequestInfo());
		String messageTemplate = util.getCustomizedMsg(topic, localizationMessage);
		List<NotificationReceiver> receiverList = new ArrayList<>();
		enrichNotificationReceivers(receiverList, notificationObj);
		receiverList.forEach(receiver -> {
			String message = util.getAppliedMsg(receiver, messageTemplate, notificationObj);
			SMSRequest sms = SMSRequest.builder().mobileNumber(receiver.getMobileNumber()).message(message).category(Category.TRANSACTION).build();
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
			throw new CustomException("PARSING_ERROR", "Notification Receiver List Can Not Be Parsed!!");
		}
	}

	@SuppressWarnings("unused")
	private void enrichEmailRequest(DemandNotificationObj notificationObj, List<EmailRequest> emailRequest,
			String topic) {
		// Currently email notification service uses SMS kafka topic. So commenting out this one.
		// Need to remove if this change is permanent.

		// String tenantId = notificationObj.getTenantId();
		// String localizationMessage = util.getLocalizationMessages(tenantId,
		// notificationObj.getRequestInfo());
		// String emailBody = util.getCustomizedMsg(topic, localizationMessage);
		// List<NotificationReceiver> receiverList = new ArrayList<>();
		// enrichNotificationReceivers(receiverList, notificationObj);
		// receiverList.forEach(receiver -> {
		// String message = util.getAppliedMsg(receiver, messageTemplate,
		// notificationObj);
		// SMSRequest sms = new SMSRequest(receiver.getMobileNumber(), message);
		// smsRequest.add(sms);
		// });
	}

}
