package org.egov.swService.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.swService.model.Connection.ApplicationStatusEnum;
import org.egov.swService.model.Connection.StatusEnum;
import org.egov.swService.model.Property;
import org.egov.swService.model.SewerageConnection;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class SewerageRowMapper implements ResultSetExtractor<List<SewerageConnection>> {

	@Override
	public List<SewerageConnection> extractData(ResultSet rs) throws SQLException, DataAccessException {

		List<SewerageConnection> sewarageConnectionList = new ArrayList<>();
		SewerageConnection sewarageConnection = new SewerageConnection();
		while (rs.next()) {
			sewarageConnection = new SewerageConnection();
			Property property = new Property();
			sewarageConnection.setId(rs.getString("connection_Id"));
			sewarageConnection.setApplicationNo(rs.getString("applicationNo"));
			sewarageConnection.setApplicationStatus(ApplicationStatusEnum.fromValue(rs.getString("applicationstatus")));
			sewarageConnection.setStatus(StatusEnum.fromValue(rs.getString("status")));
			sewarageConnection.setConnectionNo(rs.getString("connectionNo"));
			sewarageConnection.setOldConnectionNo(rs.getString("oldConnectionNo"));
			sewarageConnection.setConnectionExecutionDate(rs.getBigDecimal("connectionExecutionDate"));
			sewarageConnection.setNoOfToilets(rs.getInt("noOfToilets"));
			sewarageConnection.setNoOfWaterClosets(rs.getInt("noOfWaterClosets"));
			sewarageConnection.setUom(rs.getString("uom"));
			sewarageConnection.setConnectionType(rs.getString("connectionType"));
			sewarageConnection.setCalculationAttribute(rs.getString("calculationAttribute"));
			// get property id and get property object
			property.setPropertyId(rs.getString("property_id"));
			sewarageConnection.setProperty(property);
			// Add documents id's
			sewarageConnectionList.add(sewarageConnection);
		}
		return sewarageConnectionList;

	}

}
