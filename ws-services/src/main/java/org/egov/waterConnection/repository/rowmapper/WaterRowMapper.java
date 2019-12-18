package org.egov.waterConnection.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.Connection.ApplicationStatusEnum;
import org.egov.waterConnection.model.Connection.StatusEnum;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.WaterConnection;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class WaterRowMapper implements ResultSetExtractor<List<WaterConnection>> {

	@Override
	public List<WaterConnection> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<WaterConnection> waterConnectionList = new ArrayList<>();
		WaterConnection waterConnection = new WaterConnection();
		while (rs.next()) {
			waterConnection = new WaterConnection();
			Property property = new Property();
			waterConnection.setConnectionCategory(rs.getString("connectionCategory"));
			waterConnection.setRainWaterHarvesting(rs.getBoolean("rainWaterHarvesting"));
			waterConnection.setConnectionType(rs.getString("connectionType"));
			waterConnection.setWaterSource(rs.getString("waterSource"));
			waterConnection.setMeterId(rs.getString("meterId"));
			waterConnection.setMeterInstallationDate(rs.getLong("meterInstallationDate"));
			waterConnection.setId(rs.getString("connection_Id"));
			waterConnection.setApplicationNo(rs.getString("applicationNo"));
			waterConnection.setApplicationStatus(ApplicationStatusEnum.fromValue(rs.getString("applicationstatus")));
			waterConnection.setStatus(StatusEnum.fromValue(rs.getString("status")));
			waterConnection.setConnectionNo(rs.getString("connectionNo"));
			waterConnection.setOldConnectionNo(rs.getString("oldConnectionNo"));
			waterConnection.setPipeSize(rs.getDouble("pipeSize"));
			waterConnection.setNoOfTaps(rs.getInt("noOfTaps"));
			waterConnection.setUom(rs.getString("uom"));
			waterConnection.setWaterSubSource(rs.getString("waterSubSource"));
			waterConnection.setCalculationAttribute(rs.getString("calculationAttribute"));
			//get property id and get property object 
			property.setPropertyId(rs.getString("property_id"));
			waterConnection.setProperty(property);
			//Add documents id's
			waterConnection.setConnectionExecutionDate(rs.getLong("connectionExecutionDate"));;
			waterConnectionList.add(waterConnection);
		}
		return waterConnectionList;
	}
}
