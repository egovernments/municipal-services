package org.egov.waterconnection.consumer;

import org.egov.waterconnection.service.PaymentUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class PaymentNotificationConsumer {
    @Autowired
    private PaymentUpdateService paymentUpdateService;

    @KafkaListener(topics = {"${kafka.topics.receipt.create}"})
    public void listenPayments(final HashMap<String, Object> record) {
        paymentUpdateService.processRecieptRequest(record);
    }
}
