package org.egov.swcalculation.service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swcalculation.config.SWCalculationConfiguration;
import org.egov.swcalculation.constants.SWCalculationConstant;
import org.egov.swcalculation.model.Action;
import org.egov.swcalculation.model.ActionItem;
import org.egov.swcalculation.model.DemandNotificationObj;
import org.egov.swcalculation.model.Event;
import org.egov.swcalculation.model.EventRequest;
import org.egov.swcalculation.model.NotificationReceiver;
import org.egov.swcalculation.model.Property;
import org.egov.swcalculation.model.Recepient;
import org.egov.swcalculation.model.SMSRequest;
import org.egov.swcalculation.model.SewerageConnection;
import org.egov.swcalculation.model.SewerageConnectionRequest;
import org.egov.swcalculation.model.Source;
import org.egov.swcalculation.repository.ServiceRequestRepository;
import org.egov.swcalculation.util.CalculatorUtils;
import org.egov.swcalculation.util.SWCalculationUtil;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Slf4j
@Component
public class PaymentNotificationService {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private SWCalculationConfiguration config;

	@Autowired
	private SWCalculationUtil util;

	@Autowired
	private CalculatorUtils calculatorUtils;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private MasterDataService service;
	
	@Autowired
	private SWCalculationUtil sWCalculationUtil;

	String tenantId = "tenantId";
	String serviceName = "serviceName";
	String consumerCode = "consumerCode";
	String totalBillAmount = "billAmount";
	String dueDate = "dueDate";

	@SuppressWarnings("unchecked")
	public void process(HashMap<String, Object> record, String topic) {
		try {
			HashMap<String, Object> billRes = (HashMap<String, Object>) record.get("billResponse");
			String jsonString = new JSONObject(billRes).toString();
			DocumentContext context = JsonPath.parse(jsonString);
			HashMap<String, String> mappedRecord = mapRecords(context);
			Map<String, Object> info = (Map<String, Object>) record.get("requestInfo");
			RequestInfo requestInfo = mapper.convertValue(info, RequestInfo.class);
			
			SewerageConnection sewerageConnection = calculatorUtils.getSewerageConnection(requestInfo,
					mappedRecord.get(consumerCode), mappedRecord.get(tenantId));
			
			SewerageConnectionRequest sewerageConnectionRequest = SewerageConnectionRequest.builder()
					.sewerageConnection(sewerageConnection).requestInfo(requestInfo).build();
			Property property = sWCalculationUtil.getProperty(sewerageConnectionRequest);
			
			if (null != config.getIsUserEventsNotificationEnabled()) {
				if (config.getIsUserEventsNotificationEnabled()) {
					if (SWCalculationConstant.SERVICE_FIELD_VALUE_SW.equalsIgnoreCase(mappedRecord.get(serviceName))) {
						if (sewerageConnection == null) {
							throw new CustomException("Sewerage Connection not found for given criteria ",
									"Sewerage Connection are not present for " + mappedRecord.get(consumerCode)
											+ " connection no");
						}
						EventRequest eventRequest = getEventRequest(mappedRecord, sewerageConnectionRequest, topic,
								property);
						if (null != eventRequest)
							util.sendEventNotification(eventRequest);

					}
				}
			}
			if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {
				if (mappedRecord.get(serviceName).equalsIgnoreCase(SWCalculationConstant.SERVICE_FIELD_VALUE_SW)) {
					if (sewerageConnection == null) {
						throw new CustomException("Water Connection not found for given criteria ",
								"Water Connection are not present for " + mappedRecord.get(consumerCode)
										+ " connection no");
					}
					List<SMSRequest> smsRequests = new LinkedList<>();
					smsRequests = getSmsRequest(mappedRecord, sewerageConnectionRequest, topic, property);
					if (smsRequests != null && !CollectionUtils.isEmpty(smsRequests)) {
						log.info("SMS Notification :: -> " + mapper.writeValueAsString(smsRequests));
						util.sendSMS(smsRequests);
					}
				}
			}
		}

		catch (Exception ex) {
			log.error("Exception while processing record: ", ex);
		}
	}

