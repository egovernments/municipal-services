package org.egov.wsCalculation.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReadingSearchCriteria;
import org.egov.wsCalculation.producer.WSCalculationProducer;
import org.egov.wsCalculation.builder.WSCalculatorQueryBuilder;
import org.egov.wsCalculation.rowmapper.DemandSchedulerRowMapper;
import org.egov.wsCalculation.rowmapper.MeterReadingCurrentReadingRowMapper;
import org.egov.wsCalculation.rowmapper.MeterReadingRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class WSCalculationDaoImpl implements WSCalculationDao {

	@Autowired
	WSCalculationProducer wSCalculationProducer;

	@Autowired
	WSCalculatorQueryBuilder queryBuilder;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	MeterReadingRowMapper meterReadingRowMapper;
	
	@Autowired
	MeterReadingCurrentReadingRowMapper currentMeterReadingRowMapper;
	
	
	@Autowired
	DemandSchedulerRowMapper demandSchedulerRowMapper;
	

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
	public void saveWaterConnection(MeterConnectionRequest meterConnectionRequest) {
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
		List<MeterReading> listOfMeterReadings = new ArrayList<>();
		String query = queryBuilder.getSearchQueryString(criteria, preparedStatement);
		if(query == null)
			return listOfMeterReadings;
		log.info("Query: " + query);
		log.info("Prepared Statement" + preparedStatement.toString());
		listOfMeterReadings = jdbcTemplate.query(query, preparedStatement.toArray(), meterReadingRowMapper);
		return listOfMeterReadings;
	}
	
	@Override
	public List<MeterReading> searchCurrentMeterReadings(MeterReadingSearchCriteria criteria) {
		List<Object> preparedStatement = new ArrayList<>();
		List<MeterReading> listOfMeterReadings = new ArrayList<>();
		String query = queryBuilder.getCurrentReadingConnectionQuery(criteria, preparedStatement);
		if (query == null)
			return listOfMeterReadings;
		log.info("Query: " + query);
		log.info("Prepared Statement" + preparedStatement.toString());
		listOfMeterReadings = jdbcTemplate.query(query, preparedStatement.toArray(), currentMeterReadingRowMapper);
		return listOfMeterReadings;
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
		int n = 0;
		Set<String> connectionIds = new HashSet<>(ids);
		List<Object> preparedStatement = new ArrayList<>();
		String query = queryBuilder.getNoOfMeterReadingConnectionQuery(connectionIds, preparedStatement);
		log.info("Query: " + query);
		n = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
		return n;
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
	
//	@Override
//	public ArrayList<String> searchConnectionNos() {
//		ArrayList<String> connectionNos = new ArrayList<>();
//		String query = queryBuilder.getTenentIdConnectionQuery();
//		if (query == null)
//			return connectionNos;
//		log.info("Query: " + query);
//		connectionNos = (ArrayList<String>) jdbcTemplate.queryForList(query, String.class);
//		return connectionNos;
//	}

}
