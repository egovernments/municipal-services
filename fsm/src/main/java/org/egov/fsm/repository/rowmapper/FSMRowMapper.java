package org.egov.fsm.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.fsm.web.model.AuditDetails;
import org.egov.fsm.web.model.FSM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class FSMRowMapper implements ResultSetExtractor<List<FSM>> {

	@Autowired
	private ObjectMapper mapper;

	@SuppressWarnings("rawtypes")
	@Override
	public List<FSM> extractData(ResultSet rs) throws SQLException, DataAccessException {

		Map<String, FSM> fmsMap = new LinkedHashMap<String, FSM>();

		while (rs.next()) {
			FSM currentfsm = new FSM();
			//TODO fill the FSM object with data in the result set record
			addChildrenToProperty(rs, currentfsm);

		}

		return new ArrayList<>(fmsMap.values());

	}

	@SuppressWarnings("unused")
	private void addChildrenToProperty(ResultSet rs, FSM fsm) throws SQLException {

		//TODO add all the child data Vehicle, Pit, address 
		String tenantId = fsm.getTenantId();
		AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("fsm_createdBy"))
				.createdTime(rs.getLong("fsm_createdTime")).lastModifiedBy(rs.getString("fsm_lastModifiedBy"))
				.lastModifiedTime(rs.getLong("fsm_lastModifiedTime")).build();

		
	}

	
}