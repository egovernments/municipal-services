package org.egov.wscalculation.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.wscalculation.model.MeterReading;
import org.egov.wscalculation.model.MeterReading.MeterStatusEnum;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class MeterReadingRowMapper implements ResultSetExtractor<List<MeterReading>> {

	@Override
	public List<MeterReading> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<MeterReading> meterReadingLists = new ArrayList<>();
		
		while (rs.next()) {
			MeterReading meterReading = new MeterReading();
			meterReading.setId(rs.getString("id"));
			meterReading.setConnectionNo(rs.getString("connectionId"));
			meterReading.setBillingPeriod(rs.getString("billingPeriod"));
			meterReading.setCurrentReading(rs.getDouble("currentReading"));
			meterReading.setCurrentReadingDate(rs.getLong("currentReadingDate"));
			meterReading.setLastReading(rs.getDouble("lastReading"));
			meterReading.setLastReadingDate(rs.getLong("lastReadingDate"));
			meterReading.setMeterStatus(MeterStatusEnum.fromValue(rs.getString("meterStatus")));
			meterReadingLists.add(meterReading);
		}
		return meterReadingLists;
	}
}
