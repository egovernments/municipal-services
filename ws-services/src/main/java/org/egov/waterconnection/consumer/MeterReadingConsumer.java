package org.egov.waterconnection.consumer;

import java.util.HashMap;

import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.egov.waterconnection.service.MeterReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MeterReadingConsumer {

	@Autowired
	private MeterReadingService meterReadingService;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * Water connection object
	 * 
	 * @param record
	 * @param topic
	 */
	@KafkaListener(topics = { "${ws.meterreading.create}" })
	public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		try {
			log.info("Received request to add Meter Reading on topic - " + topic);
			WaterConnectionRequest waterConnectionRequest = mapper.convertValue(record, WaterConnectionRequest.class);
			if (!StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getConnectionType())
					&& WCConstants.METERED_CONNECTION
							.equalsIgnoreCase(waterConnectionRequest.getWaterConnection().getConnectionType())) {
				meterReadingService.process(waterConnectionRequest, topic);
			}
		} catch (Exception ex) {
			StringBuilder builder = new StringBuilder("Error while listening to value: ").append(record)
					.append("on topic: ").append(topic);
			log.error(builder.toString(), ex);
		}
	}
}
