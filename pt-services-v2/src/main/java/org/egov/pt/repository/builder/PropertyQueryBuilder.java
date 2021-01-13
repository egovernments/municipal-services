package org.egov.pt.repository.builder;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.web.models.PropertyCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class PropertyQueryBuilder {
	
	@Autowired
	private PropertyConfiguration config;

	private static final String INNER_JOIN_STRING = "INNER JOIN";
	private static final String LEFT_OUTER_JOIN_STRING = "LEFT OUTER JOIN";

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

	private static final String LIKE_QUERY = "SELECT pt.*,ptdl.*,address.*,owner.*,doc.*,unit.*,insti.*,"
			+ " pt.propertyid as ptid,ptdl.assessmentnumber as propertydetailid,doc.id as documentid,unit.id as unitid,"
			+ "address.id as addresskeyid,insti.id as instiid,pt.additionalDetails as pt_additionalDetails,"
			+ "ownerdoc.id as ownerdocid,ownerdoc.documenttype as ownerdocType,ownerdoc.filestore as ownerfileStore,"
			+ "ownerdoc.documentuid as ownerdocuid, ptdl.additionalDetails as ptdl_additionalDetails,"
			+ "ptdl.createdby as assesscreatedby,ptdl.lastModifiedBy as assesslastModifiedBy,ptdl.createdTime as assesscreatedTime,"
			+ "ptdl.lastModifiedTime as assesslastModifiedTime,"
			+ "ptdl.status as propertydetailstatus, unit.occupancyDate as unitoccupancyDate,"
			+ "insti.name as institutionname,insti.type as institutiontype,insti.tenantid as institenantId,"
			+ "ownerdoc.userid as docuserid,ownerdoc.propertydetail as docassessmentnumber,"
			+ "unit.usagecategorymajor as unitusagecategorymajor,unit.usagecategoryminor as unitusagecategoryminor,"
			+ "unit.additionalDetails as unit_additionalDetails,owner.additionalDetails as ownerInfo_additionalDetails,"
			+ "insti.additionalDetails as insti_additionalDetails,address.additionalDetails as add_additionalDetails,"
			+ "pt.lastModifiedTime as propertylastModifiedTime, pt.createdby as propertyCreatedBy, "
			+ "pt.createdtime as propertyCreatedTime, pt.lastModifiedby as propertyModifiedBy"
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
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY ptid) offset_ FROM " + "({})" + " result) result_offset "
			+ "WHERE offset_ > :offset AND offset_ <= :limit";


    private static final String LocalityQuery = "SELECT property FROM eg_pt_address_v2 addr WHERE_CLAUSE_PLACEHOLDER_LOCALITY";
    private static final String OldPropertyQuery = "SELECT propertyid FROM eg_pt_property_v2 prop WHERE_CLAUSE_PLACEHOLDER_OLDPROPERTY";
    private static final String CreatedTimeQuery = "select maxassess.createdtime from (select distinct property, max(createdtime) as createdtime from eg_pt_propertydetail_v2 ptd"
    		+ "WHERE_CLAUSE_PLACEHOLDER_CREATEDTIME group by ptd.property) as maxassess";
	private static final String NEWQUERY = "SELECT asmt.*,address.*,owner.*,unit.*,insti.*,unit.id as unitid,"+
			"  	 address.id as addresskeyid,insti.id as instiid, "+
			"    unit.occupancyDate as unitoccupancyDate, "+
			"    insti.name as institutionname,insti.type as institutiontype,insti.tenantid as institenantId, "+
			"    unit.usagecategorymajor as unitusagecategorymajor,unit.usagecategoryminor as unitusagecategoryminor,"+
			"    unit.additionalDetails as unit_additionalDetails,owner.additionalDetails as ownerInfo_additionalDetails,"+
			"    insti.additionalDetails as insti_additionalDetails,address.additionalDetails as add_additionalDetails "+
			"    FROM (" +
			"    select *,pt.propertyid as ptid,ptdl.assessmentnumber as propertydetailid,pt.additionalDetails as pt_additionalDetails, "+
			"    ptdl.additionalDetails as ptdl_additionalDetails,ptdl.createdby as assesscreatedby,"+
			"    ptdl.lastModifiedBy as assesslastModifiedBy,ptdl.createdTime as assesscreatedTime,"+
			"    ptdl.lastModifiedTime as assesslastModifiedTime,ptdl.createdby as assesscreatedby," +
			"    pt.lastModifiedBy as propertyModifiedBy,pt.createdTime as propertyCreatedTime," +
			"    pt.lastModifiedTime as propertylastModifiedTime,pt.createdby as propertyCreatedby,"+
			"    ptdl.status as propertydetailstatus "+
			"    FROM eg_pt_property_v2 pt INNER JOIN eg_pt_propertydetail_v2 ptdl ON pt.propertyid =ptdl.property " +
			"	 WHERE_CLAUSE_PLACHOLDER_PROPERTY ) as asmt "+
				 INNER_JOIN_STRING+
			"    eg_pt_owner_v2 owner ON asmt.assessmentnumber=owner.propertydetail     " +
				 INNER_JOIN_STRING+
			"    eg_pt_address_v2 address on address.property=asmt.ptid      " +
				 LEFT_OUTER_JOIN_STRING+
			"    eg_pt_unit_v2 unit ON asmt.assessmentnumber=unit.propertydetail      " +
				 LEFT_OUTER_JOIN_STRING+
			"    eg_pt_institution_v2 insti ON asmt.assessmentnumber=insti.propertydetail WHERE_CLAUSE_PLACHOLDER ";
	

	public String getPropertyLikeQuery(PropertyCriteria criteria, Map<String,Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(LIKE_QUERY);	
		
		if(!StringUtils.isEmpty(criteria.getTenantId())) {
			if(criteria.getTenantId().equals("pb")) {
				builder.append("pt.tenantid LIKE :tenantid ");
				preparedStmtList.put("tenantid","pb%");
			}else {
				builder.append("pt.tenantid = :tenantid ");
				preparedStmtList.put("tenantid",criteria.getTenantId());
			}
		}else {
			builder.append("pt.tenantid LIKE :tenantid ");
			preparedStmtList.put("tenantid","pb%");
		}
		
        return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);

	}

	private String addPaginationWrapper(String query, Map<String,Object> preparedStmtList,
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

//		preparedStmtList.add(offset);
//		preparedStmtList.add(limit + offset);
		preparedStmtList.put("offset", offset);
		preparedStmtList.put("limit", limit + offset);


		return finalQuery;
	}

	public String getPropertySearchQuery(PropertyCriteria criteria, Map<String,Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(NEWQUERY);

		StringBuilder WHERE_CLAUSE_PLACHOLDER_ASSESSMENT = new StringBuilder("");
		
		StringBuilder WHERE_CLAUSE_PLACEHOLDER_LOCALITY = new StringBuilder("");
		
		StringBuilder WHERE_CLAUSE_PLACEHOLDER_OLDPROPERTY = new StringBuilder("");
		
		StringBuilder WHERE_CLAUSE_PLACHOLDER_PROPERTY = new StringBuilder("");

		StringBuilder WHERE_CLAUSE_PLACHOLDER = new StringBuilder("");
		
		StringBuilder WHERE_CLAUSE_PLACEHOLDER_CREATEDTIME = new StringBuilder("");
		

		if (criteria.getAccountId() != null) {
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER);
			WHERE_CLAUSE_PLACHOLDER.append(" asmt.accountid = :accountid ");
//			preparedStmtList.add(criteria.getAccountId());
			preparedStmtList.put("accountid", criteria.getAccountId());


			Set<String> ownerids = criteria.getOwnerids();
			if (!CollectionUtils.isEmpty(ownerids)) {
				WHERE_CLAUSE_PLACHOLDER.append(" OR ");
				WHERE_CLAUSE_PLACHOLDER.append(" owner.userid IN ( :ownerids) ");
//				addToPreparedStatement(preparedStmtList, ownerids);
				preparedStmtList.put("ownerids", ownerids);

			}

			String defaultQuery =  builder.toString().replace("WHERE_CLAUSE_PLACHOLDER_ASSESSMENT",WHERE_CLAUSE_PLACHOLDER_ASSESSMENT.toString())
					.replace("WHERE_CLAUSE_PLACHOLDER_PROPERTY","").replace("WHERE_CLAUSE_PLACHOLDER",WHERE_CLAUSE_PLACHOLDER);

			return addPaginationWrapper(defaultQuery, preparedStmtList, criteria);
		}

		
		if (criteria.getPropertyDetailStatus() != null) {
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER_PROPERTY);
			WHERE_CLAUSE_PLACHOLDER_PROPERTY.append(" ptdl.status = :status ");
