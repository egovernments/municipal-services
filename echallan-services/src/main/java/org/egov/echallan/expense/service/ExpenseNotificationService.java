package org.egov.echallan.expense.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.expense.model.Expense;
import org.egov.echallan.expense.model.Expense.StatusEnum;
import org.egov.echallan.expense.model.ExpenseRequest;
import org.egov.echallan.expense.model.SMSRequest;
import org.egov.echallan.expense.producer.ExpenseProducer;
import org.egov.echallan.expense.repository.ExpenseServiceRequestRepository;
import org.egov.echallan.expense.util.ExpenseNotificationUtil;
import org.egov.echallan.util.NotificationUtil;
import org.egov.echallan.web.models.user.User;
import org.egov.echallan.web.models.uservevents.Action;
import org.egov.echallan.web.models.uservevents.ActionItem;
import org.egov.echallan.web.models.uservevents.Event;
import org.egov.echallan.web.models.uservevents.EventRequest;
import org.egov.echallan.web.models.uservevents.Recepient;
import org.egov.echallan.web.models.uservevents.Source;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ExpenseNotificationService {
	private ChallanConfiguration config;

	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsUrl;

	private RestTemplate restTemplate;

	private ExpenseNotificationUtil util;

	private ExpenseProducer producer;

	private ExpenseServiceRequestRepository expenseServiceRequestRepository;

	private static final String BUSINESSSERVICE_MDMS_MODULE = "BillingService";
	public static final String BUSINESSSERVICE_MDMS_MASTER = "BusinessService";
	public static final String BUSINESSSERVICE_CODES_FILTER = "$.[?(@.type=='Adhoc')].code";
	public static final String BUSINESSSERVICE_CODES_JSONPATH = "$.MdmsRes.BillingService.BusinessService";
	public static final String USREVENTS_EVENT_TYPE = "SYSTEMGENERATED";
	public static final String USREVENTS_EVENT_NAME = "Challan";
	public static final String USREVENTS_EVENT_POSTEDBY = "SYSTEM-CHALLAN";

	@Autowired
	public ExpenseNotificationService(ChallanConfiguration config, RestTemplate restTemplate,
			ExpenseNotificationUtil util, ExpenseProducer producer,
			ExpenseServiceRequestRepository expenseServiceRequestRepository) {
		this.config = config;
		this.restTemplate = restTemplate;
		this.util = util;
		this.producer = producer;
		this.expenseServiceRequestRepository = expenseServiceRequestRepository;
	}

	public void sendExpenseNotification(ExpenseRequest expenseRequest, boolean isSave) {
		if (null != config.getIsSMSEnabled()) {
			if (config.getIsSMSEnabled()) {
				HashMap<String, String> msgDetail = null;
				String tenantId = expenseRequest.getExpense().getTenantId();
				List<String> businessServiceAllowed = fetchBusinessServiceFromMDMS(expenseRequest.getRequestInfo(),
						tenantId);
				if (!CollectionUtils.isEmpty(businessServiceAllowed)) {
					Expense expense = expenseRequest.getExpense();
					if (businessServiceAllowed.contains(expense.getBusinessService())) {
						String mobilenumber = expense.getCitizen().getMobileNumber();   //TODO
						String[] users = new String[] { expense.getCitizen().getUuid() };
						if (isSave)
							msgDetail = util.getCustomizedMsg(expenseRequest.getRequestInfo(), expense);
						
						if (msgDetail != null && !StringUtils.isEmpty(msgDetail.get(NotificationUtil.MSG_KEY))) {
							SMSRequest smsRequest = SMSRequest.builder().mobileNumber(mobilenumber)
									.message(msgDetail.get(NotificationUtil.MSG_KEY))
									.templateId(msgDetail.get(NotificationUtil.TEMPLATE_KEY)).users(users).build();
							producer.push(config.getSmsNotifTopic(), smsRequest);
						} else {
							log.error("No message configured! Notification will not be sent.");
						}
					} else {
						log.info("Notification not configured for this business service!");
					}

				} else {
					log.info(
							"Business services to which notifs are to be sent, couldn't be retrieved! Notification will not be sent.");
				}
			}

			if (null != config.getIsUserEventEnabled()) {
				if (config.getIsUserEventEnabled()) {
					EventRequest eventRequest = getEventsForExpense(expenseRequest, isSave);
					if (null != eventRequest)
						sendEventNotification(eventRequest);
				}
			}

		}
	}

	private EventRequest getEventsForExpense(ExpenseRequest request, boolean isSave) {
		List<Event> events = new ArrayList<>();
		String tenantId = request.getExpense().getTenantId();
		Expense expense = request.getExpense();
		HashMap<String, String> msgDetail = null;
		if (isSave)
			msgDetail = util.getCustomizedMsg(request.getRequestInfo(), expense);
		else
			return null;
		Map<String, String> mobileNumberToOwner = new HashMap<>();
		String mobile = expense.getCitizen().getMobileNumber();
		if (mobile != null)
			mobileNumberToOwner.put(mobile, expense.getCitizen().getName());

		Map<String, String> mapOfPhnoAndUUIDs = fetchUserUUIDs(mobile, request.getRequestInfo(),
				request.getExpense().getTenantId());
		if (CollectionUtils.isEmpty(mapOfPhnoAndUUIDs.keySet()))
			return null;

		List<String> toUsers = new ArrayList<>();
		toUsers.add(mapOfPhnoAndUUIDs.get(mobile));
		Recepient recepient = Recepient.builder().toUsers(toUsers).toRoles(null).build();
		List<String> payTriggerList = Arrays.asList(config.getPayTriggers().split("[,]"));
		Action action = null;
		if (payTriggerList.contains(expense.getApplicationStatus().toString())) {
			List<ActionItem> items = new ArrayList<>();
			String actionLink = config.getPayLink().replace("$mobile", mobile)
					.replace("$applicationNo", expense.getChallanNo()).replace("$tenantId", expense.getTenantId())
					.replace("$businessService", expense.getBusinessService());
			actionLink = config.getUiAppHost() + actionLink;
			ActionItem item = ActionItem.builder().actionUrl(actionLink).code(config.getPayCode()).build();
			items.add(item);
			action = Action.builder().actionUrls(items).build();
		}
		events.add(Event.builder().tenantId(expense.getTenantId()).description(msgDetail.get(NotificationUtil.MSG_KEY))
				.eventType(USREVENTS_EVENT_TYPE).name(USREVENTS_EVENT_NAME).postedBy(USREVENTS_EVENT_POSTEDBY)
				.source(Source.WEBAPP).recepient(recepient).eventDetails(null).actions(action).build());
		if (!CollectionUtils.isEmpty(events)) {
			return EventRequest.builder().requestInfo(request.getRequestInfo()).events(events).build();
		} else {
			return null;
		}

	}

	private List<String> fetchBusinessServiceFromMDMS(RequestInfo requestInfo, String tenantId) {
		List<String> masterData = new ArrayList<>();
		StringBuilder uri = new StringBuilder();
		uri.append(mdmsHost).append(mdmsUrl);
		if (StringUtils.isEmpty(tenantId))
			return masterData;
		MdmsCriteriaReq request = getRequestForEvents(requestInfo, tenantId.split("\\.")[0]);
		try {
			Object response = restTemplate.postForObject(uri.toString(), request, Map.class);
			masterData = JsonPath.read(response, BUSINESSSERVICE_CODES_JSONPATH);
		} catch (Exception e) {
			log.error("Exception while fetching business service codes: ", e);
		}
		return masterData;
	}

	private MdmsCriteriaReq getRequestForEvents(RequestInfo requestInfo, String tenantId) {
		MasterDetail masterDetail = org.egov.mdms.model.MasterDetail.builder().name(BUSINESSSERVICE_MDMS_MASTER)
				.filter(BUSINESSSERVICE_CODES_FILTER).build();
		List<MasterDetail> masterDetails = new ArrayList<>();
		masterDetails.add(masterDetail);
		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(BUSINESSSERVICE_MDMS_MODULE)
				.masterDetails(masterDetails).build();
		List<ModuleDetail> moduleDetails = new ArrayList<>();
		moduleDetails.add(moduleDetail);
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	private Map<String, String> fetchUserUUIDs(String mobileNumber, RequestInfo requestInfo, String tenantId) {
		Map<String, String> mapOfPhnoAndUUIDs = new HashMap<>();
		StringBuilder uri = new StringBuilder();
		uri.append(config.getUserHost()).append(config.getUserSearchEndpoint());
		Map<String, Object> userSearchRequest = new HashMap<>();
		userSearchRequest.put("RequestInfo", requestInfo);
		userSearchRequest.put("tenantId", tenantId);
		userSearchRequest.put("userType", "CITIZEN");
		userSearchRequest.put("userName", mobileNumber);
		try {
			Object user = expenseServiceRequestRepository.fetchResult(uri, userSearchRequest);
			if (null != user) {
				List<User> users = JsonPath.read(user, "$.user");
				if (users.size() != 0) {
					String uuid = JsonPath.read(user, "$.user[0].uuid");
					mapOfPhnoAndUUIDs.put(mobileNumber, uuid);
				}
			} else {
				log.error("Service returned null while fetching user for username - " + mobileNumber);
			}
		} catch (Exception e) {
			log.error("Exception while fetching user for username - " + mobileNumber);
			log.error("Exception trace: ", e);
		}
		return mapOfPhnoAndUUIDs;
	}

	public void sendEventNotification(EventRequest request) {
		producer.push(config.getSaveUserEventsTopic(), request);
	}

}