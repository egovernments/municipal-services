package org.egov.swCalculation.consumer;

import java.util.HashMap;

import org.egov.swCalculation.config.SWCalculationConfiguration;
import org.egov.swCalculation.model.DemandNotificationObj;
import org.egov.swCalculation.model.SewerageConnectionRequest;
import org.egov.swCalculation.service.SewerageDemandNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DemandNotificationConsumer {

	private SewerageDemandNotificationService notificationService;

	@Autowired
	SWCalculationConfiguration sWCalculationConfiguration;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	public DemandNotificationConsumer(SewerageDemandNotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@KafkaListener(topics = { "${sw.calculator.demand.successful}", "${sw.calculator.demand.failed}" })
	public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		DemandNotificationObj demandNotificationObj = new DemandNotificationObj();
		try {
			log.info("Consuming record: " + record);
			demandNotificationObj = mapper.convertValue(record, DemandNotificationObj.class);
		} catch (final Exception e) {
			log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
		}
		log.info("Demand Notification Object Received: Billing Cycle "
				+ (demandNotificationObj.getBillingCycle() == null ? "" : demandNotificationObj.getBillingCycle())
				+ " Demand Generated Successfully :  " + demandNotificationObj.isSuccess() + " Sewerage Connection List :"
				+ (demandNotificationObj.getSewerageConnetionIds() == null ? ""
						: demandNotificationObj.getSewerageConnetionIds().toString()));
		notificationService.process(demandNotificationObj, topic);
	}

}
