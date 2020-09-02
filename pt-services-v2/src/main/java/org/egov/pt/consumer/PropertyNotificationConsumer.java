package org.egov.pt.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.pt.service.NotificationService;
import org.egov.pt.service.PaymentNotificationService;
import org.egov.pt.web.models.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class PropertyNotificationConsumer {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PaymentNotificationService paymentNotificationService;

    @Value("${update.notification.sms.enabled}")
    private boolean isUpdateSmsEnabled;

    @KafkaListener(topics = {"${persister.update.property.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        ObjectMapper mapper = new ObjectMapper();
        PropertyRequest propertyRequest = new PropertyRequest();
        try {
            log.info("Consuming record: " + record);
            propertyRequest = mapper.convertValue(record, PropertyRequest.class);
        } catch (final Exception e) {
            log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
        }
        log.info("property Received: "+propertyRequest.getProperties().get(0).getPropertyId());
        if(isUpdateSmsEnabled) {
            notificationService.process(propertyRequest,topic);
        }
    }


    @KafkaListener(topics = {"${kafka.topics.notification.payment}","${kafka.topics.notification.pg.save.txns}"})
    public void listenPayments(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        paymentNotificationService.process(record,topic);
    }





}
