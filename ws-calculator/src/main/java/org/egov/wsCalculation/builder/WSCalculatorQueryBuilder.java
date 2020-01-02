package org.egov.wsCalculation.builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReadingSearchCriteria;
import org.egov.wsCalculation.config.WSCalculationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WSCalculatorQueryBuilder {

	@Autowired
	WSCalculationConfiguration config;

	private static final String Offset_Limit_String = "OFFSET ? LIMIT ?";
	private final static String Query = "SELECT mr.id, mr.connectionNo as connectionId, mr.billingPeriod, mr.meterStatus, mr.lastReading, mr.lastReadingDate, mr.currentReading, "
			+ "mr.currentReadingDate FROM meterreading mr";

	private final static String noOfConnectionSearchQuery = "SELECT count(*) FROM meterreading WHERE";
    
	private final static String noOfConnectionSearchQueryForCurrentMeterReading= "select mr.currentReading from meterreading mr";
	
	private final static String tenentIdWaterConnectionSearchQuery="select DISTINCT tenantid from connection";
	
	private final static String connectionNoWaterConnectionSearchQuery = "SELECT conn.connectionNo as conn_no FROM water_service_connection wc INNER JOIN connection conn ON wc.connection_id = conn.id";
	
	private static final String connectionNoListQuery = "SELECT distinct(connectionno) FROM connection ws";

	private static final String distinctTenantIdsCriteria = "SELECT distinct(tenantid) FROM connection ws";


	public String getDistinctTenantIds() {
		StringBuilder query = new StringBuilder(distinctTenantIdsCriteria);
		log.info("Query : " + query);
		return query.toString();
	}
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
		resultantQuery = addOrderBy(resultantQuery);
		if (query.toString().indexOf("WHERE") > -1)
			resultantQuery = addPaginationWrapper(resultantQuery, preparedStatement, criteria);
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
			query.append(" ORDER BY mr.currentReadingDate DESC LIMIT 1");
		}
		if (isAnyCriteriaMatch == false)
			return null;
		resultantQuery = query.toString();
		return resultantQuery;
	}

	private void addIntegerListToPreparedStatement(List<Object> preparedStatement, Set<String> ids) {
		ids.forEach(id -> {
			preparedStatement.add(id);
		});
	}
	
	public String getTenentIdConnectionQuery() {
		StringBuilder query = new StringBuilder(tenentIdWaterConnectionSearchQuery);
		return query.toString();
	}
	
	private String addOrderBy(String query) {
		query = query + " ORDER BY mr.currentReadingDate DESC";
		return query;
	}
	
	public String getConnectionNumberFromWaterServicesQuery(List<Object> preparedStatement, String connectionType,
			String tenentId) {
		StringBuilder query = new StringBuilder(connectionNoWaterConnectionSearchQuery);
		boolean isAnyCriteriaMatch = false;
		if (connectionType != null && !connectionType.isEmpty()) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" wc.connectionType = ? ");
			preparedStatement.add(connectionType);
			isAnyCriteriaMatch = true;
		}

		if (tenentId != null && !tenentId.isEmpty()) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.tenantId = ? ");
			preparedStatement.add(tenentId);
			isAnyCriteriaMatch = true;
		}
		return query.toString();

	}
	
	
	public String getConnectionNumberList(String tenantId, String connectionType, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(connectionNoListQuery);
		// Add connection type
		addClauseIfRequired(preparedStatement, query);
		query.append(" ws.connectionType = ? ");
		preparedStatement.add(connectionType);

		// add tenantid
		addClauseIfRequired(preparedStatement, query);
		query.append(" ws.tenantid = ? ");
		preparedStatement.add(tenantId);
		return query.toString();
	}


}
