package org.egov.swservice.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.model.Action;
import org.egov.swservice.model.ActionItem;
import org.egov.swservice.model.CalculationCriteria;
import org.egov.swservice.model.CalculationReq;
import org.egov.swservice.model.CalculationRes;
import org.egov.swservice.model.Event;
import org.egov.swservice.model.EventRequest;
import org.egov.swservice.model.Recepient;
import org.egov.swservice.model.SMSRequest;
import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.model.Source;
import org.egov.swservice.repository.ServiceRequestRepository;
import org.egov.swservice.util.NotificationUtil;
import org.egov.swservice.util.SWConstants;
import org.egov.swservice.util.SewerageServicesUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Service
@Slf4j
public class WorkflowNotificationService {

	@Autowired
	private SewerageServicesUtil sewerageServicesUtil;

	@Autowired
	private NotificationUtil notificationUtil;

	@Autowired
	private SWConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	String tenantIdReplacer = "$tenantId";
	String fileStoreIdsReplacer = "$.filestoreIds";
	String urlReplacer = "url";
	String requestInfoReplacer = "RequestInfo";
	String sewerageConnectionReplacer = "SewerageConnection";
	String fileStoreIdReplacer = "$fileStoreIds";
	String totalAmount = "totalAmount";
	String applicationFee = "applicationFee";
	String serviceFee = "serviceFee";
	String tax = "tax";
	String applicationNumberReplacer = "$applicationNumber";
	String consumerCodeReplacer = "$consumerCode";
	String connectionNoReplacer = "$connectionNumber";

	/**
	 * 
	 * @param record
	 *            record is bill response.
	 * @param topic
	 *            topic is bill generation topic for sewerage.
	 */
	public void process(SewerageConnectionRequest request, String topic) {
		try {
			if (!SWConstants.NOTIFICATION_ENABLE_FOR_STATUS.contains(
					request.getSewerageConnection().getProcessInstance().getAction() + "_" + request.getSewerageConnection().getApplicationStatus().name())) {
				log.info("Notification Disabled For State :" + request.getSewerageConnection().getApplicationStatus().name());
				return;
			}
			if (config.getIsUserEventsNotificationEnabled() != null && config.getIsUserEventsNotificationEnabled()) {
				EventRequest eventRequest = getEventRequest(request.getSewerageConnection(), topic, request.getRequestInfo());
				if (eventRequest != null) {
					log.info("In App Notification For WorkFlow :: -> " + mapper.writeValueAsString(eventRequest));
					notificationUtil.sendEventNotification(eventRequest);
				}
			}
			if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {
				List<SMSRequest> smsRequests = getSmsRequest(request.getSewerageConnection(), topic, request.getRequestInfo());
				if (!CollectionUtils.isEmpty(smsRequests)) {
					log.info("SMS Notification For WorkFlow:: -> " + mapper.writeValueAsString(smsRequests));
					notificationUtil.sendSMS(smsRequests);
				}
			}

		} catch (Exception ex) {
			log.error("Error occured while processing the record from topic : " + topic, ex);
		}
	}

