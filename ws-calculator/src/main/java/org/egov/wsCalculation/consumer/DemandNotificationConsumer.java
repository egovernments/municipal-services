package org.egov.wsCalculation.consumer;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

import org.egov.wsCalculation.model.DemandNotificationObj;
import org.egov.wsCalculation.service.DemandNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class DemandNotificationConsumer {

	@Autowired
	private DemandNotificationService notificationService;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * 
	 * @param request
	 * @param topic
	 */
	@KafkaListener(topics = { "${ws.calculator.demand.successful}", "${ws.calculator.demand.failed}" })
	public void listen(final HashMap<String, Object> request, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		DemandNotificationObj notificationObj = new DemandNotificationObj();
		try {
			log.info("Consuming record: " + request);
			notificationObj = mapper.convertValue(request, DemandNotificationObj.class);
		} catch (final Exception e) {
			log.error("Error while listening to value: " + request + " on topic: " + topic + ": " + e);
		}
		log.info("Demand Notification Object Received: Billing Cycle " + (notificationObj.getBillingCycle() == null ? ""
				: notificationObj.getBillingCycle()) + " Demand Generated Successfully :  " + notificationObj.isSuccess()
						+ " Water Connection List :" + (notificationObj.getWaterConnectionIds() == null ? ""
								: notificationObj.getWaterConnectionIds().toString()));
		notificationService.process(notificationObj, topic);
	}
}
