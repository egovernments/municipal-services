package org.egov.wscalculation.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.egov.tracer.model.CustomException;
import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.egov.wscalculation.constants.WSCalculationConstant;
import org.egov.wscalculation.util.CalculatorUtil;
import org.egov.wscalculation.util.NotificationUtil;
import org.egov.wscalculation.util.WSCalculationUtil;
import org.egov.wscalculation.web.models.Category;
import org.egov.wscalculation.web.models.DemandNotificationObj;
import org.egov.wscalculation.web.models.EmailRequest;
import org.egov.wscalculation.web.models.NotificationReceiver;
import org.egov.wscalculation.web.models.Property;
import org.egov.wscalculation.web.models.SMSRequest;
import org.egov.wscalculation.web.models.SearchCriteria;
import org.egov.wscalculation.web.models.WaterConnection;
import org.egov.wscalculation.web.models.WaterConnectionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class DemandNotificationService {

	@Autowired
	private NotificationUtil util;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private MasterDataService masterDataService;

	@Autowired
	private WSCalculationConfiguration config;
	
	@Autowired
	private CalculatorUtil calculatorUtil;
	
	
	@Autowired
	private WSCalculationUtil wSCalculationUtil;
	
	

	public void process(DemandNotificationObj notificationObj, String topic) {
		if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {
			List<SMSRequest> smsRequests = new LinkedList<>();
			System.out.println("process ::1 topic "+topic +" smsRequests"+smsRequests);
			enrichSMSRequest(notificationObj, smsRequests, topic);
			if (!CollectionUtils.isEmpty(smsRequests))
				util.sendSMS(smsRequests);
		}
		if (config.getIsEmailEnabled() != null && config.getIsEmailEnabled()) {
			List<EmailRequest> emailRequests = new LinkedList<>();
			enrichEmailRequest(notificationObj, emailRequests, topic);
		}
	}

	@SuppressWarnings("unused")
	private void enrichSMSRequest(DemandNotificationObj notificationObj, List<SMSRequest> smsRequest, String topic) {
		String tenantId = notificationObj.getTenantId();
		String localizationMessage = util.getLocalizationMessages(tenantId, notificationObj.getRequestInfo());
		String messageTemplate = util.getCustomizedMsgForSMS(topic, localizationMessage);
		System.out.println("enrichSMSRequest ::2 "+notificationObj);
		if (messageTemplate == null) {
			log.info("No message Found For Topic : " + topic);
		}
		//List<NotificationReceiver> receiverList = new ArrayList<>();
		
		Set<String> waterConnectionIdList = notificationObj.getWaterConnectionIds();
		SearchCriteria searchCriteria = new SearchCriteria();
		searchCriteria.setIds(waterConnectionIdList);
		searchCriteria.setTenantId(tenantId);
		List<WaterConnection> waterConnections =  calculatorUtil.getWaterConnectionOnApplicationNO(notificationObj.getRequestInfo(),searchCriteria);
		log.info("waterConnections.size() "+waterConnections.size());
		System.out.println("messageTemplate **** "+messageTemplate+ "localizationMessage ::****" +localizationMessage);
	
		for (WaterConnection waterConnection : waterConnections) {
			WaterConnectionRequest waterConnectionRequest = WaterConnectionRequest.builder().waterConnection(waterConnection).requestInfo(notificationObj.getRequestInfo()).build();
			Property property = wSCalculationUtil.getProperty(WaterConnectionRequest.builder().waterConnection(waterConnection).requestInfo(notificationObj.getRequestInfo()).build());
//			User owner = property.getOwners().get(0).toCommonUser();
//			if (!CollectionUtils.isEmpty(waterConnection.getConnectionHolders())) {
//				owner = waterConnectionRequest.getWaterConnection().getConnectionHolders().get(0).toCommonUser();
//				System.out.println(owner.getMobileNumber());
//			//	mobileList.add(owner.getMobileNumber());
//			}
		
			Map<String, String> mobileNumbersAndNames = new HashMap<>();
			property.getOwners().forEach(owner -> {
				if (owner.getMobileNumber() != null)
					mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
			});
			//send the notification to the connection holders
			if (!CollectionUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionHolders())) {
				waterConnectionRequest.getWaterConnection().getConnectionHolders().forEach(holder -> {
					if (!StringUtils.isEmpty(holder.getMobileNumber())) {
						mobileNumbersAndNames.put(holder.getMobileNumber(), holder.getName());
					}
				});
			}
			Map<String, String> mobileNumberAndMessage = getMessageForMobileNumber(mobileNumbersAndNames, notificationObj, messageTemplate);			
			mobileNumberAndMessage.forEach((mobileNumber, msg) -> {
			SMSRequest req = SMSRequest.builder().mobileNumber(mobileNumber).message(msg).category(Category.TRANSACTION).build();
			smsRequest.add(req);
		});
		}
	//	enrichNotificationReceivers(receiverList, notificationObj);
		//receiverList.forEach(receiver -> {
	//		String message = util.getAppliedMsg(receiver, messageTemplate, notificationObj);
		//	SMSRequest sms = SMSRequest.builder().mobileNumber(receiver.getMobileNumber()).message(message).category(Category.TRANSACTION).build();
		//	smsRequest.add(sms);
	//	});
	}
	
	
	public Map<String, String> getMessageForMobileNumber(Map<String, String> mobileNumbersAndNames,
			DemandNotificationObj notificationObj, String message) {
		Map<String, String> messageToReturn = new HashMap<>();
		for (Entry<String, String> mobileAndName : mobileNumbersAndNames.entrySet()) {
			String messageToReplace = message;
			if (messageToReplace.contains("<Owner Name>"))
				messageToReplace = messageToReplace.replace("<Owner Name>", mobileAndName.getValue());
			if (messageToReplace.contains("<Service>"))
				messageToReplace = messageToReplace.replace("<Service>", WSCalculationConstant.SERVICE_FIELD_VALUE_WS);
			if (messageToReplace.contains("<ULB Name>"))
				messageToReplace = messageToReplace.replace("<ULB Name>",notificationObj.getTenantId());
			if (messageToReplace.contains("<<billing cycle>"))
				messageToReplace = messageToReplace.replace("<billing cycle>", notificationObj.getBillingCycle());
			messageToReturn.put(mobileAndName.getKey(), messageToReplace);
		}
		return messageToReturn;
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
			throw new CustomException("PARSING_ERROR", " Notification Receiver List Can Not Be Parsed!!");
		}
	}
	
	@SuppressWarnings("unused")
	private void enrichEmailRequest(DemandNotificationObj notificationObj, List<EmailRequest> emailRequest, String topic) {
		// Commenting out this method - As of now, egov-notification-mail service also reads from SMS Kafka Topic to
		// send out the email notification - Will remove the implementation if this change is permanent.

//		String tenantId = notificationObj.getTenantId();
//		String localizationMessage = util.getLocalizationMessages(tenantId, notificationObj.getRequestInfo());
//		String emailBody = util.getCustomizedMsg(topic, localizationMessage);
//		List<NotificationReceiver> receiverList = new ArrayList<>();
//		enrichNotificationReceivers(receiverList, notificationObj);
//		receiverList.forEach(receiver -> {
//			String message = util.getAppliedMsg(receiver, messageTemplate, notificationObj);
//			SMSRequest sms = new SMSRequest(receiver.getMobileNumber(), message);
//			smsRequest.add(sms);
//		});
	}

}
