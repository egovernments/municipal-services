package org.egov.vehiclelog.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.producer.Producer;
import org.egov.fsm.repository.querybuilder.FSMQueryBuilder;
import org.egov.fsm.repository.rowmapper.FSMRowMapper;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.FSMRequest;
import org.egov.fsm.web.model.FSMSearchCriteria;
import org.egov.vehiclelog.config.VehicleLogConfiguration;
import org.egov.vehiclelog.querybuilder.VehicleLogQueryBuilder;
import org.egov.vehiclelog.repository.rowmapper.VehicleLogRowMapper;
import org.egov.vehiclelog.web.model.VehicleLog;
import org.egov.vehiclelog.web.model.VehicleLogRequest;
import org.egov.vehiclelog.web.model.VehicleLogSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VehicleLogRepository {
	
	@Autowired
	private Producer producer;
	
	@Autowired
	private VehicleLogConfiguration config;
    
    @Autowired
	private JdbcTemplate jdbcTemplate;
    
    @Autowired
   	private VehicleLogRowMapper mapper;
    
    @Autowired
    private VehicleLogQueryBuilder queryBuilder;
	
	public void save(VehicleLogRequest request) {
		producer.push(config.getSaveVehicleLogTopic(), request);
	}
	
	public void update(VehicleLogRequest request) {
		producer.push(config.getUpdateVehicleLogTopic(), request);
	}
	
	public Integer getDataCount(String query) {
		Integer count = null;
		try {
			count = jdbcTemplate.queryForObject(query, Integer.class);
		} catch (Exception e) {
			throw e;
		}
		return count;
	}
	
	public List<VehicleLog> getVehicleLogData(VehicleLogSearchCriteria criteria) {
		List<VehicleLog> vehicleLogs = null;
		String query = queryBuilder.getVehicleLogSearchQuery(criteria);
		try {
			vehicleLogs = jdbcTemplate.query(query, mapper);
		} catch (Exception e) {
			throw e;
		}
		return vehicleLogs;
	}

}
