package org.egov.echallan.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.egov.echallan.model.ChallanRequest;
import org.egov.echallan.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import java.util.HashMap;



@Slf4j
@Component
public class ChallanConsumer {

    private NotificationService notificationService;


    @Autowired
    public ChallanConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = {"${persister.save.challan.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        ObjectMapper mapper = new ObjectMapper();
        ChallanRequest challanRequest = new ChallanRequest();
        try {
            challanRequest = mapper.convertValue(record, ChallanRequest.class);
        } catch (final Exception e) {
            log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
        }
        notificationService.sendChallanNotification(challanRequest);
    }
}
