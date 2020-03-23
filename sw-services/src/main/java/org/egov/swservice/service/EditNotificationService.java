package org.egov.swservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.model.Action;
import org.egov.swservice.model.Event;
import org.egov.swservice.model.EventRequest;
import org.egov.swservice.model.Recepient;
import org.egov.swservice.model.SMSRequest;
import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.model.Source;
import org.egov.swservice.util.NotificationUtil;
import org.egov.swservice.util.SWConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EditNotificationService {

	@Autowired
	private SWConfiguration config;

	@Autowired
	private NotificationUtil notificationUtil;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WorkflowNotificationService workflowNotificationService;

	public void sendEditNotification(SewerageConnectionRequest request) {
		try {
			if (config.getIsUserEventsNotificationEnabled() != null && config.getIsUserEventsNotificationEnabled()) {
				EventRequest eventRequest = getEventRequest(request.getSewerageConnection(), request.getRequestInfo());
				if (eventRequest != null) {
					log.debug("IN APP NOTIFICATION FOR EDIT APPLICATION :: -> "
							+ mapper.writeValueAsString(eventRequest));
					notificationUtil.sendEventNotification(eventRequest);
				}
			}
			if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {
				List<SMSRequest> smsRequests = getSmsRequest(request.getSewerageConnection(), request.getRequestInfo());
				if (!CollectionUtils.isEmpty(smsRequests)) {
					log.debug("SMS NOTIFICATION FOR EDIT APPLICATION :: -> " + mapper.writeValueAsString(smsRequests));
					notificationUtil.sendSMS(smsRequests);
				}
			}
		} catch (Exception ex) {
			log.error("Exception while trying to process edit notification.", ex);
		}
	}

	private EventRequest getEventRequest(SewerageConnection sewerageConnection, RequestInfo requestInfo) {
		
		String localizationMessage = notificationUtil
				.getLocalizationMessages(sewerageConnection.getProperty().getTenantId(), requestInfo);
		String message = notificationUtil.getCustomizedMsg(SWConstants.SW_EDIT_IN_APP, localizationMessage);
		if (message == null) {
			log.info("No localized message found!!, Using default message");
			message = SWConstants.DEFAULT_OBJECT_MODIFIED_APP_MSG;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		sewerageConnection.getProperty().getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		Map<String, String> mobileNumberAndMesssage = workflowNotificationService
				.getMessageForMobileNumber(mobileNumbersAndNames, sewerageConnection, message,requestInfo);
		Set<String> mobileNumbers = mobileNumberAndMesssage.keySet().stream().collect(Collectors.toSet());
		Map<String, String> mapOfPhnoAndUUIDs = workflowNotificationService.fetchUserUUIDs(mobileNumbers, requestInfo,
				sewerageConnection.getProperty().getTenantId());
		// Map<String, String> mapOfPhnoAndUUIDs =
		// waterConnection.getProperty().getOwners().stream().collect(Collectors.toMap(OwnerInfo::getMobileNumber,
		// OwnerInfo::getUuid));
		if (CollectionUtils.isEmpty(mapOfPhnoAndUUIDs.keySet())) {
			log.info("UUID search failed!");
		}
		List<Event> events = new ArrayList<>();
		for (String mobile : mobileNumbers) {
			if (null == mapOfPhnoAndUUIDs.get(mobile) || null == mobileNumberAndMesssage.get(mobile)) {
				log.error("No UUID/SMS for mobile {} skipping event", mobile);
				continue;
			}
			List<String> toUsers = new ArrayList<>();
			toUsers.add(mapOfPhnoAndUUIDs.get(mobile));
			Recepient recepient = Recepient.builder().toUsers(toUsers).toRoles(null).build();
			Action action = workflowNotificationService.getActionForEventNotification(mobileNumberAndMesssage, mobile,
					sewerageConnection,requestInfo);
			events.add(Event.builder().tenantId(sewerageConnection.getProperty().getTenantId())
					.description(mobileNumberAndMesssage.get(mobile)).eventType(SWConstants.USREVENTS_EVENT_TYPE)
					.name(SWConstants.USREVENTS_EVENT_NAME).postedBy(SWConstants.USREVENTS_EVENT_POSTEDBY)
					.source(Source.WEBAPP).recepient(recepient).eventDetails(null).actions(action).build());
		}
		if (!CollectionUtils.isEmpty(events)) {
			return EventRequest.builder().requestInfo(requestInfo).events(events).build();
		} else {
			return null;
		}

	}

	private List<SMSRequest> getSmsRequest(SewerageConnection sewerageConnection, RequestInfo requestInfo) {
		
		String localizationMessage = notificationUtil
				.getLocalizationMessages(sewerageConnection.getProperty().getTenantId(), requestInfo);
		String message = notificationUtil.getCustomizedMsg(SWConstants.SW_EDIT_SMS, localizationMessage);
		if (message == null) {
			log.info("No localized message found!!, Using default message");
			message = SWConstants.DEFAULT_OBJECT_MODIFIED_SMS_MSG;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		sewerageConnection.getProperty().getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		Map<String, String> mobileNumberAndMesssage = workflowNotificationService
				.getMessageForMobileNumber(mobileNumbersAndNames, sewerageConnection, message, requestInfo);
		List<SMSRequest> smsRequest = new ArrayList<>();
		mobileNumberAndMesssage.forEach((mobileNumber, messg) -> {
			SMSRequest req = new SMSRequest(mobileNumber, messg);
			smsRequest.add(req);
		});
		return smsRequest;
	}
}
