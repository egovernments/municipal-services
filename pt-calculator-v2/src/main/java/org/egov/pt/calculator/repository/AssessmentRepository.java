package org.egov.pt.calculator.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.calculator.repository.rowmapper.AssessmentRowMapper;
import org.egov.pt.calculator.repository.rowmapper.DefaultersRowMapper;
import org.egov.pt.calculator.repository.rowmapper.PropertyRowMapper;
import org.egov.pt.calculator.util.CalculatorUtils;
import org.egov.pt.calculator.web.models.Assessment;
import org.egov.pt.calculator.web.models.CreateAssessmentRequest;
import org.egov.pt.calculator.web.models.DefaultersInfo;
import org.egov.pt.calculator.web.models.property.AuditDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

import org.egov.pt.calculator.web.models.property.Property;


/**
 * Persists and retrieves the assessment data from DB
 * 
 * @author kavi elrey
 */
@Repository
@Slf4j
public class AssessmentRepository {
	
	private static final String PROPERTY_SEARCH_QUERY = "select distinct prop.id,prop.propertyid,prop.acknowldgementNumber,prop.propertytype,prop.status,prop.ownershipcategory,prop.oldPropertyId,prop.createdby,prop.createdTime,prop.lastmodifiedby,prop.lastmodifiedtime,prop.tenantid from eg_pt_property prop inner join eg_pt_address addr ON prop.id = addr.propertyid and prop.tenantid=addr.tenantid left join eg_pt_unit unit ON prop.id = unit.propertyid and prop.tenantid=addr.tenantid where prop.status='ACTIVE' ";

	private static final String ASSESSMENT_SEARCH_QUERY = "select id,assessmentnumber from eg_pt_asmt_assessment where status='ACTIVE' and propertyid=:propertyid and financialyear=:financialyear and tenantid=:tenantid";

	private static final String ASSESSMENT_DETAIL_SEARCH_QUERY = "select id,assessmentnumber,financialyear as assessmentyear,assessmentdate,propertyId,source,channel,tenantId from eg_pt_asmt_assessment where status='ACTIVE' and propertyid=:propertyid and financialyear=:financialyear and tenantid=:tenantid";

	
	private static final String ASSESSMENT_JOB_DATA_INSERT_QUERY = "Insert into eg_pt_assessment_job (id,assessmentnumber,propertyid,financialyear,createdtime,status,error,additionaldetails,tenantid) values(:id,:assessmentnumber,:propertyid,:financialyear,:createdtime,:status,:error,:additionalDetails,:tenantid)";;

	private static final String OCUUPANCY_TYPE_RENTED = "RENTED";
	
	private static final String INNER_QUERY = "select pt.propertyid,usr.name ownername,usr.mobilenumber,sum(dd.taxamount - dd.collectionamount) balance from eg_pt_property pt,eg_pt_owner ownr,eg_user usr,egbs_demanddetail_v1 dd, egbs_demand_v1 d "
			+ " where ownr.propertyid = pt.id and ownr.tenantid=pt.tenantid and usr.uuid=ownr.userid and dd.demandid=d.id and d.consumercode = pt.propertyid and d.tenantid = pt.tenantid and pt.status='ACTIVE' and d.status = 'ACTIVE'";
	private static final String OUTER_QUERY = "select result.propertyid,result.ownername,result.mobilenumber,result.balance from ({duequery}) as result where result.balance > 0 limit 10";

	private static final String GROUP_BY_CLAUSE = " group by pt.propertyid,usr.name,usr.mobilenumber";

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	@Autowired
	private AssessmentRowMapper rowMapper;
	
	@Autowired
	private CalculatorUtils utils;
	
	@Autowired
	private PropertyRowMapper propertyRowMapper;
	
	@Autowired 
	private AssessmentRowMapper assessmentRowmapper;
	
	@Autowired
	private DefaultersRowMapper defaultersRowMapper;
	
	/**
	 * Retrieves assessments for the given query
	 * 
	 * @param query
	 * @param preparedStatementList
	 * @return
	 */
	public List<Assessment> getAssessments(String query, Object[] preparedStatementList) {
		return jdbcTemplate.query(query, preparedStatementList, rowMapper);
	}

