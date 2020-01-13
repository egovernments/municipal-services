package org.egov.swCalculation.consumer;

import java.util.HashMap;
import java.util.List;

import org.egov.swCalculation.model.CalculationReq;
import org.egov.swCalculation.service.SWCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DemandGenerationConsumer {

	@Autowired
	SWCalculationService sWCalculationService;

	@Autowired
	ObjectMapper mapper;

	@KafkaListener(topics = {
			"${egov.seweragecalculatorservice.createdemand}" }, containerFactory = "kafkaListenerContainerFactory")
	public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		CalculationReq calculationreq;
		try {
			log.info("Consuming record: " + record);
		} catch (final Exception e) {
			log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
		}
		calculationreq = mapper.convertValue(record, CalculationReq.class);
		sWCalculationService.getCalculation(calculationreq);
	}

	@KafkaListener(topics = {
			"${persister.demand.based.dead.letter.topic.batch}" }, containerFactory = "kafkaListenerContainerFactory")
	public void listenDeadLetterTopic(final List<Message<?>> records) {
	}

}
