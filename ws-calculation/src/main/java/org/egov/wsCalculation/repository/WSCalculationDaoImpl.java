package org.egov.wsCalculation.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReadingSearchCriteria;
import org.egov.wsCalculation.producer.WSCalculationProducer;
import org.egov.wscalculation.builder.WSCalculatorQueryBuilder;
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
	WSCalculationProducer wSCalculationProducer;

	@Autowired
	WSCalculatorQueryBuilder queryBuilder;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	MeterReadingRowMapper meterReadingRowMapper;

	@Value("${egov.meterReadingService.createMeterConnection}")
	private String createMeterConnection;

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
		log.info("Query: " + query);
		log.info("Prepared Statement"+ preparedStatement.toString());
		listOfMeterReadings = jdbcTemplate.query(query, preparedStatement.toArray(), meterReadingRowMapper);
		return listOfMeterReadings;
	}

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
}
