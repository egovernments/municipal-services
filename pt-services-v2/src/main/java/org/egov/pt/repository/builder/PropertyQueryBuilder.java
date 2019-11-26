package org.egov.pt.repository.builder;

import java.util.List;
import java.util.Set;

import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.PropertyCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class PropertyQueryBuilder {
	
	@Autowired
	private PropertyConfiguration config;

	private static final String SELECT = "SELECT ";
	private static final String INNER_JOIN = "INNER JOIN";
	
	private static String PROEPRTY_ID_QUERY = "select propertyid from eg_pt_owner_v2 where userid IN ";

	 // Select query
	
	private static String propertySelectValues = "property.id as pid, property.propertyid, property.tenantid as ptenantid, accountid, oldpropertyid, property.status as propertystatus, acknowldgementnumber, propertytype, ownershipcategory, creationreason, occupancydate, constructiondate, nooffloors, landarea, source, parentproperties, property.createdby as pcreatedby, property.lastmodifiedby as plastmodifiedby, property.createdtime as pcreatedtime, property.lastmodifiedtime as plastmodifiedtime, property.additionaldetails as padditionaldetails, ";

	private static String addressSelectValues = "address.tenantid as adresstenantid, address.id as addressuuid, address.propertyid as addresspid, latitude, longitude, addressid, addressnumber, doorno, address.type as addresstype, addressline1, addressline2, landmark, city, pincode, detail as addressdetail, buildingname, street, locality, createdby as addresscreatedby, lastmodifiedby as addresslastmodifiedby, createdtime as addresscreatedtime, lastmodifiedtime as addresslastmodifiedtime, ";

	private static String institutionSelectValues = "institution.id as institutionid,institution.propertyid as institutionpid, institution.tenantid as institutiontenantid, institution.name as institutionname, institution.type as institutiontype, designation, institution.createdby as institutioncreatedby, institution.lastmodifiedby as institutionlastmodifiedby, institution.createdtime as institutioncreatedtime, institution.lastmodifiedtime as institutionlastmodifiedtime, ";

	private static String propertyDocSelectValues = "pdoc.id as pdocid, pdoc.tenantid as pdoctenantid, pdoc.entityid as pdocpid, pdoc.documenttype as pdocdocumenttype, pdoc.filestore as pdocfilestore, pdoc.documentuid as pdocdocumentuid, pdoc.status as pdocstatus, ";

	private static String ownerSelectValues = "owner.tenantid as owntenantid, owner.propertyid as ownpropertyid, userid, owner.status as ownstatus, isprimaryowner, ownertype, ownershippercentage, owner.institutionid as owninstitutionid, relationship, owner.createdby as owncreatedby, owner.createdtime as owncreatedtime,owner.lastmodifiedby as ownlastmodifiedby, owner.lastmodifiedtime as ownlastmodifiedtime, ";

	private static String ownerDocSelectValues = "owndoc.id as owndocid, owndoc.tenantid as owndoctenantid, owndoc.entityid as owndocpid, owndoc.documenttype as owndocdocumenttype, owndoc.filestore as owndocfilestore, owndoc.documentuid as owndocdocumentuid, owndoc.status as owndocstatus, ";

	private static final String QUERY = SELECT 
			
			+	propertySelectValues    
			
			+   addressSelectValues     
			
			+   institutionSelectValues 
			
			+   propertyDocSelectValues
			
			+   ownerSelectValues 
			
			+   ownerDocSelectValues    
			
			+   " FROM EG_PT_PROPERTY property " 
			
			+   INNER_JOIN +  " EG_PT_ADDRESS address         ON pid = address.addresspid " 
			
			+   INNER_JOIN +  " EG_PT_INSTITUTION institution ON pid = institution.institutionpid " 
			
			+   INNER_JOIN +  " EG_PT_DOCUMENT pdoc           ON pid = pdoc.pdocpid "
			
			+   INNER_JOIN +  " EG_PT_OWNER owner             ON pid = owner.ownpid " 
			
			+   INNER_JOIN +  " EG_PT_DOCUMENT owndoc         ON pid = owndocpid "
			
			+ " WHERE ";
	


	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY pid) offset_ FROM " + "({})" + " result) result_offset "
			+ "WHERE offset_ > ? AND offset_ <= ?";

	private String addPaginationWrapper(String query, List<Object> preparedStmtList, PropertyCriteria criteria) {
		
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

	/**
	 * 
	 * @param criteria
	 * @param preparedStmtList
	 * @return
	 */
	public String getPropertySearchQuery(PropertyCriteria criteria, List<Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(QUERY);

		builder.append(" property.tenantid=? ");
		preparedStmtList.add(criteria.getTenantId());

		if (null != criteria.getStatus()) {

			builder.append(" property.status = ?");
			preparedStmtList.add(criteria.getStatus());
		}

		Set<String> ids = criteria.getIds();
		if (!CollectionUtils.isEmpty(ids)) {

			builder.append("and property.propertyid IN (").append(createQuery(ids)).append(")");
			addToPreparedStatement(preparedStmtList, ids);
		}

		Set<String> oldpropertyids = criteria.getOldpropertyids();
		if (!CollectionUtils.isEmpty(oldpropertyids)) {

			builder.append("and property.oldpropertyid IN (").append(createQuery(oldpropertyids)).append(")");
			addToPreparedStatement(preparedStmtList, oldpropertyids);
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
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
