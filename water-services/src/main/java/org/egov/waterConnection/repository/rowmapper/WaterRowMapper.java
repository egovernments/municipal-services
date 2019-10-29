package org.egov.waterConnection.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
			waterConnection.setApplicationNo(rs.getString("applicationno"));
			waterConnection.setConnectionNo(rs.getString("connectionno"));
			waterConnection.setOldConnectionNo(rs.getString("oldconnectionno"));
			waterConnection.setApplicationStatus(ApplicationStatusEnum.valueOf(rs.getString("applicationstatus")));
			waterConnection.setStatus(StatusEnum.valueOf(rs.getString("status")));
			//get property id and get property object 
			waterConnection.setProperty(new Property());
			waterConnectionList.add(waterConnection);
			// waterConnection.setDocuments(rs.getString("documents_id"));

		}
		return waterConnectionList;
	}
}
