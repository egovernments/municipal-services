package org.egov.wscalculation.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class WaterConnectionRepository {

	private static final String WS_ADDTNL_DTL_QUERY = "select additionaldetails from eg_ws_connection ";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public String fetchConnectionAdditonalDetails(String connectionNumber, String tenantId) {

		List<Object> presparedStmtList = new ArrayList<>();
		StringBuilder query = new StringBuilder(WS_ADDTNL_DTL_QUERY);
		query.append(" where tenantid=?");
		query.append(" and connectionno =?");
		presparedStmtList.add(tenantId);
		presparedStmtList.add(connectionNumber);
		return jdbcTemplate.queryForObject(query.toString(), presparedStmtList.toArray(), String.class);
	}

}
