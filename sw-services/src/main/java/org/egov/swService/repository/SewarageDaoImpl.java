package org.egov.swService.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.egov.common.contract.request.RequestInfo;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.config.SWConfiguration;
import org.egov.swService.model.SearchCriteria;
import org.egov.swService.producer.SewarageConnectionProducer;
import org.egov.swService.repository.builder.sWQueryBuilder;
import org.egov.swService.repository.rowmapper.SewerageRowMapper;
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
	sWQueryBuilder sWQueryBuilder;

	@Autowired
	SewerageRowMapper sewarageRowMapper;
	
	@Autowired
	private SWConfiguration swConfiguration;

	@Value("${egov.sewarageservice.createconnection}")
	private String createSewarageConnection;

	@Value("${egov.sewarageservice.updateconnection}")
	private String updateSewarageConnection;

	@Override
	public void saveSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest) {
		sewarageConnectionProducer.push(createSewarageConnection, sewerageConnectionRequest);
	}

	@Override
	public List<SewerageConnection> getSewerageConnectionList(SearchCriteria criteria,
			RequestInfo requestInfo) {
		List<SewerageConnection> sewarageConnectionList = new ArrayList<>();
		List<Object> preparedStatement = new ArrayList<>();

		String query = sWQueryBuilder.getSearchQueryString(criteria, preparedStatement, requestInfo);
		if (query == null)
			return Collections.emptyList();
		// if (log.isDebugEnabled()) {
		StringBuilder str = new StringBuilder("Constructed query is:: ");
		str.append(query);
		log.debug(str.toString());
		// }
		sewarageConnectionList = jdbcTemplate.query(query, preparedStatement.toArray(), sewarageRowMapper);
		if (sewarageConnectionList == null) {
			return Collections.emptyList();
		}
		return sewarageConnectionList;
	}

	@Override
	public int isSewerageConnectionExist(List<String> ids) {
		int n = 0;
		Set<String> connectionIds = new HashSet<>(ids);
		List<Object> preparedStatement = new ArrayList<>();
		String query = sWQueryBuilder.getNoOfSewerageConnectionQuery(connectionIds, preparedStatement);
		n = jdbcTemplate.queryForObject(query, preparedStatement.toArray(), Integer.class);
		return n;
	}

	public void updateSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest, boolean isStateUpdatable) {
		if (isStateUpdatable)
			sewarageConnectionProducer.push(updateSewarageConnection, sewerageConnectionRequest);
		if (!isStateUpdatable)
			sewarageConnectionProducer.push(swConfiguration.getWorkFlowUpdateTopic(), sewerageConnectionRequest);
	}


}
