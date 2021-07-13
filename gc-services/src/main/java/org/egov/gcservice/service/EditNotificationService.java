package org.egov.gcservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.egov.gcservice.config.GCConfiguration;
import org.egov.gcservice.util.NotificationUtil;
import org.egov.gcservice.util.GCConstants;
import org.egov.gcservice.util.GarbageServicesUtil;
import org.egov.gcservice.validator.ValidateProperty;
import org.egov.gcservice.web.models.Action;
import org.egov.gcservice.web.models.Category;
import org.egov.gcservice.web.models.Event;
import org.egov.gcservice.web.models.EventRequest;
import org.egov.gcservice.web.models.Property;
import org.egov.gcservice.web.models.Recepient;
import org.egov.gcservice.web.models.SMSRequest;
import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.gcservice.web.models.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EditNotificationService {

	@Autowired
	private GCConfiguration config;

	@Autowired
	private NotificationUtil notificationUtil;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WorkflowNotificationService workflowNotificationService;
	
	@Autowired
	private ValidateProperty validateProperty;
	
	@Autowired
	private GarbageServicesUtil servicesUtil;


	public void sendEditNotification(GarbageConnectionRequest request) {
		try {
			Property property = validateProperty.getOrValidateProperty(request);
			
			if (config.getIsUserEventsNotificationEnabled() != null && config.getIsUserEventsNotificationEnabled()) {
				EventRequest eventRequest = getEventRequest(request, property);
				if (eventRequest != null) {
					notificationUtil.sendEventNotification(eventRequest);
				}
			}
			if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {
				List<SMSRequest> smsRequests = getSmsRequest(request, property);
				if (!CollectionUtils.isEmpty(smsRequests)) {
					notificationUtil.sendSMS(smsRequests);
				}
			}
		} catch (Exception ex) {
			log.error("Exception while trying to process edit notification.", ex);
		}
	}

	private EventRequest getEventRequest(GarbageConnectionRequest garbageConnectionRequest, Property property) {
		
		String localizationMessage = notificationUtil
				.getLocalizationMessages(property.getTenantId(), garbageConnectionRequest.getRequestInfo());
		String code = GCConstants.SW_EDIT_IN_APP;
//		if ((!garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction().equalsIgnoreCase(GCConstants.ACTIVATE_CONNECTION))
//				&& servicesUtil.isModifyConnectionRequest(garbageConnectionRequest)) {
//			code = GCConstants.SW_MODIFY_IN_APP;
//		}
		String message = notificationUtil.getCustomizedMsg(code, localizationMessage);
		if (message == null) {
			log.info("No localized message found!!, Using default message");
			message = GCConstants.DEFAULT_OBJECT_EDIT_APP_MSG;
			if (code.equalsIgnoreCase(GCConstants.SW_MODIFY_IN_APP))
				message = GCConstants.DEFAULT_OBJECT_MODIFY_APP_MSG;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		property.getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		//send the notification to the connection holders
		if (!CollectionUtils.isEmpty(garbageConnectionRequest.getGarbageConnection().getConnectionHolders())) {
			garbageConnectionRequest.getGarbageConnection().getConnectionHolders().forEach(holder -> {
				if (!StringUtils.isEmpty(holder.getMobileNumber())) {
					mobileNumbersAndNames.put(holder.getMobileNumber(), holder.getName());
				}
			});
		}
		Map<String, String> mobileNumberAndMesssage = workflowNotificationService
				.getMessageForMobileNumber(mobileNumbersAndNames, garbageConnectionRequest, message, property);
		Set<String> mobileNumbers = new HashSet<>(mobileNumberAndMesssage.keySet());
		Map<String, String> mapOfPhoneNoAndUUIDs = workflowNotificationService.fetchUserUUIDs(mobileNumbers,
				garbageConnectionRequest.getRequestInfo(), property.getTenantId());
		if (CollectionUtils.isEmpty(mapOfPhoneNoAndUUIDs.keySet())) {
			log.info("UUID search failed!");
		}
		List<Event> events = new ArrayList<>();
		for (String mobile : mobileNumbers) {
			if (null == mapOfPhoneNoAndUUIDs.get(mobile) || null == mobileNumberAndMesssage.get(mobile)) {
				log.error("No UUID/SMS for mobile {} skipping event", mobile);
				continue;
			}
			List<String> toUsers = new ArrayList<>();
			toUsers.add(mapOfPhoneNoAndUUIDs.get(mobile));
			Recepient recepient = Recepient.builder().toUsers(toUsers).toRoles(null).build();
			Action action = workflowNotificationService.getActionForEventNotification(mobileNumberAndMesssage, mobile,
					garbageConnectionRequest, property);
			events.add(Event.builder().tenantId(property.getTenantId())
					.description(mobileNumberAndMesssage.get(mobile)).eventType(GCConstants.USREVENTS_EVENT_TYPE)
					.name(GCConstants.USREVENTS_EVENT_NAME).postedBy(GCConstants.USREVENTS_EVENT_POSTEDBY)
					.source(Source.WEBAPP).recepient(recepient).eventDetails(null).actions(action).build());
		}
		if (!CollectionUtils.isEmpty(events)) {
			return EventRequest.builder().requestInfo(garbageConnectionRequest.getRequestInfo()).events(events).build();
		} else {
			return null;
		}

	}

	private List<SMSRequest> getSmsRequest(GarbageConnectionRequest garbageConnectionRequest, Property property) {
		
		String localizationMessage = notificationUtil
				.getLocalizationMessages(property.getTenantId(), garbageConnectionRequest.getRequestInfo());
		String code = GCConstants.SW_EDIT_SMS;
//		if ((!garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction().equalsIgnoreCase(GCConstants.ACTIVATE_CONNECTION))
//				&& servicesUtil.isModifyConnectionRequest(garbageConnectionRequest)) {
//			code = GCConstants.SW_MODIFY_SMS;
//		}
		String message = notificationUtil.getCustomizedMsg(code, localizationMessage);
		if (message == null) {
			log.info("No localized message found!!, Using default message");
			message = GCConstants.DEFAULT_OBJECT_EDIT_SMS_MSG;
			if (code.equalsIgnoreCase(GCConstants.SW_MODIFY_SMS)) {
				message = GCConstants.DEFAULT_OBJECT_MODIFY_SMS_MSG;
			}
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		property.getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		//send the notification to the connection holders
		if (!CollectionUtils.isEmpty(garbageConnectionRequest.getGarbageConnection().getConnectionHolders())) {
			garbageConnectionRequest.getGarbageConnection().getConnectionHolders().forEach(holder -> {
				if (!StringUtils.isEmpty(holder.getMobileNumber())) {
					mobileNumbersAndNames.put(holder.getMobileNumber(), holder.getName());
				}
			});
		}
		Map<String, String> mobileNumberAndMessage = workflowNotificationService
				.getMessageForMobileNumber(mobileNumbersAndNames, garbageConnectionRequest, message, property);
		List<SMSRequest> smsRequest = new ArrayList<>();
		mobileNumberAndMessage.forEach((mobileNumber, msg) -> {
			SMSRequest req = SMSRequest.builder().mobileNumber(mobileNumber).message(msg).category(Category.TRANSACTION).build();
			smsRequest.add(req);
		});
		return smsRequest;
	}
}
