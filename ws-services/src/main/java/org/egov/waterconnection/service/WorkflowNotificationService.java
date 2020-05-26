package org.egov.waterconnection.service;

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
import org.egov.tracer.model.CustomException;
import org.egov.waterconnection.config.WSConfiguration;
import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.model.Action;
import org.egov.waterconnection.model.ActionItem;
import org.egov.waterconnection.model.CalculationCriteria;
import org.egov.waterconnection.model.CalculationReq;
import org.egov.waterconnection.model.CalculationRes;
import org.egov.waterconnection.model.Event;
import org.egov.waterconnection.model.EventRequest;
import org.egov.waterconnection.model.Property;
import org.egov.waterconnection.model.Recepient;
import org.egov.waterconnection.model.SMSRequest;
import org.egov.waterconnection.model.Source;
import org.egov.waterconnection.model.WaterConnection;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.egov.waterconnection.model.workflow.BusinessService;
import org.egov.waterconnection.model.workflow.State;
import org.egov.waterconnection.repository.ServiceRequestRepository;
import org.egov.waterconnection.util.NotificationUtil;
import org.egov.waterconnection.util.WaterServicesUtil;
import org.egov.waterconnection.validator.ValidateProperty;
import org.egov.waterconnection.workflow.WorkflowService;
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
	private NotificationUtil notificationUtil;
	
	@Autowired
	private WSConfiguration config;
	
	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private WaterServicesUtil waterServiceUtil;
	
	@Autowired
	private ValidateProperty validateProperty;
	
	String tenantIdReplacer = "$tenantId";
	String fileStoreIdsReplacer = "$.filestoreIds";
	String urlReplacer = "url";
	String requestInfoReplacer = "RequestInfo";
	String WaterConnectionReplacer = "WaterConnection";
	String fileStoreIdReplacer = "$fileStoreIds";
	String totalAmount= "totalAmount";
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
	 * @param record record is bill response.
	 * @param topic topic is bill generation topic for water.
	 */
	public void process(WaterConnectionRequest request, String topic) {
		try {
			String applicationStatus = workflowService.getApplicationStatus(request.getRequestInfo(),
					request.getWaterConnection().getApplicationNo(),
					request.getWaterConnection().getTenantId());
			
			if (!WCConstants.NOTIFICATION_ENABLE_FOR_STATUS.contains(request.getWaterConnection().getProcessInstance().getAction()+"_"+applicationStatus)) {
				log.info("Notification Disabled For State :" + applicationStatus);
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
	 * @param waterConnection
	 * @param topic
	 * @param requestInfo
	 * @return EventRequest Object
	 */
	private EventRequest getEventRequest(WaterConnectionRequest request, String topic, Property property, String applicationStatus) {
		String localizationMessage = notificationUtil
				.getLocalizationMessages(property.getTenantId(), request.getRequestInfo());
		String message = notificationUtil.getCustomizedMsgForInApp(request.getWaterConnection().getProcessInstance().getAction(), applicationStatus,
				localizationMessage);
		if (message == null) {
			log.info("No message Found For Topic : " + topic);
			return null;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		property.getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames, request,
				message, property);
		Set<String> mobileNumbers = mobileNumberAndMesssage.keySet().stream().collect(Collectors.toSet());
		Map<String, String> mapOfPhnoAndUUIDs = fetchUserUUIDs(mobileNumbers, request.getRequestInfo(), property.getTenantId());
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

			Action  action = getActionForEventNotification(mobileNumberAndMesssage, mobile, request, property);
			events.add(Event.builder().tenantId(property.getTenantId())
					.description(mobileNumberAndMesssage.get(mobile)).eventType(WCConstants.USREVENTS_EVENT_TYPE)
					.name(WCConstants.USREVENTS_EVENT_NAME).postedBy(WCConstants.USREVENTS_EVENT_POSTEDBY)
					.source(Source.WEBAPP).recepient(recepient).eventDetails(null).actions(action).build());
		}
		if (!CollectionUtils.isEmpty(events)) {
			return EventRequest.builder().requestInfo(request.getRequestInfo()).events(events).build();
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
			String mobileNumber, WaterConnectionRequest connectionRequest, Property property) {
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
				actionLink = actionLink.replace(applicationNumberReplacer, connectionRequest.getWaterConnection().getApplicationNo());
				actionLink = actionLink.replace(tenantIdReplacer, property.getTenantId());
			}
			if (code.equalsIgnoreCase("PAY NOW")) {
				actionLink = config.getNotificationUrl() + config.getApplicationPayLink();
				actionLink = actionLink.replace(mobileNoReplacer, mobileNumber);
				actionLink = actionLink.replace(consumerCodeReplacer, connectionRequest.getWaterConnection().getApplicationNo());
				actionLink = actionLink.replace(tenantIdReplacer, property.getTenantId());
			}
			if (code.equalsIgnoreCase("DOWNLOAD RECEIPT")) {
				actionLink = config.getNotificationUrl() + config.getViewHistoryLink();
				actionLink = actionLink.replace(mobileNoReplacer, mobileNumber);
				actionLink = actionLink.replace(applicationNumberReplacer, connectionRequest.getWaterConnection().getApplicationNo());
				actionLink = actionLink.replace(tenantIdReplacer, property.getTenantId());
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
	private List<SMSRequest> getSmsRequest(WaterConnectionRequest waterConnectionRequest, String topic,
			Property property, String applicationStatus) {
		String localizationMessage = notificationUtil.getLocalizationMessages(property.getTenantId(),
				waterConnectionRequest.getRequestInfo());
		String message = notificationUtil.getCustomizedMsgForSMS(
				waterConnectionRequest.getWaterConnection().getProcessInstance().getAction(), applicationStatus,
				localizationMessage);
		if (message == null) {
			log.info("No message Found For Topic : " + topic);
			return null;
		}
		Map<String, String> mobileNumbersAndNames = new HashMap<>();
		property.getOwners().forEach(owner -> {
			if (owner.getMobileNumber() != null)
				mobileNumbersAndNames.put(owner.getMobileNumber(), owner.getName());
		});
		Map<String, String> mobileNumberAndMesssage = getMessageForMobileNumber(mobileNumbersAndNames,
				waterConnectionRequest, message, property);
		List<SMSRequest> smsRequest = new ArrayList<>();
		mobileNumberAndMesssage.forEach((mobileNumber, messg) -> {
			SMSRequest req = new SMSRequest(mobileNumber, messg);
			smsRequest.add(req);
		});
		return smsRequest;
	}
	
	public Map<String, String> getMessageForMobileNumber(Map<String, String> mobileNumbersAndNames,
			WaterConnectionRequest waterConnectionRequest, String message, Property property) {
		Map<String, String> messagetoreturn = new HashMap<>();
		String messageToreplace = message;
		for (Entry<String, String> mobileAndName : mobileNumbersAndNames.entrySet()) {
			messageToreplace = message;
			if (messageToreplace.contains("<Owner Name>"))
				messageToreplace = messageToreplace.replace("<Owner Name>", mobileAndName.getValue());
			if (messageToreplace.contains("<Service>"))
				messageToreplace = messageToreplace.replace("<Service>", WCConstants.SERVICE_FIELD_VALUE_NOTIFICATION);

			if (messageToreplace.contains("<Plumber Info>"))
				messageToreplace = getMessageForPlumberInfo(waterConnectionRequest.getWaterConnection(), messageToreplace);
			
			if (messageToreplace.contains("<SLA>"))
				messageToreplace = messageToreplace.replace("<SLA>", getSLAForState(waterConnectionRequest, property));

			if (messageToreplace.contains("<Application number>"))
				messageToreplace = messageToreplace.replace("<Application number>", waterConnectionRequest.getWaterConnection().getApplicationNo());

			if (messageToreplace.contains("<Application download link>"))
				messageToreplace = messageToreplace.replace("<Application download link>",
						waterServiceUtil.getShortnerURL(getApplicationDownlonadLink(waterConnectionRequest, property)));

			if (messageToreplace.contains("<mseva URL>"))
				messageToreplace = messageToreplace.replace("<mseva URL>",
						waterServiceUtil.getShortnerURL(config.getNotificationUrl()));

			if (messageToreplace.contains("<mseva app link>"))
				messageToreplace = messageToreplace.replace("<mseva app link>",
						waterServiceUtil.getShortnerURL(config.getMSevaAppLink()));

			if (messageToreplace.contains("<View History Link>")) {
				String historyLink = config.getNotificationUrl() + config.getViewHistoryLink();
				historyLink = historyLink.replace(mobileNoReplacer, mobileAndName.getKey());
				historyLink = historyLink.replace(applicationNumberReplacer, waterConnectionRequest.getWaterConnection().getApplicationNo());
				historyLink = historyLink.replace(tenantIdReplacer, property.getTenantId());
				messageToreplace = messageToreplace.replace("<View History Link>",
						waterServiceUtil.getShortnerURL(historyLink));
			}
			if (messageToreplace.contains("<payment link>")) {
				String paymentLink = config.getNotificationUrl() + config.getApplicationPayLink();
				paymentLink = paymentLink.replace(mobileNoReplacer, mobileAndName.getKey());
				paymentLink = paymentLink.replace(consumerCodeReplacer, waterConnectionRequest.getWaterConnection().getApplicationNo());
				paymentLink = paymentLink.replace(tenantIdReplacer, property.getTenantId());
				messageToreplace = messageToreplace.replace("<payment link>",
						waterServiceUtil.getShortnerURL(paymentLink));
			}
			if (messageToreplace.contains("<receipt download link>"))
				messageToreplace = messageToreplace.replace("<receipt download link>",
						waterServiceUtil.getShortnerURL(config.getNotificationUrl()));

			if (messageToreplace.contains("<connection details page>")) {
				String connectionDetaislLink = config.getNotificationUrl() + config.getConnectionDetailsLink();
				connectionDetaislLink = connectionDetaislLink.replace(connectionNoReplacer,
						waterConnectionRequest.getWaterConnection().getConnectionNo());
				connectionDetaislLink = connectionDetaislLink.replace(tenantIdReplacer,
						property.getTenantId());
				messageToreplace = messageToreplace.replace("<connection details page>",
						waterServiceUtil.getShortnerURL(connectionDetaislLink));
			}
			messagetoreturn.put(mobileAndName.getKey(), messageToreplace);
		}
		return messagetoreturn;
	}

	/**
	 * This method returns message to replace for plumber info depending upon
	 * whether the plumber info type is either SELF or ULB
	 * 
	 * @param waterConnection
	 * @param messageTemplate
	 * @return updated messageTemplate
	 */
	 
	@SuppressWarnings("unchecked")
	public String getMessageForPlumberInfo(WaterConnection waterConnection, String messageTemplate) {
			HashMap<String, Object> addDetail = mapper.convertValue(waterConnection.getAdditionalDetails(),
					HashMap.class);
			if(!StringUtils.isEmpty(String.valueOf(addDetail.get(WCConstants.DETAILS_PROVIDED_BY)))){
			   String detailsProvidedBy = String.valueOf(addDetail.get(WCConstants.DETAILS_PROVIDED_BY));
			if ( StringUtils.isEmpty(detailsProvidedBy) || detailsProvidedBy.equalsIgnoreCase(WCConstants.SELF)) {
				String code = StringUtils.substringBetween(messageTemplate, "<Plumber Info>", "</Plumber Info>");
				messageTemplate = messageTemplate.replace("<Plumber Info>", "");
				messageTemplate = messageTemplate.replace("</Plumber Info>", "");
				messageTemplate = messageTemplate.replace(code, "");
			} else {
				messageTemplate = messageTemplate.replace("<Plumber Info>", "").replace("</Plumber Info>", "");
				messageTemplate = messageTemplate.replace("<Plumber name>",
						StringUtils.isEmpty(waterConnection.getPlumberInfo().get(0).getName()) == true ? ""
								: waterConnection.getPlumberInfo().get(0).getName());
				messageTemplate = messageTemplate.replace("<Plumber Licence No.>",
						StringUtils.isEmpty(waterConnection.getPlumberInfo().get(0).getLicenseNo()) == true ? ""
								: waterConnection.getPlumberInfo().get(0).getLicenseNo());
				messageTemplate = messageTemplate.replace("<Plumber Mobile No.>",
						StringUtils.isEmpty(waterConnection.getPlumberInfo().get(0).getMobileNumber()) == true ? ""
								: waterConnection.getPlumberInfo().get(0).getMobileNumber());
			}
		  
		}
		return messageTemplate;

	}
	
	/**
	 * Fetches SLA of CITIZENs based on the phone number.
	 * 
	 * @param waterConnection
	 * @param requestInfo
	 * @return string consisting SLA
	 */

	public String getSLAForState(WaterConnectionRequest connectionRequest, Property property) {
		String resultSla = "";
		BusinessService businessService = workflowService
				.getBusinessService(property.getTenantId(), connectionRequest.getRequestInfo());
		if (businessService != null && businessService.getStates() != null && businessService.getStates().size() > 0) {
			for (State state : businessService.getStates()) {
				if (WCConstants.PENDING_FOR_CONNECTION_ACTIVATION.equalsIgnoreCase(state.getState())) {
					resultSla = String.valueOf(
							(state.getSla() == null ? config.getSlaDefaultValue() : state.getSla()) / 86400000);
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
    
    /**
     * Fetch URL for application download link
     * 
     * @param waterConnection
     * @param requestInfo
     * @return application download link
     */
	private String getApplicationDownlonadLink(WaterConnectionRequest waterConnectionRequest, Property property) {
		CalculationCriteria criteria = CalculationCriteria.builder().applicationNo(waterConnectionRequest.getWaterConnection().getApplicationNo())
				.waterConnection(waterConnectionRequest.getWaterConnection()).tenantId(property.getTenantId()).build();
		CalculationReq calRequest = CalculationReq.builder().calculationCriteria(Arrays.asList(criteria))
				.requestInfo(waterConnectionRequest.getRequestInfo()).isconnectionCalculation(false).build();
		try {
			Object response = serviceRequestRepository.fetchResult(waterServiceUtil.getEstimationURL(), calRequest);
			CalculationRes calResponse = mapper.convertValue(response, CalculationRes.class);
			JSONObject waterobject = mapper.convertValue(waterConnectionRequest.getWaterConnection(), JSONObject.class);
			if (CollectionUtils.isEmpty(calResponse.getCalculation())) {
				throw new CustomException("NO_ESTIMATION_FOUND", "Estimation not found!!!");
			}
			waterobject.put(totalAmount, calResponse.getCalculation().get(0).getTotalAmount());
			waterobject.put(applicationFee, calResponse.getCalculation().get(0).getFee());
			waterobject.put(serviceFee, calResponse.getCalculation().get(0).getCharge());
			waterobject.put(tax, calResponse.getCalculation().get(0).getTaxAmount());
			String tenantId = property.getTenantId().split("\\.")[0];
			String fileStoreId = getFielStoreIdFromPDFService(waterobject, waterConnectionRequest.getRequestInfo(), tenantId);
			return getApplicationDownloadLink(tenantId, fileStoreId);
		} catch (Exception ex) {
			log.error("Calculation response error!!", ex);
			throw new CustomException("WATER_CALCULATION_EXCEPTION", "Calculation response can not parsed!!!");
		}
	}
	/**
	 * Get file store id from PDF service
	 * 
	 * @param waterobject
	 * @param requestInfo
	 * @param tenantId
	 * @return file store id
	 */
	private String getFielStoreIdFromPDFService(JSONObject waterobject, RequestInfo requestInfo, String tenantId) {
		JSONArray waterconnectionlist = new JSONArray();
		waterconnectionlist.add(waterobject);
		JSONObject requestPayload = new JSONObject();
		requestPayload.put(requestInfoReplacer, requestInfo);
		requestPayload.put(WaterConnectionReplacer, waterconnectionlist);
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(config.getPdfServiceHost());
			String pdfLink = config.getPdfServiceLink();
			pdfLink = pdfLink.replace(tenantIdReplacer, tenantId).replace(applicationKey, WCConstants.PDF_APPLICATION_KEY);
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
			throw new CustomException("WATER_FILESTORE_PDF_EXCEPTION", "PDF response can not parsed!!!");
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
			throw new CustomException("WATER_FILESTORE_PDF_EXCEPTION", "PDF response can not parsed!!!");
		}
	}
	
}
