package org.egov.waterconnection.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.waterconnection.config.WSConfiguration;
import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.model.Action;
import org.egov.waterconnection.model.Event;
import org.egov.waterconnection.model.EventRequest;
import org.egov.waterconnection.model.Property;
import org.egov.waterconnection.model.Recepient;
import org.egov.waterconnection.model.SMSRequest;
import org.egov.waterconnection.model.Source;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.egov.waterconnection.util.NotificationUtil;
import org.egov.waterconnection.validator.ValidateProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EditNotificationService {

	@Autowired
	private WSConfiguration config;

	@Autowired
	private NotificationUtil notificationUtil;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WorkflowNotificationService workflowNotificationService;
	
	@Autowired
	private ValidateProperty validateProperty;
	
	

	public void sendEditNotification(WaterConnectionRequest request) {
		try {
			
			Property property = validateProperty.getOrValidateProperty(request);
			
			if (config.getIsUserEventsNotificationEnabled() != null && config.getIsUserEventsNotificationEnabled()) {
				EventRequest eventRequest = getEventRequest(request, property);
				if (eventRequest != null) {
					log.debug("IN APP NOTIFICATION FOR EDIT APPLICATION :: -> "
							+ mapper.writeValueAsString(eventRequest));
					notificationUtil.sendEventNotification(eventRequest);
				}
			}
			if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {
				List<SMSRequest> smsRequests = getSmsRequest(request, property);
				if (!CollectionUtils.isEmpty(smsRequests)) {
					log.debug("SMS NOTIFICATION FOR EDIT APPLICATION :: -> " + mapper.writeValueAsString(smsRequests));
					notificationUtil.sendSMS(smsRequests);
				}
			}
		} catch (Exception ex) {
			log.error("Exception when trying to send notification. ", ex);
		}
	}

	private EventRequest getEventRequest(WaterConnectionRequest waterConnectionRequest, Property property) {
		String localizationMessage = notificationUtil
				.getLocalizationMessages(property.getTenantId(), waterConnectionRequest.getRequestInfo());
		String message = notificationUtil.getCustomizedMsg(WCConstants.WS_EDIT_IN_APP, localizationMessage);
		if (message == null) {
			log.info("No localized message found!!, Using default message");
			message = WCConstants.DEFAULT_OBJECT_MODIFIED_APP_MSG;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		property.getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		Map<String, String> mobileNumberAndMesssage = workflowNotificationService
				.getMessageForMobileNumber(mobileNumbersAndNames, waterConnectionRequest, message, property);
		Set<String> mobileNumbers = mobileNumberAndMesssage.keySet().stream().collect(Collectors.toSet());
		Map<String, String> mapOfPhnoAndUUIDs = workflowNotificationService.fetchUserUUIDs(mobileNumbers, waterConnectionRequest.getRequestInfo(),
				property.getTenantId());
//		 Map<String, String> mapOfPhnoAndUUIDs =
//		 waterConnection.getProperty().getOwners().stream().collect(Collectors.toMap(OwnerInfo::getMobileNumber,
//		 OwnerInfo::getUuid));
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
					waterConnectionRequest, property);
			events.add(Event.builder().tenantId(property.getTenantId())
					.description(mobileNumberAndMesssage.get(mobile)).eventType(WCConstants.USREVENTS_EVENT_TYPE)
					.name(WCConstants.USREVENTS_EVENT_NAME).postedBy(WCConstants.USREVENTS_EVENT_POSTEDBY)
					.source(Source.WEBAPP).recepient(recepient).eventDetails(null).actions(action).build());
		}
		if (!CollectionUtils.isEmpty(events)) {
			return EventRequest.builder().requestInfo(waterConnectionRequest.getRequestInfo()).events(events).build();
		} else {
			return null;
		}

	}

	private List<SMSRequest> getSmsRequest(WaterConnectionRequest waterConnectionRequest, Property property) {
		String localizationMessage = notificationUtil
				.getLocalizationMessages(property.getTenantId(), waterConnectionRequest.getRequestInfo());
		String message = notificationUtil.getCustomizedMsg(WCConstants.WS_EDIT_SMS, localizationMessage);
		if (message == null) {
			log.info("No localized message found!!, Using default message");
			message = WCConstants.DEFAULT_OBJECT_MODIFIED_SMS_MSG;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		property.getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		Map<String, String> mobileNumberAndMesssage = workflowNotificationService
				.getMessageForMobileNumber(mobileNumbersAndNames, waterConnectionRequest, message, property);
		List<SMSRequest> smsRequest = new ArrayList<>();
		mobileNumberAndMesssage.forEach((mobileNumber, messg) -> {
			SMSRequest req = new SMSRequest(mobileNumber, messg);
			smsRequest.add(req);
		});
		return smsRequest;
	}
}
