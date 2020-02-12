package org.egov.swService.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.egov.swService.model.Connection.ApplicationStatusEnum;
import org.egov.swService.model.Connection.StatusEnum;
import org.egov.swService.model.Document;
import org.egov.swService.model.PlumberInfo;
import org.egov.swService.model.PlumberInfo.RelationshipEnum;
import org.egov.swService.model.Property;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.Status;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class SewerageRowMapper implements ResultSetExtractor<List<SewerageConnection>> {

	@Override
	public List<SewerageConnection> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, SewerageConnection> connectionListMap = new HashMap<>();
		List<SewerageConnection> sewarageConnectionList = new ArrayList<>();
		SewerageConnection sewarageConnection = new SewerageConnection();
		while (rs.next()) {
			String Id = rs.getString("connection_Id");
			if (connectionListMap.getOrDefault(Id, null) == null) {
				sewarageConnection = new SewerageConnection();
				Property property = new Property();
				sewarageConnection.setId(rs.getString("connection_Id"));
				sewarageConnection.setApplicationNo(rs.getString("applicationNo"));
				sewarageConnection
						.setApplicationStatus(ApplicationStatusEnum.fromValue(rs.getString("applicationstatus")));
				sewarageConnection.setStatus(StatusEnum.fromValue(rs.getString("status")));
				sewarageConnection.setConnectionNo(rs.getString("connectionNo"));
				sewarageConnection.setOldConnectionNo(rs.getString("oldConnectionNo"));
				sewarageConnection.setConnectionExecutionDate(rs.getBigDecimal("connectionExecutionDate"));
				sewarageConnection.setNoOfToilets(rs.getInt("noOfToilets"));
				sewarageConnection.setNoOfWaterClosets(rs.getInt("noOfWaterClosets"));
				sewarageConnection.setUom(rs.getString("uom"));
				sewarageConnection.setConnectionType(rs.getString("connectionType"));
				sewarageConnection.setCalculationAttribute(rs.getString("calculationAttribute"));
				sewarageConnection.setAction(rs.getString("action"));
				sewarageConnection.setRoadCuttingArea(rs.getFloat("roadcuttingarea"));
				sewarageConnection.setRoadType(rs.getString("roadtype"));
				// get property id and get property object
				property.setPropertyId(rs.getString("property_id"));
				sewarageConnection.setProperty(property);
				// Add documents id's
				sewarageConnectionList.add(sewarageConnection);
			}
			addChildrenToProperty(rs, sewarageConnection);
		}
		return sewarageConnectionList;

	}

	private void addChildrenToProperty(ResultSet rs, SewerageConnection sewerageConnection) throws SQLException {
		String document_Id = rs.getString("doc_Id");
		String isActive = rs.getString("doc_active");
		String activeString = Status.ACTIVE.name();
		boolean documentActive = false;
		if (isActive != null)
			documentActive = isActive.equalsIgnoreCase(activeString) == true ? true : false;
		if (document_Id != null && documentActive) {
			Document applicationDocument = new Document();
			applicationDocument.setId(document_Id);
			applicationDocument.setDocumentType(rs.getString("documenttype"));
			applicationDocument.setFileStoreId(rs.getString("filestoreid"));
			applicationDocument.setDocumentUid(rs.getString("filestoreid"));
			applicationDocument.setStatus(Status.fromValue(isActive));
			sewerageConnection.addDocumentsItem(applicationDocument);
		}
		String plumber_id = rs.getString("plumber_id");
		if (plumber_id != null) {
			PlumberInfo plumber = new PlumberInfo();
			plumber.setId(plumber_id);
			plumber.setName(rs.getString("plumber_name"));
			plumber.setGender(rs.getString("plumber_gender"));
			plumber.setLicenseNo(rs.getString("licenseno"));
			plumber.setMobileNumber(rs.getString("plumber_mobileNumber"));
			plumber.setRelationship(RelationshipEnum.fromValue(rs.getString("relationship")));
			plumber.setCorrespondenceAddress(rs.getString("correspondenceaddress"));
			plumber.setFatherOrHusbandName(rs.getString("fatherorhusbandname"));
			sewerageConnection.addPlumberInfoItem(plumber);
		}
	}

}
