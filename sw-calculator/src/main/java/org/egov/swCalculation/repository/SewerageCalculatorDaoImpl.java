package org.egov.swCalculation.repository;

import java.util.ArrayList;
import java.util.List;

import org.egov.swCalculation.repository.builder.sWCalculatorQueryBuilder;
import org.egov.swCalculation.rowMapper.DemandSchedulerRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SewerageCalculatorDaoImpl implements SewerageCalculatorDao {
	
	@Autowired
	sWCalculatorQueryBuilder queryBuilder;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	DemandSchedulerRowMapper demandSchedulerRowMapper;

	@Override
	public List<String> getTenantId() {
		ArrayList<String> tenentIds = new ArrayList<>();
		String query = queryBuilder.getDistinctTenantIds();
		log.info("Tenant Id's List Query : "+query);
		tenentIds = (ArrayList<String>) jdbcTemplate.queryForList(query, String.class);
		return tenentIds;
	}

	@Override
	public List<String> getConnectionsNoList(String tenantId, String connectionType) {
		List<String> connectionNosList = new ArrayList<>();
		List<Object> preparedStatement = new ArrayList<>();
		String query = queryBuilder.getConnectionNumberList(tenantId, connectionType, preparedStatement);
		log.info("sewerage " + connectionType + " connection list : " + query);
		connectionNosList = jdbcTemplate.query(query, preparedStatement.toArray(), demandSchedulerRowMapper);
		return connectionNosList;
	}

}
