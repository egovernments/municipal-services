package org.egov.wscalculation.consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	 * @param records
	 *            would be calculation criteria.
	 */
	@KafkaListener(topics = {
			"${egov.watercalculatorservice.createdemand}" }, containerFactory = "kafkaListenerContainerFactoryBatch")
	public void listen(final List<Message<?>> records) {
		CalculationReq calculationReq = mapper.convertValue(records.get(0).getPayload(), CalculationReq.class);
		Map<String, Object> masterMap = mDataService.loadMasterData(calculationReq.getRequestInfo(),
				calculationReq.getCalculationCriteria().get(0).getTenantId());
		List<CalculationCriteria> calculationCriteria = new ArrayList<>();
		records.forEach(record -> {
			try {
				CalculationReq calcReq = mapper.convertValue(record.getPayload(), CalculationReq.class);
				calculationCriteria.addAll(calcReq.getCalculationCriteria());
				log.info("Consuming record: " + mapper.writeValueAsString(record));
			} catch (final Exception e) {
				StringBuilder builder = new StringBuilder();
				try {
					builder.append("Error while listening to value: ").append(mapper.writeValueAsString(record))
							.append(" on topic: ").append(e);
				} catch (JsonProcessingException e1) {
					e1.printStackTrace();
				}
				log.error(builder.toString());
			}
		});
		CalculationReq request = CalculationReq.builder().calculationCriteria(calculationCriteria)
				.requestInfo(calculationReq.getRequestInfo()).isconnectionCalculation(true).build();
		generateDemandInBatch(request, masterMap, config.getDeadLetterTopicBatch());
		log.info("Number of batch records:  " + records.size());
	}

	/**
	 * Listens on the dead letter topic of the bulk request and processes every
	 * record individually and pushes failed records on error topic
	 * 
	 * @param records
	 *            failed batch processing
	 */
	@KafkaListener(topics = {
			"${persister.demand.based.dead.letter.topic.batch}" }, containerFactory = "kafkaListenerContainerFactory")
	public void listenDeadLetterTopic(final List<Message<?>> records) {
		CalculationReq calculationReq = mapper.convertValue(records.get(0).getPayload(), CalculationReq.class);
		Map<String, Object> masterMap = mDataService.loadMasterData(calculationReq.getRequestInfo(),
				calculationReq.getCalculationCriteria().get(0).getTenantId());
		records.forEach(record -> {
			try {
				log.info("Consuming record on dead letter topic : " + mapper.writeValueAsString(record));
				CalculationReq calcReq = mapper.convertValue(record.getPayload(), CalculationReq.class);

				calcReq.getCalculationCriteria().forEach(calcCriteria -> {
					CalculationReq request = CalculationReq.builder().calculationCriteria(Arrays.asList(calcCriteria))
							.requestInfo(calculationReq.getRequestInfo()).isconnectionCalculation(true).build();
					try {
						log.info("Generating Demand for Criteria : " + mapper.writeValueAsString(calcCriteria));
						// processing single
						generateDemandInBatch(request, masterMap, config.getDeadLetterTopicSingle());
					} catch (final Exception e) {
						StringBuilder builder = new StringBuilder();
						try {
							builder.append("Error while generating Demand for Criteria: ")
									.append(mapper.writeValueAsString(calcCriteria));
						} catch (JsonProcessingException e1) {
							e1.printStackTrace();
						}
						log.error(builder.toString(), e);
					}
				});
			} catch (final Exception e) {
				StringBuilder builder = new StringBuilder();
				builder.append("Error while listening to value: ").append(record).append(" on dead letter topic.");
				log.error(builder.toString(), e);
			}
		});
	}

	/**
	 * Generate demand in bulk on given criteria
	 * 
	 * @param request
	 *            Calculation request
	 * @param masterMap
	 *            master data
	 * @param errorTopic
	 *            error topic
	 */
	private void generateDemandInBatch(CalculationReq request, Map<String, Object> masterMap, String errorTopic) {
		try {
			wSCalculationServiceImpl.bulkDemandGeneration(request, masterMap);
			String connectionNoStrings = request.getCalculationCriteria().stream()
					.map(criteria -> criteria.getConnectionNo()).collect(Collectors.toSet()).toString();
			StringBuilder str = new StringBuilder("Demand generated Successfully. For records : ")
					.append(connectionNoStrings);
			log.info(str.toString());
		} catch (Exception ex) {
			log.error("Demand generation error: ", ex);
			log.info("From Topic: " + errorTopic);
			producer.push(errorTopic, request);
		}

	}
}