//			preparedStmtList.add(criteria.getPropertyDetailStatus());
			preparedStmtList.put("status", criteria.getPropertyDetailStatus());

		}else {
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER_PROPERTY);
			WHERE_CLAUSE_PLACHOLDER_PROPERTY.append(" ptdl.status = 'ACTIVE' ");
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER_PROPERTY);
			WHERE_CLAUSE_PLACHOLDER_PROPERTY.append(" ptdl.createdtime IN (").append(CreatedTimeQuery).append(")");

		}

		Set<String> propertyDetailids = criteria.getPropertyDetailids();
		if (!CollectionUtils.isEmpty(propertyDetailids)) {
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER_PROPERTY);
			WHERE_CLAUSE_PLACHOLDER_PROPERTY.append(" ptdl.assessmentnumber IN ( :propertyDetailids)");
//			addToPreparedStatement(preparedStmtList, propertyDetailids);
			preparedStmtList.put("propertyDetailids", propertyDetailids);

		}

		if(criteria.getAsOnDate()!=null){
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER_PROPERTY);
			WHERE_CLAUSE_PLACHOLDER_PROPERTY.append(" createdTime <= :createdTime");
//			preparedStmtList.add(criteria.getAsOnDate());
			preparedStmtList.put("createdTime", criteria.getAsOnDate());

		}

		if(criteria.getFinancialYear()!=null){
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER_PROPERTY);
			WHERE_CLAUSE_PLACHOLDER_PROPERTY.append(" financialYear = :financialYear");
