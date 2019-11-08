package org.egov.waterConnection.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterConnection.model.SewerageConnection;
import org.egov.waterConnection.model.SewerageConnectionRequest;
import org.egov.waterConnection.model.WaterConnectionSearchCriteria;
import org.egov.waterConnection.producer.SewarageConnectionProducer;
import org.egov.waterConnection.producer.WaterConnectionProducer;
import org.egov.waterConnection.repository.builder.SCQueryBuilder;
import org.egov.waterConnection.repository.builder.WCQueryBuilder;
import org.egov.waterConnection.repository.rowmapper.SewerageRowMapper;
import org.egov.waterConnection.repository.rowmapper.WaterRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SewarageDaoImpl implements SewarageDao {

	@Autowired
	SewarageConnectionProducer sewarageConnectionProducer;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	SCQueryBuilder sCQueryBuilder;

	@Autowired
	WaterRowMapper waterRowMapper;

	@Autowired
	SewerageRowMapper sewarageRowMapper;

	@Value("${egov.sewarageservice.createSewarageConnection}")
	private String createSewarageConnection;

	@Value("${egov.sewarageservice.updateSewarageConnection}")
	private String updateSewarageConnection;

	@Override
	public void saveSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest) {
		sewarageConnectionProducer.push(createSewarageConnection, sewerageConnectionRequest);
	}

	@Override
	public List<SewerageConnection> getSewerageConnectionList(WaterConnectionSearchCriteria criteria,
			RequestInfo requestInfo) {
		List<SewerageConnection> sewarageConnectionList = new ArrayList<>();
		List<Object> preparedStatement = new ArrayList<>();
		String query = sCQueryBuilder.getSearchQueryString(criteria, preparedStatement, requestInfo);
		//sewarageConnectionList = jdbcTemplate.query(query, preparedStatement.toArray(), sewarageRowMapper);
		return sewarageConnectionList;
	}

	@Override
	public int isSewerageConnectionExist(List<String> ids) {
		int n = 0;
		Set<String> connectionIds = new HashSet<>(ids);
		List<Object> preparedStatement = new ArrayList<>();
		String query = sCQueryBuilder.getNoOfWaterConnectionQuery(connectionIds, preparedStatement);
		n = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
		return n;
	}

	@Override
	public void updatSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest) {
		sewarageConnectionProducer.push(updateSewarageConnection, sewerageConnectionRequest);
	}

}
