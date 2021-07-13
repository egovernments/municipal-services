package org.egov.echallan.expense.consumer;

import java.util.HashMap;

import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.expense.model.ExpenseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExpenseConsumer {


	private ChallanConfiguration config;

	@Autowired
	public ExpenseConsumer(  ChallanConfiguration config) {
		this.config = config;
	}

	@KafkaListener(topics = { "${persister.save.expense.topic}", "${persister.update.expense.topic}" })
	public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			ExpenseRequest expenseRequest = mapper.convertValue(record, ExpenseRequest.class);

		} catch (final Exception e) {
			e.printStackTrace();
			log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
		}
	}
}
