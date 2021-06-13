package org.egov.fsm.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.egov.fsm.web.model.*;

@Component
public class DataMartRowMapper implements ResultSetExtractor<List<DataMartModel>> {

	@Override
	public List<DataMartModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<DataMartModel> dataList = new ArrayList<DataMartModel>();
		while (rs.next()) {
			DataMartModel dataMartModel = new DataMartModel();
			dataMartModel.setApplicationId(rs.getString("applicationId"));
			dataMartModel.setApplicationStatus(rs.getString("fsmapplicationstatus"));
			dataMartModel.setPropertyType(rs.getString("propertyType"));
			dataMartModel.setPropertySubType(rs.getString("propertySubType"));
			dataMartModel.setSanitationType(rs.getString("sanitationType"));
			dataMartModel.setDoorNo(rs.getString("doorno"));
			dataMartModel.setCity(rs.getString("city"));
			dataMartModel.setStreetName(rs.getString("streetName"));
			dataMartModel.setPinCode(rs.getString("pinCode"));
			dataMartModel.setLocality(rs.getString("locality"));
			dataMartModel.setDistrict(rs.getString("district"));
			dataMartModel.setState(rs.getString("state"));
			dataMartModel.setSlumName(rs.getString("slumname"));
			if (rs.getString("longitude") != null ) {
				dataMartModel.setLongtitude(rs.getString("longitude"));
				dataMartModel.setLatitude(rs.getString("latitude"));
				if(Float.valueOf(rs.getString("longitude"))>0) {
					
					dataMartModel.setGeoLocationProvided(true);
				}
				
			}
			dataMartModel.setApplicationSource(rs.getString("applicationchannel"));
			dataMartModel.setDesludgingEntity(rs.getString("dsoname"));
			dataMartModel.setDesludgingVechicleNumber(rs.getString("vehicleNumber"));
			dataMartModel.setVechileType(rs.getString("vehicleType"));
			dataMartModel.setVechicleCapacity(rs.getInt("vehicleCapacity"));
			dataMartModel.setWasteCollected(rs.getInt("wasteCollected"));
			dataMartModel.setWasteDumped(rs.getInt("wasteDumped"));
			dataMartModel.setPaymentAmount(rs.getDouble("paymentAmount"));
			dataMartModel.setPaymentInstrumentType(rs.getString("paymentStatus"));
			dataMartModel.setPaymentInstrumentType(rs.getString("paymentsource"));
			if (rs.getLong("tripstarttime") != 0) {
				LocalDateTime startDateTime = Instant.ofEpochMilli(rs.getLong("tripstarttime"))
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				dataMartModel.setVechileInDateTime(startDateTime);

			}
			if (rs.getLong("tripendtime") != 0) {
				LocalDateTime endDateTime = Instant.ofEpochMilli(rs.getLong("tripendtime"))
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				dataMartModel.setVechileOutDateTime(endDateTime);

			}
			dataList.add(dataMartModel);

		}
		return dataList;
	}

}
