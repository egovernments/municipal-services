package org.egov.vehiclelog.repository;

import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.producer.Producer;
import org.egov.fsm.repository.querybuilder.FSMQueryBuilder;
import org.egov.fsm.web.model.FSMRequest;
import org.egov.vehiclelog.web.model.VehicleLogRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VehicleLogRepository {
	
	@Autowired
	private Producer producer;
	
	@Autowired
	private FSMConfiguration config;
    
    @Autowired
	private JdbcTemplate jdbcTemplate;
	
	public void save(VehicleLogRequest request) {
		producer.push(config.getSaveVehicleLogTopic(), request);
	}
	
	public Integer getFSMApplicationCount(String query) {
		return jdbcTemplate.queryForObject(query, Integer.class);
	}

}
