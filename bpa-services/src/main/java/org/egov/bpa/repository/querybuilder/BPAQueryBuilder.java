package org.egov.bpa.repository.querybuilder;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
public class BPAQueryBuilder {

	@Autowired
	private BPAConfiguration config;

	private static final String INNER_JOIN_STRING = " INNER JOIN ";
	private static final String LEFT_OUTER_JOIN_STRING = " LEFT OUTER JOIN ";

	private static final String QUERY = "SELECT bpa.*,bpaunit.*,bpaowner.*,"
			+ "bpaaddress.*,bpageolocation.*,bpadoc.*,bpaownerdoc.*,bpa.id as bpa_id,bpa.tenantid as bpa_tenantId,bpa.lastModifiedTime as "
			+ "bpa_lastModifiedTime,bpa.createdBy as bpa_createdBy,bpa.lastModifiedBy as bpa_lastModifiedBy,bpa.createdTime as "
			+ "bpa_createdTime,bpaaddress.id as bpa_ad_id,bpageolocation.id as bpa_geo_loc,"
			+ "bpaowner.id as bpaowner_uuid,"
			+ "bpaownerdoc.owner as docuserid,bpaownerdoc.id as ownerdocid,"
			+ "bpaownerdoc.documenttype as ownerdocType,bpaownerdoc.filestore as ownerfileStore,bpaownerdoc.buildingplanid as docdetailid,bpaownerdoc.documentuid as ownerdocuid,"
			+ "bpaunit.id as bpa_un_id, bpadoc.id as bpa_doc_id,bpadoc.documenttype as bpa_doc_documenttype,bpadoc.filestore as bpa_doc_filestore"
			+ " FROM eg_bpa_buildingplan bpa"
			+ INNER_JOIN_STRING
			+ "eg_bpa_address bpaaddress ON bpaaddress.buildingplanid = bpa.id"
			+ INNER_JOIN_STRING
			+ "eg_bpa_owner bpaowner ON bpaowner.buildingplanid = bpa.id"
			+ LEFT_OUTER_JOIN_STRING
			+ "eg_bpa_document_owner bpaownerdoc ON bpaownerdoc.owner = bpaowner.id"
			+ LEFT_OUTER_JOIN_STRING
			+ "eg_bpa_unit bpaunit ON bpaunit.buildingplanid = bpa.id"
			+ LEFT_OUTER_JOIN_STRING
			+ "eg_bpa_document bpadoc ON bpadoc.buildingplanid = bpa.id"
			+ LEFT_OUTER_JOIN_STRING
			+ "eg_bpa_geolocation bpageolocation ON bpageolocation.addressid = bpaaddress.id";;

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY bpa_id) offset_ FROM "
			+ "({})" + " result) result_offset "
			+ "WHERE offset_ > ? AND offset_ <= ?";

	public String getBPASearchQuery(BPASearchCriteria criteria,
			List<Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(QUERY);

		if (criteria.getTenantId() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpa.tenantid=? ");
			preparedStmtList.add(criteria.getTenantId());
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


		List<String> applicationNos = criteria.getApplicationNos();
		if (!CollectionUtils.isEmpty(applicationNos)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpa.applicationNo IN (").append(createQuery(applicationNos)).append(")");
			addToPreparedStatement(preparedStmtList, applicationNos);
		}
		
		
		
		if (criteria.getMobileNumber() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" bpaowner.mobileNumber = ? ");
			preparedStmtList.add(criteria.getMobileNumber());
		}


		

		

		return addPaginationWrapper(builder.toString(), preparedStmtList,
				criteria);

	}

	private String addPaginationWrapper(String query,
			List<Object> preparedStmtList, BPASearchCriteria criteria) {

		int limit = config.getDefaultLimit();
		int offset = config.getDefaultOffset();
		String finalQuery = paginationWrapper.replace("{}", query);

		if (criteria.getLimit() != null
				&& criteria.getLimit() <= config.getMaxSearchLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null
				&& criteria.getLimit() > config.getMaxSearchLimit())
			limit = config.getMaxSearchLimit();

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		preparedStmtList.add(offset);
		preparedStmtList.add(limit + offset);

		return finalQuery;

	}

	private void addClauseIfRequired(List<Object> values,
			StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND");
		}
	}

	private void addToPreparedStatement(List<Object> preparedStmtList,
			List<String> ids) {
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
