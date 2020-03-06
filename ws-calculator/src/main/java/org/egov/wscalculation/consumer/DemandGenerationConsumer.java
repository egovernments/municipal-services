package org.egov.wscalculation.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.StrBuilder;
import org.egov.common.contract.request.RequestInfo;
import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.egov.wscalculation.model.CalculationCriteria;
import org.egov.wscalculation.model.CalculationReq;
import org.egov.wscalculation.producer.WSCalculationProducer;
import org.egov.wscalculation.service.MasterDataService;
import org.egov.wscalculation.service.WSCalculationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
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
	
	@Autowired
	private MasterDataService mDataService;
	
	
	/**
	 * Listen the topic for processing the batch records.
	 * 
	 * @param records would be calculation criteria.
	 */
	@KafkaListener(topics = {"${egov.watercalculatorservice.createdemand}" }, containerFactory = "kafkaListenerContainerFactoryBatch")
	@SuppressWarnings("unchecked")
	public void listen(final List<Message<?>> records) {
		List<CalculationCriteria> calculationCriteria = new ArrayList<>();
		CalculationReq calculationReq = mapper.convertValue(records.get(0).getPayload(), CalculationReq.class);
		RequestInfo requestInfo = calculationReq.getRequestInfo();
		Map<String, Object> masterMap = mDataService.loadMasterData(requestInfo, calculationReq.getCalculationCriteria().get(0).getTenantId());
		records.forEach(record -> {
			try {
				CalculationReq calcReq = mapper.convertValue(record.getPayload(),
						CalculationReq.class);
				calculationCriteria.addAll(calcReq.getCalculationCriteria());
				log.info("Consuming record: " + record);
			} catch (final Exception e) {
				StringBuilder builder = new StringBuilder();
				builder.append("Error while listening to value: ").append(record).append(" on topic: ").append(": ").append(e);
				log.error(builder.toString());
			}
		});
		CalculationReq request = CalculationReq.builder().calculationCriteria(calculationCriteria)
				.requestInfo(requestInfo).isconnectionCalculation(true).build();
		generateDemandInBatch(request, masterMap, config.getDeadLetterTopicBatch());
		log.info("Number of batch records:  " + records.size());
	}
	
	

	/**
	 * Listens on the dead letter topic of the bulk request and processes
	 * every record individually and pushes failed records on error topic
	 * @param records failed batch processing
	 */
	@KafkaListener(topics = {"${persister.demand.based.dead.letter.topic.batch}"}, containerFactory = "kafkaListenerContainerFactory")
	@SuppressWarnings("unchecked")
	public void listenDeadLetterTopic(final List<Message<?>> records) {
		
		List<CalculationReq> CalculationReqList = new ArrayList<>();
		CalculationReq calculationReq = mapper.convertValue(records.get(0).getPayload(), CalculationReq.class);
		RequestInfo requestInfo = calculationReq.getRequestInfo();
		Map<String, Object> masterMap = mDataService.loadMasterData(requestInfo, calculationReq.getCalculationCriteria().get(0).getTenantId());
		records.forEach(record -> {
			try {
				CalculationReq calcReq = mapper.convertValue(record.getPayload(),
						CalculationReq.class);
				CalculationReqList.add(calcReq);
				log.info("Consuming record: " + record);
			} catch (final Exception e) {
				log.error("Error while listening to value: " + record + " on topic: " + ": " + e);
			}
			// processing single
			CalculationReqList.forEach(calcReq -> {
				generateDemandInBatch(calcReq, masterMap, config.getDeadLetterTopicSingle());
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
