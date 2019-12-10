package org.egov.wsCalculation.builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReadingSearchCriteria;
import org.egov.wsCalculation.config.WSCalculationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WSCalculatorQueryBuilder {

	@Autowired
	WSCalculationConfiguration config;

	private static final String Offset_Limit_String = "OFFSET ? LIMIT ?";
	private final static String Query = "SELECT mr.id, mr.connectionNo as connectionId, mr.billingPeriod, mr.meterStatus, mr.lastReading, mr.lastReadingDate, mr.currentReading, "
			+ "mr.currentReadingDate FROM meterreading mr";

	private final static String noOfConnectionSearchQuery = "SELECT count(*) FROM meterreading WHERE";
    
	private final static String noOfConnectionSearchQueryForCurrentMeterReading= "select mr.currentReading from meterreading ORDER BY currentReadingDate DESC LIMIT 1";
	/**
	 * 
	 * @param criteria
	 *            would be meter reading criteria
	 * @param preparedStatement
	 * @return Query for given criteria
	 */
	public String getSearchQueryString(MeterReadingSearchCriteria criteria, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(Query);
		String resultantQuery = Query;
		boolean isAnyCriteriaMatch = false;
		if (!criteria.getConnectionNos().isEmpty()) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" mr.connectionNo IN (").append(createQuery(criteria.getConnectionNos())).append(" )");
			addToPreparedStatement(preparedStatement, criteria.getConnectionNos());
			isAnyCriteriaMatch = true;
		}
		if(isAnyCriteriaMatch == false)
			return null;
		resultantQuery = query.toString();
		if (query.toString().indexOf("WHERE") > -1)
			resultantQuery = addPaginationWrapper(query.toString(), preparedStatement, criteria);
		return resultantQuery;
	}

	private String createQuery(Set<String> ids) {
		StringBuilder builder = new StringBuilder();
		int length = ids.size();
		for (int i = 0; i < length; i++) {
			builder.append(" ?");
			if (i != length - 1)
				builder.append(",");
		}
		return builder.toString();
	}

	private void addToPreparedStatement(List<Object> preparedStatement, Set<String> ids) {
		ids.forEach(id -> {
			preparedStatement.add(id);
		});
	}

	private void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND");
		}
	}

	private String addPaginationWrapper(String query, List<Object> preparedStmtList,
			MeterReadingSearchCriteria criteria) {
		query = query + " " + Offset_Limit_String;
		Integer limit = config.getMeterReadingDefaultLimit();
		Integer offset = config.getMeterReadingDefaultOffset();

		if (criteria.getLimit() != null && criteria.getLimit() <= config.getMeterReadingDefaultLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > config.getMeterReadingDefaultLimit())
			limit = config.getMeterReadingDefaultLimit();

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		preparedStmtList.add(offset);
		preparedStmtList.add(limit + offset);
		return query;
	}

	public String getNoOfMeterReadingConnectionQuery(Set<String> connectionIds, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(noOfConnectionSearchQuery);
		Set<String> listOfIds = new HashSet<>();
		connectionIds.forEach(id -> listOfIds.add(id));
		query.append(" connectionNo in (").append(createQuery(connectionIds)).append(" )");
		addToPreparedStatement(preparedStatement, listOfIds);
		return query.toString();
	}
	
	public String getCurrentReadingConnectionQuery(MeterReadingSearchCriteria criteria,
			List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(noOfConnectionSearchQueryForCurrentMeterReading);
		String resultantQuery = noOfConnectionSearchQueryForCurrentMeterReading;
		boolean isAnyCriteriaMatch = false;
		if (!criteria.getConnectionNos().isEmpty()) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" mr.connectionNo IN (").append(createQuery(criteria.getConnectionNos())).append(" )");
			addToPreparedStatement(preparedStatement, criteria.getConnectionNos());
			isAnyCriteriaMatch = true;
		}
		if (isAnyCriteriaMatch == false)
			return null;
		resultantQuery = query.toString();
		if (query.toString().indexOf("WHERE") > -1)
			resultantQuery = addPaginationWrapper(query.toString(), preparedStatement, criteria);
		return resultantQuery;
	}

	private void addIntegerListToPreparedStatement(List<Object> preparedStatement, Set<String> ids) {
		ids.forEach(id -> {
			preparedStatement.add(id);
		});
	}
}
