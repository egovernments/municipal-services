package org.egov.waterConnection.repository;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.WaterConnectionSearchCriteria;
import org.egov.waterConnection.producer.WaterConnectionProducer;
import org.egov.waterConnection.repository.builder.WCQueryBuilder;
import org.egov.waterConnection.repository.rowmapper.WaterRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class WaterDaoImpl implements WaterDao {

	@Autowired
	WaterConnectionProducer waterConnectionProducer;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	WCQueryBuilder wCQueryBuilder;

	@Autowired
	WaterRowMapper waterRowMapper;

	@Override
	public void saveWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		waterConnectionProducer.push("${egov.waterservice.createWaterConnection}", waterConnectionRequest);
	}

	@Override
	public List<WaterConnection> getWaterConnectionList(WaterConnectionSearchCriteria criteria, RequestInfo requestInfo) {
		List<WaterConnection> waterConnectionList = new ArrayList<>();
		List<Object> preparedStatement = new ArrayList<>();
		String query = wCQueryBuilder.getSearchQueryString(criteria, preparedStatement,requestInfo);
		log.info("Query: " + query);
		waterConnectionList = jdbcTemplate.query(query, preparedStatement.toArray(), waterRowMapper);
		return waterConnectionList;
	}
	
	@Override
	public void updateWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		waterConnectionProducer.push("${egov.waterservice.updateWaterConnection}", waterConnectionRequest);
	}

	@Override
	public int isWaterConnectionExist(List<String> ids) {
		int n = 0;
		List<Object> preparedStatement = new ArrayList<>();
		String query = wCQueryBuilder.getNoOfWaterConnectionQuery(ids, preparedStatement);
		log.info("Query: " + query);
		n = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
		return n;
	}

}
