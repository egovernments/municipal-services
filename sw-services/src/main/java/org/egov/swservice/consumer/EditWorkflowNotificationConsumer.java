package org.egov.swservice.consumer;

import java.util.HashMap;

import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.service.DiffService;
import org.egov.swservice.service.SewarageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EditWorkflowNotificationConsumer {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private SewarageServiceImpl sewarageServiceImpl;

	@Autowired
	private DiffService diffService;

	/**
	 * Consumes the sewerage connection record and send the edit notification
	 * 
	 * @param record
	 * @param topic
	 */
	@KafkaListener(topics = { "${sw.editnotification.topic}" })
	public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		try {
			SewerageConnectionRequest sewerageConnectionRequest = mapper.convertValue(record,
					SewerageConnectionRequest.class);
			SewerageConnection searchResult = sewarageServiceImpl.getConnectionForUpdateRequest(
					sewerageConnectionRequest.getSewerageConnection().getId(),
					sewerageConnectionRequest.getRequestInfo());
			diffService.checkDifferenceAndSendEditNotification(sewerageConnectionRequest, searchResult);
		} catch (Exception ex) {
			StringBuilder builder = new StringBuilder("Error while listening to value: ").append(record)
					.append("on topic: ").append(topic);
			log.error(builder.toString(), ex);
		}
	}

}
