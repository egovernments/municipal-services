package org.egov.swService.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.swService.config.SWConfiguration;
import org.egov.swService.model.SearchCriteria;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.model.collection.PaymentDetail;
import org.egov.swService.model.collection.PaymentRequest;
import org.egov.swService.repository.ServiceRequestRepository;
import org.egov.swService.repository.SewarageDao;
import org.egov.swService.util.SWConstants;
import org.egov.swService.workflow.WorkflowIntegrator;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	/**
	 * After payment change the application status
	 * 
	 * @param record
	 *            payment request
	 */
	public void process(HashMap<String, Object> record) {
		try {
			PaymentRequest paymentRequest = mapper.convertValue(record, PaymentRequest.class);
//			paymentRequest.getRequestInfo().setUserInfo(fetchUser(
//					paymentRequest.getRequestInfo().getUserInfo().getUuid(), paymentRequest.getRequestInfo()));
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
					if (sewerageConnections.size() > 1) {
						throw new CustomException("INVALID_RECEIPT",
								"More than one application found on consumerCode " + criteria.getApplicationNumber());
					}
					sewerageConnections
							.forEach(sewerageConnection -> sewerageConnection.setAction(SWConstants.ACTION_PAY));
					SewerageConnectionRequest sewerageConnectionRequest = SewerageConnectionRequest.builder()
							.sewerageConnection(sewerageConnections.get(0)).requestInfo(paymentRequest.getRequestInfo())
							.build();
					try {
						log.info("", "Sewerage Request " + mapper.writeValueAsString(sewerageConnectionRequest));
					} catch (Exception ex) {
						log.error("", ex);
					}
					wfIntegrator.callWorkFlow(sewerageConnectionRequest);
					log.info("Sewerage connection application status: "
							+ sewerageConnectionRequest.getSewerageConnection().getApplicationStatus());
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
		try {
			log.info("user info response" + mapper.writeValueAsString(response));
		} catch (JsonProcessingException e) {
			log.error("error occured while parsing user info", e);
		}
		return mapper.convertValue(response, User.class);
	}

}
