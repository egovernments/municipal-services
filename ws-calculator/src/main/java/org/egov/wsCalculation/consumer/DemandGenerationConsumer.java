package org.egov.wsCalculation.consumer;

import java.util.HashMap;
import java.util.List;
import org.egov.wsCalculation.model.CalculationReq;
import org.egov.wsCalculation.service.DemandGenerationService;
import org.egov.wsCalculation.service.WSCalculationService;
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
	WSCalculationService wsCalculationService;
	
	@Autowired
	DemandGenerationService demandGenerationService;

	@Autowired
	private ObjectMapper mapper;
	@KafkaListener(topics = {
			"${egov.watercalculatorservice.createdemand}" }, containerFactory = "kafkaListenerContainerFactory")

		
		public void listen(final List<HashMap<String, Object>> records) {
		CalculationReq calculationreq;
		
		try {
			log.info("Consuming record: " + records);
		} catch (final Exception e) {
			log.error("Error while listening to value: " + records + " on topic: " +  ": " + e);
		}
		
		demandGenerationService.process(records);
	}

	@KafkaListener(topics = {
			"${persister.demand.based.dead.letter.topic.batch}" }, containerFactory = "kafkaListenerContainerFactory")
	public void listenDeadLetterTopic(final List<Message<?>> records) {
	}
	
	
	/**
	* Get CalculationReq and Calculate the Tax Head on Water Charge
	*
	* @param request
	* @return List of calculation.
	*/
//	public List<Calculation> bulkDemandGeneration(CalculationReq request, Map<String, Object> masterMap) {
//	List<Calculation> calculations = getCalculations(request, masterMap);
//	demandService.generateDemand(request.getRequestInfo(), calculations, masterMap);
//	return calculations;
//	}

}