	private List<SMSRequest> getSmsRequest(HashMap<String, String> mappedRecord, SewerageConnectionRequest sewerageConnectionRequest,
			String topic, Property property) {
		String localizationMessage = util.getLocalizationMessages(mappedRecord.get(tenantId), sewerageConnectionRequest.getRequestInfo());
		String message = util.getCustomizedMsgForSMS(topic, localizationMessage);
		if (message == null) {
			log.info("No message Found For Topic : " + topic);
			return null;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		property.getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames, mappedRecord,
				message);
		List<SMSRequest> smsRequest = new ArrayList<>();
		mobileNumberAndMesssage.forEach((mobileNumber, messg) -> {
			if (messg.contains("<Link to Bill>")) {
				String actionLink = config.getSmsNotificationLink()
						.replace("$consumerCode", sewerageConnectionRequest.getSewerageConnection().getConnectionNo())
						.replace("$tenantId", property.getTenantId());
				actionLink = config.getNotificationUrl() + actionLink;
				messg = messg.replace("<Link to Bill>", actionLink);
			}
			SMSRequest req = new SMSRequest(mobileNumber, messg);
			smsRequest.add(req);
		});
		return smsRequest;
	}

//	@SuppressWarnings("unused")
//	private void enrichSMSRequest(List<SMSRequest> smsRequest, String topic, HashMap<String, String> mappedRecord,
//			RequestInfo requestInfo, SewerageConnection sewerageConnection) {
//		String localizationMessages = util.getLocalizationMessages(mappedRecord.get(tenantId), requestInfo);
//		String message = util.getCustomizedMsg(topic, localizationMessages);
//
//		if (message == null) {
//			log.info("No message Found For Topic : " + topic);
//		}
//		Map<String, String> mobileNumbersAndNames = new HashMap<>();
//		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames, mappedRecord,
//				message);
//		sewerageConnection.getProperty().getOwners().forEach(owner -> {
//			if (owner.getMobileNumber() != null)
//				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
//			SMSRequest sms = new SMSRequest(owner.getMobileNumber(), message);
//			smsRequest.add(sms);
//		});
//
//	}

	@SuppressWarnings("unused")
	private void enrichNotificationReceivers(List<NotificationReceiver> receiverList,
			DemandNotificationObj notificationObj) {
		try {
			JSONArray receiver = service.getMasterListOfReceiver(notificationObj.getRequestInfo(),
					notificationObj.getTenantId());
			receiverList.addAll(mapper.readValue(receiver.toJSONString(),
					mapper.getTypeFactory().constructCollectionType(List.class, NotificationReceiver.class)));
		} catch (IOException e) {
			throw new CustomException("Parsing Exception", " Notification Receiver List Can Not Be Parsed!!");
		}
	}

