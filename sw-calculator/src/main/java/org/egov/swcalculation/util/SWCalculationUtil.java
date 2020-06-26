package org.egov.swcalculation.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.swcalculation.config.SWCalculationConfiguration;
import org.egov.swcalculation.constants.SWCalculationConstant;
import org.egov.swcalculation.model.DemandDetail;
import org.egov.swcalculation.model.DemandDetailAndCollection;
import org.egov.swcalculation.model.DemandNotificationObj;
import org.egov.swcalculation.model.EmailRequest;
import org.egov.swcalculation.model.EventRequest;
import org.egov.swcalculation.model.GetBillCriteria;
import org.egov.swcalculation.model.NotificationReceiver;
import org.egov.swcalculation.model.Property;
import org.egov.swcalculation.model.PropertyCriteria;
import org.egov.swcalculation.model.PropertyResponse;
import org.egov.swcalculation.model.RequestInfoWrapper;
import org.egov.swcalculation.model.SMSRequest;
import org.egov.swcalculation.model.SewerageConnectionRequest;
import org.egov.swcalculation.producer.SWCalculationProducer;
import org.egov.swcalculation.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class SWCalculationUtil {

	@Autowired
	private SWCalculationConfiguration configurations;

	@Autowired
	private SWCalculationConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private SWCalculationProducer producer;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	
	@Value("${egov.property.service.host}")
	private String propertyHost;

	@Value("${egov.property.searchendpoint}")
	private String searchPropertyEndPoint;

	private String tenantId = "tenantId=";
	private String mobileNumber = "mobileNumber=";
	private String propertyIds = "propertyIds=";
	private String uuids = "uuids=";
	private String URL = "url";


	/**
	 * Returns the tax head search Url with tenantId and SW service name
	 * parameters
	 *
	 * @param tenantId
	 * @return
	 */
	public StringBuilder getTaxPeriodSearchUrl(String tenantId, String serviceFieldValue) {

		return new StringBuilder().append(configurations.getBillingServiceHost())
				.append(configurations.getTaxPeriodSearchEndpoint()).append(SWCalculationConstant.URL_PARAMS_SEPARATER)
				.append(SWCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(tenantId)
				.append(SWCalculationConstant.SEPARATER).append(SWCalculationConstant.SERVICE_FIELD_FOR_SEARCH_URL)
				.append(serviceFieldValue);
	}

	/**
	 * Returns the tax head search Url with tenantId and SW service name
	 * parameters
	 *
	 * @param tenantId
	 * @return
	 */
	public StringBuilder getTaxHeadSearchUrl(String tenantId, String serviceFieldValue) {

		return new StringBuilder().append(configurations.getBillingServiceHost())
				.append(configurations.getTaxheadsSearchEndpoint()).append(SWCalculationConstant.URL_PARAMS_SEPARATER)
				.append(SWCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(tenantId)
				.append(SWCalculationConstant.SEPARATER).append(SWCalculationConstant.SERVICE_FIELD_FOR_SEARCH_URL)
				.append(serviceFieldValue);
	}

	/**
	 * Creates generate bill url using tenantId,consumerCode and businessService
	 * 
	 * @return Bill Generate url
	 */
	public String getBillGenerateURI() {
		StringBuilder url = new StringBuilder(configurations.getBillingServiceHost());
		url.append(configurations.getBillGenEndPoint());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("consumerCode=");
		url.append("{2}");
		url.append("&");
		url.append("businessService=");
		url.append("{3}");

		return url.toString();
	}

	/**
	 * method to create demandsearch url with demand criteria
	 *
	 * @param getBillCriteria
	 * @return
	 */
	public StringBuilder getDemandSearchUrl(GetBillCriteria getBillCriteria) {

		if (CollectionUtils.isEmpty(getBillCriteria.getConsumerCodes()))
			return new StringBuilder().append(configurations.getBillingServiceHost())
					.append(configurations.getDemandSearchEndPoint()).append(SWCalculationConstant.URL_PARAMS_SEPARATER)
					.append(SWCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(getBillCriteria.getTenantId())
					.append(SWCalculationConstant.SEPARATER)
					.append(SWCalculationConstant.CONSUMER_CODE_SEARCH_FIELD_NAME)
					.append(getBillCriteria.getConnectionId() + SWCalculationConstant.SW_CONSUMER_CODE_SEPARATOR
							+ getBillCriteria.getConnectionNumber());

		else
			return new StringBuilder().append(configurations.getBillingServiceHost())
					.append(configurations.getDemandSearchEndPoint()).append(SWCalculationConstant.URL_PARAMS_SEPARATER)
					.append(SWCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(getBillCriteria.getTenantId())
					.append(SWCalculationConstant.SEPARATER)
					.append(SWCalculationConstant.CONSUMER_CODE_SEARCH_FIELD_NAME)
					.append(StringUtils.join(getBillCriteria.getConsumerCodes(), ","));

	}

	/**
	 * Returns url for demand update Api
	 *
	 * @return
	 */
	public StringBuilder getUpdateDemandUrl() {
		return new StringBuilder().append(configurations.getBillingServiceHost())
				.append(configurations.getDemandUpdateEndPoint());
	}

	public DemandDetailAndCollection getLatestDemandDetailByTaxHead(String taxHeadCode,
			List<DemandDetail> demandDetails) {
		List<DemandDetail> details = demandDetails.stream()
				.filter(demandDetail -> demandDetail.getTaxHeadMasterCode().equalsIgnoreCase(taxHeadCode))
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(details))
			return null;

		BigDecimal taxAmountForTaxHead = BigDecimal.ZERO;
		BigDecimal collectionAmountForTaxHead = BigDecimal.ZERO;
		DemandDetail latestDemandDetail = null;
		long maxCreatedTime = 0l;

		for (DemandDetail detail : details) {
			taxAmountForTaxHead = taxAmountForTaxHead.add(detail.getTaxAmount());
			collectionAmountForTaxHead = collectionAmountForTaxHead.add(detail.getCollectionAmount());
			if (detail.getAuditDetails().getCreatedTime() > maxCreatedTime) {
				maxCreatedTime = detail.getAuditDetails().getCreatedTime();
				latestDemandDetail = detail;
			}
		}

		return DemandDetailAndCollection.builder().taxHeadCode(taxHeadCode).latestDemandDetail(latestDemandDetail)
				.taxAmountForTaxHead(taxAmountForTaxHead).collectionAmountForTaxHead(collectionAmountForTaxHead)
				.build();

	}

	/**
	 * Creates sms request for the each owners
	 * 
	 * @param message
	 *            The message for the specific sewerage connection
	 * @param mobileNumberToOwnerName
	 *            Map of mobileNumber to OwnerName
	 * @return List of SMSRequest
	 */
	public List<SMSRequest> createSMSRequest(String message, Map<String, String> mobileNumberToOwnerName) {
		List<SMSRequest> smsRequest = new LinkedList<>();
		for (Map.Entry<String, String> entryset : mobileNumberToOwnerName.entrySet()) {
			String customizedMsg = message.replace("<1>", entryset.getValue());
			smsRequest.add(new SMSRequest(entryset.getKey(), customizedMsg));
		}
		return smsRequest;
	}

	/**
	 * Returns the uri for the localization call
	 * 
	 * @param tenantId
	 *            TenantId demand Notification Obj
	 * @return The uri for localization search call
	 */
	public StringBuilder getUri(String tenantId, RequestInfo requestInfo) {

		if (config.getIsLocalizationStateLevel())
			tenantId = tenantId.split("\\.")[0];

		String locale = SWCalculationConstant.NOTIFICATION_LOCALE;
		if (!StringUtils.isEmpty(requestInfo.getMsgId()) && requestInfo.getMsgId().split("|").length >= 2)
			locale = requestInfo.getMsgId().split("\\|")[1];

		StringBuilder uri = new StringBuilder();
		uri.append(config.getLocalizationHost()).append(config.getLocalizationContextPath())
				.append(config.getLocalizationSearchEndpoint()).append("?").append("locale=").append(locale)
				.append("&tenantId=").append(tenantId).append("&module=").append(SWCalculationConstant.MODULE);

		return uri;
	}

	/**
	 * Fetches messages from localization service
	 * 
	 * @param tenantId
	 *            tenantId of the sewerage connection
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @return Localization messages for the module
	 */
	@SuppressWarnings("rawtypes")
	public String getLocalizationMessages(String tenantId, RequestInfo requestInfo) {
		return new JSONObject(
				(LinkedHashMap) serviceRequestRepository.fetchResult(getUri(tenantId, requestInfo), requestInfo))
						.toString();
	}

	/**
	 * Extracts message for the specific code
	 * 
	 * @param notificationCode
	 *            The code for which message is required
	 * @param localizationMessage
	 *            The localization messages
	 * @return message for the specific code
	 */
	@SuppressWarnings("unchecked")
	private String getMessageTemplate(String notificationCode, String localizationMessage) {
		String path = "$..messages[?(@.code==\"{}\")].message";
		path = path.replace("{}", notificationCode);
		String message = null;
		try {
			Object messageObj = JsonPath.parse(localizationMessage).read(path);
			message = ((ArrayList<String>) messageObj).get(0);
		} catch (Exception e) {
			log.warn("Fetching from localization failed", e);
		}
		return message;
	}

	public String getCustomizedMsg(String topic, String localizationMessage) {
		String messageString = null;
		if (topic.equalsIgnoreCase(config.getOnDemandSuccess())) {
			messageString = getMessageTemplate(SWCalculationConstant.DEMAND_SUCCESS_MESSAGE, localizationMessage);
		}
		if (topic.equalsIgnoreCase(config.getOnDemandFailed())) {
			messageString = getMessageTemplate(SWCalculationConstant.DEMAND_FAILURE_MESSAGE, localizationMessage);
		}
		return messageString;
	}
	
	public String getCustomizedMsgForSMS(String topic, String localizationMessage) {
		String messageString = null;
		if (topic.equalsIgnoreCase(config.getOnDemandSuccess())) {
			messageString = getMessageTemplate(SWCalculationConstant.DEMAND_SUCCESS_MESSAGE, localizationMessage);
		}
		if (topic.equalsIgnoreCase(config.getOnDemandFailed())) {
			messageString = getMessageTemplate(SWCalculationConstant.DEMAND_FAILURE_MESSAGE, localizationMessage);
		}
		if (topic.equalsIgnoreCase(config.getPayTriggers())) {
			messageString = getMessageTemplate(SWCalculationConstant.SEWERAGE_CONNECTION_BILL_GENERATION_SMS_MESSAGE,
					localizationMessage);
		}
		return messageString;
	}
	
	public String getCustomizedMsgForInApp(String topic, String localizationMessage) {
		String messageString = null;
		if (topic.equalsIgnoreCase(config.getOnDemandSuccess())) {
			messageString = getMessageTemplate(SWCalculationConstant.DEMAND_SUCCESS_MESSAGE, localizationMessage);
		}
		if (topic.equalsIgnoreCase(config.getOnDemandFailed())) {
			messageString = getMessageTemplate(SWCalculationConstant.DEMAND_FAILURE_MESSAGE, localizationMessage);
		}
		if (topic.equalsIgnoreCase(config.getPayTriggers())) {
			messageString = getMessageTemplate(SWCalculationConstant.SEWERAGE_CONNECTION_BILL_GENERATION_APP_MESSAGE,
					localizationMessage);
		}
		return messageString;
	}

	/**
	 * 
	 * @param license
	 * @param messages
	 * @return
	 */
	public String getAppliedMsg(NotificationReceiver receiver, String message, DemandNotificationObj obj) {
		message = message.replace("<First Name>", receiver.getFirstName() == null ? "" : receiver.getFirstName());
		message = message.replace("<Last Name>", receiver.getLastName() == null ? "" : receiver.getLastName());
		message = message.replace("<service name>", receiver.getServiceName() == null ? "" : receiver.getServiceName());
		message = message.replace("<ULB Name>", receiver.getUlbName() == null ? "" : receiver.getUlbName());
		message = message.replace("<billing cycle>", obj.getBillingCycle() == null ? "" : obj.getBillingCycle());
		return message;
	}

	/**
	 * Send the SMSRequest on the SMSNotification kafka topic
	 * 
	 * @param smsRequestList
	 *            The list of SMSRequest to be sent
	 */
	public void sendSMS(List<SMSRequest> smsRequestList) {
		if (config.getIsSMSEnabled()) {
			if (CollectionUtils.isEmpty(smsRequestList))
				 log.info("Messages from localization couldn't be fetched!");
			for (SMSRequest smsRequest : smsRequestList) {
				producer.push(config.getSmsNotifTopic(), smsRequest);
				log.info("MobileNumber: " + smsRequest.getMobileNumber() + " Messages: " + smsRequest.getMessage());
			}
		}
	}
	
	/**
	 * Send the SMSRequest on the EmailNotification kafka topic
	 * @param emailRequest The list of EmailRequest to be sent
	 */
	public void sendEmail(List<EmailRequest> emailRequestList) {
		if (config.getIsSMSEnabled()) {
			if (CollectionUtils.isEmpty(emailRequestList))
				log.info("Messages from localization couldn't be fetched!");
			emailRequestList.forEach(emailRequest -> {
				producer.push(config.getEmailNotifyTopic(), emailRequest);
				log.info("Email To : " + emailRequest.getEmail() + " Body: " + emailRequest.getBody()+" Subject: "+ emailRequest.getSubject());
			});
		}
	}
	
	
	public String getCustomizedMsgForEmail(String topic, String localizationMessage) {
		String messageString = null;
		if (topic.equalsIgnoreCase(config.getOnDemandSuccess())) {
			messageString = getMessageTemplate(SWCalculationConstant.DEMAND_SUCCESS_MESSAGE_EMAIL, localizationMessage);
		}
		if (topic.equalsIgnoreCase(config.getOnDemandFailed())) {
			messageString = getMessageTemplate(SWCalculationConstant.DEMAND_FAILURE_MESSAGE_EMAIL, localizationMessage);
		}
		return messageString;
	}

	
	/**
	 * Pushes the event request to Kafka Queue.
	 * 
	 * @param request
	 */
	public void sendEventNotification(EventRequest request) {
		producer.push(config.getSaveUserEventsTopic(), request);
	}
	
	
	/**
	 * 
	 * @param SewerageConnectionRequest
	 *            SewerageConnectionRequest containing property
	 * @return List of Property
	 */
	public List<Property> propertySearch(SewerageConnectionRequest sewerageConnectionRequest) {
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		HashSet<String> propertyUUID = new HashSet<>();
		propertyUUID.add(sewerageConnectionRequest.getSewerageConnection().getPropertyId());
		propertyCriteria.setUuids(propertyUUID);
		propertyCriteria.setTenantId(sewerageConnectionRequest.getSewerageConnection().getTenantId());
		Object result = serviceRequestRepository.fetchResult(getPropertyURL(propertyCriteria),
				RequestInfoWrapper.builder().requestInfo(sewerageConnectionRequest.getRequestInfo()).build());
		List<Property> propertyList = getPropertyDetails(result);
		if (CollectionUtils.isEmpty(propertyList)) {
			throw new CustomException("INCORRECT PROPERTY ID", "SEWERAGE CONNECTION CAN NOT BE CREATED");
		}
		return propertyList;
	}

	/**
	 * 
	 * @param SewerageConnectionRequest
	 *            SewerageConnectionRequest
	 */
	public Property getProperty(SewerageConnectionRequest sewerageConnectionRequest) {
		Optional<Property> propertyList = propertySearch(sewerageConnectionRequest).stream().findFirst();
		if (!propertyList.isPresent()) {
			throw new CustomException("INVALID SEWERAGE CONNECTION PROPERTY",
					"Swerage connection cannot be enriched without property");
		}
		Property property = propertyList.get();
		if (StringUtils.isEmpty(property.getUsageCategory())) {
			throw new CustomException("INVALID SEWERAGE CONNECTION PROPERTY USAGE TYPE",
					"Sewerage connection cannot be enriched without property usage type");
		}
		return property;
	}

	/**
	 * 
	 * @param criteria
	 * @return property URL
	 */
	private StringBuilder getPropertyURL(PropertyCriteria criteria) {
		StringBuilder url = new StringBuilder(getPropertyURL());
		boolean isanyparametermatch = false;
		url.append("?");
		if (!StringUtils.isEmpty(criteria.getTenantId())) {
			isanyparametermatch = true;
			url.append(tenantId).append(criteria.getTenantId());
		}
		if (!CollectionUtils.isEmpty(criteria.getPropertyIds())) {
			if (isanyparametermatch)
				url.append("&");
			isanyparametermatch = true;
			String propertyIdsString = criteria.getPropertyIds().stream().map(propertyId -> propertyId)
					.collect(Collectors.toSet()).stream().collect(Collectors.joining(","));
			url.append(propertyIds).append(propertyIdsString);
		}
		if (!StringUtils.isEmpty(criteria.getMobileNumber())) {
			if (isanyparametermatch)
				url.append("&");
			isanyparametermatch = true;
			url.append(mobileNumber).append(criteria.getMobileNumber());
		}
		if (!CollectionUtils.isEmpty(criteria.getUuids())) {
			if (isanyparametermatch)
				url.append("&");
			String uuidString = criteria.getUuids().stream().map(uuid -> uuid).collect(Collectors.toSet()).stream()
					.collect(Collectors.joining(","));
			url.append(uuids).append(uuidString);
		}
		return url;
	}

	/**
	 * 
	 * @param result
	 *            Response object from property service call
	 * @return List of property
	 */
	private List<Property> getPropertyDetails(Object result) {

		try {
			PropertyResponse propertyResponse = objectMapper.convertValue(result, PropertyResponse.class);
			return propertyResponse.getProperties();
		} catch (Exception ex) {
			throw new CustomException("PARSING ERROR", "The property json cannot be parsed");
		}
	}

	public StringBuilder getPropertyURL() {
		return new StringBuilder().append(propertyHost).append(searchPropertyEndPoint);
	}
	
}
