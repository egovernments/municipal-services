package org.egov.waterConnection.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;

import org.springframework.stereotype.Component;

import java.util.HashMap;


@Slf4j
@Component
@Getter
@NoArgsConstructor
@Setter
@Builder
public class WaterConsumer {


	
    @KafkaListener(topics = "shriya",group="${spring.kafka.consumer.group-id}")
    public void listen(final String record, @RequestHeader(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    	
    	log.info("inside kafka !!"+record);

    }
}
