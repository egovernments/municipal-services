package org.egov.bpa.consumer;

import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

import org.egov.bpa.service.PaymentUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReceiptConsumer {

	private PaymentUpdateService paymentUpdateService;	

    @Autowired
    public ReceiptConsumer(PaymentUpdateService paymentUpdateService) {
        this.paymentUpdateService = paymentUpdateService;
    }

    @KafkaListener(topics = {"${kafka.topics.receipt.create}"})
    public void listenPayments(final HashMap<String, Object> record) {
        paymentUpdateService.process(record);
    }
}
