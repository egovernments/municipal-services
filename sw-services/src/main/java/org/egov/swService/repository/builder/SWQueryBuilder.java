package org.egov.swService.repository.builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.swService.config.SWConfiguration;
import org.egov.swService.model.Property;
import org.egov.swService.model.SearchCriteria;
import org.egov.swService.util.SewerageServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


@Component
public class SWQueryBuilder {

	@Autowired
	SewerageServicesUtil sewerageServicesUtil;

	@Autowired
	SWConfiguration config;

	private static final String INNER_JOIN_STRING = "INNER JOIN";
	 private static final String LEFT_OUTER_JOIN_STRING = " LEFT OUTER JOIN ";
	//private static final String Offset_Limit_String = "OFFSET ? LIMIT ?";
	
	private final static String noOfConnectionSearchQuery = "SELECT count(*) FROM eg_sw_connection WHERE";
	
	private final static String SEWERAGE_SEARCH_QUERY = "SELECT conn.*, sc.*, document.*, plumber.*, sc.connectionExecutionDate,"
			+ "sc.noOfWaterClosets, sc.noOfToilets,sc.proposedWaterClosets, sc.proposedToilets, sc.connectionType, sc.connection_id as connection_Id, conn.id as conn_id, conn.applicationNo, conn.applicationStatus, conn.status, conn.connectionNo, conn.oldConnectionNo, conn.property_id, conn.roadcuttingarea, conn.action, conn.adhocpenalty, conn.adhocrebate, conn.adhocpenaltyreason, conn.adhocpenaltycomment, conn.adhocrebatereason, conn.adhocrebatecomment,"
			+ " conn.roadtype, document.id as doc_Id, document.documenttype, document.filestoreid, document.active as doc_active, plumber.id as plumber_id, plumber.name as plumber_name, plumber.licenseno,"
			+ " plumber.mobilenumber as plumber_mobileNumber, plumber.gender as plumber_gender, plumber.fatherorhusbandname, plumber.correspondenceaddress, plumber.relationship FROM eg_sw_connection conn "
	+  INNER_JOIN_STRING 
	+" eg_sw_service sc ON sc.connection_id = conn.id"
	+  LEFT_OUTER_JOIN_STRING
	+ "eg_sw_applicationdocument document ON document.swid = conn.id" 
	+  LEFT_OUTER_JOIN_STRING
	+ "eg_sw_plumberinfo plumber ON plumber.swid = conn.id";

	/**
	 * 
	 * @param criteria on search criteria
	 * @param preparedStatement preparedStatement
	 * @param requestInfo
	 * @return
	 */
	
	
	private final String paginationWrapper = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY conn_id) offset_ FROM " +
            "({})" +
            " result) result_offset " +
            "WHERE offset_ > ? AND offset_ <= ?";
	
	
	public String getSearchQueryString(SearchCriteria criteria, List<Object> preparedStatement,
			RequestInfo requestInfo) {
		StringBuilder query = new StringBuilder(SEWERAGE_SEARCH_QUERY);
		boolean isAnyCriteriaMatch = false;
		if (!StringUtils.isEmpty(criteria.getMobileNumber())) {
			Set<String> propertyIds = new HashSet<>();
			List<Property> propertyList = sewerageServicesUtil.propertySearchOnCriteria(criteria, requestInfo);
			propertyList.forEach(property -> propertyIds.add(property.getPropertyId()));
			if (!propertyIds.isEmpty()) {
				addClauseIfRequired(preparedStatement, query);
				query.append(" conn.property_id in (").append(createQuery(propertyIds)).append(" )");
				addToPreparedStatement(preparedStatement, propertyIds);
				isAnyCriteriaMatch = true;
			}
		}
		if (!CollectionUtils.isEmpty(criteria.getIds())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.id in (").append(createQuery(criteria.getIds())).append(" )");
			addToPreparedStatement(preparedStatement, criteria.getIds());
			isAnyCriteriaMatch = true;
		}

		if (!StringUtils.isEmpty(criteria.getPropertyId())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.property_id = ? ");
			preparedStatement.add(criteria.getPropertyId());
			isAnyCriteriaMatch = true;
		}
		if (!StringUtils.isEmpty(criteria.getOldConnectionNumber())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.oldconnectionno = ? ");
			preparedStatement.add(criteria.getOldConnectionNumber());
			isAnyCriteriaMatch = true;
		}

		if (!StringUtils.isEmpty(criteria.getConnectionNumber())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.connectionno = ? ");
			preparedStatement.add(criteria.getConnectionNumber());
			isAnyCriteriaMatch = true;
		}
		if (!StringUtils.isEmpty(criteria.getStatus())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.status = ? ");
			preparedStatement.add(criteria.getStatus());
			isAnyCriteriaMatch = true;
		}
		if (!StringUtils.isEmpty(criteria.getApplicationNumber())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.applicationno = ? ");
			preparedStatement.add(criteria.getApplicationNumber());
			isAnyCriteriaMatch = true;
		}
		if (!StringUtils.isEmpty(criteria.getApplicationStatus())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.applicationStatus = ? ");
			preparedStatement.add(criteria.getApplicationStatus());
			isAnyCriteriaMatch = true;
		}
		if (criteria.getFromDate() != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append("  sc.connectionExecutionDate >= ? ");
			preparedStatement.add(criteria.getFromDate());
			isAnyCriteriaMatch = true;
		}
		if (criteria.getToDate() != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append("  sc.connectionExecutionDate <= ? ");
			preparedStatement.add(criteria.getToDate());
			isAnyCriteriaMatch = true;
		}
		if (isAnyCriteriaMatch == false)
			return null;
		
		//Add OrderBy clause
		query.append(" ORDER BY sc.connectionExecutionDate DESC");
		
		if (query.toString().indexOf("WHERE") > -1)
			 return addPaginationWrapper(query.toString(), preparedStatement, criteria);
		return query.toString();
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
//		query = query + " " + Offset_Limit_String;
		Integer limit = config.getDefaultLimit();
		Integer offset = config.getDefaultOffset();

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
	
	public String getNoOfSewerageConnectionQuery(Set<String> connectionIds, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(noOfConnectionSearchQuery);
		Set<String> listOfIds = new HashSet<>();
		connectionIds.forEach(id -> listOfIds.add(id));
		query.append(" connectionno in (").append(createQuery(connectionIds)).append(" )");
		addToPreparedStatement(preparedStatement, listOfIds);
		return query.toString();
	}

}
