package org.egov.waterConnection.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.waterConnection.config.WSConfiguration;
import org.egov.waterConnection.constants.WCConstants;
import org.egov.waterConnection.model.Action;
import org.egov.waterConnection.model.ActionItem;
import org.egov.waterConnection.model.Event;
import org.egov.waterConnection.model.EventRequest;
import org.egov.waterConnection.model.Recepient;
import org.egov.waterConnection.model.SMSRequest;
import org.egov.waterConnection.model.Source;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.repository.ServiceRequestRepository;
import org.egov.waterConnection.util.NotificationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkflowNotificationService {
	
	@Autowired
	private NotificationUtil notificationUtil;
	
	@Autowired
	private WSConfiguration config;
	
	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
	private ObjectMapper mapper;
	

	/**
	 * 
	 * @param record record is bill response.
	 * @param topic topic is bill generation topic for water.
	 */
	public void process(WaterConnectionRequest request, String topic) {
		try {
			if (!WCConstants.NOTIFICATION_ENABLE_FOR_STATUS.contains(request.getWaterConnection().getAction()+"_"+request.getWaterConnection().getApplicationStatus().name())) {
				log.info("Notification Disabled For State :" + request.getWaterConnection().getApplicationStatus().name());
				return;
			}
			if (config.getIsUserEventsNotificationEnabled() != null && config.getIsUserEventsNotificationEnabled()) {
			      EventRequest eventRequest = getEventRequest(request.getWaterConnection(), topic, request.getRequestInfo());
					if (eventRequest != null) {
						log.info("In App Notification For WorkFlow :: -> " + mapper.writeValueAsString(eventRequest));
						notificationUtil.sendEventNotification(eventRequest);
					}
			}
			if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {
					List<SMSRequest> smsRequests = getSmsRequest(request.getWaterConnection(), topic, request.getRequestInfo());
					if (!CollectionUtils.isEmpty(smsRequests)) {
						log.info("SMS Notification For WorkFlow:: -> " + mapper.writeValueAsString(smsRequests));
						notificationUtil.sendSMS(smsRequests);
					}
			}

		} catch (Exception ex) {
			log.error(ex.toString());
			log.error("Error occured while processing the record from topic : " + topic);
		}
	}
	
	/**
	 * 
	 * @param waterConnection
	 * @param topic
	 * @param requestInfo
	 * @return EventRequest Object
	 */
	private EventRequest getEventRequest(WaterConnection waterConnection, String topic,
			RequestInfo requestInfo) {
		String localizationMessage = notificationUtil
				.getLocalizationMessages(waterConnection.getProperty().getTenantId(), requestInfo);
		String message = notificationUtil.getCustomizedMsgForInApp(waterConnection.getAction(), waterConnection.getApplicationStatus().name(),
				localizationMessage);
		if (message == null) {
			log.info("No message Found For Topic : " + topic);
			return null;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		waterConnection.getProperty().getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames, waterConnection,
				message);
		Set<String> mobileNumbers = mobileNumberAndMesssage.keySet().stream().collect(Collectors.toSet());
		Map<String, String> mapOfPhnoAndUUIDs = fetchUserUUIDs(mobileNumbers, requestInfo,
				waterConnection.getProperty().getTenantId());
//		Map<String, String> mapOfPhnoAndUUIDs = waterConnection.getProperty().getOwners().stream().collect(Collectors.toMap(OwnerInfo::getMobileNumber, OwnerInfo::getUuid));

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
			// List<String> payTriggerList =
			// Arrays.asList(config.getPayTriggers().split("[,]"));

			Action  action = getActionForEventNotification(mobileNumberAndMesssage, mobile, waterConnection);
			events.add(Event.builder().tenantId(waterConnection.getProperty().getTenantId())
					.description(mobileNumberAndMesssage.get(mobile)).eventType(WCConstants.USREVENTS_EVENT_TYPE)
					.name(WCConstants.USREVENTS_EVENT_NAME).postedBy(WCConstants.USREVENTS_EVENT_POSTEDBY)
					.source(Source.WEBAPP).recepient(recepient).eventDetails(null).actions(action).build());
		}
		if (!CollectionUtils.isEmpty(events)) {
			return EventRequest.builder().requestInfo(requestInfo).events(events).build();
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param messageTemplate
	 * @param connection
	 * @return return action link
	 */
	public Action getActionForEventNotification(Map<String, String> mobileNumberAndMesssage,
			String mobileNumber, WaterConnection connection) {
		String messageTemplate = mobileNumberAndMesssage.get(mobileNumber);
		List<ActionItem> items = new ArrayList<>();
		if (messageTemplate.contains("<Action Button>")) {
			String code = StringUtils.substringBetween(messageTemplate, "<Action Button>", "</Action Button>");
			messageTemplate = messageTemplate.replace("<Action Button>", "");
			messageTemplate = messageTemplate.replace("</Action Button>", "");
			messageTemplate = messageTemplate.replace(code, "");
			String actionLink = "";
			if (code.equalsIgnoreCase("Download Application")) {

				// String actionLink = config.getPayLink().replace("$mobile", mobile)
				// .replace("$consumerCode", waterConnection.getConnectionNo())
				// .replace("$tenantId", waterConnection.getProperty().getTenantId());
				actionLink = config.getNotificationUrl();
			}
			if (code.equalsIgnoreCase("PAY NOW")) {
				actionLink = config.getNotificationUrl();
			}
			if (code.equalsIgnoreCase("DOWNLOAD RECEIPT")) {
				actionLink = config.getNotificationUrl();
			}
			ActionItem item = ActionItem.builder().actionUrl(actionLink).code(code).build();
			items.add(item);
			mobileNumberAndMesssage.replace(mobileNumber, messageTemplate);
		}
		return Action.builder().actionUrls(items).build();
	}
	
	/**
	 * 
	 * @param mappedRecord
	 * @param waterConnection
	 * @param topic
	 * @param requestInfo
	 * @return
	 */
	private List<SMSRequest> getSmsRequest(WaterConnection waterConnection, String topic,
			RequestInfo requestInfo) {
		String localizationMessage = notificationUtil
				.getLocalizationMessages(waterConnection.getProperty().getTenantId(), requestInfo);
		String message = notificationUtil.getCustomizedMsgForSMS(waterConnection.getAction(), waterConnection.getApplicationStatus().name(), localizationMessage);
		if (message == null) {
			log.info("No message Found For Topic : " + topic);
			return null;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		waterConnection.getProperty().getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames, waterConnection,
				message);
		List<SMSRequest> smsRequest = new ArrayList<>();
		mobileNumberAndMesssage.forEach((mobileNumber, messg) -> {
			SMSRequest req = new SMSRequest(mobileNumber, messg);
			smsRequest.add(req);
		});
		return smsRequest;
	}
	
	public Map<String, String> getMessageForMobileNumber(Map<String, String> mobileNumbersAndNames,
			WaterConnection waterConnection, String message) {
		Map<String, String> messagetoreturn = new HashMap<>();
		String messageToreplace = message;
		for (Entry<String, String> mobileAndName : mobileNumbersAndNames.entrySet()) {
			messageToreplace = message;
			if (messageToreplace.contains("<Owner Name>"))
				messageToreplace = messageToreplace.replace("<Owner Name>", mobileAndName.getValue());
			if (messageToreplace.contains("<Service>"))
				messageToreplace = messageToreplace.replace("<Service>", WCConstants.SERVICE_FIELD_VALUE_WS);

			if (messageToreplace.contains("<Application number>"))
				messageToreplace = messageToreplace.replace("<Application number>", waterConnection.getApplicationNo());

			if (messageToreplace.contains("<Application download link>"))
				messageToreplace = messageToreplace.replace("<Application download link>", config.getNotificationUrl());

			if (messageToreplace.contains("<mseva URL>"))
				messageToreplace = messageToreplace.replace("<mseva URL>", config.getNotificationUrl());

			if (messageToreplace.contains("<mseva app link>"))
				messageToreplace = messageToreplace.replace("<mseva app link>", config.getNotificationUrl());

			if (messageToreplace.contains("<View History Link>"))
				messageToreplace = messageToreplace.replace("<View History Link>", config.getNotificationUrl());

			if (messageToreplace.contains("<payment link>"))
				messageToreplace = messageToreplace.replace("<payment link>", config.getNotificationUrl());

			if (messageToreplace.contains("<receipt download link>"))
				messageToreplace = messageToreplace.replace("<receipt download link>", config.getNotificationUrl());

			if (messageToreplace.contains("<connection details page>"))
				messageToreplace = messageToreplace.replace("<connection details page>", config.getNotificationUrl());
			messagetoreturn.put(mobileAndName.getKey(), messageToreplace);
		}
		return messagetoreturn;
	}
	
	
	
	/**
     * Fetches UUIDs of CITIZENs based on the phone number.
     * 
     * @param mobileNumbers
     * @param requestInfo
     * @param tenantId
     * @return
     */
    public Map<String, String> fetchUserUUIDs(Set<String> mobileNumbers, RequestInfo requestInfo, String tenantId) {
    	Map<String, String> mapOfPhnoAndUUIDs = new HashMap<>();
    	StringBuilder uri = new StringBuilder();
    	uri.append(config.getUserHost()).append(config.getUserSearchEndpoint());
    	Map<String, Object> userSearchRequest = new HashMap<>();
    	userSearchRequest.put("RequestInfo", requestInfo);
		userSearchRequest.put("tenantId", tenantId);
		userSearchRequest.put("userType", "CITIZEN");
    	for(String mobileNo: mobileNumbers) {
    		userSearchRequest.put("userName", mobileNo);
    		try {
    			Object user = serviceRequestRepository.fetchResult(uri, userSearchRequest);
    			if(null != user) {
    				String uuid = JsonPath.read(user, "$.user[0].uuid");
    				mapOfPhnoAndUUIDs.put(mobileNo, uuid);
    			}else {
        			log.error("Service returned null while fetching user for username - "+mobileNo);
    			}
    		}catch(Exception e) {
    			log.error("Exception while fetching user for username - "+mobileNo);
    			log.error("Exception trace: ",e);
    			continue;
    		}
    	}
    	return mapOfPhnoAndUUIDs;
    }

	

}
