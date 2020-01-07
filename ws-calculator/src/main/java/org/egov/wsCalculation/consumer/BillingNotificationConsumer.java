package org.egov.wsCalculation.consumer;

import java.util.HashMap;

import org.egov.wsCalculation.model.DemandNotificationObj;
import org.egov.wsCalculation.service.PaymentNotificationService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BillingNotificationConsumer {

	@Autowired
	PaymentNotificationService paymentService;

	/**
	 * 
	 * @param request bill Object
	 * @param topic
	 */
	@KafkaListener(topics = { "${kafka.topics.billgen.topic}" })
	public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		try {
			log.info("Consuming record: " + record);
		} catch (final Exception e) {
			log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
		}
		paymentService.process(record, topic);
	}
}
