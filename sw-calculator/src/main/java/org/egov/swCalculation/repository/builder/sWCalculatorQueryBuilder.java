package org.egov.swCalculation.repository.builder;

import java.util.List;

import org.springframework.stereotype.Controller;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class sWCalculatorQueryBuilder {
	
	private static final String INNER_JOIN_STRING = "INNER JOIN";
	
	private static final String connectionNoListQuery = "SELECT distinct(conn.eg_sw_connection) FROM eg_sw_connection conn INNER JOIN eg_sw_service sw ON conn.id = sw.connection_id";
	
	private static final String distinctTenantIdsCriteria = "SELECT distinct(tenantid) FROM eg_sw_connection sw";

	public String getDistinctTenantIds() {
		StringBuilder query = new StringBuilder(distinctTenantIdsCriteria);
		log.info("Query : " + query);
		return query.toString();
	}

	public String getConnectionNumberList(String tenantId, String connectionType, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(connectionNoListQuery);
		// Add connection type
		addClauseIfRequired(preparedStatement, query);
		query.append(" sw.connectiontype = ? ");
		preparedStatement.add(connectionType);

		// add tenantid
		addClauseIfRequired(preparedStatement, query);
		query.append(" conn.tenantid = ? ");
		preparedStatement.add(tenantId);
		return query.toString();
	}

	private void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND ");
		}
	}
}
