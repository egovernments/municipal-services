package org.egov.fsm.repository.querybuilder;

import java.util.List;
import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.web.model.FSMSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class FSMQueryBuilder {

	@Autowired
	private FSMConfiguration config;
				
			private static final String Query = "select fsm.*,fsm_address.*,fsm_geo.*,fsm_pit.*,fsm.id as fsm_id, fsm.createdby as fsm_createdby,"
					+ " fsm.lastmodifiedby as fsm_lastmodifiedby, fsm.createdtime as fsm_createdtime, fsm.lastmodifiedtime as fsm_lastmodifiedtime,"
					+ "	 fsm.additionaldetails,fsm_address.id as fsm_address_id,fsm_geo.id as fsm_geo_id,"
					+ "	 fsm_pit.id as fsm_pit_id"
					+ "	 FROM eg_fsm_application fsm"
					+ "	 INNER JOIN   eg_fsm_address fsm_address on fsm_address.fsm_id = fsm.id"
					+ "	 LEFT OUTER JOIN  eg_fsm_geolocation fsm_geo on fsm_geo.address_id = fsm_address.id"
					+ "	 LEFT OUTER JOIN  eg_fsm_pit_detail fsm_pit on fsm_pit.fms_id = fsm.id";
			
	private final String paginationWrapper = "SELECT * FROM "
					+ "(SELECT *, DENSE_RANK() OVER (ORDER BY fsm_lastModifiedTime DESC) offset_ FROM " + "({})"
					+ " result) result_offset " + "WHERE offset_ > ? AND offset_ <= ?";
	

	
	public String getFSMSearchQuery(FSMSearchCriteria criteria, List<Object> preparedStmtList) {
		
		StringBuilder builder = new StringBuilder(Query);
		if(criteria.getTenantId() != null) {
			if(criteria.getTenantId().split("\\.").length == 1) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" fsm.tenantid like ?");
				preparedStmtList.add('%' + criteria.getTenantId() + '%');
			}else {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" fsm.tenantid=? ");
				preparedStmtList.add(criteria.getTenantId());
			}
		}
		
		List<String> application_number = criteria.getApplicationNumber();
		if (!CollectionUtils.isEmpty(application_number)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" fsm.applicationNo IN (").append(createQuery(application_number)).append(")");
			addToPreparedStatement(preparedStmtList, application_number);
			
		}
		if (criteria.getFromDate() != null && criteria.getToDate() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" fsm.createdtime BETWEEN ").append(criteria.getFromDate()).append(" AND ")
					.append(criteria.getToDate());
		}else if (criteria.getFromDate() != null && criteria.getToDate() == null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" fsm.createdtime >= ").append(criteria.getFromDate());
		}
		List<String> ownerIds = criteria.getOwnerIds();
		if (!CollectionUtils.isEmpty(ownerIds)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" fsm.accountId IN (").append(createQuery(ownerIds)).append(")");
			addToPreparedStatement(preparedStmtList, ownerIds);
		}
		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
		
	}
	/**
	 * 
	 * @param query
	 *            prepared Query
	 * @param preparedStmtList
	 *            values to be replased on the query
	 * @param criteria
	 *            fsm search criteria
	 * @return the query by replacing the placeholders with preparedStmtList
	 */
	private String addPaginationWrapper(String query, List<Object> preparedStmtList, FSMSearchCriteria criteria) {
		
		int limit = config.getDefaultLimit();
		int offset = config.getDefaultOffset();
		String finalQuery = paginationWrapper.replace("{}", query);

		if (criteria.getLimit() != null && criteria.getLimit() <= config.getMaxSearchLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit()) {
			limit = config.getMaxSearchLimit();
		}

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		if (limit == -1) {
			finalQuery = finalQuery.replace("WHERE offset_ > ? AND offset_ <= ?", "");
		} else {
			preparedStmtList.add(offset);
			preparedStmtList.add(limit + offset);
		}
		
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
