package org.egov.vehicle.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.vehicle.web.model.AuditDetails;
import org.egov.vehicle.web.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RowMapper  implements ResultSetExtractor<List<Vehicle>> {

	@Autowired
	private ObjectMapper mapper;

	@SuppressWarnings("rawtypes")
	@Override
	public List<Vehicle> extractData(ResultSet rs) throws SQLException, DataAccessException {
	
		Map<String, Vehicle> vehicleMap = new LinkedHashMap<String, Vehicle>();

		while (rs.next()) {
			Vehicle currentVehicle = new Vehicle();
			String id = rs.getString("id");
			currentVehicle = vehicleMap.get(id);
			String tenantId = rs.getString("tenantid");
			String registrationNumber = rs.getString("registrationNumber");
			String model = rs.getString("model");
			String type = rs.getString("type");
			Double tankCapicity = rs.getDouble("tankCapicity");
			String suctionType = rs.getString("suctionType");
			Long pollutionCertiValidTill = rs.getLong("pollutionCertiValidTill");
			Long InsuranceCertValidTill = rs.getLong("InsuranceCertValidTill");
			Long fitnessValidTill = rs.getLong("fitnessValidTill");
			Long roadTaxPaidTill = rs.getLong("roadTaxPaidTill");
			Boolean gpsEnabled = rs.getBoolean("gpsenabled");
			String source = rs.getString("source");
			String status = rs.getString("status");
			String owner_id = rs.getString("owner_id");
			String additionalDetails = rs.getString("additionalDetails");
			
			if(currentVehicle == null) {
				Long lastModifiedTime = rs.getLong("lastmodifiedtime");

				if (rs.wasNull()) {
					lastModifiedTime = null;
				}

				currentVehicle = Vehicle.builder().tenantId(tenantId).registrationNumber(registrationNumber).model(model).type(type).tankCapicity(tankCapicity)
						.suctionType(suctionType).pollutionCertiValidTill(pollutionCertiValidTill).InsuranceCertValidTill(InsuranceCertValidTill)
						.fitnessValidTill(fitnessValidTill).roadTaxPaidTill(roadTaxPaidTill).gpsEnabled(gpsEnabled).source(source)
						.status(Vehicle.StatusEnum.valueOf(status)).additionalDetails(additionalDetails).id(id).build();
				
				vehicleMap.put(id, currentVehicle);
			}
			
			
			
			addChildrenToProperty(rs, currentVehicle);
			
		}

		return new ArrayList<>(vehicleMap.values());
	}
	
	@SuppressWarnings("unused")
	private void addChildrenToProperty(ResultSet rs, Vehicle vehicle) throws SQLException {

		// TODO add all the child data Vehicle, Pit, address
		String tenantId = vehicle.getTenantId(); 
		
		AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("createdBy"))
				.createdTime(rs.getLong("createdTime")).lastModifiedBy(rs.getString("lastModifiedBy"))
				.lastModifiedTime(rs.getLong("lastModifiedTime")).build();
		
		vehicle.setAuditDetails(auditdetails);

	}
	

}
