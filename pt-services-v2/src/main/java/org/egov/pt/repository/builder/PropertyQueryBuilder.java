package org.egov.pt.repository.builder;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.PropertyCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class PropertyQueryBuilder {
	
	@Autowired
	private PropertyConfiguration config;

	private static final String INNER_JOIN_STRING = "INNER JOIN";
	private static final String LEFT_OUTER_JOIN_STRING = "LEFT OUTER JOIN";
	
	private static String PROEPRTY_ID_QUERY = "select propertyid from eg_pt_owner_v2 where id IN ";

	private static final String QUERY = "SELECT pt.*,ptdl.*,address.*,owner.*,doc.*,unit.*,insti.*,"
			+ " pt.propertyid as propertyid,ptdl.assessmentnumber as propertydetailid,doc.id as documentid,unit.id as unitid,"
			+ "address.id as addresskeyid,insti.id as instiid,pt.additionalDetails as pt_additionalDetails,"
			+ "ownerdoc.id as ownerdocid,ownerdoc.documenttype as ownerdocType,ownerdoc.filestore as ownerfileStore,"
			+ "ownerdoc.documentuid as ownerdocuid,ptdl.additionalDetails as ptdl_additionalDetails,"
			+ "ptdl.createdby as assesscreatedby,ptdl.lastModifiedBy as assesslastModifiedBy,ptdl.createdTime as assesscreatedTime,"
			+ "ptdl.lastModifiedTime as assesslastModifiedTime,"
			+ "ptdl.status as propertydetailstatus, unit.occupancyDate as unitoccupancyDate,"
			+ "insti.name as institutionname,insti.type as institutiontype,insti.tenantid as institenantId,"
			+ "ownerdoc.userid as docuserid,ownerdoc.propertydetail as docassessmentnumber,"
			+ "unit.usagecategorymajor as unitusagecategorymajor,unit.usagecategoryminor as unitusagecategoryminor"
			+ " FROM eg_pt_property_v2 pt " + INNER_JOIN_STRING
			+ " eg_pt_propertydetail_v2 ptdl ON pt.propertyid =ptdl.property " + INNER_JOIN_STRING
			+ " eg_pt_owner_v2 owner ON ptdl.assessmentnumber=owner.propertydetail " + INNER_JOIN_STRING
			+ " eg_pt_address_v2 address on address.property=pt.propertyid " + LEFT_OUTER_JOIN_STRING
			+ " eg_pt_unit_v2 unit ON ptdl.assessmentnumber=unit.propertydetail " + LEFT_OUTER_JOIN_STRING
			+ " eg_pt_document_propertydetail_v2 doc ON ptdl.assessmentnumber=doc.propertydetail "
			+ LEFT_OUTER_JOIN_STRING + " eg_pt_document_owner_v2 ownerdoc ON ownerdoc.userid=owner.userid "
			+ LEFT_OUTER_JOIN_STRING + " eg_pt_institution_v2 insti ON ptdl.assessmentnumber=insti.propertydetail "
			+ " WHERE ";

	private static final String  LIKE_QUERY = "SELECT pt.*,ptdl.*,address.*,owner.*,doc.*,unit.*,insti.*,"
			+ " pt.propertyid as propid,ptdl.assessmentnumber as propertydetailid,doc.id as documentid,unit.id as unitid,"
			+ "address.id as addresskeyid,insti.id as instiid,pt.additionalDetails as pt_additionalDetails,"
			+ "ownerdoc.id as ownerdocid,ownerdoc.documenttype as ownerdocType,ownerdoc.filestore as ownerfileStore,"
			+ "ownerdoc.documentuid as ownerdocuid, ptdl.additionalDetails as ptdl_additionalDetails,"
			+ "ptdl.createdby as assesscreatedby,ptdl.lastModifiedBy as assesslastModifiedBy,ptdl.createdTime as assesscreatedTime,"
			+ "ptdl.lastModifiedTime as assesslastModifiedTime,"
			+ "ptdl.status as propertydetailstatus, unit.occupancyDate as unitoccupancyDate,"
			+ "insti.name as institutionname,insti.type as institutiontype,insti.tenantid as institenantId,"
			+ "ownerdoc.userid as docuserid,ownerdoc.propertydetail as docassessmentnumber,"
			+ "unit.usagecategorymajor as unitusagecategorymajor,unit.usagecategoryminor as unitusagecategoryminor"
			+ " FROM eg_pt_property_v2 pt " + INNER_JOIN_STRING
			+ " eg_pt_propertydetail_v2 ptdl ON pt.propertyid =ptdl.property " + INNER_JOIN_STRING
			+ " eg_pt_owner_v2 owner ON ptdl.assessmentnumber=owner.propertydetail " + INNER_JOIN_STRING
			+ " eg_pt_address_v2 address on address.property=pt.propertyid " + LEFT_OUTER_JOIN_STRING
			+ " eg_pt_unit_v2 unit ON ptdl.assessmentnumber=unit.propertydetail " + LEFT_OUTER_JOIN_STRING
			+ " eg_pt_document_propertydetail_v2 doc ON ptdl.assessmentnumber=doc.propertydetail "
			+ LEFT_OUTER_JOIN_STRING + " eg_pt_document_owner_v2 ownerdoc ON ownerdoc.userid=owner.userid "
			+ LEFT_OUTER_JOIN_STRING + " eg_pt_institution_v2 insti ON ptdl.assessmentnumber=insti.propertydetail "
			+ " WHERE ";

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY propid) offset_ FROM " + "({})" + " result) result_offset "
			+ "WHERE offset_ > ? AND offset_ <= ?";

	public String getPropertyLikeQuery(PropertyCriteria criteria, List<Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(LIKE_QUERY);	
		
		if(!StringUtils.isEmpty(criteria.getTenantId())) {
			if(criteria.getTenantId().equals("pb")) {
				builder.append("pt.tenantid LIKE ? ");
				preparedStmtList.add("pb%");
			}else {
				builder.append("pt.tenantid = ? ");
				preparedStmtList.add(criteria.getTenantId());
			}
		}else {
			builder.append("pt.tenantid LIKE ? ");
			preparedStmtList.add("pb%");
		}
		
        return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);

	}

	private String addPaginationWrapper(String query, List<Object> preparedStmtList,
			PropertyCriteria criteria) {
		Long limit = config.getDefaultLimit();
		Long offset = config.getDefaultOffset();
		String finalQuery = paginationWrapper.replace("{}", query);

		if (criteria.getLimit() != null && criteria.getLimit() <= config.getMaxSearchLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit())
			limit = config.getMaxSearchLimit();

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		preparedStmtList.add(offset);
		preparedStmtList.add(limit + offset);

		return finalQuery;
	}

	public String getPropertySearchQuery(PropertyCriteria criteria, List<Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(QUERY);

		builder.append(" and pt.tenantid=? ");
		preparedStmtList.add(criteria.getTenantId());
		
		if (null != criteria.getStatus()) {
			
			builder.append(" pt.status = ?");
			preparedStmtList.add(criteria.getStatus());
		}

		Set<String> ids = criteria.getIds();
		if (!CollectionUtils.isEmpty(ids)) {

			builder.append("and pt.propertyid IN (").append(createQuery(ids)).append(")");
			addToPreparedStatement(preparedStmtList, ids);
		}

		Set<String> oldpropertyids = criteria.getOldpropertyids();
		if (!CollectionUtils.isEmpty(oldpropertyids)) {

			builder.append("and pt.oldpropertyid IN (").append(createQuery(oldpropertyids)).append(")");
			addToPreparedStatement(preparedStmtList, oldpropertyids);
		}

		return builder.toString();
	}

	public String getPropertyIdsQuery(Set<String> ownerIds, List<Object> preparedStmtList) {

		StringBuilder query = new StringBuilder(PROEPRTY_ID_QUERY);
		query.append("(");
		createQuery(ownerIds);
		addToPreparedStatement(preparedStmtList, ownerIds);
		query.append(")");
		return query.toString();
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

	private void addToPreparedStatement(List<Object> preparedStmtList, Set<String> ids) {
		ids.forEach(id -> {
			preparedStmtList.add(id);
		});
	}

}
