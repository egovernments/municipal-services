package org.egov.swCalculation.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.egov.swCalculation.config.SWCalculationConfiguration;
import org.egov.swCalculation.constants.SWCalculationConstant;
import org.egov.swCalculation.model.BillingSlab;
import org.egov.swCalculation.model.DemandNotificationObj;
import org.egov.swCalculation.model.EmailRequest;
import org.egov.swCalculation.model.NotificationReceiver;
import org.egov.swCalculation.model.SMSRequest;
import org.egov.swCalculation.model.SewerageConnectionRequest;
import org.egov.swCalculation.repository.ServiceRequestRepository;
import org.egov.swCalculation.util.SWCalculationUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class SewerageDemandNotificationService {

	@Autowired
	SWCalculationUtil util;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	MasterDataService masterDataService;

	@Autowired
	SWCalculationConfiguration config;

	public void process(DemandNotificationObj noiticationObj, String topic) {
		if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {
			List<SMSRequest> smsRequests = new LinkedList<>();
			enrichSMSRequest(noiticationObj, smsRequests, topic);
			if (!CollectionUtils.isEmpty(smsRequests))
				util.sendSMS(smsRequests);
		}
		if (config.getIsMailEnabled() != null && config.getIsMailEnabled()) {
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
	private void enrichEmailRequest(DemandNotificationObj notificationObj, List<EmailRequest> emailRequest,
			String topic) {
		// String tenantId = notificationObj.getTenantId();
		// String loclizationMessage = util.getLocalizationMessages(tenantId,
		// notificationObj.getRequestInfo());
		// String emailBody = util.getCustomizedMsg(topic, loclizationMessage);
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
