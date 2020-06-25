package org.egov.waterconnection.consumer;

import java.util.HashMap;

import org.egov.waterconnection.service.WorkflowNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WorkflowNotificationConsumer {
	
	@Autowired
	WorkflowNotificationService workflowNotificationService;
	
	/**
	 * Consumes the water connection record and send notification
	 * 
	 * @param record
	 * @param topic
	 */
	@KafkaListener(topics = { "${egov.waterservice.createwaterconnection}",
			"${egov.waterservice.updatewaterconnection}", 
			"${egov.waterservice.updatewaterconnection.workflow.topic}"},
			containerFactory = "kafkaListenerContainerFactory")
	public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		log.info("Consuming record with topic");
		workflowNotificationService.process(record, topic);
	}
}
