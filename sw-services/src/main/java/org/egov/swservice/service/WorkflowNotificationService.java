package org.egov.swservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.model.*;
import org.egov.swservice.model.workflow.BusinessService;
import org.egov.swservice.model.workflow.State;
import org.egov.swservice.repository.ServiceRequestRepository;
import org.egov.swservice.util.NotificationUtil;
import org.egov.swservice.util.SWConstants;
import org.egov.swservice.util.SewerageServicesUtil;
import org.egov.swservice.validator.ValidateProperty;
import org.egov.swservice.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
	
	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private ValidateProperty validateProperty;

	String tenantIdReplacer = "$tenantId";
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
	String mobileNoReplacer = "$mobileNo";
	String applicationKey = "$applicationkey";


	/**
	 *
	 * @param request record is bill response.
	 * @param topic opic is bill generation topic for sewerage.
	 */
	public void process(SewerageConnectionRequest request, String topic) {
		try {
			String applicationStatus = request.getSewerageConnection().getApplicationStatus();
			if (!SWConstants.NOTIFICATION_ENABLE_FOR_STATUS
					.contains(request.getSewerageConnection().getProcessInstance().getAction() + "_"
							+ applicationStatus)) {
				log.info("Notification Disabled For State :"
						+ applicationStatus);
				return;
			}
			Property property = validateProperty.getOrValidateProperty(request);
			
			if (config.getIsUserEventsNotificationEnabled() != null && config.getIsUserEventsNotificationEnabled()) {
				EventRequest eventRequest = getEventRequest(request, topic, property, applicationStatus);
				if (eventRequest != null) {
					log.info("In App Notification For WorkFlow :: -> " + mapper.writeValueAsString(eventRequest));
					notificationUtil.sendEventNotification(eventRequest);
				}
			}
			if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {
				List<SMSRequest> smsRequests = getSmsRequest(request, topic, property, applicationStatus);
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
	 * @param sewerageConnectionRequest
	 * @param topic
	 * @param property
	 * @param applicationStatus
	 * @return EventRequest Object
	 */
	private EventRequest getEventRequest(SewerageConnectionRequest sewerageConnectionRequest, String topic, Property property, String applicationStatus) {
		String localizationMessage = notificationUtil
				.getLocalizationMessages(property.getTenantId(), sewerageConnectionRequest.getRequestInfo());

		int reqType = SWConstants.UPDATE_APPLICATION;
		if ((!sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction().equalsIgnoreCase(SWConstants.ACTIVATE_CONNECTION))
				&& sewerageServicesUtil.isModifyConnectionRequest(sewerageConnectionRequest)) {
			reqType = SWConstants.MODIFY_CONNECTION;
		}
		String message = notificationUtil.getCustomizedMsgForInApp(
				sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction(), applicationStatus,
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
		if(!CollectionUtils.isEmpty(sewerageConnectionRequest.getSewerageConnection().getConnectionHolders())) {
			sewerageConnectionRequest.getSewerageConnection().getConnectionHolders().forEach(holder -> {
				if (!StringUtils.isEmpty(holder.getMobileNumber())) {
					mobileNumbersAndNames.put(holder.getMobileNumber(), holder.getName());
				}
			});
		}
		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames,
				sewerageConnectionRequest, message, property);
		Set<String> mobileNumbers = mobileNumberAndMesssage.keySet().stream().collect(Collectors.toSet());
		Map<String, String> mapOfPhnoAndUUIDs = fetchUserUUIDs(mobileNumbers, sewerageConnectionRequest.getRequestInfo(),
				property.getTenantId());
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

			Action action = getActionForEventNotification(mobileNumberAndMesssage, mobile, sewerageConnectionRequest,
					property);
			events.add(Event.builder().tenantId(property.getTenantId())
					.description(mobileNumberAndMesssage.get(mobile)).eventType(SWConstants.USREVENTS_EVENT_TYPE)
					.name(SWConstants.USREVENTS_EVENT_NAME).postedBy(SWConstants.USREVENTS_EVENT_POSTEDBY)
					.source(Source.WEBAPP).recepient(recepient).eventDetails(null).actions(action).build());
		}
		if (!CollectionUtils.isEmpty(events)) {
			return EventRequest.builder().requestInfo(sewerageConnectionRequest.getRequestInfo()).events(events).build();
		} else {
			return null;
		}
	}

	/**
	 *
	 * @param mobileNumberAndMesssage
	 * @param mobileNumber
	 * @param sewerageConnectionRequest
	 * @param property
	 * @return return action link
	 */
	public Action getActionForEventNotification(Map<String, String> mobileNumberAndMesssage, String mobileNumber,
			SewerageConnectionRequest sewerageConnectionRequest, Property property) {
		String messageTemplate = mobileNumberAndMesssage.get(mobileNumber);
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
				actionLink = actionLink.replace(applicationNumberReplacer, sewerageConnectionRequest.getSewerageConnection().getApplicationNo());
				actionLink = actionLink.replace(tenantIdReplacer, property.getTenantId());
			}
			if (code.equalsIgnoreCase("PAY NOW")) {
				actionLink = config.getNotificationUrl() + config.getApplicationPayLink();
				actionLink = actionLink.replace(mobileNoReplacer, mobileNumber);
				actionLink = actionLink.replace(consumerCodeReplacer, sewerageConnectionRequest.getSewerageConnection().getApplicationNo());
				actionLink = actionLink.replace(tenantIdReplacer, property.getTenantId());
			}
			if (code.equalsIgnoreCase("DOWNLOAD RECEIPT")) {
				actionLink = config.getNotificationUrl() + config.getViewHistoryLink();
				actionLink = actionLink.replace(mobileNoReplacer, mobileNumber);
				actionLink = actionLink.replace(applicationNumberReplacer, sewerageConnectionRequest.getSewerageConnection().getApplicationNo());
				actionLink = actionLink.replace(tenantIdReplacer, property.getTenantId());
			}
			if (code.equalsIgnoreCase("View History Link")) {
				actionLink = config.getNotificationUrl() + config.getViewHistoryLink();
				actionLink = actionLink.replace(mobileNoReplacer, mobileNumber);
				actionLink = actionLink.replace(applicationNumberReplacer,
						sewerageConnectionRequest.getSewerageConnection().getApplicationNo());
				actionLink = actionLink.replace(tenantIdReplacer, property.getTenantId());
				actionLink = actionLink.replace("<View History Link>",
						sewerageServicesUtil.getShortnerURL(actionLink));
			}
			ActionItem item = ActionItem.builder().actionUrl(actionLink).code(code).build();
			items.add(item);
			mobileNumberAndMesssage.replace(mobileNumber, messageTemplate);
		}
		return Action.builder().actionUrls(items).build();

	}


	/**
	 *
	 * @param sewerageConnectionRequest
	 * @param topic
	 * @param property
	 * @param applicationStatus
	 * @return
	 */
	private List<SMSRequest> getSmsRequest(SewerageConnectionRequest sewerageConnectionRequest, String topic, Property property, String applicationStatus) {
		String localizationMessage = notificationUtil
				.getLocalizationMessages(property.getTenantId(), sewerageConnectionRequest.getRequestInfo());
		int reqType = SWConstants.UPDATE_APPLICATION;
		if ((!sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction().equalsIgnoreCase(SWConstants.ACTIVATE_CONNECTION))
				&& sewerageServicesUtil.isModifyConnectionRequest(sewerageConnectionRequest)) {
			reqType = SWConstants.MODIFY_CONNECTION;
		}
		String message = notificationUtil.getCustomizedMsgForSMS(
				sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction(), applicationStatus,
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
		if(!CollectionUtils.isEmpty(sewerageConnectionRequest.getSewerageConnection().getConnectionHolders())) {
			sewerageConnectionRequest.getSewerageConnection().getConnectionHolders().forEach(holder -> {
				if (!StringUtils.isEmpty(holder.getMobileNumber())) {
					mobileNumbersAndNames.put(holder.getMobileNumber(), holder.getName());
				}
			});
		}
		List<SMSRequest> smsRequest = new ArrayList<>();
		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames,
				sewerageConnectionRequest, message, property);
		mobileNumberAndMesssage.forEach((mobileNumber, messg) -> {
			SMSRequest req = new SMSRequest(mobileNumber, messg);
			smsRequest.add(req);
		});
		return smsRequest;
	}

	/**
	 *
	 * @param mobileNumbersAndNames
	 * @param sewerageConnectionRequest
	 * @param message
	 * @param property
	 * @return
	 */
	public Map<String, String> getMessageForMobileNumber(Map<String, String> mobileNumbersAndNames,
			SewerageConnectionRequest sewerageConnectionRequest, String message, Property property) {
		Map<String, String> messagetoreturn = new HashMap<>();
		String messageToreplace = message;
		for (Entry<String, String> mobileAndName : mobileNumbersAndNames.entrySet()) {
			messageToreplace = message;
			if (messageToreplace.contains("<Owner Name>"))
				messageToreplace = messageToreplace.replace("<Owner Name>", mobileAndName.getValue());
			if (messageToreplace.contains("<Service>"))
				messageToreplace = messageToreplace.replace("<Service>", SWConstants.SERVICE_FIELD_VALUE_NOTIFICATION);

			if (messageToreplace.contains("<Application number>"))
				messageToreplace = messageToreplace.replace("<Application number>",
						sewerageConnectionRequest.getSewerageConnection().getApplicationNo());

			if (messageToreplace.contains("<Application download link>"))
				messageToreplace = messageToreplace.replace("<Application download link>",
						sewerageServicesUtil.getShortnerURL(
								getApplicationDownloaderLink(sewerageConnectionRequest, property)));

			if (messageToreplace.contains("<mseva URL>"))
				messageToreplace = messageToreplace.replace("<mseva URL>",
						sewerageServicesUtil.getShortnerURL(config.getNotificationUrl()));
			
			if (messageToreplace.contains("<Plumber Info>"))
				messageToreplace = getMessageForPlumberInfo(sewerageConnectionRequest.getSewerageConnection(), messageToreplace);
			
			if (messageToreplace.contains("<SLA>"))
				messageToreplace = messageToreplace.replace("<SLA>", getSLAForState(
						sewerageConnectionRequest, property));

			if (messageToreplace.contains("<mseva app link>"))
				messageToreplace = messageToreplace.replace("<mseva app link>",
						sewerageServicesUtil.getShortnerURL(config.getMSevaAppLink()));

			if (messageToreplace.contains("<View History Link>")) {
				String historyLink = config.getNotificationUrl() + config.getViewHistoryLink();
				historyLink = historyLink.replace(mobileNoReplacer, mobileAndName.getKey());
				historyLink = historyLink.replace(applicationNumberReplacer,
						sewerageConnectionRequest.getSewerageConnection().getApplicationNo());
				historyLink = historyLink.replace(tenantIdReplacer, property.getTenantId());
				messageToreplace = messageToreplace.replace("<View History Link>",
						sewerageServicesUtil.getShortnerURL(historyLink));
			}
			if (messageToreplace.contains("<payment link>")) {
				String paymentLink = config.getNotificationUrl() + config.getApplicationPayLink();
				paymentLink = paymentLink.replace(mobileNoReplacer, mobileAndName.getKey());
				paymentLink = paymentLink.replace(consumerCodeReplacer,
						sewerageConnectionRequest.getSewerageConnection().getApplicationNo());
				paymentLink = paymentLink.replace(tenantIdReplacer, property.getTenantId());
				messageToreplace = messageToreplace.replace("<payment link>",
						sewerageServicesUtil.getShortnerURL(paymentLink));
			}
			if (messageToreplace.contains("<receipt download link>"))
				messageToreplace = messageToreplace.replace("<receipt download link>",
						sewerageServicesUtil.getShortnerURL(config.getNotificationUrl()));

			if (messageToreplace.contains("<connection details page>")) {
				String connectionDetaislLink = config.getNotificationUrl() + config.getConnectionDetailsLink();
				connectionDetaislLink = connectionDetaislLink.replace(connectionNoReplacer,
						sewerageConnectionRequest.getSewerageConnection().getConnectionNo());
				connectionDetaislLink = connectionDetaislLink.replace(tenantIdReplacer, property.getTenantId());
				messageToreplace = messageToreplace.replace("<connection details page>",
						sewerageServicesUtil.getShortnerURL(connectionDetaislLink));
			}
			if(messageToreplace.contains("<Date effective from>")) {
				if (sewerageConnectionRequest.getSewerageConnection().getDateEffectiveFrom() != null) {
					LocalDate date = Instant
							.ofEpochMilli(sewerageConnectionRequest.getSewerageConnection().getDateEffectiveFrom() > 10 ?
									sewerageConnectionRequest.getSewerageConnection().getDateEffectiveFrom() :
									sewerageConnectionRequest.getSewerageConnection().getDateEffectiveFrom() * 1000)
							.atZone(ZoneId.systemDefault()).toLocalDate();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
					messageToreplace = messageToreplace.replace("<Date effective from>", date.format(formatter));
				} else {
					messageToreplace = messageToreplace.replace("<Date effective from>", "");
				}
			}
			messagetoreturn.put(mobileAndName.getKey(), messageToreplace);
		}
		return messagetoreturn;
	}
	
	/**
	 * This method returns message to replace for plumber info depending upon
	 * whether the plumber info type is either SELF or ULB
	 * 
	 * @param sewerageConnection
	 * @param messageTemplate
	 * @return updated messageTemplate
	 */

	@SuppressWarnings("unchecked")
	public String getMessageForPlumberInfo(SewerageConnection sewerageConnection, String messageTemplate) {
		HashMap<String, Object> addDetail = mapper.convertValue(sewerageConnection.getAdditionalDetails(),
				HashMap.class);
		if (!StringUtils.isEmpty(String.valueOf(addDetail.get(SWConstants.DETAILS_PROVIDED_BY)))) {
			String detailsProvidedBy = String.valueOf(addDetail.get(SWConstants.DETAILS_PROVIDED_BY));
			if (StringUtils.isEmpty(detailsProvidedBy) || detailsProvidedBy.equalsIgnoreCase(SWConstants.SELF)) {
				String code = StringUtils.substringBetween(messageTemplate, "<Plumber Info>", "</Plumber Info>");
				messageTemplate = messageTemplate.replace("<Plumber Info>", "");
				messageTemplate = messageTemplate.replace("</Plumber Info>", "");
				messageTemplate = messageTemplate.replace(code, "");
			} else {
				messageTemplate = messageTemplate.replace("<Plumber Info>", "").replace("</Plumber Info>", "");
				messageTemplate = messageTemplate.replace("<Plumber name>",
						StringUtils.isEmpty(sewerageConnection.getPlumberInfo().get(0).getName()) == true ? ""
								: sewerageConnection.getPlumberInfo().get(0).getName());
				messageTemplate = messageTemplate.replace("<Plumber Licence No.>",
						StringUtils.isEmpty(sewerageConnection.getPlumberInfo().get(0).getLicenseNo()) == true ? ""
								: sewerageConnection.getPlumberInfo().get(0).getLicenseNo());
				messageTemplate = messageTemplate.replace("<Plumber Mobile No.>",
						StringUtils.isEmpty(sewerageConnection.getPlumberInfo().get(0).getMobileNumber()) == true ? ""
								: sewerageConnection.getPlumberInfo().get(0).getMobileNumber());
			}

		}

		return messageTemplate;

	}

	/**
	 * Fetches SLA of CITIZENs based on the phone number.
	 *
	 * @param sewerageConnectionRequest
	 * @param property
	 * @return
	 */
	public String getSLAForState(SewerageConnectionRequest sewerageConnectionRequest, Property property) {
		String resultSla = "";
		BusinessService businessService = workflowService
				.getBusinessService(config.getBusinessServiceValue(), property.getTenantId(), sewerageConnectionRequest.getRequestInfo());
		if (businessService != null && businessService.getStates() != null && businessService.getStates().size() > 0) {
			for (State state : businessService.getStates()) {
				if (SWConstants.PENDING_FOR_CONNECTION_ACTIVATION.equalsIgnoreCase(state.getState())) {
					resultSla = String.valueOf(
							(state.getSla() == null ? 0l : state.getSla()) / 86400000);
				}
			}
		}
		return resultSla;
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
	 * @param sewerageConnectionRequest
	 * @param property
	 * @return
	 */
	private String getApplicationDownloaderLink(SewerageConnectionRequest sewerageConnectionRequest,
			Property property) {
		CalculationCriteria criteria = CalculationCriteria.builder()
				.applicationNo(sewerageConnectionRequest.getSewerageConnection().getApplicationNo())
				.sewerageConnection(sewerageConnectionRequest.getSewerageConnection()).tenantId(property.getTenantId())
				.build();
		CalculationReq calRequest = CalculationReq.builder().calculationCriteria(Arrays.asList(criteria))
				.requestInfo(sewerageConnectionRequest.getRequestInfo()).isconnectionCalculation(false).build();
		try {
			Object response = serviceRequestRepository.fetchResult(sewerageServicesUtil.getEstimationURL(), calRequest);
			CalculationRes calResponse = mapper.convertValue(response, CalculationRes.class);
			JSONObject sewerageobject = mapper.convertValue(sewerageConnectionRequest.getSewerageConnection(),
					JSONObject.class);
			if (CollectionUtils.isEmpty(calResponse.getCalculation())) {
				throw new CustomException("NO_ESTIMATION_FOUND", "Estimation not found!!!");
			}
			sewerageobject.put(totalAmount, calResponse.getCalculation().get(0).getTotalAmount());
			sewerageobject.put(applicationFee, calResponse.getCalculation().get(0).getFee());
			sewerageobject.put(serviceFee, calResponse.getCalculation().get(0).getCharge());
			sewerageobject.put(tax, calResponse.getCalculation().get(0).getTaxAmount());
			String tenantId = property.getTenantId().split("\\.")[0];
			String fileStoreId = getFielStoreIdFromPDFService(sewerageobject,
					sewerageConnectionRequest.getRequestInfo(), tenantId);
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
			pdfLink = pdfLink.replace(tenantIdReplacer, tenantId).replace(applicationKey, SWConstants.PDF_APPLICATION_KEY);;
			builder.append(pdfLink);
			Object response = serviceRequestRepository.fetchResult(builder, requestPayload);
			DocumentContext responseContext = JsonPath.parse(response);
			List<Object> fileStoreIds = responseContext.read("$.filestoreIds");
			if (CollectionUtils.isEmpty(fileStoreIds)) {
				throw new CustomException("EMPTY_FILESTORE_IDS_FROM_PDF_SERVICE",
						"NO file store id found from pdf service");
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