//			preparedStmtList.add(criteria.getFinancialYear());
			preparedStmtList.put("financialYear", criteria.getFinancialYear());

		}

		if(criteria.getTenantId()!=null){
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER_PROPERTY);
			WHERE_CLAUSE_PLACHOLDER_PROPERTY.append("  pt.tenantid= :tenantid ");
			addClauseIfRequired(WHERE_CLAUSE_PLACEHOLDER_CREATEDTIME);
			WHERE_CLAUSE_PLACEHOLDER_CREATEDTIME.append("  ptd.tenantid= :tenantid ");
//			preparedStmtList.add(criteria.getTenantId());
			preparedStmtList.put("tenantid",criteria.getTenantId() );


		}

		Set<String> statuses = new HashSet<>();
		criteria.getStatuses().forEach(statusEnum -> {
			statuses.add(statusEnum.toString());
		});

		if (!CollectionUtils.isEmpty(statuses)) {
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER_PROPERTY);
			WHERE_CLAUSE_PLACHOLDER_PROPERTY.append(" pt.status IN ( :statuses)");
//			addToPreparedStatement(preparedStmtList, statuses);
			preparedStmtList.put("statuses", statuses);

		}

		Set<String> ids = criteria.getIds();
		if (!CollectionUtils.isEmpty(ids)) {
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER_PROPERTY);
			WHERE_CLAUSE_PLACHOLDER_PROPERTY.append(" pt.propertyid IN ( :ids)");
			addClauseIfRequired(WHERE_CLAUSE_PLACEHOLDER_CREATEDTIME);
			WHERE_CLAUSE_PLACEHOLDER_CREATEDTIME.append(" ptd.property IN ( :ids)");
//			addToPreparedStatement(preparedStmtList, ids);
			preparedStmtList.put("ids", ids);


		}

		Set<String> oldpropertyids = criteria.getOldpropertyids();
		if (!CollectionUtils.isEmpty(oldpropertyids)) {
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER_PROPERTY);
			WHERE_CLAUSE_PLACHOLDER_PROPERTY.append(" pt.propertyid IN (").append(OldPropertyQuery).append(")");
			addClauseIfRequired(WHERE_CLAUSE_PLACEHOLDER_CREATEDTIME);
			WHERE_CLAUSE_PLACEHOLDER_CREATEDTIME.append(" ptd.property IN (").append(OldPropertyQuery).append(")");
			addClauseIfRequired(WHERE_CLAUSE_PLACEHOLDER_OLDPROPERTY);
			WHERE_CLAUSE_PLACEHOLDER_OLDPROPERTY.append(" prop.oldpropertyid IN ( :oldpropertyids)");
//			addToPreparedStatement(preparedStmtList, oldpropertyids);
			preparedStmtList.put("oldpropertyids", oldpropertyids);


		}


		Set<String> addressids = criteria.getAddressids();
		if (!CollectionUtils.isEmpty(addressids)) {
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER);
			WHERE_CLAUSE_PLACHOLDER.append(" address.id IN ( :addressids)");
