package org.egov.waterConnection.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.SearchCriteria;
import org.egov.waterConnection.producer.WaterConnectionProducer;
import org.egov.waterConnection.repository.builder.WsQueryBuilder;
import org.egov.waterConnection.repository.rowmapper.WaterRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	WsQueryBuilder wCQueryBuilder;

	@Autowired
	WaterRowMapper waterRowMapper;

	@Value("${egov.waterservice.createWaterConnection}")
	private String createWaterConnection;

	@Value("${egov.waterservice.updateWaterConnection}")
	private String updateWaterConnection;

	@Override
	public void saveWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		waterConnectionProducer.push(createWaterConnection, waterConnectionRequest);
	}

	@Override
	public List<WaterConnection> getWaterConnectionList(SearchCriteria criteria,
			RequestInfo requestInfo) {
		List<WaterConnection> waterConnectionList = new ArrayList<>();
		List<Object> preparedStatement = new ArrayList<>();
		String query = wCQueryBuilder.getSearchQueryString(criteria, preparedStatement, requestInfo, true);
		log.info("Query: " + query);
		waterConnectionList = jdbcTemplate.query(query, preparedStatement.toArray(), waterRowMapper);
		return waterConnectionList;
	}

	@Override
	public void updateWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		waterConnectionProducer.push(updateWaterConnection, waterConnectionRequest);
	}

	@Override
	public int isWaterConnectionExist(List<String> ids) {
		int n = 0;
		Set<String> connectionIds = new HashSet<>(ids);
		List<Object> preparedStatement = new ArrayList<>();
		String query = wCQueryBuilder.getNoOfWaterConnectionQuery(connectionIds, preparedStatement);
		log.info("Query: " + query);
		n = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
		return n;
	}

}
