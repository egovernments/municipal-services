package org.egov.swcalculation.repository.builder;

import java.util.List;

import org.springframework.stereotype.Controller;

@Controller
public class SWCalculatorQueryBuilder {
	
	private static final String connectionNoListQuery = "SELECT distinct(conn.connectionno) FROM eg_sw_connection conn INNER JOIN eg_sw_service sw ON conn.id = sw.connection_id";
 
	private static final String distinctTenantIdsCriteria = "SELECT distinct(tenantid) FROM eg_sw_connection sw";

	private  static final String countQuery = "select count(distinct(conn.connectionno)) from eg_sw_connection conn inner join eg_sw_service sw ON sw.connection_id = conn.id where conn.tenantid = '{}' and sw.connectiontype ='Non Metered' and conn.connectionno is not null";


	public String getDistinctTenantIds() {
		return distinctTenantIdsCriteria;
	}

	public String getCountQuery() {
		return countQuery;
	}

	public String getConnectionNumberList(String tenantId, String connectionType, List<Object> preparedStatement, Integer batchOffset, Integer batchsize) {
		StringBuilder query = new StringBuilder(connectionNoListQuery);
		// Add connection type
		addClauseIfRequired(preparedStatement, query);
		query.append(" sw.connectiontype = ? ");
		preparedStatement.add(connectionType);

		// add tenantid
		addClauseIfRequired(preparedStatement, query);
		query.append(" conn.tenantid = ? ");
		preparedStatement.add(tenantId);
		
		//Add not null condition
		addClauseIfRequired(preparedStatement, query);
		query.append(" conn.connectionno is not null");

		String orderbyClause = " ORDER BY conn.connectionno OFFSET ? LIMIT ?";
		preparedStatement.add(batchOffset);
		preparedStatement.add(batchsize);
		query.append(orderbyClause);

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
