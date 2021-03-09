package org.egov.wscalculation.consumer;

import java.util.List;

import org.egov.wscalculation.repository.BillGeneratorDao;
import org.egov.wscalculation.service.DemandService;
import org.egov.wscalculation.web.models.BillGeneraterReq;
import org.egov.wscalculation.web.models.BillScheduler.StatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BillGenerationConsumer {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private DemandService demandService;

	@Autowired
	private BillGeneratorDao billGeneratorDao;

	/**
	 * Listen the topic for processing the batch records.
	 * 
	 * @param records
	 *            would be bill generator request.
	 */
	@KafkaListener(topics = {
	"${egov.watercalculatorservice.billgenerate.topic}" }, containerFactory = "kafkaListenerContainerFactoryBatch")
	public void listen(final List<Message<?>> records) {
		log.info("bill generator consumer received records:  " + records.size());

		BillGeneraterReq billGeneraterReq = mapper.convertValue(records.get(0).getPayload(), BillGeneraterReq.class);
		billGeneratorDao.updateBillSchedularStatus(billGeneraterReq.getBillSchedular().getId(), StatusEnum.INPROGRESS);
		
		if(billGeneraterReq.getConsumerCodes() != null && !billGeneraterReq.getConsumerCodes().isEmpty() && billGeneraterReq.getTenantId() != null) {
			Boolean bill = demandService.fetchBillScheduler(billGeneraterReq.getConsumerCodes(),billGeneraterReq.getTenantId() ,billGeneraterReq.getRequestInfoWrapper().getRequestInfo());
			log.info("Is Bill generator completed: {}", bill);
		}
		
		billGeneratorDao.updateBillSchedularStatus(billGeneraterReq.getBillSchedular().getId(), StatusEnum.COMPLETED);

		log.info("Number of batch records:  " + billGeneraterReq.getConsumerCodes().size());
	}

}
