package org.egov.swcalculation.repository;

import java.util.ArrayList;
import java.util.List;

import org.egov.swcalculation.constants.SWCalculationConstant;
import org.egov.swcalculation.repository.builder.SWCalculatorQueryBuilder;
import org.egov.swcalculation.repository.rowMapper.DemandSchedulerRowMapper;
import org.egov.swcalculation.web.models.SewerageDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SewerageCalculatorDaoImpl implements SewerageCalculatorDao {
	
	@Autowired
	SWCalculatorQueryBuilder queryBuilder;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	DemandSchedulerRowMapper demandSchedulerRowMapper;

	@Override
	public List<String> getTenantId() {
		String query = queryBuilder.getDistinctTenantIds();
		log.info("Tenant Id's List Query : "+query);
		return (ArrayList<String>) jdbcTemplate.queryForList(query, String.class);
	}

	@Override
	public List<SewerageDetails> getConnectionsNoList(String tenantId, String connectionType, Long taxPeriodFrom, Long taxPeriodTo, String cone ) {
		List<Object> preparedStatement = new ArrayList<>();
		String query = queryBuilder.getConnectionNumberList(tenantId, connectionType,SWCalculationConstant.ACTIVE, taxPeriodFrom, taxPeriodTo, cone,   preparedStatement);
		StringBuilder builder = new StringBuilder();
		builder.append("preparedStatement:").append(preparedStatement).append("sewerage: ").append(connectionType).append(" connection list : ").append(query);
		log.info(builder.toString());
		return jdbcTemplate.query(query, preparedStatement.toArray(), demandSchedulerRowMapper);
	}
	
	@Override
	public List<String> getConnectionsNoByLocality(String tenantId, String connectionType, String locality) {
		List<Object> preparedStatement = new ArrayList<>();
		String query = queryBuilder.getConnectionsNoByLocality(tenantId, connectionType,SWCalculationConstant.ACTIVE, locality, preparedStatement);
		log.info("Sewerage " + connectionType + " connection list : " + query);
		return jdbcTemplate.queryForList(query, preparedStatement.toArray(), String.class);
	}

	@Override
	public Long searchLastDemandGenFromDate(String consumerCode, String tenantId) {
		List<Object> preparedStatement = new ArrayList<>();
		String query = queryBuilder.searchLastDemandGenFromDate(consumerCode, tenantId, preparedStatement);
		log.info("preparedStatement: "+ preparedStatement + " searchLastDemandGenFromDate Query : " + query);
		List<Long> fromDate = jdbcTemplate.queryForList(query, preparedStatement.toArray(), Long.class);
		if(fromDate != null && !fromDate.isEmpty())
			return  fromDate.get(0);
		
		return null;
	}
	
	@Override
	public Boolean isConnectionDemandAvailableForBillingCycle(String tenantId, Long taxPeriodFrom, Long taxPeriodTo,
			String consumerCode) {
		List<Object> preparedStatement = new ArrayList<>();
		String query = queryBuilder.isConnectionDemandAvailableForBillingCycle(tenantId, taxPeriodFrom, taxPeriodTo, consumerCode, preparedStatement);
		log.info("isConnectionDemandAvailableForBillingCycle Query: " + query + " preparedStatement: "+ preparedStatement);
		
		return jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Boolean.class);
	}

}
