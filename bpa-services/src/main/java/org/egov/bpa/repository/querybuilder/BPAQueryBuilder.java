package org.egov.bpa.repository.querybuilder;

import java.util.Calendar;
import java.util.List;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class BPAQueryBuilder {

	@Autowired
	private BPAConfiguration config;

	private static final String INNER_JOIN_STRING = " INNER JOIN ";
	private static final String LEFT_OUTER_JOIN_STRING = " LEFT OUTER JOIN ";

	private static final String QUERY = "SELECT bpa.*,bpadoc.*,bpa.id as bpa_id,bpa.tenantid as bpa_tenantId,bpa.lastModifiedTime as "
			+ "bpa_lastModifiedTime,bpa.createdBy as bpa_createdBy,bpa.lastModifiedBy as bpa_lastModifiedBy,bpa.createdTime as "
			+ "bpa_createdTime,bpa.additionalDetails,bpa.landId as bpa_landId, bpadoc.id as bpa_doc_id,bpadoc.documenttype as bpa_doc_documenttype,bpadoc.filestoreid as bpa_doc_filestore"
			+ " FROM eg_bpa_buildingplan bpa"
			+ LEFT_OUTER_JOIN_STRING
			+ "eg_bpa_document bpadoc ON bpadoc.buildingplanid = bpa.id";;

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
	public String getBPASearchQuery(BPASearchCriteria criteria, List<Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(QUERY);

		if (criteria.getTenantId() != null) {
			if (criteria.getTenantId().split("\\.").length == 1) {

				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" bpa.tenantid like ?");
				preparedStmtList.add('%' + criteria.getTenantId() + '%');
			} else {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" bpa.tenantid=? ");
				preparedStmtList.add(criteria.getTenantId());
			}
		}

		List<String> ids = criteria.getIds();
		if (!CollectionUtils.isEmpty(ids)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpa.id IN (").append(createQuery(ids)).append(")");
			addToPreparedStatement(preparedStmtList, ids);
		}

		List<String> edcrNumbers = criteria.getEdcrNumbers();
		if (!CollectionUtils.isEmpty(edcrNumbers)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpa.edcrNumber IN (").append(createQuery(edcrNumbers)).append(")");
			addToPreparedStatement(preparedStmtList, edcrNumbers);
		}

		List<String> applicationNos = criteria.getApplicationNo();
		if (!CollectionUtils.isEmpty(applicationNos)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpa.applicationNo IN (").append(createQuery(applicationNos)).append(")");
			addToPreparedStatement(preparedStmtList, applicationNos);
		}
		
		List<String> approvalNo = criteria.getApprovalNo();
		if (!CollectionUtils.isEmpty(approvalNo)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpa.approvalNo IN (").append(createQuery(approvalNo)).append(")");
			addToPreparedStatement(preparedStmtList, approvalNo);
		}
		String landId = criteria.getLandId();
		if (landId!=null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpa.landId = ?");
			preparedStmtList.add(criteria.getLandId());
		}
		List<String> status = criteria.getStatus();
		if (!CollectionUtils.isEmpty(status)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpa.status IN (").append(createQuery(status)).append(")");
			addToPreparedStatement(preparedStmtList, status);
		}
		Long permitDt = criteria.getApprovalDate();
		if ( permitDt != null) {
			
			Calendar permitDate = Calendar.getInstance();
			permitDate.setTimeInMillis(permitDt);
			
			int year = permitDate.get(Calendar.YEAR);
		    int month = permitDate.get(Calendar.MONTH);
		    int day = permitDate.get(Calendar.DATE);
			
			Calendar permitStrDate = Calendar.getInstance();
			permitStrDate.setTimeInMillis(0);
			permitStrDate.set(year, month, day, 0, 0, 0);
			
			Calendar permitEndDate = Calendar.getInstance();
			permitEndDate.setTimeInMillis(0);
			permitEndDate.set(year, month, day, 23, 59, 59);
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpa.approvalDate BETWEEN ").append(permitStrDate.getTimeInMillis()).append(" AND ")
			.append(permitEndDate.getTimeInMillis());
			
		}
		
		/*if (criteria.getMobileNumber() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpaowner.mobileNumber = ? ");
			preparedStmtList.add(criteria.getMobileNumber());

		}*/
		/*else if (criteria.getCreatedBy() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" ( bpa.createdby = ? ");
			preparedStmtList.add(criteria.getCreatedBy());
			builder.append(" OR bpaowner.id = ? )");
			preparedStmtList.add(criteria.getCreatedBy());
		}

		if (criteria.getFromDate() != null && criteria.getToDate() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpa.createdtime BETWEEN ").append(criteria.getFromDate()).append(" AND ")
					.append(criteria.getToDate());
		} else if (criteria.getFromDate() != null && criteria.getToDate() == null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpa.createdtime >= ").append(criteria.getFromDate());
		}
		
		if(criteria.getApplicationDate() != null){
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpa.applicationDate = ").append(criteria.getApplicationDate());
		}

		if(criteria.getOrderGeneratedDate() != null){
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpa.orderGeneratedDate = ").append(criteria.getOrderGeneratedDate());
		}*/
		
//		addClauseIfRequired(preparedStmtList, builder);
//		builder.append(" bpaowner.active = TRUE"); // To get the active owners

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
	private String addPaginationWrapper(String query, List<Object> preparedStmtList, BPASearchCriteria criteria) {

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
