package org.egov.swservice.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.model.Property;
import org.egov.swservice.model.SearchCriteria;
import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.model.collection.PaymentDetail;
import org.egov.swservice.model.collection.PaymentRequest;
import org.egov.swservice.repository.ServiceRequestRepository;
import org.egov.swservice.repository.SewarageDao;
import org.egov.swservice.util.SWConstants;
import org.egov.swservice.validator.ValidateProperty;
import org.egov.swservice.workflow.WorkflowIntegrator;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentUpdateService {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private SWConfiguration config;

	@Autowired
	private SewarageServiceImpl sewerageService;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	private SewarageDao repo;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
	private ValidateProperty validateProperty;
	
	@Autowired
	private EnrichmentService enrichmentService;

	/**
	 * After payment change the application status
	 * 
	 * @param record
	 *            payment request
	 */
	public void process(HashMap<String, Object> record) {
		try {
			PaymentRequest paymentRequest = mapper.convertValue(record, PaymentRequest.class);
			try {
				log.info("payment Request " + mapper.writeValueAsString(paymentRequest));
			} catch (Exception ex) {
				log.error("Temp Catch Excption:", ex);
			}
			paymentRequest.getRequestInfo().setUserInfo(fetchUser(
					paymentRequest.getRequestInfo().getUserInfo().getUuid(), paymentRequest.getRequestInfo()));
			for (PaymentDetail paymentDetail : paymentRequest.getPayment().getPaymentDetails()) {
				log.info("Consuming Business Service", paymentDetail.getBusinessService());
				if (paymentDetail.getBusinessService().equalsIgnoreCase(config.getReceiptBusinessservice())) {
					SearchCriteria criteria = SearchCriteria.builder()
							.tenantId(paymentRequest.getPayment().getTenantId())
							.applicationNumber(paymentDetail.getBill().getConsumerCode()).build();
					List<SewerageConnection> sewerageConnections = sewerageService.search(criteria,
							paymentRequest.getRequestInfo());
					if (CollectionUtils.isEmpty(sewerageConnections)) {
						throw new CustomException("INVALID_RECEIPT",
								"No sewerageConnection found for the consumerCode " + criteria.getApplicationNumber());
					}
					Optional<SewerageConnection> connections = sewerageConnections.stream().findFirst();
					SewerageConnection connection = connections.get();
					if (sewerageConnections.size() > 1) {
						throw new CustomException("INVALID_RECEIPT",
								"More than one application found on consumerCode " + criteria.getApplicationNumber());
					}
					sewerageConnections
							.forEach(sewerageConnection -> sewerageConnection.getProcessInstance().setAction(SWConstants.ACTION_PAY));
					SewerageConnectionRequest sewerageConnectionRequest = SewerageConnectionRequest.builder()
							.sewerageConnection(connection).requestInfo(paymentRequest.getRequestInfo())
							.build();
					try {
						log.info("", "Sewerage Request " + mapper.writeValueAsString(sewerageConnectionRequest));
					} catch (Exception ex) {
						log.error("", ex);
					}
					
					Property property = validateProperty.getOrValidateProperty(sewerageConnectionRequest);
					wfIntegrator.callWorkFlow(sewerageConnectionRequest, property);
					enrichmentService.enrichFileStoreIds(sewerageConnectionRequest);
					repo.updateSewerageConnection(sewerageConnectionRequest, false);
				}
			}
		} catch (Exception ex) {
			log.error("", ex);
		}
	}

	/**
	 * 
	 * @param uuid
	 * @param requestInfo
	 * @return User
	 */
	private User fetchUser(String uuid, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder();
		uri.append(config.getUserHost()).append(config.getUserSearchEndpoint());
		Map<String, Object> userSearchRequest = new HashMap<>();
		List<String> uuids = Arrays.asList(uuid);
		userSearchRequest.put("RequestInfo", requestInfo);
		userSearchRequest.put("uuid", uuids);
		Object response = serviceRequestRepository.fetchResult(uri, userSearchRequest);
		List<Object> users = new ArrayList<>();
		try {
			log.info("user info response" + mapper.writeValueAsString(response));
			DocumentContext context = JsonPath.parse(mapper.writeValueAsString(response));
			users = context.read("$.user");
		} catch (JsonProcessingException e) {
			log.error("error occured while parsing user info", e);
		}
		if (CollectionUtils.isEmpty(users)) {
			throw new CustomException("INVALID_SEARCH_ON_USER", "No user found on given criteria!!!");
		}
		return mapper.convertValue(users.get(0), User.class);
	}

}
