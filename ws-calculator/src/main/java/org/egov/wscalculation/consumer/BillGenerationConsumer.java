package org.egov.wscalculation.consumer;

import java.util.List;

import org.egov.wscalculation.repository.BillGeneratorDao;
import org.egov.wscalculation.service.DemandService;
import org.egov.wscalculation.service.WSCalculationServiceImpl;
import org.egov.wscalculation.web.models.BillGeneraterReq;
import org.egov.wscalculation.web.models.BillScheduler.StatusEnum;
import org.egov.wscalculation.web.models.BillV2;
import org.egov.wscalculation.web.models.Demand;
import org.egov.wscalculation.web.models.GetBillCriteria;
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
	private WSCalculationServiceImpl wSCalculationServiceImpl;	

	@Autowired
	private DemandService demandService;
	
	@Autowired
	private BillGeneratorDao billGeneratorDao;
	
	/**
	 * Listen the topic for processing the batch records.
	 * 
	 * @param records
	 *            would be calculation criteria.
	 */
	@KafkaListener(topics = {
			"${egov.watercalculatorservice.billgenerate.topic}" }, containerFactory = "kafkaListenerContainerFactoryBatch")
	public void listen(final List<Message<?>> records) {
		
		BillGeneraterReq billGeneraterReq = mapper.convertValue(records.get(0).getPayload(), BillGeneraterReq.class);
		billGeneratorDao.updateBillSchedularStatus(billGeneraterReq.getBillSchedular().getId(), StatusEnum.INPROGRESS);
		
		BillV2 bill = demandService.fetchBillScheduler(billGeneraterReq.getDemands(),billGeneraterReq.getTaxPeriodFrom(), billGeneraterReq.getTaxPeriodTo(), billGeneraterReq.getRequestInfoWrapper().getRequestInfo());
		log.info("Bill Object: {}", bill);
		GetBillCriteria getBillCriteria = GetBillCriteria.builder()
				                              .billId(bill.getId())
				                              .tenantId(bill.getTenantId())
				                              .connectionNumber(billGeneraterReq.getConsumerCode()).build();
		List<Demand> demands = demandService.updateDemands(getBillCriteria, billGeneraterReq.getRequestInfoWrapper());
		demandService.fetchBillScheduler(demands,billGeneraterReq.getTaxPeriodFrom(), billGeneraterReq.getTaxPeriodTo(), billGeneraterReq.getRequestInfoWrapper().getRequestInfo());

		billGeneratorDao.updateBillSchedularStatus(billGeneraterReq.getBillSchedular().getId(), StatusEnum.COMPLETED);

		log.info("Number of batch records:  " + records.size());
	}

}
