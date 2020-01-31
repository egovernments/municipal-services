package org.egov.waterConnection.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.config.WSConfiguration;
import org.egov.waterConnection.constants.WCConstants;
import org.egov.waterConnection.model.Action;
import org.egov.waterConnection.model.ActionItem;
import org.egov.waterConnection.model.Event;
import org.egov.waterConnection.model.EventRequest;
import org.egov.waterConnection.model.Recepient;
import org.egov.waterConnection.model.SMSRequest;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.repository.ServiceRequestRepository;
import org.egov.waterConnection.util.NotificationUtil;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WorkflowNotificationService {
	
	@Autowired
	private WaterServicesUtil waterServicesUtil;
	
	@Autowired
	private NotificationUtil notificationUtil;
	
	@Autowired
	private WSConfiguration config;
	
	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
	private ObjectMapper mapper;
	
	String tenantId = "tenantId";
	String serviceName = "serviceName";
	String consumerCode = "consumerCode";
	String totalBillAmount = "billAmount";
	String dueDate = "dueDate";
	
	/**
	 * 
	 * @param record record is bill response.
	 * @param topic topic is bill generation topic for water.
	 */
	public void process(WaterConnectionRequest request, String topic) {
		try {
			RequestInfo requestInfo = request.getRequestInfo();
			WaterConnection waterConnection = request.getWaterConnection();
			if (config.getIsUserEventsNotificationEnabled() != null && config.getIsUserEventsNotificationEnabled()) {
			      EventRequest eventRequest = getEventRequest(waterConnection, topic, requestInfo);
					if (eventRequest != null) {
						log.info("In App Notification :: -> " + mapper.writeValueAsString(eventRequest));
						notificationUtil.sendEventNotification(eventRequest);
					}
			}
			if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {
					List<SMSRequest> smsRequests = new LinkedList<>();
				    smsRequests = getSmsRequest(waterConnection, topic, requestInfo);
					if (smsRequests != null && !CollectionUtils.isEmpty(smsRequests)) {
						log.info("SMS Notification :: -> " + mapper.writeValueAsString(smsRequests));
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
	 * @param mappedRecord
	 * @param waterConnection
	 * @param topic
	 * @param requestInfo
	 * @return
	 */
	private EventRequest getEventRequest(WaterConnection waterConnection, String topic,
			RequestInfo requestInfo) {
		List<Event> events = new ArrayList<>();
		String localizationMessage = notificationUtil.getLocalizationMessages(waterConnection.getProperty().getTenantId(), requestInfo);
		String message = notificationUtil.getCustomizedMsgForInApp(topic, localizationMessage);
		if (message == null) {
			log.info("No message Found For Topic : " + topic);
			return null;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		waterConnection.getProperty().getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames,waterConnection,
				message);
		Set<String> mobileNumbers = mobileNumberAndMesssage.keySet().stream().collect(Collectors.toSet());
		Map<String, String> mapOfPhnoAndUUIDs = fetchUserUUIDs(mobileNumbers, requestInfo,
				waterConnection.getProperty().getTenantId());
		
		if (CollectionUtils.isEmpty(mapOfPhnoAndUUIDs.keySet())) {
			log.info("UUID search failed!");
		}
		for (String mobile : mobileNumbers) {
			if (null == mapOfPhnoAndUUIDs.get(mobile) || null == mobileNumberAndMesssage.get(mobile)) {
				log.error("No UUID/SMS for mobile {} skipping event", mobile);
				continue;
			}
			List<String> toUsers = new ArrayList<>();
			toUsers.add(mapOfPhnoAndUUIDs.get(mobile));
			Recepient recepient = Recepient.builder().toUsers(toUsers).toRoles(null).build();
		//	List<String> payTriggerList = Arrays.asList(config.getPayTriggers().split("[,]"));
			Action action = null;
			List<ActionItem> items = new ArrayList<>();
//			String actionLink = config.getPayLink().replace("$mobile", mobile)
//					.replace("$consumerCode", waterConnection.getConnectionNo())
//					.replace("$tenantId", waterConnection.getProperty().getTenantId());
//			actionLink = config.getNotificationUrl() + actionLink;
//			ActionItem item = ActionItem.builder().actionUrl(actionLink).code(config.getPayCode()).build();
//			items.add(item);
			action = Action.builder().actionUrls(items).build();
//			events.add(Event.builder().tenantId(waterConnection.getProperty().getTenantId())
//					.description(mobileNumberAndMesssage.get(mobile))
//					.eventType(WSCalculationConstant.USREVENTS_EVENT_TYPE)
//					.name(WSCalculationConstant.USREVENTS_EVENT_NAME)
//					.postedBy(WSCalculationConstant.USREVENTS_EVENT_POSTEDBY).source(Source.WEBAPP).recepient(recepient)
//					.eventDetails(null).actions(action).build());
		}
		if (!CollectionUtils.isEmpty(events)) {
			return EventRequest.builder().requestInfo(requestInfo).events(events).build();
		} else {
			return null;
		}
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
		List<SMSRequest> smsRequest = new ArrayList<>();
		String localizationMessage = notificationUtil.getLocalizationMessages(waterConnection.getProperty().getTenantId(), requestInfo);
		String message = notificationUtil.getCustomizedMsgForSMS(topic, localizationMessage);
		if (message == null) {
			log.info("No message Found For Topic : " + topic);
			return null;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		waterConnection.getProperty().getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames,waterConnection,
				message);
		mobileNumberAndMesssage.forEach((mobileNumber, messg) -> {
			if (messg.contains("<Link to Bill>")) {
				String actionLink = config.getSmsNotificationLink()
						.replace("$consumerCode", waterConnection.getConnectionNo())
						.replace("$tenantId", waterConnection.getProperty().getTenantId());
				actionLink = config.getNotificationUrl() + actionLink;
				messg = messg.replace("<Link to Bill>", actionLink);
			}
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
			if (messageToreplace.contains("<Due Date>"))
				messageToreplace = messageToreplace.replace("<Due Date>", waterConnection.getConnectionNo());
			messagetoreturn.put(mobileAndName.getKey(), messageToreplace);
		}
		return messagetoreturn;
	}
	
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public HashMap<String, String> mapRecords(DocumentContext context) {
		HashMap<String, String> mappedRecord = new HashMap<>();
		try {
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
			mappedRecord.put(tenantId, context.read("$.Bill[0].billDetails[0].tenantId"));
			mappedRecord.put(serviceName, context.read("$.Bill[0].businessService"));
			mappedRecord.put(consumerCode, context.read("$.Bill[0].consumerCode"));
			mappedRecord.put(totalBillAmount, context.read("$.Bill[0].totalAmount").toString());
			mappedRecord.put(dueDate, getLatestBillDetails(mapper.writeValueAsString(context.read("$.Bill[0].billDetails"))));
		} catch (Exception ex) {
			ex.printStackTrace();
	            throw new CustomException("Bill Fetch Error","Unable to fetch values from bill");
		}

		return mappedRecord;
	}
	
	private String getLatestBillDetails(String billdetails) {
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		log.info("Bill Details : -> " + billdetails);
		JSONArray jsonArray = new JSONArray(billdetails);
		ArrayList<Long> billDates = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObj = jsonArray.getJSONObject(i);
			billDates.add((Long) jsonObj.get("expiryDate"));
		}
		Collections.sort(billDates, Collections.reverseOrder());
		
		return formatter.format(billDates.get(0));
	}
	/**
     * Fetches UUIDs of CITIZENs based on the phone number.
     * 
     * @param mobileNumbers
     * @param requestInfo
     * @param tenantId
     * @return
     */
    private Map<String, String> fetchUserUUIDs(Set<String> mobileNumbers, RequestInfo requestInfo, String tenantId) {
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
