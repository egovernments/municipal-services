package org.egov.vehiclelog.repository.rowmapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.fsm.web.model.AuditDetails;
import org.egov.fsm.web.model.FSM;
import org.egov.vehiclelog.web.model.VehicleLog;
import org.egov.vehiclelog.web.model.VehicleLog.StatusEnum;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@Component
public class VehicleLogRowMapper implements ResultSetExtractor<List<VehicleLog>> {

	@Autowired
	private ObjectMapper mapper;

	@SuppressWarnings("rawtypes")
	@Override
	public List<VehicleLog> extractData(ResultSet rs) throws SQLException, DataAccessException {

		Map<String, VehicleLog> vehicleLogMap = new LinkedHashMap<String, VehicleLog>();

		while (rs.next()) {
			VehicleLog vehicleLog = new VehicleLog();
			String id = rs.getString("id");
			String applicationNo = rs.getString("applicationno");
			vehicleLog = vehicleLogMap.get(id);
			String tenantId = rs.getString("tenantid");
			String status = rs.getString("status");
			String applicationStatus = rs.getString("applicationstatus");

			String dsoId = rs.getString("dso_id");
			String vehicleId = rs.getString("vehicle_id");
			Integer wasteDumped = rs.getInt("waste_dumped");
			Long dumpTime = rs.getLong("dump_time");
			if (vehicleLog == null) {
				Long lastModifiedTime = rs.getLong("lastmodifiedtime");

				if (rs.wasNull()) {
					lastModifiedTime = null;
				}

				String createdBy = rs.getString("createdby");
				String lastModifiedBy = rs.getString("lastmodifiedby");
				Long createdTime = rs.getLong("createdtime");
				lastModifiedTime = rs.getLong("lastmodifiedtime");

				AuditDetails audit = new AuditDetails();
				audit = audit.builder().createdBy(createdBy).lastModifiedBy(lastModifiedBy).createdTime(createdTime)
						.lastModifiedTime(lastModifiedTime).build();

				vehicleLog = VehicleLog.builder().id(id).applicationNo(applicationNo).tenantId(tenantId)
						.applicationStatus(applicationStatus).dsoId(dsoId).vehicleId(vehicleId).wasteDumped(wasteDumped)
						.dumpTime(dumpTime).status(StatusEnum.valueOf(status)).auditDetails(audit).build();
			}
			addChildrenToProperty(rs, vehicleLog);
			vehicleLogMap.put(id, vehicleLog);
		}

		return new ArrayList<>(vehicleLogMap.values());
	}

	private void addChildrenToProperty(ResultSet rs, VehicleLog vehicleLog) throws SQLException {
		String fsmId = rs.getString("fsm_id");
		List<FSM> fsmList = vehicleLog.getFsms();
		if(fsmList==null) {
			fsmList = new ArrayList<FSM>();
		}
		FSM fsm = new FSM();
		fsm.setId(fsmId);
		fsmList.add(fsm);
		vehicleLog.setFsms(fsmList);
	}

}
