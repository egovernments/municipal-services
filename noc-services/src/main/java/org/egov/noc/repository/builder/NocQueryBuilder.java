package org.egov.noc.repository.builder;

import java.util.List;

import org.egov.noc.config.NOCConfiguration;
import org.egov.noc.web.model.NocSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class NocQueryBuilder {
	@Autowired
	private NOCConfiguration nocConfig;

	private static final String QUERY = "SELECT noc.*,nocdoc.*,noc.id as noc_id,noc.tenantid as noc_tenantId,noc.lastModifiedTime as "
			+ "noc_lastModifiedTime,noc.createdBy as noc_createdBy,noc.lastModifiedBy as noc_lastModifiedBy,noc.createdTime as "
			+ "noc_createdTime,noc.additionalDetails,noc.landId as noc_landId, nocdoc.id as noc_doc_id, nocdoc.additionalDetails as doc_details, "
			+ "nocdoc.documenttype as noc_doc_documenttype,nocdoc.filestoreid as noc_doc_filestore"
			+ " FROM eg_noc noc  LEFT OUTER JOIN "
			+ "eg_noc_document nocdoc ON nocdoc.nocid = noc.id";

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY noc_lastModifiedTime DESC) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > ? AND offset_ <= ?";

	/**
	 * To give the Search query based on the requirements.
	 * 
	 * @param criteria
	 *            NOC search criteria
	 * @param preparedStmtList
	 *            values to be replased on the query
	 * @return Final Search Query
	 */
	public String getNocSearchQuery(NocSearchCriteria criteria, List<Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(QUERY);

		if (criteria.getTenantId() != null) {
	        addClauseIfRequired(preparedStmtList, builder);
	        builder.append(" noc.tenantid=? ");
	        preparedStmtList.add(criteria.getTenantId());
		}

		List<String> ids = criteria.getIds();
		if (!CollectionUtils.isEmpty(ids)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" noc.id IN (").append(createQuery(ids)).append(")");
			addToPreparedStatement(preparedStmtList, ids);
		}		

		String applicationNo = criteria.getApplicationNo();
		if (applicationNo!=null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" noc.applicationNo =?");
			preparedStmtList.add(criteria.getApplicationNo());
		}
		
		String approvalNo = criteria.getNocNo();
		if (approvalNo!=null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" noc.nocNo = ?");
			preparedStmtList.add(criteria.getNocNo());
		}
		
		String source = criteria.getSource();
		if (source!=null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" noc.source = ?");
			preparedStmtList.add(criteria.getSource());
		}
		
		String sourceRefId = criteria.getSourceRefId();
		if (sourceRefId!=null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" noc.sourceRefId = ?");
			preparedStmtList.add(criteria.getSourceRefId());
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
	 *            bpa search criteria
	 * @return the query by replacing the placeholders with preparedStmtList
	 */
	private String addPaginationWrapper(String query, List<Object> preparedStmtList, NocSearchCriteria criteria) {

		int limit = nocConfig.getDefaultLimit();
		int offset = nocConfig.getDefaultOffset();
		String finalQuery = paginationWrapper.replace("{}", query);

		if (criteria.getLimit() != null && criteria.getLimit() <= nocConfig.getMaxSearchLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > nocConfig.getMaxSearchLimit()) {
			limit = nocConfig.getMaxSearchLimit();
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
