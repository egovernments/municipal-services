package org.egov.echallancalculation.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.echallancalculation.config.ChallanConfiguration;
import org.egov.echallancalculation.model.ChallanRequest;
import org.egov.echallancalculation.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;



@Slf4j
@Repository
public class ChallanRepository {

    private JdbcTemplate jdbcTemplate;

    private Producer producer;
    
    private ChallanConfiguration config;




    @Autowired
    public ChallanRepository(JdbcTemplate jdbcTemplate,Producer producer, ChallanConfiguration config) {
        this.jdbcTemplate = jdbcTemplate;
        this.producer = producer;
        this.config = config;
    }



    /**
     * Pushes the request on save topic
     *
     * @param ChallanRequest The challan create request
     */
    public void save(ChallanRequest tradeLicenseRequest) {
        producer.push(config.getSaveChallanTopic(), tradeLicenseRequest);
    }
    
}
