package org.egov.echallan.expense.util;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;

import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.expense.model.Expense;
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
public class ExpenseNotificationUtil {
	public static final String NOTIFICATION_LOCALE = "en_IN";
	public static final String MODULE ="rainmaker-uc";
	private static final String CODES = "echallan.create.sms";
	public static final String BILL_AMOUNT_JSONPATH = "$.Bill[0].totalAmount";
	public static final String BILL_DUEDATE = "$.Bill[0].billDetails[0].expiryDate";
	public static final String BUSINESSSERVICELOCALIZATION_CODE_PREFIX = "BILLINGSERVICE_BUSINESSSERVICE_";
	public static final String LOCALIZATION_CODES_JSONPATH = "$.messages[0].code";
	public static final String LOCALIZATION_MSGS_JSONPATH = "$.messages[0].message";
	public static final String LOCALIZATION_TEMPLATEID_JSONPATH = "$.messages[0].templateId";
	public static final String MSG_KEY="message";
	public static final String TEMPLATE_KEY="templateId";
	private static final String CREATE_CODE = "echallan.create.sms";
	private static final String UPDATE_CODE = "echallan.update.sms";
	private static final String CANCEL_CODE = "echallan.cancel.sms";
	private ChallanConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private RestTemplate restTemplate;
	
	private static final String EXPENSE_CREATE_CODE = "expense.create.sms";

	@Autowired
	public ExpenseNotificationUtil(ChallanConfiguration config, ServiceRequestRepository serviceRequestRepository,
			RestTemplate restTemplate) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.restTemplate = restTemplate;
	}


	public HashMap<String, String> getCustomizedMsg(RequestInfo requestInfo, Expense expense ) {
		HashMap<String, String> msgDetail  = fetchContentFromLocalization(requestInfo,expense.getTenantId(),MODULE,CREATE_CODE);
		msgDetail.put(MSG_KEY, getCreateMsg(requestInfo,expense,msgDetail.get(MSG_KEY)));
		return msgDetail;
	}
	


	private String getCancelMsg(RequestInfo requestInfo,Expense challan, String message) {
		 HashMap<String, String> businessMsg  =  fetchContentFromLocalization(requestInfo,challan.getTenantId(),MODULE,formatCodes(challan.getBusinessService()));
		 message = message.replace("<citizen>",challan.getCitizen().getName());
	     message = message.replace("<challanno>", challan.getChallanNo());
	     message = message.replace("<service>", businessMsg.get(MSG_KEY));
	     return message;
	}
	
	private String getCreateMsg(RequestInfo requestInfo,Expense expense, String message) {
		message =" <citizen>  <challanno> <service> <amount>";
		HashMap<String, String> businessMsg  =  fetchContentFromLocalization(requestInfo,expense.getTenantId(),MODULE,formatCodes(expense.getBusinessService()));
        message = message.replace("<citizen>",expense.getCitizen().getName());
        message = message.replace("<challanno>", expense.getChallanNo());
        message = message.replace("<service>", businessMsg.get(MSG_KEY));
        message = message.replace("<amount>", expense.getTotalAmount().toString());
        String UIHost = config.getUiAppHost();
		String paymentPath = config.getPayLinkSMS();
		paymentPath = paymentPath.replace("$consumercode",expense.getChallanNo());
		paymentPath = paymentPath.replace("$tenantId",expense.getTenantId());
		paymentPath = paymentPath.replace("$businessservice",expense.getBusinessService());
		String finalPath = UIHost + paymentPath;
        return message;
    }
	
	private HashMap<String, String> fetchContentFromLocalization(RequestInfo requestInfo, String tenantId, String module, String code) {
		if (config.getIsLocalizationStateLevel())
			tenantId = tenantId.split("\\.")[0];
		String message = null;
		String templateId = null;
		HashMap<String, String> msgDetail = new HashMap<String, String>();
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
			Configuration suppressExceptionConfiguration = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
		    ReadContext jsonData = JsonPath.using(suppressExceptionConfiguration).parse(result);

			
			templateId = jsonData.read( LOCALIZATION_TEMPLATEID_JSONPATH);
			message = jsonData.read( LOCALIZATION_MSGS_JSONPATH);
		 
			msgDetail.put(MSG_KEY,message);
			msgDetail.put(TEMPLATE_KEY,templateId);
		} catch (Exception e) {
			log.error("Exception while fetching from localization: ", e);
		}
		return msgDetail;
		
	}
	
	private String formatCodes(String code) {
		String regexForSpecialCharacters = "[$&+,:;=?@#|'<>.^*()%!-]";
		code = code.replaceAll(regexForSpecialCharacters, "_");
		code = code.replaceAll(" ", "_");

		return BUSINESSSERVICELOCALIZATION_CODE_PREFIX + code.toUpperCase();
	}

	
	private String getBillDetails(RequestInfo requestInfo, Expense expensee) {

		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(getBillUri(expensee),
				new RequestInfoWrapper(requestInfo));
		
		String jsonString = new JSONObject(responseMap).toString();

		return jsonString;
	}
	
	public String getShortenedUrl(String url){
		HashMap<String,String> body = new HashMap<>();
		body.put("url",url);
		StringBuilder builder = new StringBuilder(config.getUrlShortnerHost());
		builder.append(config.getUrlShortnerEndpoint());
		String res = restTemplate.postForObject(builder.toString(), body, String.class);
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
	
	private StringBuilder getBillUri(Expense expense) {
		StringBuilder builder = new StringBuilder(config.getBillingHost());
		builder.append(config.getFetchBillEndpoint());
		builder.append("?tenantId=");
		builder.append(expense.getTenantId());
		builder.append("&consumerCode=");
		builder.append(expense.getChallanNo());
		builder.append("&businessService=");
		builder.append(expense.getBusinessService());
		return builder;
	}
	
}
