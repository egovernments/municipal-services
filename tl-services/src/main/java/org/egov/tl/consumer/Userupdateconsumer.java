package org.egov.tl.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tl.service.UserService;
import org.egov.tl.web.models.TradeLicenseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class Userupdateconsumer {

    @Autowired
    UserService userService;

    @KafkaListener(topics = {"${kafka.topics.user.update}"})
    public void listenCreateUsers(final HashMap<String, Object> record) {
        ObjectMapper mapper = new ObjectMapper();
        TradeLicenseRequest tradeLicenseRequest = new TradeLicenseRequest();
        try {
            tradeLicenseRequest = mapper.convertValue(record, TradeLicenseRequest.class);
            userService.createUser(tradeLicenseRequest, true);
        } catch (final Exception e) {
            log.error("Error occurred while adding roles for BPA user " + e);
        }
    }
}
