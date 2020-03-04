package org.egov.waterConnection.service;

import java.util.HashMap;
import java.util.List;

import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.config.WSConfiguration;
import org.egov.waterConnection.constants.WCConstants;
import org.egov.waterConnection.model.SearchCriteria;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.collection.PaymentDetail;
import org.egov.waterConnection.model.collection.PaymentRequest;
import org.egov.waterConnection.repository.WaterDao;
import org.egov.waterConnection.workflow.WorkflowIntegrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentUpdateService {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WSConfiguration config;

	@Autowired
	private WaterServiceImpl waterService;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	private WaterDao repo;

	/**
	 * After payment change the application status
	 * 
	 * @param record
	 *            payment request
	 */
	public void process(HashMap<String, Object> record) {
		try {
			PaymentRequest paymentRequest = mapper.convertValue(record, PaymentRequest.class);
			for (PaymentDetail paymentDetail : paymentRequest.getPayment().getPaymentDetails()) {
				log.info("Consuming Business Service" + paymentDetail.getBusinessService());
				if (paymentDetail.getBusinessService().equalsIgnoreCase(config.getReceiptBusinessservice())) {
					SearchCriteria criteria = SearchCriteria.builder()
							.tenantId(paymentRequest.getPayment().getTenantId())
							.applicationNumber(paymentDetail.getBill().getConsumerCode()).build();
					List<WaterConnection> waterConnections = waterService.search(criteria,
							paymentRequest.getRequestInfo());
					if (CollectionUtils.isEmpty(waterConnections)) {
						throw new CustomException("INVALID_RECEIPT",
								"No waterConnection found for the consumerCode " + criteria.getApplicationNumber());
					}
					if (waterConnections.size() > 1) {
						throw new CustomException("INVALID_RECEIPT",
								"More than one application found on consumerCode " + criteria.getApplicationNumber());
					}
					waterConnections.forEach(waterConnection -> waterConnection.setAction(WCConstants.ACTION_PAY));
					WaterConnectionRequest waterConnectionRequest = WaterConnectionRequest.builder()
							.waterConnection(waterConnections.get(0)).requestInfo(paymentRequest.getRequestInfo())
							.build();
					try {
						log.info("WaterConnection Request " + mapper.writeValueAsString(waterConnectionRequest));
					} catch (Exception ex) {
						log.error("Temp Catch Excption:", ex);
					}
					wfIntegrator.callWorkFlow(waterConnectionRequest);
					log.info("Water connection application status: "
							+ waterConnectionRequest.getWaterConnection().getApplicationStatus());
					repo.updateWaterConnection(waterConnectionRequest, false);
				}
			}
		} catch (Exception ex) {
			log.error("Failed to process payment topic message. Exception: ", ex);
		}
	}

}
