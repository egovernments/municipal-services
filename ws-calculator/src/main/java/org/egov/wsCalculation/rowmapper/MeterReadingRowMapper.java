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
public class MeterReadingRowMapper implements ResultSetExtractor<List<MeterReading>> {

	@Override
	public List<MeterReading> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<MeterReading> meterReadingLists = new ArrayList<>();
		MeterReading meterReading = new MeterReading();
		while (rs.next()) {
			meterReading.setId(rs.getString("connectionId"));
			meterReading.setBillingPeriod(rs.getString("billingPeriod"));
			meterReading.setCurrentReading(rs.getInt("currentReading"));
			meterReading.setCurrentReadingDate(rs.getLong("currentReadingDate"));
			meterReading.setLastReading(rs.getInt("lastReading"));
			meterReading.setLastReadingDate(rs.getLong("lastReadingDate"));
			meterReading.setMeterStatus(MeterStatusEnum.fromValue(rs.getString("meterStatus")));
			meterReadingLists.add(meterReading);
		}
		return meterReadingLists;
	}
}
