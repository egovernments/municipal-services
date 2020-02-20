package org.egov.swService.consumer;

import java.util.HashMap;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.service.WorkflowNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkflowNotificationConsumer {

	@Autowired
	WorkflowNotificationService workflowNotificationService;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * Consumes the sewerage connection record and send notification
	 * 
	 * @param record
	 *
	 * @param topic
	 * @throws JsonProcessingException
	 */

	@KafkaListener(topics = { "${egov.sewarageservice.createconnection}", "${egov.sewarageservice.updateconnection}",
			"${egov.sewerageservice.updatesewerageconnection.workflow.topic}" })
	public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		try {
			SewerageConnectionRequest sewerageConnectionRequest = mapper.convertValue(record,
					SewerageConnectionRequest.class);
			workflowNotificationService.process(sewerageConnectionRequest, topic);
		} catch (Exception ex) {
			StringBuilder builder = new StringBuilder("Error while listening to value: ").append(record)
					.append("on topic: ").append(topic).append(" :").append(ex);
			log.error(builder.toString());
		}
	}

}
