package org.egov.waterconnection.consumer;

import java.util.HashMap;

import org.egov.waterconnection.service.MeterReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MeterReadingConsumer {

	@Autowired
	private MeterReadingService meterReadingService;

	/**
	 * Water connection object
	 * 
	 * @param record
	 * @param topic
	 */
	@KafkaListener(topics = { "${ws.meterreading.create}"}, containerFactory = "kafkaListenerContainerFactory")
	public void listen(final HashMap<String, Object> record) {
		meterReadingService.process(record);
	}
}
