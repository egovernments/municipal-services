package org.egov.wscalculation.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.wscalculation.builder.WSCalculatorQueryBuilder;
import org.egov.wscalculation.model.MeterConnectionRequest;
import org.egov.wscalculation.model.MeterReading;
import org.egov.wscalculation.model.MeterReadingSearchCriteria;
import org.egov.wscalculation.producer.WSCalculationProducer;
import org.egov.wscalculation.rowmapper.DemandSchedulerRowMapper;
import org.egov.wscalculation.rowmapper.MeterReadingCurrentReadingRowMapper;
import org.egov.wscalculation.rowmapper.MeterReadingRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class WSCalculationDaoImpl implements WSCalculationDao {

	@Autowired
	private WSCalculationProducer wSCalculationProducer;

	@Autowired
	private WSCalculatorQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private MeterReadingRowMapper meterReadingRowMapper;
	
	@Autowired
	private MeterReadingCurrentReadingRowMapper currentMeterReadingRowMapper;
	
	@Autowired
	private DemandSchedulerRowMapper demandSchedulerRowMapper;
	

	@Value("${egov.meterservice.createmeterconnection}")
	private String createMeterConnection;

	/**
	 * 
	 * @param meterConnectionRequest
	 *            MeterConnectionRequest contains meter reading connection to be
	 *            created
	 * @return List of MeterReading to be pushed to kafka queue after create and
	 *         returning list of water connection
	 */
	@Override
	public void savemeterReading(MeterConnectionRequest meterConnectionRequest) {
		wSCalculationProducer.push(createMeterConnection, meterConnectionRequest);
	}
	/**
	 * 
	 * @param criteria would be meter reading criteria
	 * @return List of meter readings based on criteria
	 */
	@Override
	public List<MeterReading> searchMeterReadings(MeterReadingSearchCriteria criteria) {
		List<Object> preparedStatement = new ArrayList<>();
		String query = queryBuilder.getSearchQueryString(criteria, preparedStatement);
		if(query == null)
			Collections.emptyList();
		log.info("Query: " + query);
		log.info("Prepared Statement" + preparedStatement.toString());
		return jdbcTemplate.query(query, preparedStatement.toArray(), meterReadingRowMapper);
	}
	
	@Override
	public List<MeterReading> searchCurrentMeterReadings(MeterReadingSearchCriteria criteria) {
		List<Object> preparedStatement = new ArrayList<>();
		String query = queryBuilder.getCurrentReadingConnectionQuery(criteria, preparedStatement);
		if (query == null)
			return Collections.emptyList();
		log.info("Query: " + query);
		log.info("Prepared Statement" + preparedStatement.toString());
		return jdbcTemplate.query(query, preparedStatement.toArray(), currentMeterReadingRowMapper);
	}

	/**
	 * 
	 * @param List
	 *            of string of connection ids on which search is performed
	 * @return total number of meter reading objects if present in the table for
	 *         that particular connection ids
	 */

	@Override
	public int isMeterReadingConnectionExist(List<String> ids) {
		Set<String> connectionIds = new HashSet<>(ids);
		List<Object> preparedStatement = new ArrayList<>();
		String query = queryBuilder.getNoOfMeterReadingConnectionQuery(connectionIds, preparedStatement);
		log.info("Query: " + query);
		return jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
	}
	
	@Override
	public ArrayList<String> searchTenentIds() {
		ArrayList<String> tenentIds = new ArrayList<>();
		String query = queryBuilder.getTenentIdConnectionQuery();
		if (query == null)
			return tenentIds;
		log.info("Query: " + query);
		tenentIds = (ArrayList<String>) jdbcTemplate.queryForList(query, String.class);
		return tenentIds;
	}
	
	@Override
	public ArrayList<String> searchConnectionNos(String connectionType,String tenentId) {
		ArrayList<String> connectionNos = new ArrayList<>();
		List<Object> preparedStatement = new ArrayList<>();
		String query = queryBuilder.getConnectionNumberFromWaterServicesQuery(preparedStatement,connectionType,tenentId);
		if (query == null)
			return connectionNos;
		log.info("Query: " + query);

		connectionNos = (ArrayList<String>)jdbcTemplate.query(query,preparedStatement.toArray(),demandSchedulerRowMapper);
		return connectionNos;
	}
	
	@Override
	public List<String> getConnectionsNoList(String tenantId, String connectionType) {
		List<Object> preparedStatement = new ArrayList<>();
		String query = queryBuilder.getConnectionNumberList(tenantId, connectionType, preparedStatement);
		log.info("water " + connectionType + " connection list : " + query);
		return jdbcTemplate.query(query, preparedStatement.toArray(), demandSchedulerRowMapper);
	}

	@Override
	public List<String> getTenantId() {
		String query = queryBuilder.getDistinctTenantIds();
		log.info("Tenant Id's List Query : " + query);
		return (ArrayList<String>) jdbcTemplate.queryForList(query, String.class);
	}
	
	@Override
	public int isBillingPeriodExists(String connectionNo, String billingPeriod) {
		List<Object> preparedStatement = new ArrayList<>();
		String query = queryBuilder.isBillingPeriodExists(connectionNo, billingPeriod, preparedStatement);
		log.info("Is BillingPeriod Exits Query: " + query);
		return jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
	}

}
