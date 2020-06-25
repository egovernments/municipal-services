package org.egov.wscalculation.consumer;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

import org.egov.wscalculation.model.DemandNotificationObj;
import org.egov.wscalculation.service.DemandNotificationService;
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
	@KafkaListener(topics = { "${ws.calculator.demand.successful}",
			"${ws.calculator.demand.failed}" },
			containerFactory = "kafkaListenerContainerFactory")
	public void listen(final HashMap<String, Object> request, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		DemandNotificationObj notificationObj = null;
		try {
			log.info("Consuming record: " + request);
			notificationObj = mapper.convertValue(request, DemandNotificationObj.class);
			StringBuilder builder = new StringBuilder();
			builder.append("Demand Notification Object Received: Billing Cycle ")
					.append((notificationObj.getBillingCycle() == null ? "" : notificationObj.getBillingCycle()))
					.append(" Demand Generated Successfully :  ")
					.append(notificationObj.isSuccess() + " Water Connection List :")
					.append((notificationObj.getWaterConnectionIds() == null ? ""
							: notificationObj.getWaterConnectionIds().toString()));
			log.info(builder.toString());
			notificationService.process(notificationObj, topic);
		} catch (final Exception e) {
			log.error("Error while listening to value: " + request + " on topic: " + topic + ": " + e);
		}
	}
}