	/**
	 * 
	 * @param sewerageConnection
	 * @param topic
	 * @param requestInfo
	 * @return EventRequest Object
	 */
	private EventRequest getEventRequest(SewerageConnection sewerageConnection, String topic, RequestInfo requestInfo) {
		String localizationMessage = notificationUtil
				.getLocalizationMessages(sewerageConnection.getProperty().getTenantId(), requestInfo);
		String message = notificationUtil.getCustomizedMsgForInApp(sewerageConnection.getProcessInstance().getAction(),
				sewerageConnection.getApplicationStatus().name(), localizationMessage);
		if (message == null) {
			log.info("No message Found For Topic : " + topic);
			return null;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		sewerageConnection.getProperty().getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames,
				sewerageConnection, message,requestInfo);
		Set<String> mobileNumbers = mobileNumberAndMesssage.keySet().stream().collect(Collectors.toSet());
		Map<String, String> mapOfPhnoAndUUIDs = fetchUserUUIDs(mobileNumbers, requestInfo,
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
			// List<String> payTriggerList =
			// Arrays.asList(config.getPayTriggers().split("[,]"));

			Action action = getActionForEventNotification(mobileNumberAndMesssage, mobile, sewerageConnection,requestInfo);
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

	/**
	 * 
	 * @param messageTemplate
	 * @param connection
	 * @return return action link
	 */
	public Action getActionForEventNotification(Map<String, String> mobileNumberAndMesssage, String mobileNumber,
			SewerageConnection connection, RequestInfo requestInfo) {
		String code = "";
		String messageTemplate = mobileNumberAndMesssage.get(mobileNumber);
		if (messageTemplate.contains("<Action Button>")) {
			code = StringUtils.substringBetween(messageTemplate, "<Action Button>", "</Action Button>");
			messageTemplate = messageTemplate.replace("<Action Button>", "");
			messageTemplate = messageTemplate.replace("</Action Button>", "");
			messageTemplate = messageTemplate.replace(code, "");
			List<ActionItem> items = new ArrayList<>();
			String actionLink = "";
			if (code.equalsIgnoreCase("Download Application")) {
				actionLink = sewerageServicesUtil.getShortnerURL(getApplicationDownloaderLink(connection, requestInfo));
			}
			if (code.equalsIgnoreCase("PAY NOW")) {
				String paymentLink = config.getNotificationUrl() + config.getApplicationPayLink();
				paymentLink = paymentLink.replace(consumerCodeReplacer, connection.getApplicationNo());
				paymentLink = paymentLink.replace(tenantIdReplacer, connection.getProperty().getTenantId());
				actionLink = sewerageServicesUtil.getShortnerURL(paymentLink);
			}
			if (code.equalsIgnoreCase("DOWNLOAD RECEIPT")) {
				actionLink = config.getNotificationUrl();
			}
			ActionItem item = ActionItem.builder().actionUrl(actionLink).code(code).build();
			items.add(item);
			mobileNumberAndMesssage.replace(mobileNumber, messageTemplate);
			return Action.builder().actionUrls(items).build();
		}
		// actionLinkAndMsg.put("Action", action);
		// actionLinkAndMsg.put(key, value);
		return null;
	}

	/**
	 * 
	 * @param mappedRecord
	 * @param sewerageConnection
	 * @param topic
	 * @param requestInfo
	 * @return
	 */
	private List<SMSRequest> getSmsRequest(SewerageConnection sewerageConnection, String topic,
			RequestInfo requestInfo) {
		String localizationMessage = notificationUtil
				.getLocalizationMessages(sewerageConnection.getProperty().getTenantId(), requestInfo);
		String message = notificationUtil.getCustomizedMsgForSMS(sewerageConnection.getProcessInstance().getAction(),
				sewerageConnection.getApplicationStatus().name(), localizationMessage);
		if (message == null) {
			log.info("No message Found For Topic : " + topic);
			return null;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		sewerageConnection.getProperty().getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		List<SMSRequest> smsRequest = new ArrayList<>();
		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames,
				sewerageConnection, message,requestInfo);
		mobileNumberAndMesssage.forEach((mobileNumber, messg) -> {
			SMSRequest req = new SMSRequest(mobileNumber, messg);
			smsRequest.add(req);
		});
		return smsRequest;
	}

	public Map<String, String> getMessageForMobileNumber(Map<String, String> mobileNumbersAndNames,
			SewerageConnection sewerageConnection, String message,RequestInfo requestInfo) {
		Map<String, String> messagetoreturn = new HashMap<>();
		String messageToreplace = message;
		for (Entry<String, String> mobileAndName : mobileNumbersAndNames.entrySet()) {
			messageToreplace = message;
			if (messageToreplace.contains("<Owner Name>"))
				messageToreplace = messageToreplace.replace("<Owner Name>", mobileAndName.getValue());
			if (messageToreplace.contains("<Service>"))
				messageToreplace = messageToreplace.replace("<Service>", SWConstants.SERVICE_FIELD_VALUE_SW);

			if (messageToreplace.contains("<Application number>"))
				messageToreplace = messageToreplace.replace("<Application number>",
						sewerageConnection.getApplicationNo());

			if (messageToreplace.contains("<Application download link>"))
				messageToreplace = messageToreplace.replace("<Application download link>",
						sewerageServicesUtil.getShortnerURL(getApplicationDownloaderLink(sewerageConnection, requestInfo)));

			if (messageToreplace.contains("<mseva URL>"))
				messageToreplace = messageToreplace.replace("<mseva URL>",
						sewerageServicesUtil.getShortnerURL(config.getNotificationUrl()));

			if (messageToreplace.contains("<mseva app link>"))
				messageToreplace = messageToreplace.replace("<mseva app link>",
						sewerageServicesUtil.getShortnerURL(config.getMSevaAppLink()));

			if (messageToreplace.contains("<View History Link>")) {
				String historyLink = config.getNotificationUrl() + config.getViewHistoryLink();
				historyLink = historyLink.replace(applicationNumberReplacer, sewerageConnection.getApplicationNo());
				historyLink = historyLink.replace(tenantIdReplacer, sewerageConnection.getProperty().getTenantId());
				messageToreplace = messageToreplace.replace("<View History Link>",
						sewerageServicesUtil.getShortnerURL(historyLink));
			}
			if (messageToreplace.contains("<payment link>")) {
				String paymentLink = config.getNotificationUrl() + config.getApplicationPayLink();
				paymentLink = paymentLink.replace(consumerCodeReplacer, sewerageConnection.getApplicationNo());
				paymentLink = paymentLink.replace(tenantIdReplacer, sewerageConnection.getProperty().getTenantId());
				messageToreplace = messageToreplace.replace("<payment link>",
						sewerageServicesUtil.getShortnerURL(paymentLink));
			}
			if (messageToreplace.contains("<receipt download link>"))
				messageToreplace = messageToreplace.replace("<receipt download link>",
						sewerageServicesUtil.getShortnerURL(config.getNotificationUrl()));


			if (messageToreplace.contains("<connection details page>")) {
				String connectionDetaislLink = config.getNotificationUrl() + config.getConnectionDetailsLink();
				connectionDetaislLink = connectionDetaislLink.replace(connectionNoReplacer,
						sewerageConnection.getConnectionNo());
				connectionDetaislLink = connectionDetaislLink.replace(tenantIdReplacer,
						sewerageConnection.getProperty().getTenantId());
				messageToreplace = messageToreplace.replace("<connection details page>",
						sewerageServicesUtil.getShortnerURL(connectionDetaislLink));
			}
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
	
	
	   /**
     * Fetch URL for application download link
     * 
     * @param waterConnection
     * @param requestInfo
     * @return application download link
     */
	private String getApplicationDownloaderLink(SewerageConnection sewerageConnection, RequestInfo requestInfo) {
		CalculationCriteria criteria = CalculationCriteria.builder().applicationNo(sewerageConnection.getApplicationNo())
				.sewerageConnection(sewerageConnection).tenantId(sewerageConnection.getProperty().getTenantId()).build();
		CalculationReq calRequest = CalculationReq.builder().calculationCriteria(Arrays.asList(criteria))
				.requestInfo(requestInfo).isconnectionCalculation(false).build();
		try {
			Object response = serviceRequestRepository.fetchResult(sewerageServicesUtil.getEstimationURL(), calRequest);
			CalculationRes calResponse = mapper.convertValue(response, CalculationRes.class);
			JSONObject sewerageobject = mapper.convertValue(sewerageConnection, JSONObject.class);
			if (CollectionUtils.isEmpty(calResponse.getCalculation())) {
				throw new CustomException("NO_ESTIMATION_FOUND", "Estimation not found!!!");
			}
			sewerageobject.put(totalAmount, calResponse.getCalculation().get(0).getTotalAmount());
			sewerageobject.put(applicationFee, calResponse.getCalculation().get(0).getFee());
			sewerageobject.put(serviceFee, calResponse.getCalculation().get(0).getCharge());
			sewerageobject.put(tax, calResponse.getCalculation().get(0).getTaxAmount());
			String tenantId = sewerageConnection.getProperty().getTenantId().split("\\.")[0];
			String fileStoreId = getFielStoreIdFromPDFService(sewerageobject, requestInfo, tenantId);
			return getApplicationDownloadLink(tenantId, fileStoreId);
		} catch (Exception ex) {
			log.error("Calculation response error!!", ex);
			throw new CustomException("WATER_CALCULATION_EXCEPTION", "Calculation response can not parsed!!!");
		}
	}
	/**
	 * Get file store id from PDF service
	 * 
	 * @param sewerageobject
	 * @param requestInfo
	 * @param tenantId
	 * @return file store id
	 */
	private String getFielStoreIdFromPDFService(JSONObject sewerageobject, RequestInfo requestInfo, String tenantId) {
		JSONArray sewerageconnectionlist = new JSONArray();
		sewerageconnectionlist.add(sewerageobject);
		JSONObject requestPayload = new JSONObject();
		requestPayload.put(requestInfoReplacer, requestInfo);
		requestPayload.put(sewerageConnectionReplacer, sewerageconnectionlist);
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(config.getPdfServiceHost());
			String pdfLink = config.getPdfServiceLink();
			pdfLink = pdfLink.replace(tenantIdReplacer, tenantId);
			builder.append(pdfLink);
			Object response = serviceRequestRepository.fetchResult(builder, requestPayload);
			DocumentContext responseContext = JsonPath.parse(response);
			List<Object> fileStoreIds = responseContext.read("$.filestoreIds");
			if(CollectionUtils.isEmpty(fileStoreIds)) {
				throw new CustomException("EMPTY_FILESTORE_IDS_FROM_PDF_SERVICE", "NO file store id found from pdf service");
			}
			return fileStoreIds.get(0).toString();
		} catch (Exception ex) {
			log.error("PDF file store id response error!!", ex);
			throw new CustomException("SEWERAGE_FILESTORE_PDF_EXCEPTION", "PDF response can not parsed!!!");
		}
	}
	/**
	 * 
	 * @param tenantId
	 * @param fileStoreId
	 * @return file store id
	 */
	private String getApplicationDownloadLink(String tenantId, String fileStoreId) {
		String fileStoreServiceLink = config.getFileStoreHost() + config.getFileStoreLink();
		fileStoreServiceLink = fileStoreServiceLink.replace(tenantIdReplacer, tenantId);
		fileStoreServiceLink = fileStoreServiceLink.replace(fileStoreIdReplacer, fileStoreId);
		try {
			Object response = serviceRequestRepository.fetchResultUsingGet(new StringBuilder(fileStoreServiceLink));
			DocumentContext responseContext = JsonPath.parse(response);
			List<Object> fileStoreIds = responseContext.read("$.fileStoreIds");
			if (CollectionUtils.isEmpty(fileStoreIds)) {
				throw new CustomException("EMPTY_FILESTORE_IDS_FROM_PDF_SERVICE",
						"NO file store id found from pdf service");
			}
			JSONObject obje = mapper.convertValue(fileStoreIds.get(0), JSONObject.class);
			return obje.get(urlReplacer).toString();
		} catch (Exception ex) {
			log.error("PDF file store id response error!!", ex);
			throw new CustomException("SEWERAGE_FILESTORE_PDF_EXCEPTION", "PDF response can not parsed!!!");
		}
	}

}
