package org.egov.swService.consumer;

import java.util.List;

import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.service.WorkflowNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.parser.JSONParser;

@Service
@Slf4j
public class WorkflowNotificationConsumer {

	@Autowired
	WorkflowNotificationService workflowNotificationService;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * 
	 * @param request
	 *            Sewerage Connection Request Object
	 * @param topic
	 * @throws JsonProcessingException
	 */
	@KafkaListener(topics = { "${egov.sewarageservice.createconnection}", "${egov.sewarageservice.updateconnection}","${egov.sewerageservice.updatesewerageconnection.workflow.topic}" })
	public void listen(final List<Message<?>> records) throws JsonProcessingException {
		log.info("SewerageConnection Obj " + mapper.writeValueAsString(records));
		records.forEach(record -> {
			String topic = record.getHeaders().get("kafka_receivedTopic").toString();
			JSONParser parser = new JSONParser();
			try {
				log.info("Consuming record: " + record);
				Object waterConnectionRequestJson = parser.parse((String) record.getPayload());
				SewerageConnectionRequest sewerageConnectionRequest = mapper.convertValue(waterConnectionRequestJson,
						SewerageConnectionRequest.class);
				workflowNotificationService.process(sewerageConnectionRequest, topic);
			} catch (final Exception e) {
				e.printStackTrace();
				log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
			}
		});
	}
}
