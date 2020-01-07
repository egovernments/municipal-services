package org.egov.bpa.repository.rowmapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.bpa.web.models.Address;
import org.egov.bpa.web.models.AuditDetails;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.Boundary;
import org.egov.bpa.web.models.Document;
import org.egov.bpa.web.models.GeoLocation;
import org.egov.bpa.web.models.OwnerInfo;
import org.egov.bpa.web.models.Status;
import org.egov.bpa.web.models.Unit;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BPARowMapper implements ResultSetExtractor<List<BPA>> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<BPA> extractData(ResultSet rs) throws SQLException,
			DataAccessException {

		Map<String, BPA> buildingMap = new HashMap<>();

		while (rs.next()) {
			String id = rs.getString("bpa_id");
			String applicationNo = rs.getString("applicationno");
			String permitNumber = rs.getString("permitorderno");
			BPA currentbpa = buildingMap.get(id);
			String tenantId = rs.getString("bpa_tenantId");
			if (currentbpa == null) {
				Long lastModifiedTime = rs.getLong("bpa_lastModifiedTime");
				if (rs.wasNull()) {
					lastModifiedTime = null;
				}

				AuditDetails auditdetails = AuditDetails.builder()
						.createdBy(rs.getString("bpa_createdBy"))
						.createdTime(rs.getLong("bpa_createdTime"))
						.lastModifiedBy(rs.getString("bpa_lastModifiedBy"))
						.lastModifiedTime(lastModifiedTime).build();
				
				
				Double latitude = (Double) rs.getObject("latitude");
				Double longitude = (Double) rs.getObject("longitude");
				
				Boundary locality = Boundary.builder()
						.code(rs.getString("locality")).build();

				GeoLocation location = GeoLocation.builder().latitude(latitude)
						.build();

				GeoLocation location2 = GeoLocation.builder().longitude(longitude)
						.build();
				
				
				Address address = Address
						.builder()
						.buildingName(rs.getString("buildingName"))
						.city(rs.getString("city")).plotNo(rs.getString("plotno"))
						.district(rs.getString("district"))
						.region(rs.getString("region"))
						.state(rs.getString("state"))
						.country(rs.getString("country"))
						.id(rs.getString("bpa_ad_id"))
						.landmark(rs.getString("landmark")).locality(locality)
//						.geoLocation(location).geoLocation(location2)
						.pincode(rs.getString("pincode"))
						.doorNo(rs.getString("doorno"))
						.street(rs.getString("street")).tenantId(tenantId).build();

				currentbpa = BPA.builder().auditDetails(auditdetails)
						.applicationNo(applicationNo)
						.status(rs.getString("status"))
						.tenantId(tenantId)
						.permitOrderNo(permitNumber)
						.edcrNumber(rs.getString("edcrnumber"))
						.serviceType(rs.getString("servicetype"))
						.applicationType(rs.getString("applicationType"))
						.riskType(BPA.RiskTypeEnum.fromValue(rs.getString("riskType")))
						.ownershipCategory(rs.getString("ownershipcategory"))
						.holdingNo(rs.getString("holdingNo"))
						.govtOrQuasi(rs.getString("govtOrQuasi"))
						.registrationDetails(rs.getString("registrationDetails"))
						.remarks(rs.getString("remarks"))
						.address(address)
						.id(id).build();

				buildingMap.put(id, currentbpa);
			}
			addChildrenToProperty(rs, currentbpa);

		}

		return new ArrayList<>(buildingMap.values());

	}

	private void addChildrenToProperty(ResultSet rs, BPA bpa)
			throws SQLException {

		String tenantId = bpa.getTenantId();
		String bpaId = rs.getString("bpa_id");
		
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

		if (rs.getString("bpa_un_id") != null) {
			Unit unit = Unit.builder().id(rs.getString("bpa_un_id"))
					.tenantId(tenantId).build();
			bpa.addUnitsItem(unit);
		}

		Document ownerDocument = Document.builder()
				.id(rs.getString("ownerdocid"))
				.documentType(rs.getString("ownerdocType"))
				.fileStore(rs.getString("ownerfileStore"))
				.documentUid(rs.getString("ownerdocuid")).build();

		Boolean isPrimaryOwner = (Boolean) rs.getObject("isprimaryowner");
		Double ownerShipPercentage = (Double) rs
				.getObject("ownershippercentage");

		if (rs.getString("bpaowner_uuid") != null) {
			OwnerInfo owner = OwnerInfo.builder()
					.uuid(rs.getString("bpaowner_uuid"))
					.isPrimaryOwner(isPrimaryOwner)
					.ownerType(rs.getString("ownerType"))
					.ownerShipPercentage(ownerShipPercentage)
					.relationship(
							OwnerInfo.RelationshipEnum.fromValue(rs
									.getString("relationship")))
					.institutionId(rs.getString("institutionid")).build();
			bpa.addOwnersItem(owner);
		}

		// Add owner document to the specific bpa for which it was used
		String docowner = rs.getString("docuserid");
		String docId = rs.getString("docdetailid");
		if (bpaId.equalsIgnoreCase(docId) && docowner != null
				&& rs.getBoolean("ownerdocactive")) {
			bpa.getOwners().forEach(ownerInfo -> {
				if (docowner.equalsIgnoreCase(ownerInfo.getUuid()))
					ownerInfo.addDocumentsItem(ownerDocument);
			});
		}

		if (rs.getString("bpa_doc_id") != null) {
			Document document = Document.builder()
					.documentType(rs.getString("bpa_doc_documenttype"))
					.fileStore(rs.getString("bpa_doc_filestore"))
					.id(rs.getString("bpa_doc_id")).build();
			bpa.addDocumentsItem(document);
		}

	}

}
