package org.egov.waterConnection.repository.builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterConnection.config.WSConfiguration;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.SearchCriteria;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


@Component
public class WsQueryBuilder {

	@Autowired
	WaterServicesUtil waterServicesUtil;

	@Autowired
	WSConfiguration config;

	private static final String INNER_JOIN_STRING = "INNER JOIN";
    private static final String LEFT_OUTER_JOIN_STRING = " LEFT OUTER JOIN ";
//	private static final String Offset_Limit_String = "OFFSET ? LIMIT ?";
	private final static String WATER_SEARCH_Query = "SELECT conn.*, wc.*, document.*, plumber.*, wc.connectionCategory, wc.rainWaterHarvesting, wc.connectionType, wc.waterSource,"
			+ " wc.meterId, wc.meterInstallationDate, wc.pipeSize, wc.noOfTaps, wc.proposedPipeSize, wc.proposedTaps, wc.uom, wc.waterSubSource, wc.connection_id as connection_Id, wc.connectionExecutionDate,"
			+ " conn.id as conn_id, conn.applicationNo, conn.applicationStatus, conn.status, conn.connectionNo, conn.oldConnectionNo, conn.property_id, conn.roadcuttingarea, conn.action,"
			+ " conn.roadtype, document.id as doc_Id, document.documenttype, document.filestoreid, document.active as doc_active, plumber.id as plumber_id, plumber.name as plumber_name, plumber.licenseno,"
			+ " plumber.mobilenumber as plumber_mobileNumber, plumber.gender as plumber_gender, plumber.fatherorhusbandname, plumber.correspondenceaddress, plumber.relationship  FROM connection conn "
			+  INNER_JOIN_STRING 
			+" water_service_connection wc ON wc.connection_id = conn.id"
			+  LEFT_OUTER_JOIN_STRING
			+ "eg_ws_applicationdocument document ON document.wsid = conn.id" 
			+  LEFT_OUTER_JOIN_STRING
			+ "eg_ws_plumberinfo plumber ON plumber.wsid = conn.id";
	
	private final static String noOfConnectionSearchQuery = "SELECT count(*) FROM connection WHERE";
	
	private final String paginationWrapper = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY conn_id) offset_ FROM " +
            "({})" +
            " result) result_offset " +
            "WHERE offset_ > ? AND offset_ <= ?";
	
	private final String ORDER_BY_CLAUSE= " ORDER BY wc.connectionExecutionDate DESC";
	/**
	 * 
	 * @param criteria
	 *            The WaterCriteria
	 * @param preparedStatement
	 *            The Array Of Object
	 * @param requestInfo
	 *            The Request Info
	 * @return query as a string
	 */
	public String getSearchQueryString(SearchCriteria criteria, List<Object> preparedStatement, RequestInfo requestInfo) {
		StringBuilder query = new StringBuilder(WATER_SEARCH_Query);
		boolean isAnyCriteriaMatch = false;
		if ((criteria.getMobileNumber() != null && !criteria.getMobileNumber().isEmpty())) {
			Set<String> propertyIds = new HashSet<>();
			List<Property> propertyList = waterServicesUtil.propertySearchOnCriteria(criteria, requestInfo);
			propertyList.forEach(property -> propertyIds.add(property.getPropertyId()));
			if (!propertyIds.isEmpty()) {
				addClauseIfRequired(preparedStatement, query);
				query.append(" conn.property_id in (").append(createQuery(propertyIds)).append(" )");
				addToPreparedStatement(preparedStatement, propertyIds);
				isAnyCriteriaMatch = true;
			}

		}
		if (criteria.getPropertyId() != null && !criteria.getPropertyId().isEmpty()) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.property_id = ? ");
			preparedStatement.add(criteria.getPropertyId());
			isAnyCriteriaMatch = true;
		}
		if (!CollectionUtils.isEmpty(criteria.getIds())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.id in (").append(createQuery(criteria.getIds())).append(" )");
			addToPreparedStatement(preparedStatement, criteria.getIds());
			isAnyCriteriaMatch = true;
		}
		if (criteria.getOldConnectionNumber() != null && !criteria.getOldConnectionNumber().isEmpty()) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.oldconnectionno = ? ");
			preparedStatement.add(criteria.getOldConnectionNumber());
			isAnyCriteriaMatch = true;
		}

		if (criteria.getConnectionNumber() != null && !criteria.getConnectionNumber().isEmpty()) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.connectionno = ? ");
			preparedStatement.add(criteria.getConnectionNumber());
			isAnyCriteriaMatch = true;
		}
		if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.status = ? ");
			preparedStatement.add(criteria.getStatus());
			isAnyCriteriaMatch = true;
		}
		if (criteria.getApplicationNumber() != null && !criteria.getApplicationNumber().isEmpty()) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.applicationno = ? ");
			preparedStatement.add(criteria.getApplicationNumber());
			isAnyCriteriaMatch = true;
		}
		if (isAnyCriteriaMatch == false) {
			return null;
			}
		query.append(ORDER_BY_CLAUSE);
		return addPaginationWrapper(query.toString(), preparedStatement, criteria);
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
	 * @param query
	 *            The
	 * @param preparedStmtList
	 *            Array of object for preparedStatement list
	 * @return It's returns query
	 */
	private String addPaginationWrapper(String query, List<Object> preparedStmtList, SearchCriteria criteria) {
		Integer limit = config.getDefaultLimit();
		Integer offset = config.getDefaultOffset();
		if (criteria.getLimit() == null && criteria.getOffset() == null)
			limit = config.getMaxLimit();

		if (criteria.getLimit() != null && criteria.getLimit() <= config.getDefaultLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > config.getDefaultOffset())
			limit = config.getDefaultLimit();

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		preparedStmtList.add(offset);
		preparedStmtList.add(limit + offset);
		return paginationWrapper.replace("{}",query);
	}

	public String getNoOfWaterConnectionQuery(Set<String> connectionIds, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(noOfConnectionSearchQuery);
		query.append(" connectionno in (").append(createQuery(connectionIds)).append(" )");
		addToPreparedStatement(preparedStatement, connectionIds);
		return query.toString();
	}
	
}
