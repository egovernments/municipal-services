package org.egov.wsCalculation.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReading.MeterStatusEnum;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class MeterReadingCurrentReadingRowMapper implements ResultSetExtractor<List<MeterReading>> {

	@Override
	public List<MeterReading> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<MeterReading> meterReadingLists = new ArrayList<>();
		
		while (rs.next()) {
			MeterReading meterReading = new MeterReading();
			
			meterReading.setConnectionNo(rs.getString("connectionId"));

			meterReading.setCurrentReading(rs.getInt("currentReading"));
			meterReading.setCurrentReadingDate(rs.getLong("currentReadingDate"));
			
			meterReadingLists.add(meterReading);
		}
		return meterReadingLists;
	}
}