//			addToPreparedStatement(preparedStmtList, addressids);
			preparedStmtList.put("addressids", addressids);

		}

		Set<String> ownerids = criteria.getOwnerids();
		if (!CollectionUtils.isEmpty(ownerids)) {
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER);
			WHERE_CLAUSE_PLACHOLDER.append(" owner.userid IN ( :ownerids)");
//			addToPreparedStatement(preparedStmtList, ownerids);
			preparedStmtList.put("ownerids", ownerids);

		}

		Set<String> unitids = criteria.getUnitids();
		if (!CollectionUtils.isEmpty(unitids)) {
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER);
			WHERE_CLAUSE_PLACHOLDER.append(" unit.id IN ( :unitids )");
			preparedStmtList.put("unitids", unitids);
		}

		Set<String> documentids = criteria.getDocumentids();
		if (!CollectionUtils.isEmpty(documentids)) {
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER);
			WHERE_CLAUSE_PLACHOLDER.append(" doc.id IN ( :documentids)");
//			addToPreparedStatement(preparedStmtList, documentids);
			preparedStmtList.put("documentids", documentids);

		}

		if (criteria.getDoorNo() != null && criteria.getLocality() != null) {
			addClauseIfRequired(WHERE_CLAUSE_PLACHOLDER_PROPERTY);
			WHERE_CLAUSE_PLACHOLDER_PROPERTY.append(" pt.propertyid IN (").append(LocalityQuery).append(")");
			addClauseIfRequired(WHERE_CLAUSE_PLACEHOLDER_CREATEDTIME);
			WHERE_CLAUSE_PLACEHOLDER_CREATEDTIME.append(" ptd.property IN (").append(LocalityQuery).append(")");
			addClauseIfRequired(WHERE_CLAUSE_PLACEHOLDER_LOCALITY);
			WHERE_CLAUSE_PLACEHOLDER_LOCALITY.append(" addr.doorno = :doorno ").append(" and addr.locality = :locality ");
//			preparedStmtList.add(criteria.getDoorNo());
//			preparedStmtList.add(criteria.getLocality());
			preparedStmtList.put("doorno", criteria.getDoorNo());
			preparedStmtList.put("locality", criteria.getLocality());

		}

        String query = builder.toString();

		query = query.replace("WHERE_CLAUSE_PLACHOLDER_ASSESSMENT",WHERE_CLAUSE_PLACHOLDER_ASSESSMENT);

		query = query.replace("WHERE_CLAUSE_PLACHOLDER_PROPERTY",WHERE_CLAUSE_PLACHOLDER_PROPERTY);
		
		query = query.replace("WHERE_CLAUSE_PLACHOLDER",WHERE_CLAUSE_PLACHOLDER);

		query = query.replace("WHERE_CLAUSE_PLACEHOLDER_CREATEDTIME",WHERE_CLAUSE_PLACEHOLDER_CREATEDTIME);

		query = query.replace("WHERE_CLAUSE_PLACEHOLDER_LOCALITY",WHERE_CLAUSE_PLACEHOLDER_LOCALITY);

		query = query.replace("WHERE_CLAUSE_PLACEHOLDER_OLDPROPERTY", WHERE_CLAUSE_PLACEHOLDER_OLDPROPERTY);

		return addPaginationWrapper(query, preparedStmtList, criteria);

	}

	/*
	 * private String createQuery(Set<String> ids) {
	 * 
	 * final String quotes = "'"; final String comma = ","; StringBuilder builder =
	 * new StringBuilder(); Iterator<String> iterator = ids.iterator();
	 * while(iterator.hasNext()) {
	 * builder.append(quotes).append(iterator.next()).append(quotes);
	 * if(iterator.hasNext()) builder.append(comma); } return builder.toString(); }
	 */

	private String createQuery(Set<String> ids) {
		StringBuilder builder = new StringBuilder();
		int length = ids.size();
		for (String id : ids) {
			builder.append(id+",");
		}
		return builder.toString().substring(0, builder.toString().length()-1);
	}

//	private void addToPreparedStatement(Map<String,Object> preparedStmtList, Set<String> ids) {
//		ids.forEach(id -> {
//			preparedStmtList.add(id);
//		});
//	}

	private void addClauseIfRequired(StringBuilder builder){
		if(builder.toString().isEmpty())
			builder.append(" WHERE");
		else builder.append(" AND ");
	}

}
