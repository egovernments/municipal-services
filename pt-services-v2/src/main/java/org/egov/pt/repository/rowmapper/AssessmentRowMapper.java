package org.egov.pt.repository.rowmapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.pt.models.Assessment;
import org.egov.pt.models.Document;
import org.egov.pt.models.Unit;
import org.egov.pt.models.enums.DocumentBelongsTo;
import org.egov.pt.models.enums.OccupancyType;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AssessmentRowMapper implements ResultSetExtractor<List<Assessment>> {
	
	
	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<Assessment> extractData(ResultSet rs) throws SQLException, DataAccessException {

		
		Map<String, Assessment> assessmentMap = new HashMap<>();
		while(rs.next()) {
			String currentAssessmentId = rs.getString("ass_assessmentid");
			Assessment assessment = assessmentMap.get(currentAssessmentId);
			if(null == assessment) {
				assessment = Assessment.builder()
						.id(rs.getString("ass_assessmentid"))
						.assessmentNumber(rs.getString("ass_assessmentnumber"))
						.tenantId(rs.getString("ass_tenantId"))
						.assessmentDate(rs.getLong("ass_assessmentdate"))
						.buildUpArea(rs.getDouble("ass_builduparea"))
						.financialYear(rs.getString("ass_financialyear"))
						.propertyID(rs.getString("ass_propertyid"))
						.units(new ArrayList<>())
						.documents(new ArrayList<>()).build();
				
				try {
					PGobject obj = (PGobject) rs.getObject("pt_additionalDetails");
					if (obj != null) {
						JsonNode propertyAdditionalDetails = mapper.readTree(obj.getValue());
						assessment.setAdditionalDetails(propertyAdditionalDetails);
					}
				} catch (IOException e) {
					throw new CustomException("PARSING ERROR", "The assessment additionaldetails json cannot be parsed");
				}
				assessment.getUnits().add(getUnit(rs));
				assessment.getDocuments().add(getDocument(rs));
				
				assessmentMap.put(assessment.getId(), assessment);
			}else {
				assessment.getUnits().add(getUnit(rs));
				assessment.getDocuments().add(getDocument(rs));
			}
		}

		return new ArrayList<>(assessmentMap.values());
	}
	
	
	
	private Unit getUnit(ResultSet rs) throws SQLException {
		if(null == rs.getString("unit_unitid"))
			return null;
		
		return Unit.builder().id(rs.getString("unit_unitid"))
				.active(rs.getBoolean("unit_active"))
				.arv(rs.getDouble("unit_arv"))
				.assessmentId(rs.getString("unit_assessmentid"))
				.constructionType(rs.getString("unit_constructiontype"))
				.floorNo(rs.getString("unit_floorno"))
				.occupancyDate(rs.getLong("unit_occupancydate"))
				.occupancyType((OccupancyType.valueOf(rs.getString("unit_occupancytype"))))
				.tenantId(rs.getString("unit_tenantid"))
				.usageCategory(rs.getString("unit_usagecategory"))
				.unitArea(rs.getDouble("unit_unitarea"))
				.build();
	}
	
	
	
	private Document getDocument(ResultSet rs) throws SQLException {
		if(null == rs.getString("doc_docid"))
			return null;
		
		return Document.builder().id(rs.getString("doc_docid"))
				.active(rs.getBoolean("doc_active"))
				.documentBelongsTo(DocumentBelongsTo.valueOf(rs.getString("doc_docbelongsto")))
				.documentType(rs.getString("doc_doctype"))
				.documentUid(rs.getString("doc_docUid"))
				.entityId(rs.getString("doc_entityid"))
				.fileStore(rs.getString("doc_filestore"))
				.tenantId(rs.getString("doc_tenantid"))
				.build();
	}
	
	

}