	/**
	 * Saves the assessments in to assessment table
	 * 
	 * @param assessments
	 * @param info
	 * @return
	 */
	public List<Assessment> saveAssessments(List<Assessment> assessments, RequestInfo info){
	
		jdbcTemplate.batchUpdate(utils.getAssessmentInsertQuery(), new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int rowNum) throws SQLException {

				Assessment current = assessments.get(rowNum);
				AuditDetails audit = current.getAuditDetails();

				ps.setString(1, current.getUuid());
				ps.setString(2, current.getAssessmentNumber());
				ps.setString(3, current.getAssessmentYear());
				ps.setString(4, current.getDemandId());
				ps.setString(5, current.getPropertyId());
				ps.setString(6, current.getTenantId());
				ps.setString(7, audit.getCreatedBy());
				ps.setLong(8, audit.getCreatedTime());
				ps.setString(9, audit.getLastModifiedBy());
				if (audit.getLastModifiedTime() == null)
					ps.setLong(10, 0);
				else
					ps.setLong(10, audit.getLastModifiedTime());
			}
			
			@Override
			public int getBatchSize() {
				return assessments.size();
			}
		});
		return assessments;
	}
	
	public List<Property> fetchAllActiveProperties(CreateAssessmentRequest request) {
		StringBuilder query = new StringBuilder(PROPERTY_SEARCH_QUERY);
		final Map<String, Object> params = new HashMap<>();
		if (request.getLocality() != null) {
			query.append(" and addr.locality in (:locality) ");
			params.put("locality", request.getLocality());
		}
		//currently this filter is disabled in MDMS config
		/*if (request.getPropertyType() != null) {
			query.append(" and SPLIT_PART(prop.usagecategory,'.',1) in (:propertytype) ");
			params.put("propertytype", request.getPropertyType());
		}*/
		
		/*
		 * Include or exclude rented properties based on isRented flag in MDMS
		 * config if true then include rented properties, else exclude rented
		 * properties (If any one of the unit of the property is Rented then
		 * total property will be considered as Rented)
		 */
		
		if (!request.getIsRented()) {
			query.append(
					" and prop.id not in (select propertyid from eg_pt_unit where tenantid=:tenantid and occupancytype = :occupancytype)");
			params.put("occupancytype", OCUUPANCY_TYPE_RENTED);
		}

		query.append(" and prop.tenantid=:tenantid");
		params.put("tenantid", request.getTenantId());
		return namedParameterJdbcTemplate.query(query.toString(), params, propertyRowMapper);
	}
	
	public List<DefaultersInfo> fetchAllPropertiesForReAssess(Long fromDate, Long toDate, String tenantId) {

		final Map<String, Object> params = new HashMap<>();
		List<DefaultersInfo> defaultersInfo = new ArrayList<>();
		StringBuilder dueQuery = new StringBuilder(INNER_QUERY);
		if (fromDate != null && toDate != null) {
			dueQuery.append(" and d.taxperiodfrom >=:fromDate and d.taxperiodto <=:toDate ");
			params.put("fromDate", fromDate);
			params.put("toDate", toDate);
		}
		dueQuery.append(" and pt.tenantId=:tenantId");
		params.put("tenantId", tenantId);
		dueQuery.append(GROUP_BY_CLAUSE);

	
		String mainQuery = OUTER_QUERY.replace("{duequery}", dueQuery);
		log.info("re-assess query" + mainQuery);
		try {
			defaultersInfo = namedParameterJdbcTemplate.query(mainQuery, params, defaultersRowMapper);
		} catch (Exception ex) {
			log.info("exception while fetching PT details for reassess " + ex.getMessage());
		}
		return defaultersInfo;
	}
	
	public List<Assessment> fetchAssessments(String propertyId, String assessmentYear, String tenantId) {
		StringBuilder query = new StringBuilder(ASSESSMENT_DETAIL_SEARCH_QUERY);
		final Map<String, Object> params = new HashMap<>();
		params.put("propertyid", propertyId);
		params.put("financialyear", assessmentYear);
		params.put("tenantid", tenantId);
		List<Assessment> assessments = new ArrayList<>();
		try {
			assessments = namedParameterJdbcTemplate.query(query.toString(), params, assessmentRowmapper);
		} catch (final DataAccessException e) {

		}

		if (assessments.isEmpty())
			return Collections.emptyList();
		else
			return assessments;
	}
	
	public boolean isAssessmentExists(String propertyId, String assessmentYear, String tenantId) {
		StringBuilder query = new StringBuilder(ASSESSMENT_SEARCH_QUERY);
		final Map<String, Object> params = new HashMap<>();
		params.put("propertyid", propertyId);
		params.put("financialyear", assessmentYear);
		params.put("tenantid", tenantId);
		List<String> assessmentIds = new ArrayList<>();
		try {
			assessmentIds = namedParameterJdbcTemplate.queryForList(query.toString(), params, String.class);
		} catch (final DataAccessException e) {

		}

		if (assessmentIds.isEmpty())
			return false;
		else
			return true;
	}
	
	
	public void saveAssessmentGenerationDetails(Assessment assessment, String status, String additionalDetails,String error) {
		StringBuilder query = new StringBuilder(ASSESSMENT_JOB_DATA_INSERT_QUERY);
		final Map<String, Object> params = new HashMap<>();
		params.put("id", UUID.randomUUID());
		params.put("assessmentnumber", assessment.getAssessmentNumber());
		params.put("propertyid", assessment.getPropertyId());
		params.put("financialyear", assessment.getFinancialYear());
		params.put("createdtime", System.currentTimeMillis());
		params.put("status", status);
		params.put("error", error);
		params.put("tenantid", assessment.getTenantId());
		params.put("additionaldetails", additionalDetails);
		try {
			namedParameterJdbcTemplate.update(query.toString(), params);
		} catch (final DataAccessException e) {
           log.info("exception in saving assessment job details");
		}
	}

}
