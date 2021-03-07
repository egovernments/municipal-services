
package org.egov.pgr.consumer;

import java.util.HashMap;

import org.egov.pgr.service.NotificationService;
import org.egov.pgr.web.models.ServiceRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationConsumer {
    @Autowired
    NotificationService notificationService;

    @Autowired
    private ObjectMapper mapper;


/**
     * Consumes the water connection record and send notification
     *
     * @param record
     * @param topic
     */

    @KafkaListener(topics = { "${pgr.kafka.create.topic}" ,"${pgr.kafka.update.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            ServiceRequest request = mapper.convertValue(record, ServiceRequest.class);

            notificationService.process(request, topic);
        } catch (CustomException ex) {
            StringBuilder builder = new StringBuilder("Error while listening to value: ").append(record)
                    .append("on topic: ").append(topic);
            log.error(builder.toString(), ex);
        }
    }
}

