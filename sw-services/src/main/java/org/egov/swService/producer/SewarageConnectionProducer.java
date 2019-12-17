package org.egov.swService.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.egov.tracer.kafka.CustomKafkaTemplate;

import lombok.extern.slf4j.Slf4j;


@Service
public class SewarageConnectionProducer {

	@Autowired
	private CustomKafkaTemplate<String, Object> kafkaTemplate;

	public void push(String topic, Object value) {
		kafkaTemplate.send(topic, value);
	}

}
