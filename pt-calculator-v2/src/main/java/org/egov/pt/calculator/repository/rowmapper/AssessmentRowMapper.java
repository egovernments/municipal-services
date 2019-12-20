package org.egov.pt.calculator.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.egov.pt.calculator.web.models.property.Assessment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class AssessmentRowMapper implements RowMapper<Assessment> {

	@Override
	public Assessment mapRow(ResultSet rs, int rowNum) throws SQLException {

		return Assessment.builder().propertyID(rs.getString("propertyId")).financialYear(rs.getString("assessmentyear"))
				.id(rs.getString("uuid")).assessmentNumber(rs.getString("assessmentNumber")).tenantId(rs.getString("tenantId")).build();
	}

}
