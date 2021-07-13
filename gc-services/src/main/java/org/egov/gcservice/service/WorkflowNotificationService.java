package org.egov.gcservice.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.gcservice.config.GCConfiguration;
import org.egov.gcservice.repository.ServiceRequestRepository;
import org.egov.gcservice.util.NotificationUtil;
import org.egov.gcservice.util.GCConstants;
import org.egov.gcservice.util.GarbageServicesUtil;
import org.egov.gcservice.validator.ValidateProperty;
import org.egov.gcservice.web.models.*;
import org.egov.gcservice.web.models.collection.PaymentResponse;
import org.egov.gcservice.web.models.workflow.BusinessService;
import org.egov.gcservice.web.models.workflow.State;
import org.egov.gcservice.workflow.WorkflowService;
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
	private GarbageServicesUtil garbageServicesUtil;

	@Autowired
	private NotificationUtil notificationUtil;

	@Autowired
	private GCConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private ValidateProperty validateProperty;

	String tenantIdReplacer = "$tenantId";
	String urlReplacer = "url";
	String requestInfoReplacer = "RequestInfo";
	String GarbageConnectionReplacer = "GarbageConnection";
	String fileStoreIdReplacer = "$fileStoreIds";
	String totalAmount = "totalAmount";
	String applicationFee = "applicationFee";
	String serviceFee = "serviceFee";
	String tax = "tax";
	String applicationNumberReplacer = "$applicationNumber";
	String consumerCodeReplacer = "$consumerCode";
	String connectionNoReplacer = "$connectionNumber";
	String mobileNoReplacer = "$mobileNo";
	String applicationKey = "$applicationkey";
	String propertyKey = "property";
	String businessService = "SW.ONE_TIME_FEE";

	/**
	 * 
	 * @param request - Sewerage GarbageConnection Request Object
	 * @param topic - Received Topic Name
	 */
	public void process(GarbageConnectionRequest request, String topic) {
		try {
			String applicationStatus = request.getGarbageConnection().getStatus();
			if (!GCConstants.NOTIFICATION_ENABLE_FOR_STATUS
					.contains(request.getGarbageConnection().getProcessInstance().getAction() + "_"
							+ applicationStatus)) {
				log.info("Notification Disabled For State :"
						+ applicationStatus);
				return;
			}
			Property property = validateProperty.getOrValidateProperty(request);
			
			if (config.getIsUserEventsNotificationEnabled() != null && config.getIsUserEventsNotificationEnabled()) {
				EventRequest eventRequest = getEventRequest(request, topic, property, applicationStatus);
				if (eventRequest != null) {
					notificationUtil.sendEventNotification(eventRequest);
				}
			}
			if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {
				List<SMSRequest> smsRequests = getSmsRequest(request, topic, property, applicationStatus);
				if (!CollectionUtils.isEmpty(smsRequests)) {
					notificationUtil.sendSMS(smsRequests);
				}
			}

		} catch (Exception ex) {
			log.error("Error occured while processing the record from topic : " + topic, ex);
		}
	}

	/**
	 * 
	 * @param garbageConnectionRequest - Sewerage GarbageConnection Request
	 * @param topic - Topic Name
	 * @param property - Property Object
	 * @param applicationStatus - ApplicationStatus
	 * @return EventRequest Object
	 */
	private EventRequest getEventRequest(GarbageConnectionRequest garbageConnectionRequest, String topic, Property property, String applicationStatus) {
		String localizationMessage = notificationUtil
				.getLocalizationMessages(property.getTenantId(), garbageConnectionRequest.getRequestInfo());
		int reqType = GCConstants.UPDATE_APPLICATION;
		if ((!garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction().equalsIgnoreCase(GCConstants.ACTIVATE_CONNECTION))
				&& garbageServicesUtil.isModifyConnectionRequest(garbageConnectionRequest)) {
			reqType = GCConstants.MODIFY_CONNECTION;
		}
		
		String message = notificationUtil.getCustomizedMsgForInApp(
				garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction(), applicationStatus,
				localizationMessage, reqType);
		if (message == null) {
			log.info("No message Found For Topic : " + topic);
			return null;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		property.getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		//send the notification to the connection holders
		if(!CollectionUtils.isEmpty(garbageConnectionRequest.getGarbageConnection().getConnectionHolders())) {
			garbageConnectionRequest.getGarbageConnection().getConnectionHolders().forEach(holder -> {
				if (!StringUtils.isEmpty(holder.getMobileNumber())) {
					mobileNumbersAndNames.put(holder.getMobileNumber(), holder.getName());
				}
			});
		}
		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames,
				garbageConnectionRequest, message, property);
		if (message.contains("<receipt download link>"))
			mobileNumberAndMesssage = setRecepitDownloadLink(mobileNumberAndMesssage, garbageConnectionRequest, message, property);
		Set<String> mobileNumbers = new HashSet<>(mobileNumberAndMesssage.keySet());
		Map<String, String> mapOfPhoneNoAndUUIDs = fetchUserUUIDs(mobileNumbers, garbageConnectionRequest.getRequestInfo(),
				property.getTenantId());

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
			// List<String> payTriggerList =
			// Arrays.asList(config.getPayTriggers().split("[,]"));

			Action action = getActionForEventNotification(mobileNumberAndMesssage, mobile, garbageConnectionRequest,
					property);
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

	/**
	 * 
	 * @param mobileNumberAndMessage - List of Mobile and it's messages
	 * @param mobileNumber - MobileNumber
	 * @param garbageConnectionRequest - GarbageConnection Request Object
	 * @param property Property Object
	 * @return return action link
	 */
	public Action getActionForEventNotification(Map<String, String> mobileNumberAndMessage, String mobileNumber,
			GarbageConnectionRequest garbageConnectionRequest, Property property) {
		String messageTemplate = mobileNumberAndMessage.get(mobileNumber);
		List<ActionItem> items = new ArrayList<>();
		if (messageTemplate.contains("<Action Button>")) {
			String code = StringUtils.substringBetween(messageTemplate, "<Action Button>", "</Action Button>");
			messageTemplate = messageTemplate.replace("<Action Button>", "");
			messageTemplate = messageTemplate.replace("</Action Button>", "");
			messageTemplate = messageTemplate.replace(code, "");
			String actionLink = "";
			if (code.equalsIgnoreCase("Download Application")) {
				actionLink = config.getNotificationUrl() + config.getViewHistoryLink();
				actionLink = actionLink.replace(mobileNoReplacer, mobileNumber);
				actionLink = actionLink.replace(applicationNumberReplacer, garbageConnectionRequest.getGarbageConnection().getApplicationNo());
				actionLink = actionLink.replace(tenantIdReplacer, property.getTenantId());
			}
			if (code.equalsIgnoreCase("PAY NOW")) {
				actionLink = config.getNotificationUrl() + config.getUserEventApplicationPayLink();
				actionLink = actionLink.replace(mobileNoReplacer, mobileNumber);
				actionLink = actionLink.replace(consumerCodeReplacer, garbageConnectionRequest.getGarbageConnection().getApplicationNo());
				actionLink = actionLink.replace(tenantIdReplacer, property.getTenantId());
			}
			if (code.equalsIgnoreCase("DOWNLOAD RECEIPT")) {
				String receiptNumber = getReceiptNumber(garbageConnectionRequest);
				String consumerCode,service;
				if(StringUtils.isEmpty(garbageConnectionRequest.getGarbageConnection().getConnectionNo())){
					consumerCode = garbageConnectionRequest.getGarbageConnection().getApplicationNo();
					service = businessService;
				}
				else{
					consumerCode = garbageConnectionRequest.getGarbageConnection().getConnectionNo();
					service = "SW";
				}
				actionLink = config.getNotificationUrl() + config.getUserEventReceiptDownloadLink();
				actionLink = actionLink.replace("$consumerCode", consumerCode);
				actionLink = actionLink.replace("$tenantId", property.getTenantId());
				actionLink = actionLink.replace("$businessService", service);
				actionLink = actionLink.replace("$receiptNumber", receiptNumber);
				actionLink = actionLink.replace("$mobile", mobileNumber);
			}
			if (code.equalsIgnoreCase("View History Link")) {
				actionLink = config.getNotificationUrl() + config.getViewHistoryLink();
				actionLink = actionLink.replace(mobileNoReplacer, mobileNumber);
				actionLink = actionLink.replace(applicationNumberReplacer,
						garbageConnectionRequest.getGarbageConnection().getApplicationNo());
				actionLink = actionLink.replace(tenantIdReplacer, property.getTenantId());
			}
			if (code.equalsIgnoreCase("GarbageConnection Detail Page")) {
				actionLink = config.getNotificationUrl() + config.getConnectionDetailsLink();
				actionLink = actionLink.replace(connectionNoReplacer, garbageConnectionRequest.getGarbageConnection().getConnectionNo());
				actionLink = actionLink.replace(tenantIdReplacer, property.getTenantId());
				actionLink = actionLink.replace(mobileNoReplacer, mobileNumber);
			}
			ActionItem item = ActionItem.builder().actionUrl(actionLink).code(code).build();
			items.add(item);
			mobileNumberAndMessage.replace(mobileNumber, messageTemplate);
		}
		return Action.builder().actionUrls(items).build();

	}

	/**
	 * 
	 * @param garbageConnectionRequest - Sewerage GarbageConnection Request
	 * @param topic - Topic Name
	 * @param property - Property Object
	 * @param applicationStatus - Application Status
	 * @return - Returns the list of SMSRequest Object
	 */
	private List<SMSRequest> getSmsRequest(GarbageConnectionRequest garbageConnectionRequest, String topic, Property property, String applicationStatus) {
		String localizationMessage = notificationUtil
				.getLocalizationMessages(property.getTenantId(), garbageConnectionRequest.getRequestInfo());
		int reqType = GCConstants.UPDATE_APPLICATION;
		if ((!garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction().equalsIgnoreCase(GCConstants.ACTIVATE_CONNECTION))
				&& garbageServicesUtil.isModifyConnectionRequest(garbageConnectionRequest)) {
			reqType = GCConstants.MODIFY_CONNECTION;
		}
		String message = notificationUtil.getCustomizedMsgForSMS(
				garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction(), applicationStatus,
				localizationMessage, reqType);
		if (message == null) {
			log.info("No message Found For Topic : " + topic);
			return Collections.emptyList();
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		property.getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		//send the notification to the connection holders
		if(!CollectionUtils.isEmpty(garbageConnectionRequest.getGarbageConnection().getConnectionHolders())) {
			garbageConnectionRequest.getGarbageConnection().getConnectionHolders().forEach(holder -> {
				if (!StringUtils.isEmpty(holder.getMobileNumber())) {
					mobileNumbersAndNames.put(holder.getMobileNumber(), holder.getName());
				}
			});
		}
		List<SMSRequest> smsRequest = new ArrayList<>();
		Map<String, String> mobileNumberAndMessage = getMessageForMobileNumber(mobileNumbersAndNames,
				garbageConnectionRequest, message, property);
		if (message.contains("<receipt download link>"))
			mobileNumberAndMessage = setRecepitDownloadLink(mobileNumberAndMessage, garbageConnectionRequest, message, property);
		mobileNumberAndMessage.forEach((mobileNumber, msg) -> {
			SMSRequest req = SMSRequest.builder().mobileNumber(mobileNumber).message(msg).category(Category.TRANSACTION).build();
			smsRequest.add(req);
		});
		return smsRequest;
	}

	public Map<String, String> getMessageForMobileNumber(Map<String, String> mobileNumbersAndNames,
			GarbageConnectionRequest garbageConnectionRequest, String message, Property property) {
		Map<String, String> messageToReturn = new HashMap<>();
		for (Entry<String, String> mobileAndName : mobileNumbersAndNames.entrySet()) {
			String messageToReplace = message;
			if (messageToReplace.contains("<Owner Name>"))
				messageToReplace = messageToReplace.replace("<Owner Name>", mobileAndName.getValue());
			if (messageToReplace.contains("<Service>"))
				messageToReplace = messageToReplace.replace("<Service>", GCConstants.SERVICE_FIELD_VALUE_NOTIFICATION);

			if (messageToReplace.contains("<Application number>"))
				messageToReplace = messageToReplace.replace("<Application number>",
						garbageConnectionRequest.getGarbageConnection().getApplicationNo());

			if (messageToReplace.contains("<Application download link>"))
				messageToReplace = messageToReplace.replace("<Application download link>",
						garbageServicesUtil.getShortenedURL(
								getApplicationDownloaderLink(garbageConnectionRequest, property)));

			if (messageToReplace.contains("<mseva URL>"))
				messageToReplace = messageToReplace.replace("<mseva URL>",
						garbageServicesUtil.getShortenedURL(config.getNotificationUrl()));
			
			if (messageToReplace.contains("<Plumber Info>"))
				messageToReplace = getMessageForPlumberInfo(garbageConnectionRequest.getGarbageConnection(), messageToReplace);
			
			if (messageToReplace.contains("<SLA>"))
				messageToReplace = messageToReplace.replace("<SLA>", getSLAForState(
						garbageConnectionRequest, property));

			if (messageToReplace.contains("<mseva app link>"))
				messageToReplace = messageToReplace.replace("<mseva app link>",
						garbageServicesUtil.getShortenedURL(config.getMSevaAppLink()));

			if (messageToReplace.contains("<View History Link>")) {
				String historyLink = config.getNotificationUrl() + config.getViewHistoryLink();
				historyLink = historyLink.replace(mobileNoReplacer, mobileAndName.getKey());
				historyLink = historyLink.replace(applicationNumberReplacer,
						garbageConnectionRequest.getGarbageConnection().getApplicationNo());
				historyLink = historyLink.replace(tenantIdReplacer, property.getTenantId());
				messageToReplace = messageToReplace.replace("<View History Link>",
						garbageServicesUtil.getShortenedURL(historyLink));
			}
			if (messageToReplace.contains("<payment link>")) {
				String paymentLink = config.getNotificationUrl() + config.getApplicationPayLink();
				paymentLink = paymentLink.replace(mobileNoReplacer, mobileAndName.getKey());
				paymentLink = paymentLink.replace(consumerCodeReplacer,
						garbageConnectionRequest.getGarbageConnection().getApplicationNo());
				paymentLink = paymentLink.replace(tenantIdReplacer, property.getTenantId());
				messageToReplace = messageToReplace.replace("<payment link>",
						garbageServicesUtil.getShortenedURL(paymentLink));
			}
			/*if (messageToReplace.contains("<receipt download link>"))
				messageToReplace = messageToReplace.replace("<receipt download link>",
						garbageServicesUtil.getShortenedURL(config.getNotificationUrl()));*/

			if (messageToReplace.contains("<connection details page>")) {
				String connectionDetaislLink = config.getNotificationUrl() + config.getConnectionDetailsLink();
				connectionDetaislLink = connectionDetaislLink.replace(connectionNoReplacer,
						garbageConnectionRequest.getGarbageConnection().getConnectionNo());
				connectionDetaislLink = connectionDetaislLink.replace(tenantIdReplacer, property.getTenantId());
				messageToReplace = messageToReplace.replace("<connection details page>",
						garbageServicesUtil.getShortenedURL(connectionDetaislLink));
			}
			if(messageToReplace.contains("<Date effective from>")) {
				if (garbageConnectionRequest.getGarbageConnection().getEffectiveFrom() != null) {
					LocalDate date = Instant
							.ofEpochMilli(garbageConnectionRequest.getGarbageConnection().getEffectiveFrom() > 10 ?
									garbageConnectionRequest.getGarbageConnection().getEffectiveFrom() :
									garbageConnectionRequest.getGarbageConnection().getEffectiveFrom() * 1000)
							.atZone(ZoneId.systemDefault()).toLocalDate();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
					messageToReplace = messageToReplace.replace("<Date effective from>", date.format(formatter));
				} else {
					messageToReplace = messageToReplace.replace("<Date effective from>", "");
				}
			}
			messageToReturn.put(mobileAndName.getKey(), messageToReplace);
		}
		return messageToReturn;
	}
	
	/**
	 * This method returns message to replace for plumber info depending upon
	 * whether the plumber info type is either SELF or ULB
	 * 
	 * @param GarbageConnection - Sewerage GarbageConnection Request Object
	 * @param messageTemplate - Message Template
	 * @return updated messageTemplate
	 */

	@SuppressWarnings("unchecked")
	public String getMessageForPlumberInfo(GarbageConnection GarbageConnection, String messageTemplate) {
//		HashMap<String, Object> addDetail = mapper.convertValue(GarbageConnection.getAdditionalDetails(),
//				HashMap.class);
//		if (!StringUtils.isEmpty(String.valueOf(addDetail.get(GCConstants.DETAILS_PROVIDED_BY)))) {
//			String detailsProvidedBy = String.valueOf(addDetail.get(GCConstants.DETAILS_PROVIDED_BY));
//			if (StringUtils.isEmpty(detailsProvidedBy) || detailsProvidedBy.equalsIgnoreCase(GCConstants.SELF)) {
//				String code = StringUtils.substringBetween(messageTemplate, "<Plumber Info>", "</Plumber Info>");
//				messageTemplate = messageTemplate.replace("<Plumber Info>", "");
//				messageTemplate = messageTemplate.replace("</Plumber Info>", "");
//				messageTemplate = messageTemplate.replace(code, "");
//			} else {
//				messageTemplate = messageTemplate.replace("<Plumber Info>", "").replace("</Plumber Info>", "");
//				messageTemplate = messageTemplate.replace("<Plumber name>",
//						StringUtils.isEmpty(GarbageConnection.getPlumberInfo().get(0).getName()) ? ""
//								: GarbageConnection.getPlumberInfo().get(0).getName());
//				messageTemplate = messageTemplate.replace("<Plumber Licence No.>",
//						StringUtils.isEmpty(GarbageConnection.getPlumberInfo().get(0).getLicenseNo()) ? ""
//								: GarbageConnection.getPlumberInfo().get(0).getLicenseNo());
//				messageTemplate = messageTemplate.replace("<Plumber Mobile No.>",
//						StringUtils.isEmpty(GarbageConnection.getPlumberInfo().get(0).getMobileNumber()) ? ""
//								: GarbageConnection.getPlumberInfo().get(0).getMobileNumber());
//			}
//
//		}else{
//			String code = StringUtils.substringBetween(messageTemplate, "<Plumber Info>", "</Plumber Info>");
//			messageTemplate = messageTemplate.replace("<Plumber Info>", "");
//			messageTemplate = messageTemplate.replace("</Plumber Info>", "");
//			messageTemplate = messageTemplate.replace(code, "");
//		}

		return messageTemplate;

	}

	/**
	 * Fetches SLA of CITIZEN based on the phone number.
	 * 
	 * @param garbageConnectionRequest - Sewerage GarbageConnection Request Object
	 * @param property - Property Object
	 * @return string consisting SLA
	 */

	public String getSLAForState(GarbageConnectionRequest garbageConnectionRequest, Property property) {
		String resultSla = "";
		BusinessService businessService = workflowService
				.getBusinessService(config.getBusinessServiceValue(), property.getTenantId(), garbageConnectionRequest.getRequestInfo());
		if (businessService != null && businessService.getStates() != null && businessService.getStates().size() > 0) {
			for (State state : businessService.getStates()) {
				if (GCConstants.PENDING_FOR_CONNECTION_ACTIVATION.equalsIgnoreCase(state.getState())) {
					resultSla = String.valueOf(
							(state.getSla() == null ? 0L : state.getSla()) / 86400000);
				}
			}
		}
		return resultSla;
	}

	/**
	 * Fetches UUIDs of CITIZEN based on the phone number.
	 * 
	 * @param mobileNumbers - List of Mobile Numbers
	 * @param requestInfo - Request Info Object
	 * @param tenantId - TenantId
	 * @return - Returns list of MobileNumbers and UUIDs
	 */
	public Map<String, String> fetchUserUUIDs(Set<String> mobileNumbers, RequestInfo requestInfo, String tenantId) {
		Map<String, String> mapOfPhoneNoAndUUIDs = new HashMap<>();
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
					mapOfPhoneNoAndUUIDs.put(mobileNo, uuid);
				} else {
					log.error("Service returned null while fetching user ");
				}
			} catch (Exception e) {
				log.error("Exception trace: ", e);
			}
		}
		return mapOfPhoneNoAndUUIDs;
	}

	/**
	 * Fetch URL for application download link
	 * 
	 * @param garbageConnectionRequest - Sewerage GarbageConnection Request Object
	 * @param property - Property Object
	 * @return application download link
	 */
	private String getApplicationDownloaderLink(GarbageConnectionRequest garbageConnectionRequest,
			Property property) {
		CalculationCriteria criteria = CalculationCriteria.builder()
				.applicationNo(garbageConnectionRequest.getGarbageConnection().getApplicationNo())
				.GarbageConnection(garbageConnectionRequest.getGarbageConnection()).tenantId(property.getTenantId())
				.build();
		CalculationReq calRequest = CalculationReq.builder().calculationCriteria(Arrays.asList(criteria))
				.requestInfo(garbageConnectionRequest.getRequestInfo()).isconnectionCalculation(false).build();
		try {
			Object response = serviceRequestRepository.fetchResult(garbageServicesUtil.getEstimationURL(), calRequest);
			CalculationRes calResponse = mapper.convertValue(response, CalculationRes.class);
			JSONObject sewerageObject = mapper.convertValue(garbageConnectionRequest.getGarbageConnection(),
					JSONObject.class);
			if (CollectionUtils.isEmpty(calResponse.getCalculation())) {
				throw new CustomException("NO_ESTIMATION_FOUND", "Estimation not found!!!");
			}
			sewerageObject.put(totalAmount, calResponse.getCalculation().get(0).getTotalAmount());
			sewerageObject.put(applicationFee, calResponse.getCalculation().get(0).getFee());
			sewerageObject.put(serviceFee, calResponse.getCalculation().get(0).getCharge());
			sewerageObject.put(tax, calResponse.getCalculation().get(0).getTaxAmount());
			sewerageObject.put(propertyKey, property);
			String tenantId = property.getTenantId().split("\\.")[0];
			String fileStoreId = getFielStoreIdFromPDFService(sewerageObject,
					garbageConnectionRequest.getRequestInfo(), tenantId);
			return getApplicationDownloadLink(tenantId, fileStoreId);
		} catch (Exception ex) {
			log.error("Calculation response error!!", ex);
			throw new CustomException("WATER_CALCULATION_EXCEPTION", "Calculation response can not parsed!!!");
		}
	}

	/**
	 * Get file store id from PDF service
	 * 
	 * @param sewerageObject - Sewerage GarbageConnection JSON Object
	 * @param requestInfo - Request Info
	 * @param tenantId - Tenant Id
	 * @return file store id
	 */
	private String getFielStoreIdFromPDFService(JSONObject sewerageObject, RequestInfo requestInfo, String tenantId) {
		JSONArray GarbageConnectionlist = new JSONArray();
		GarbageConnectionlist.add(sewerageObject);
		JSONObject requestPayload = new JSONObject();
		requestPayload.put(requestInfoReplacer, requestInfo);
		requestPayload.put(GarbageConnectionReplacer, GarbageConnectionlist);
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(config.getPdfServiceHost());
			String pdfLink = config.getPdfServiceLink();
			pdfLink = pdfLink.replace(tenantIdReplacer, tenantId).replace(applicationKey, GCConstants.PDF_APPLICATION_KEY);
			builder.append(pdfLink);
			Object response = serviceRequestRepository.fetchResult(builder, requestPayload);
			DocumentContext responseContext = JsonPath.parse(response);
			List<Object> fileStoreIds = responseContext.read("$.filestoreIds");
			if (CollectionUtils.isEmpty(fileStoreIds)) {
				throw new CustomException("EMPTY_FILESTORE_IDS_FROM_PDF_SERVICE",
						"No file store id found from pdf service");
			}
			return fileStoreIds.get(0).toString();
		} catch (Exception ex) {
			log.error("PDF file store id response error!!", ex);
			throw new CustomException("SEWERAGE_FILESTORE_PDF_EXCEPTION", "PDF response can not parsed!!!");
		}
	}

	/**
	 * 
	 * @param tenantId - TenantId
	 * @param fileStoreId - File Store Id
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
						"No file store id found from pdf service");
			}
			JSONObject object = mapper.convertValue(fileStoreIds.get(0), JSONObject.class);
			return object.get(urlReplacer).toString();
		} catch (Exception ex) {
			log.error("PDF file store id response error!!", ex);
			throw new CustomException("SEWERAGE_FILESTORE_PDF_EXCEPTION", "PDF response can not parsed!!!");
		}
	}

	public Map<String, String> setRecepitDownloadLink(Map<String, String> mobileNumberAndMesssage,
													  GarbageConnectionRequest garbageConnectionRequest, String message, Property property) {

			Map<String, String> messageToReturn = new HashMap<>();
			String receiptNumber = getReceiptNumber(garbageConnectionRequest);
			for (Entry<String, String> mobileAndMsg : mobileNumberAndMesssage.entrySet()) {
				String messageToReplace = mobileAndMsg.getValue();
				String consumerCode,service;
				if(StringUtils.isEmpty(garbageConnectionRequest.getGarbageConnection().getConnectionNo())){
					consumerCode = garbageConnectionRequest.getGarbageConnection().getApplicationNo();
					service = businessService;
				}
				else{
					consumerCode = garbageConnectionRequest.getGarbageConnection().getConnectionNo();
					service = "SW";
				}
				String link = config.getNotificationUrl() + config.getReceiptDownloadLink();
				link = link.replace("$consumerCode", consumerCode);
				link = link.replace("$tenantId", property.getTenantId());
				link = link.replace("$businessService", service);
				link = link.replace("$receiptNumber", receiptNumber);
				link = link.replace("$mobile", mobileAndMsg.getKey());
				link = garbageServicesUtil.getShortenedURL(link);
				messageToReplace = messageToReplace.replace("<receipt download link>", link);

				messageToReturn.put(mobileAndMsg.getKey(), messageToReplace);
			}
			
		return messageToReturn;

	}

	public String getReceiptNumber(GarbageConnectionRequest garbageConnectionRequest){
		String consumerCode,service;
		if(StringUtils.isEmpty(garbageConnectionRequest.getGarbageConnection().getConnectionNo())){
			consumerCode = garbageConnectionRequest.getGarbageConnection().getApplicationNo();
			service = businessService;
		}
		else{
			consumerCode = garbageConnectionRequest.getGarbageConnection().getConnectionNo();
			service = "SW";
		}
		StringBuilder URL = garbageServicesUtil.getcollectionURL();
		URL.append(service).append("/_search").append("?").append("consumerCodes=").append(consumerCode)
				.append("&").append("tenantId=").append(garbageConnectionRequest.getGarbageConnection().getTenantId());
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(garbageConnectionRequest.getRequestInfo()).build();
		Object response = serviceRequestRepository.fetchResult(URL,requestInfoWrapper);
		PaymentResponse paymentResponse = mapper.convertValue(response, PaymentResponse.class);
		return paymentResponse.getPayments().get(0).getPaymentDetails().get(0).getReceiptNumber();
	}

}
