package org.egov.waterConnection.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RequestHeader;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.support.KafkaHeaders;

import org.springframework.stereotype.Component;



@Slf4j
@Component
@Getter
@NoArgsConstructor
@Setter
@Builder
public class WaterConsumer {


	
    @KafkaListener(topics = "RandomChecker",group="${spring.kafka.consumer.group-id}")
    public void listen(final String record, @RequestHeader(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    	
    	log.info("inside kafka !!"+record);

    }
}
