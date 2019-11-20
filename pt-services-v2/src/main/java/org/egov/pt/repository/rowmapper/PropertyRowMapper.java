package org.egov.pt.repository.rowmapper;


import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.pt.models.Address;
import org.egov.pt.models.AuditDetails;
import org.egov.pt.models.Boundary;
import org.egov.pt.models.Document;
import org.egov.pt.models.Institution;
import org.egov.pt.models.OwnerInfo;
import org.egov.pt.models.Property;
import org.egov.pt.models.enums.CreationReason;
import org.egov.pt.models.enums.Relationship;
import org.egov.pt.models.enums.Status;
import org.egov.pt.models.enums.Type;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
@Component
public class PropertyRowMapper implements ResultSetExtractor<List<Property>> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<Property> extractData(ResultSet rs) throws SQLException, DataAccessException {

		Map<String, Property> propertyMap = new HashMap<>();

		while (rs.next()) {

			String currentId = rs.getString("propertyid");
			Property currentProperty = propertyMap.get(currentId);
			String tenanId = rs.getString("tenantId");

			if (null == currentProperty) {

				Address address = getAddress(rs, tenanId);

				AuditDetails auditdetails = getAuditDetail(rs);

				Long occupancyDate = rs.getLong("occupancyDate");
				if (rs.wasNull()) {
					occupancyDate = null;
				}
				
				Double landArea = rs.getDouble("landArea");
				if(rs.wasNull()){landArea = null;}

				currentProperty = Property.builder()
						.creationReason(CreationReason.fromValue(rs.getString("creationReason")))
						.acknowldgementNumber(rs.getString("acknowldgementNumber"))
						.status(Status.fromValue(rs.getString("status")))
						.oldPropertyId(rs.getString("oldPropertyId"))
						.occupancyDate(occupancyDate)
						.auditDetails(auditdetails)
						.landArea(landArea)
						.propertyId(currentId)
						.propertyId(currentId)
						.address(address)
						.build();
				
				currentProperty.addInstitutionItem(getInstitution(rs));
				
				currentProperty.addOwnersItem(getOwner(rs));
				
				currentProperty.addDocumentsItem(getDocument(rs));
				
				try {

					PGobject obj = (PGobject) rs.getObject("pt_additionalDetails");
					if (obj != null) {
						JsonNode propertyAdditionalDetails = mapper.readTree(obj.getValue());
						currentProperty.setAdditionalDetails(propertyAdditionalDetails);
					}

					propertyMap.put(currentId, currentProperty);
				} catch (IOException e) {
					throw new CustomException("PARSING ERROR", "The propertyAdditionalDetail json cannot be parsed");
				}
			}
		
			// addChildrenToProperty(rs, currentProperty);
			currentProperty.addInstitutionItem(getInstitution(rs));
			
			currentProperty.addOwnersItem(getOwner(rs));
			
			currentProperty.addDocumentsItem(getDocument(rs));
		}

		return new ArrayList<>(propertyMap.values());
		
	}


	private Document getDocument(ResultSet rs) throws SQLException {
		
	if(rs.getString("documentid") == null)
		return null;
	
		return Document.builder().id(rs.getString("documentid"))
			.documentType(rs.getString("documentType"))
			.fileStore(rs.getString("fileStore"))
			.documentUid(rs.getString("documentuid"))
			.build();
	}
	
	/**
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private OwnerInfo getOwner(ResultSet rs) throws SQLException {
		
		if(rs.getString("ownerid") == null)
			return null;
		
		Double ownerShipPercentage = rs.getDouble("ownerShipPercentage");
		if(rs.wasNull()) {
			ownerShipPercentage = null;
			}
		
		Boolean isPrimaryOwner = rs.getBoolean("isPrimaryOwner");
		if(rs.wasNull()) {
			isPrimaryOwner = null;
			}
		
		OwnerInfo owner = OwnerInfo.builder()
				.relationship(Relationship.fromValue(rs.getString("relationship")))
				.institutionId(rs.getString("institutionid"))
				.ownerShipPercentage(ownerShipPercentage)
				.ownerType(rs.getString("ownerType"))
				.isPrimaryOwner(isPrimaryOwner)
				.uuid(rs.getString("userid"))
				.build();
		
		return owner;
	}
	
		
	/**
	 * @param rs
	 * @throws SQLException
	 */
	private Institution getInstitution(ResultSet rs) throws SQLException {

		if (rs.getString("instiid") == null)
			return null;

			 return Institution.builder()
					.designation(rs.getString("designation"))
					.tenantId(rs.getString("institenantId"))
					.name(rs.getString("institutionName"))
					.type(rs.getString("institutionType"))
					.id(rs.getString("instiid"))
					.build();
	}

	
	/**
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private AuditDetails getAuditDetail(ResultSet rs) throws SQLException {
		Long lastModifiedTime = rs.getLong("lastModifiedTime");
		if (rs.wasNull()) {
			lastModifiedTime = null;
		}
		
		AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("createdBy"))
				.createdTime(rs.getLong("createdTime")).lastModifiedBy(rs.getString("lastModifiedBy"))
				.lastModifiedTime(lastModifiedTime).build();
		return auditdetails;
	}

	/**
	 * @param rs
	 * @param tenanId
	 * @return
	 * @throws SQLException
	 */
	private Address getAddress(ResultSet rs, String tenanId) throws SQLException {
		Boundary locality = Boundary.builder().code(rs.getString("locality")).build();

		/*
		 * id of the address table is being fetched as address key to avoid confusion
		 * with addressId field
		 */
		Double latitude = rs.getDouble("latitude");
		if (rs.wasNull()) {
			latitude = null;
		}
		
		Double longitude = rs.getDouble("longitude");
		if (rs.wasNull()) {
			longitude = null;
		}

		Address address = Address.builder()
				.addressNumber(rs.getString("addressNumber"))
				.addressLine1(rs.getString("addressLine1"))
				.addressLine2(rs.getString("addressLine2"))
				.buildingName(rs.getString("buildingName"))
				.type(Type.fromValue(rs.getString("type")))
				.addressId(rs.getString("addresskeyid"))
				.addressId(rs.getString("addressId"))
				.landmark(rs.getString("landmark"))
				.pincode(rs.getString("pincode"))
				.doorNo(rs.getString("doorno"))
				.street(rs.getString("street"))
				.detail(rs.getString("detail"))
				.city(rs.getString("city"))
				.latitude(latitude)
				.locality(locality)
				.longitude(longitude)
				.tenantId(tenanId)
				.build();
		return address;
	}
}
