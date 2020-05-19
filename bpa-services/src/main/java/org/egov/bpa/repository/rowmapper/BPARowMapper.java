package org.egov.bpa.repository.rowmapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.bpa.web.model.AuditDetails;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.Boundary;
import org.egov.bpa.web.model.Document;
import org.egov.bpa.web.model.GeoLocation;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@Component
public class BPARowMapper implements ResultSetExtractor<List<BPA>> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<BPA> extractData(ResultSet rs) throws SQLException, DataAccessException {

		Map<String, BPA> buildingMap = new LinkedHashMap<String, BPA>();

		while (rs.next()) {
			String id = rs.getString("bpa_id");
			String applicationNo = rs.getString("applicationno");
			String approvalNo = rs.getString("approvalNo");
			BPA currentbpa = buildingMap.get(id);
			String tenantId = rs.getString("bpa_tenantId");
			if (currentbpa == null) {
				Long lastModifiedTime = rs.getLong("bpa_lastModifiedTime");
				if (rs.wasNull()) {
					lastModifiedTime = null;
				}

				Object additionalDetails = new Gson().fromJson(rs.getString("additionalDetails").equals("{}")
						|| rs.getString("additionalDetails").equals("null") ? null : rs.getString("additionalDetails"),
						Object.class);

				AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("bpa_createdBy"))
						.createdTime(rs.getLong("bpa_createdTime")).lastModifiedBy(rs.getString("bpa_lastModifiedBy"))
						.lastModifiedTime(lastModifiedTime).build();

//				Double latitude = (Double) rs.getObject("latitude");
//				Double longitude = (Double) rs.getObject("longitude");

//				Boundary locality = Boundary.builder().code(rs.getString("locality")).build();

//				GeoLocation geoLocation = GeoLocation.builder().latitude(latitude).longitude(longitude).build();

				/*Address address = Address.builder().buildingName(rs.getString("buildingName"))
						.city(rs.getString("city")).plotNo(rs.getString("plotno")).district(rs.getString("district"))
						.region(rs.getString("region")).state(rs.getString("state")).country(rs.getString("country"))
						.id(rs.getString("bpa_ad_id")).landmark(rs.getString("landmark")).locality(locality)
						.geoLocation(geoLocation).pincode(rs.getString("pincode")).doorNo(rs.getString("doorno"))
						.street(rs.getString("street")).tenantId(tenantId).build();*/

				currentbpa = BPA.builder()
						.auditDetails(auditdetails)
						.applicationNo(applicationNo)
						.status(rs.getString("status"))
						.tenantId(tenantId)
						.approvalNo(approvalNo)
						.edcrNumber(rs.getString("edcrnumber"))
						.approvalDate(rs.getLong("approvalDate"))
						.accountId(rs.getString("accountId"))
						.landId(rs.getString("landId"))
						.id(id)
						.additionalDetails(additionalDetails)
						.build();

				buildingMap.put(id, currentbpa);
			}
			addChildrenToProperty(rs, currentbpa);

		}

		return new ArrayList<>(buildingMap.values());

	}

	@SuppressWarnings("unused")
	private void addChildrenToProperty(ResultSet rs, BPA bpa) throws SQLException {

		String tenantId = bpa.getTenantId();
		AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("bpa_createdBy"))
				.createdTime(rs.getLong("bpa_createdTime")).lastModifiedBy(rs.getString("bpa_lastModifiedBy"))
				.lastModifiedTime(rs.getLong("bpa_lastModifiedTime")).build();

		if (bpa == null) {
			PGobject pgObj = (PGobject) rs.getObject("additionaldetail");
			JsonNode additionalDetail = null;
			try {
				additionalDetail = mapper.readTree(pgObj.getValue());
			} catch (IOException e) {
				e.printStackTrace();
			}
			bpa.setAdditionalDetails(additionalDetail);
		}


		String documentId = rs.getString("bpa_doc_id");
		if (documentId != null) {
			Document document = Document.builder().documentType(rs.getString("bpa_doc_documenttype"))
					.fileStoreId(rs.getString("bpa_doc_filestore"))
					.id(documentId)
					.additionalDetails(rs.getString("additionalDetails"))
					.documentUid(rs.getString("documentUid")).build();
			bpa.addDocumentsItem(document);
		}
	}
}
