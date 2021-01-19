package org.egov.vehicle.consumer;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.vehicle.web.model.VehicleRequest;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class VehicleConsumer {

    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        ObjectMapper mapper = new ObjectMapper();
        VehicleRequest vehicleRequest = new VehicleRequest();

        try {
            log.debug("Consuming record: " + record);
            vehicleRequest = mapper.convertValue(record, VehicleRequest.class);
        } catch (final Exception e) {
            log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
        }
        log.debug("Vehicle Received: " + vehicleRequest.getVehicle().getRegistrationNumber());
    }

}
