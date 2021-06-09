package org.egov.pt.calculator.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.calculator.repository.rowmapper.AssessmentRowMapper;
import org.egov.pt.calculator.repository.rowmapper.PropertyRowMapper;
import org.egov.pt.calculator.util.CalculatorUtils;
import org.egov.pt.calculator.web.models.Assessment;
import org.egov.pt.calculator.web.models.GenerateAssessmentRequest;
import org.egov.pt.calculator.web.models.property.AuditDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.egov.pt.calculator.web.models.property.Property;


/**
 * Persists and retrieves the assessment data from DB
 * 
 * @author kavi elrey
 */
@Repository
public class AssessmentRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
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
	
	public List<Property> fetchAllActiveProperties(GenerateAssessmentRequest request){
		List<Object> preparedStmtList = new ArrayList<>();
		StringBuilder query = new StringBuilder("select prop.* from eg_pt_property prop ,eg_pt_address addr where addr.propertyid=prop.id and addr.tenantid=prop.tenantid and prop.status='ACTIVE' ");
		
		if(request.getLocality()!=null){
			query.append(" and addr.locality=?");
			preparedStmtList.add(request.getLocality());
		}
		query.append(" and prop.tenantid=?");
		preparedStmtList.add(request.getTenantId());
		return jdbcTemplate.query(query.toString(), preparedStmtList.toArray(), propertyRowMapper);
		
		//return null;
	}
	
	public boolean isAssessmentExists(String propertyId, String assessmentYear, String tenantId) {
		List<Object> preparedStmtList = new ArrayList<>();
		StringBuilder query = new StringBuilder("select id from eg_pt_asmt_assessment where status='ACTIVE' ");
		query.append(" and tenantid=?");
		preparedStmtList.add(propertyId);
		preparedStmtList.add(assessmentYear);
		preparedStmtList.add(tenantId);
		List<String> assessmentIds = new ArrayList<>();
		try {
			assessmentIds = jdbcTemplate.queryForList(query.toString(), preparedStmtList.toArray(), String.class);
		} catch (final DataAccessException e) {

		}

		if (assessmentIds.isEmpty())
			return false;
		else
			return true;
	}
	
	public void saveAssessmentGenerationDetails(Assessment assessment, String status, String error) {
		List<Object> preparedStmtList = new ArrayList<>();
		StringBuilder query = new StringBuilder(
				"Insert into eg_pt_assessment_job (id,assessmentnumber,propertyid,financialyear,createdtime,status,error,tenantid) values(?,?,?,?,?,?,?,?) ");
		preparedStmtList.add(UUID.randomUUID());
		preparedStmtList.add(assessment.getAssessmentNumber());
		preparedStmtList.add(assessment.getPropertyId());
		preparedStmtList.add(assessment.getFinancialYear());
		preparedStmtList.add(System.currentTimeMillis());
		preparedStmtList.add(status);
		preparedStmtList.add(error);
		preparedStmtList.add(assessment.getTenantId());
		try {
			jdbcTemplate.update(query.toString(), preparedStmtList.toArray());
		} catch (final DataAccessException e) {

		}
	}

}
