package org.egov.echallan.util;

import com.jayway.jsonpath.JsonPath;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.model.Challan;
import org.egov.echallan.model.RequestInfoWrapper;
import org.egov.echallan.repository.ServiceRequestRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.util.*;


@Component
@Slf4j
public class NotificationUtil {
	public static final String NOTIFICATION_LOCALE = "en_IN";
	public static final String MODULE ="rainmaker-uc";
	private static final String CODES = "echallan.create.sms";
	public static final String BILL_AMOUNT_JSONPATH = "$.Bill[0].totalAmount";
	public static final String BILL_DUEDATE = "$.Bill[0].billDetails[0].expiryDate";
	public static final String BUSINESSSERVICELOCALIZATION_CODE_PREFIX = "BILLINGSERVICE_BUSINESSSERVICE_";
	public static final String LOCALIZATION_CODES_JSONPATH = "$.messages[0].code";
	public static final String LOCALIZATION_MSGS_JSONPATH = "$.messages[0].message";
	private ChallanConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private RestTemplate restTemplate;

	@Autowired
	public NotificationUtil(ChallanConfiguration config, ServiceRequestRepository serviceRequestRepository,
			RestTemplate restTemplate) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.restTemplate = restTemplate;
	}


	public String getCustomizedMsg(RequestInfo requestInfo, Challan challan, String localizationMessage) {
		String message = null, messageTemplate;
		
		
		messageTemplate = getMessageTemplate("echallan.create.sms", localizationMessage);
		System.out.println("message template==="+messageTemplate);
		message = getCreateMsg(requestInfo,challan, messageTemplate);
		

		return message;
	}

	private String getCreateMsg(RequestInfo requestInfo,Challan challan, String message) {
		String billDetails = getBillDetails(requestInfo,challan);
		
		Object obj = JsonPath.parse(billDetails).read(BILL_AMOUNT_JSONPATH);
		Object expiryDate = JsonPath.parse(billDetails).read(BILL_DUEDATE);
		BigDecimal amountToBePaid = new BigDecimal(obj.toString());
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis((long) expiryDate);
		
		Calendar fromcal = Calendar.getInstance();
		fromcal.setTimeInMillis((long) challan.getTaxPeriodFrom());
		
		String service = fetchContentFromLocalization(requestInfo,challan.getTenantId(),MODULE,formatCodes(challan.getBusinessService()));
		//String service = getMessageTemplate(formatCodes(challan.getBusinessService()),localizationMessage);
		
		Calendar tocal = Calendar.getInstance();
		tocal.setTimeInMillis((long) challan.getTaxPeriodTo());
        message = message.replace("<citizen>",challan.getCitizen().getName());
        message = message.replace("<service>", service);
        message = message.replace("<fromdate>", " "+ fromcal.get(Calendar.DATE) + "/" + fromcal.get(Calendar.MONTH) + "/" + fromcal.get(Calendar.YEAR)+ " ".toUpperCase());
        message = message.replace("<todate>", " "+ tocal.get(Calendar.DATE) + "/" + tocal.get(Calendar.MONTH) + "/" + tocal.get(Calendar.YEAR)+ " ".toUpperCase());

        
        message = message.replace("<duedate>", " "+ cal.get(Calendar.DATE) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR)+ " ".toUpperCase());
        message = message.replace("<amount>", amountToBePaid.toString());
        String UIHost = config.getUiAppHost();
		String paymentPath = config.getPayLinkSMS();
		paymentPath = paymentPath.replace("$consumercode",challan.getChallanNo());
		paymentPath = paymentPath.replace("$tenantId",challan.getTenantId());
		paymentPath = paymentPath.replace("$businessservice",challan.getBusinessService());
		String finalPath = UIHost + paymentPath;
		System.out.println("final path---->"+finalPath);
		message = message.replace("<Link>",getShortenedUrl(finalPath));

        return message;
    }
	
	private String fetchContentFromLocalization(RequestInfo requestInfo, String tenantId, String module, String code) {
		if (config.getIsLocalizationStateLevel())
			tenantId = tenantId.split("\\.")[0];
		String message = null;
		String code1 = null;
		Object result = null;
		String locale = requestInfo.getMsgId().split("[|]")[1]; // Conventionally locale is sent in the first index of msgid split by |
		if(StringUtils.isEmpty(locale))
			locale = NOTIFICATION_LOCALE;
		StringBuilder uri = new StringBuilder();
		uri.append(config.getLocalizationHost()).append(config.getLocalizationContextPath())
		.append(config.getLocalizationSearchEndpoint()).append("?").append("locale=").append(locale)
		.append("&tenantId=").append(tenantId).append("&module=").append(module)
		.append("&codes=").append(code);
		
		Map<String, Object> request = new HashMap<>();
		request.put("RequestInfo", requestInfo);
		try {
			result = restTemplate.postForObject(uri.toString(), request, Map.class);
			System.out.println("result=="+result);
			code1 = JsonPath.read(result, LOCALIZATION_CODES_JSONPATH);
			message = JsonPath.read(result, LOCALIZATION_MSGS_JSONPATH);
		} catch (Exception e) {
			log.error("Exception while fetching from localization: " + e);
		}
		return message;
		
	}
	
	private String formatCodes(String code) {
		String regexForSpecialCharacters = "[$&+,:;=?@#|'<>.^*()%!-]";
		code = code.replaceAll(regexForSpecialCharacters, "_");
		code = code.replaceAll(" ", "_");

		return BUSINESSSERVICELOCALIZATION_CODE_PREFIX + code.toUpperCase();
	}

	
	private String getBillDetails(RequestInfo requestInfo, Challan challan) {

		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(getBillUri(challan),
				new RequestInfoWrapper(requestInfo));
		String jsonString = new JSONObject(responseMap).toString();

		return jsonString;
	}
	
	public String getShortenedUrl(String url){
		HashMap<String,String> body = new HashMap<>();
		body.put("url",url);
		System.out.println("input url :::"+url);
		StringBuilder builder = new StringBuilder(config.getUrlShortnerHost());
		builder.append(config.getUrlShortnerEndpoint());
		String res = restTemplate.postForObject(builder.toString(), body, String.class);
		System.out.println("response from shortneer:::"+res);
		if(StringUtils.isEmpty(res)){
			log.error("URL_SHORTENING_ERROR","Unable to shorten url: "+url); ;
			return url;
		}
		else return res;
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
	private String getMessageTemplate(String notificationCode, String localizationMessage) {
		String path = "$..messages[?(@.code==\"{}\")].message";
		path = path.replace("{}", notificationCode);
		System.out.println("notificationCode=="+notificationCode);
		String message = null;
		try {
			Object messageObj = JsonPath.parse(localizationMessage).read(path);
			message = ((ArrayList<String>) messageObj).get(0);
		} catch (Exception e) {
			log.warn("Fetching from localization failed", e);
		}
		return message;
	}

	/**
	 * Returns the uri for the localization call
	 * 
	 * @param tenantId
	 *            TenantId of the challan
	 * @return The uri for localization search call
	 */
	public StringBuilder getUri(String tenantId, RequestInfo requestInfo) {

		if (config.getIsLocalizationStateLevel())
			tenantId = tenantId.split("\\.")[0];
		
		String locale = NOTIFICATION_LOCALE;
		if (!StringUtils.isEmpty(requestInfo.getMsgId()) && requestInfo.getMsgId().split("|").length >= 2)
			locale = requestInfo.getMsgId().split("\\|")[1];

		StringBuilder uri = new StringBuilder();
		uri.append(config.getLocalizationHost()).append(config.getLocalizationContextPath())
				.append(config.getLocalizationSearchEndpoint()).append("?").append("locale=").append(locale)
				.append("&tenantId=").append(tenantId).append("&module=").append(MODULE)
				.append("&codes=").append(CODES);

		return uri;
	}
	
	private StringBuilder getBillUri(Challan challan) {
		StringBuilder builder = new StringBuilder(config.getBillingHost());
		builder.append(config.getFetchBillEndpoint());
		builder.append("?tenantId=");
		builder.append(challan.getTenantId());
		builder.append("&consumerCode=");
		builder.append(challan.getChallanNo());
		builder.append("&businessService=");
		builder.append(challan.getBusinessService());
		return builder;
	}


	/**
	 * Fetches messages from localization service
	 * 
	 * @param tenantId
	 *            tenantId of the challan
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @return Localization messages for the module
	 */
	public String getLocalizationMessages(String tenantId, RequestInfo requestInfo) {
		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(getUri(tenantId, requestInfo),
				requestInfo);
		String jsonString = new JSONObject(responseMap).toString();
		return jsonString;
	}

	
}
