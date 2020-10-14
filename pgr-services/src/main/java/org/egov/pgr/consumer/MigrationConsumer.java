package org.egov.pgr.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.pgr.service.MigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class MigrationConsumer {


    @Autowired
    private MigrationService migrationService;

    @Autowired
    private ObjectMapper mapper;


    @KafkaListener(topics = { "${pgr.kafka.migration.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        try {

        }
        catch (Exception e){

        }

    }

}