	private EventRequest getEventRequest(HashMap<String, String> mappedRecord, SewerageConnectionRequest sewerageConnectionRequest,
			String topic, Property property) {

		String localizationMessages = util.getLocalizationMessages(mappedRecord.get(tenantId), sewerageConnectionRequest.getRequestInfo());
		String message = util.getCustomizedMsgForInApp(topic, localizationMessages);

		if (message == null) {
			log.info("No message Found For Topic : " + topic);
			return null;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		property.getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames, mappedRecord,
				message);
		Set<String> mobileNumbers = mobileNumberAndMesssage.keySet().stream().collect(Collectors.toSet());
		// Map<String, String> mapOfPhnoAndUUIDs =
		// sewerageConnection.getProperty().getOwners().stream()
		// .collect(Collectors.toMap(OwnerInfo::getMobileNumber,
		// OwnerInfo::getUuid));

		Map<String, String> mapOfPhnoAndUUIDs = fetchUserUUIDs(mobileNumbers, sewerageConnectionRequest.getRequestInfo(),
				property.getTenantId());
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
			Action action = null;
			List<ActionItem> items = new ArrayList<>();
			String actionLink = config.getPayLink().replace("$mobile", mobile)
					.replace("$consumerCode", sewerageConnectionRequest.getSewerageConnection().getConnectionNo())
					.replace("$tenantId", property.getTenantId());
			actionLink = config.getNotificationUrl() + actionLink;
			ActionItem item = ActionItem.builder().actionUrl(actionLink).code(config.getPayCode()).build();
			items.add(item);
			action = Action.builder().actionUrls(items).build();
			events.add(Event.builder().tenantId(property.getTenantId())
					.description(mobileNumberAndMesssage.get(mobile))
					.eventType(SWCalculationConstant.USREVENTS_EVENT_TYPE)
					.name(SWCalculationConstant.USREVENTS_EVENT_NAME)
					.postedBy(SWCalculationConstant.USREVENTS_EVENT_POSTEDBY).source(Source.WEBAPP).recepient(recepient)
					.eventDetails(null).actions(action).build());

		}
		if (!CollectionUtils.isEmpty(events)) {
			return EventRequest.builder().requestInfo(sewerageConnectionRequest.getRequestInfo()).events(events).build();
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public HashMap<String, String> mapRecords(DocumentContext context) {
		HashMap<String, String> mappedRecord = new HashMap<>();
		try {
			mappedRecord.put(tenantId, context.read("$.Bill[0].billDetails[0].tenantId"));
			mappedRecord.put(serviceName, context.read("$.Bill[0].businessService"));
			mappedRecord.put(consumerCode, context.read("$.Bill[0].consumerCode"));
			mappedRecord.put(totalBillAmount, context.read("$.Bill[0].totalAmount").toString());
			mappedRecord.put(dueDate,
					getLatestBillDetails(mapper.writeValueAsString(context.read("$.Bill[0].billDetails"))));
		} catch (Exception ex) {
			log.error("Unable to fetch values from bill ",ex);
			throw new CustomException("Bill Fetch Error", "Unable to fetch values from bill");
		}

		return mappedRecord;
	}

	private String getLatestBillDetails(String billdetails) {
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		log.info("Bill Details : -> " + billdetails);
		org.json.JSONArray jsonArray = new org.json.JSONArray(billdetails);
		ArrayList<Long> billDates = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObj = jsonArray.getJSONObject(i);
			billDates.add((Long) jsonObj.get("expiryDate"));
		}
		Collections.sort(billDates, Collections.reverseOrder());

		return formatter.format(billDates.get(0));
	}

	public Map<String, String> getMessageForMobileNumber(Map<String, String> mobileNumbersAndNames,
			HashMap<String, String> mapRecords, String message) {
		Map<String, String> messagetoreturn = new HashMap<>();
		String messageToreplace = message;
		for (Entry<String, String> mobileAndName : mobileNumbersAndNames.entrySet()) {
			messageToreplace = message;
			if (messageToreplace.contains("<Owner Name>"))
				messageToreplace = messageToreplace.replace("<Owner Name>", mobileAndName.getValue());
			if (messageToreplace.contains("<Service>"))
				messageToreplace = messageToreplace.replace("<Service>", SWCalculationConstant.SERVICE_FIELD_VALUE_SW);
			if (messageToreplace.contains("<bill amount>"))
				messageToreplace = messageToreplace.replace("<bill amount>", mapRecords.get(totalBillAmount));
			if (messageToreplace.contains("<Due Date>"))
				messageToreplace = messageToreplace.replace("<Due Date>", mapRecords.get(dueDate));
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
	private Map<String, String> fetchUserUUIDs(Set<String> mobileNumbers, RequestInfo requestInfo, String tenantId) {
		Map<String, String> mapOfPhnoAndUUIDs = new HashMap<>();
		StringBuilder uri = new StringBuilder();
		uri.append(config.getUserHost()).append(config.getUserSearchEndpoint());
		Map<String, Object> userSearchRequest = new HashMap<>();
		userSearchRequest.put("RequestInfo", requestInfo);
		userSearchRequest.put("tenantId", tenantId);
		userSearchRequest.put("userType", "CITIZEN");
		for (String mobileNo : mobileNumbers) {
			userSearchRequest.put("userName", mobileNo);
			try {
				Object user = serviceRequestRepository.fetchResult(uri, userSearchRequest);
				if (null != user) {
					String uuid = JsonPath.read(user, "$.user[0].uuid");
					mapOfPhnoAndUUIDs.put(mobileNo, uuid);
				} else {
					log.error("Service returned null while fetching user for username - " + mobileNo);
				}
			} catch (Exception e) {
				log.error("Exception while fetching user for username - " + mobileNo);
				log.error("Exception trace: ", e);
				continue;
			}
		}
		return mapOfPhnoAndUUIDs;
	}

}
