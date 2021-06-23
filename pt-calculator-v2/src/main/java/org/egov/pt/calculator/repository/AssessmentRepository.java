package org.egov.pt.calculator.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.calculator.repository.rowmapper.AssessmentRowMapper;
import org.egov.pt.calculator.repository.rowmapper.PropertyRowMapper;
import org.egov.pt.calculator.util.CalculatorUtils;
import org.egov.pt.calculator.web.models.Assessment;
import org.egov.pt.calculator.web.models.CreateAssessmentRequest;
import org.egov.pt.calculator.web.models.property.AuditDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.egov.pt.calculator.web.models.property.Property;


/**
 * Persists and retrieves the assessment data from DB
 * 
 * @author kavi elrey
 */
@Repository
public class AssessmentRepository {
	
	private static final String PROPERTY_SEARCH_QUERY = "select prop.* from eg_pt_property prop ,eg_pt_address addr where addr.propertyid=prop.id and addr.tenantid=prop.tenantid and prop.status='ACTIVE' ";

	private static final String ASSESSMENT_SEARCH_QUERY = "select id from eg_pt_asmt_assessment where status='ACTIVE' and propertyid=:propertyid and financialyear=:financialyear and tenantid=:tenantid";

	private static final String ASSESSMENT_JOB_DATA_INSERT_QUERY = "Insert into eg_pt_assessment_job (id,assessmentnumber,propertyid,financialyear,createdtime,status,error,tenantid) values(:id,:assessmentnumber,:propertyid,:financialyear,:createdtime,:status,:error,:tenantid)";;

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
		if (request.getPropertyType() != null) {
			query.append(" and prop.usagecategory in (:propertytype) ");
			params.put("propertytype", request.getPropertyType());
		}
		query.append(" and prop.tenantid=:tenantid");
		params.put("tenantid", request.getTenantId());
		return namedParameterJdbcTemplate.query(query.toString(), params, propertyRowMapper);
	}
	
	public boolean isAssessmentExists(String propertyId, String assessmentYear, String tenantId) {
		StringBuilder query = new StringBuilder(ASSESSMENT_SEARCH_QUERY);
		final Map<String, Object> params = new HashMap<>();
		params.put("propertyid", propertyId);
		params.put("financialyear", assessmentYear);
		params.put("tenantId", tenantId);
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
	
	public void saveAssessmentGenerationDetails(Assessment assessment, String status, String error) {
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
		try {
			namedParameterJdbcTemplate.update(query.toString(), params);
		} catch (final DataAccessException e) {

		}
	}

}
