package org.egov.waterConnection.repository.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterConnection.config.WCConfiguration;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.WaterConnectionSearchCriteria;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WCQueryBuilder {

	@Autowired
	WaterServicesUtil waterServicesUtil;

	@Autowired
	WCConfiguration config;

	private static final String INNER_JOIN_STRING = "INNER JOIN";
	private static final String Offset_Limit_String = "OFFSET ? LIMIT ?";
	private final static String Query = "SELECT wc.id as water_Id, wc.connectionCategory, wc.rainWaterHarvesting, wc.connectionType, wc.waterSource, wc.meterId, "
			+ "wc.meterInstallationDate, conn.id as connection_Id, conn.applicationNo, conn.applicationStatus, conn.status, conn.connectionNo,"
			+ " conn.oldConnectionNo, conn.documents_id, conn.property_id FROM water_service_connection wc"
			+ INNER_JOIN_STRING + " connection conn ON wc.connection_id = conn.id";

	private final static String noOfConnectionSearchQuery = "select count(*) from water_service_connection where";

	/**
	 * 
	 * @param criteria The WaterCriteria
	 * @param preparedStatement The Array Of Object
	 * @param requestInfo The Request Inof
	 * @return query as a string
	 */
	public String getSearchQueryString(WaterConnectionSearchCriteria criteria, List<Object> preparedStatement,
			RequestInfo requestInfo) {
		StringBuilder query = new StringBuilder(Query);

		if (criteria.getTenantId() != null && !criteria.getTenantId().isEmpty()) {
			Set<String> propertyIds = new HashSet<>();
			addClauseIfRequired(preparedStatement, query);
			List<Property> propertyList = waterServicesUtil.propertySearchOnCriteria(criteria, requestInfo);
			propertyList.forEach(property -> propertyIds.add(property.getId()));
			if (!propertyIds.isEmpty())
				query.append(" property_id in (").append(createQuery(propertyIds)).append(" )");
		}
		if (!CollectionUtils.isEmpty(criteria.getIds())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" id in (").append(createQuery(criteria.getIds())).append(" )");
			addToPreparedStatement(preparedStatement, criteria.getIds());
		}
		if (criteria.getOldConnectionNumber() != null && !criteria.getOldConnectionNumber().isEmpty()) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" oldconnectionno = ? ");
			preparedStatement.add(criteria.getOldConnectionNumber());
		}

		if (criteria.getOldConnectionNumber() != null && !criteria.getOldConnectionNumber().isEmpty()) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" connectionno = ? ");
			preparedStatement.add(criteria.getConnectionNumber());
		}
		return addPaginationWrapper(query.toString(), preparedStatement);
	}

	private void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND");
		}
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
	/**
	 * 
	 * @param query The
	 * @param preparedStmtList Array of object for preparedStatement list
	 * @return It's returns query
	 */
	private String addPaginationWrapper(String query, List<Object> preparedStmtList) {
		query = query + Offset_Limit_String;
		Long limit = config.getDefaultLimit();
		Long offset = config.getDefaultOffset();
		preparedStmtList.add(offset);
		preparedStmtList.add(limit + offset);
		return query;
	}

	public String getNoOfWaterConnectionQuery(Set<String> connectionIds, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(noOfConnectionSearchQuery);
		query.append(" id in (").append(createQuery(connectionIds)).append(" )");
		addToPreparedStatement(preparedStatement, connectionIds);
		return query.toString();
	}
}
