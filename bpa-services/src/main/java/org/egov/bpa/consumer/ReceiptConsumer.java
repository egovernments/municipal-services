package org.egov.bpa.consumer;

import java.util.HashMap;

import org.egov.bpa.service.PaymentUpdateService;
import org.egov.bpa.service.notification.PaymentNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReceiptConsumer {

	private PaymentUpdateService paymentUpdateService;

    private PaymentNotificationService paymentNotificationService;


    @Autowired
    public ReceiptConsumer(PaymentUpdateService paymentUpdateService, PaymentNotificationService paymentNotificationService) {
        this.paymentUpdateService = paymentUpdateService;
        this.paymentNotificationService = paymentNotificationService;
    }

    @KafkaListener(topics = {"${kafka.topics.receipt.create}"})
    public void listenPayments(final HashMap<String, Object> record) {
        paymentUpdateService.process(record);
//        paymentNotificationService.process(record); // TODO Notification
    }
}
