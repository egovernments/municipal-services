package org.egov.land.repository.querybuilder;

import java.util.List;

import org.egov.land.web.models.LandSearchCriteria;

public class LandQueryBuilder {


	private static final String INNER_JOIN_STRING = " INNER JOIN ";
	private static final String LEFT_OUTER_JOIN_STRING = " LEFT OUTER JOIN ";

//	taken dummy table name as eg_land_info woth land info details
	private static final String QUERY = "SELECT * from eg_land_info landinfo";
	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY bpa_lastModifiedTime DESC) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > ? AND offset_ <= ?";
	
	
	/**
	 * To give the Search query based on the requirements.
	 * 
	 * @param criteria
	 *            BPA search criteria
	 * @param preparedStmtList
	 *            values to be replased on the query
	 * @return Final Search Query
	 */
	public String getBPASearchQuery(LandSearchCriteria criteria, List<Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(QUERY);

		

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);

	}
	
	
	/**
	 * 
	 * @param query
	 *            prepared Query
	 * @param preparedStmtList
	 *            values to be replased on the query
	 * @param criteria
	 *            bpa search criteria
	 * @return the query by replacing the placeholders with preparedStmtList
	 */
	private String addPaginationWrapper(String query, List<Object> preparedStmtList, LandSearchCriteria criteria) {

	
		String finalQuery = paginationWrapper.replace("{}", query);

		return finalQuery;

	}

	private void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND");
		}
	}

	private void addToPreparedStatement(List<Object> preparedStmtList, List<String> ids) {
		ids.forEach(id -> {
			preparedStmtList.add(id);
		});

	}

	private Object createQuery(List<String> ids) {
		StringBuilder builder = new StringBuilder();
		int length = ids.size();
		for (int i = 0; i < length; i++) {
			builder.append(" ?");
			if (i != length - 1)
				builder.append(",");
		}
		return builder.toString();
	}
}
