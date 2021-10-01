package org.egov.pt.calculator.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.egov.pt.calculator.web.models.Assessment;
import org.egov.pt.calculator.web.models.property.Channel;
import org.egov.pt.calculator.web.models.propertyV2.AssessmentV2.Source;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class AssessmentRowMapper implements RowMapper<Assessment> {

	@Override
	public Assessment mapRow(ResultSet rs, int rowNum) throws SQLException {

		return Assessment.builder().propertyId(rs.getString("propertyId"))
				.assessmentYear(rs.getString("assessmentyear")).id(rs.getString("id"))
				.assessmentNumber(rs.getString("assessmentNumber")).assessmentDate(rs.getLong("assessmentDate"))
				.source(Source.fromValue(rs.getString("source"))).channel(Channel.fromValue(rs.getString("channel")))
				.tenantId(rs.getString("tenantId")).build();
	}

}
