package org.egov.wsCalculation.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wsCalculation.config.WSCalculationConfiguration;
import org.egov.wsCalculation.model.CalculationCriteria;
import org.egov.wsCalculation.model.CalculationReq;
import org.egov.wsCalculation.producer.WSCalculationProducer;
import org.egov.wsCalculation.service.WSCalculationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DemandGenerationConsumer {
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private WSCalculationConfiguration config;
	
	@Autowired
	private WSCalculationServiceImpl wSCalculationServiceImpl;
	
	@Autowired
	private WSCalculationProducer producer;
	
	/**
	 * Listen the topic for processing the batch records.
	 * 
	 * @param records would be calculation criteria.
	 */
	@KafkaListener(topics = {"${egov.watercalculatorservice.createdemand}" }, containerFactory = "kafkaListenerContainerFactory")
	@SuppressWarnings("unchecked")
	public void listen(final List<HashMap<String, Object>> records) {
		List<CalculationCriteria> calculationCriteria = new ArrayList<>();
		Map<String, Object> masterMap = (Map<String, Object>) records.get(0).get("masterData");
		RequestInfo requestInfo = mapper.convertValue(records.get(0).get("calculationReq"), CalculationReq.class)
				.getRequestInfo();
		records.forEach(record -> {
			try {
				CalculationReq calcReq = mapper.convertValue(records.get(0).get("calculationReq"),
						CalculationReq.class);
				calculationCriteria.addAll(calcReq.getCalculationCriteria());
				log.info("Consuming record: " + record);
			} catch (final Exception e) {
				log.error("Error while listening to value: " + record + " on topic: " + ": " + e);
			}
		});
		CalculationReq request = CalculationReq.builder().calculationCriteria(calculationCriteria)
				.requestInfo(requestInfo).build();
		generateDemandInBatch(request, masterMap, config.getDeadLetterTopicBatch());
		log.info("Number of batch records:  " + records.size());
	}
	
	

	/**
	 * Listens on the dead letter topic of the bulk request and processes
	 * every record individually and pushes failed records on error topic
	 * @param records failed batch processing
	 */
	@KafkaListener(topics = {"${persister.demand.based.dead.letter.topic.batch}" }, containerFactory = "kafkaListenerContainerFactory")
	@SuppressWarnings("unchecked")
	public void listenDeadLetterTopic(List<HashMap<String, Object>> records) {
		List<CalculationReq> CalculationReqList = new ArrayList<>();
		Map<String, Object> masterMap = (Map<String, Object>) records.get(0).get("masterData");
		records.forEach(record -> {
			try {
				CalculationReq calcReq = mapper.convertValue(records.get(0).get("calculationReq"),
						CalculationReq.class);
				CalculationReqList.add(calcReq);
				log.info("Consuming record: " + record);
			} catch (final Exception e) {
				log.error("Error while listening to value: " + record + " on topic: " + ": " + e);
			}
			// processing single
			CalculationReqList.forEach(calculationReq -> {
				generateDemandInBatch(calculationReq, masterMap, config.getDeadLetterTopicSingle());
			});
		});
	}
	
	/**
	 * Generate demand in bulk on given criteria
	 * 
	 * @param request Calculation request
	 * @param masterMap master data
	 * @param errorTopic error topic
	 */
	private void generateDemandInBatch(CalculationReq request, Map<String, Object> masterMap, String errorTopic) {
		try {
			wSCalculationServiceImpl.bulkDemandGeneration(request, masterMap);
			if (errorTopic.equalsIgnoreCase(config.getDeadLetterTopicBatch()))
				log.info("Batch Processed Successfully: {}", request.getCalculationCriteria());
		} catch (Exception ex) {
			log.error("Demand generation error: " + ex);
			log.info("From Topic: " + errorTopic);
			producer.push(errorTopic, request);
		}

	}
}
