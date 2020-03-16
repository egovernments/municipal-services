package org.egov.wscalculation.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.wscalculation.model.Connection;
import org.egov.wscalculation.model.MeterReading;
import org.egov.wscalculation.model.MeterReading.MeterStatusEnum;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class DemandSchedulerRowMapper implements ResultSetExtractor<List<String>> {

	@Override
	public List<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<String> connectionLists = new ArrayList<>();
		while (rs.next()) {
			connectionLists.add(rs.getString("connectionno"));
		}
		return connectionLists;
	}
}