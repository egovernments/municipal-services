package org.egov.fsm.repository.rowmapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.fsm.service.FSMService;
import org.egov.fsm.util.FSMAuditUtil;
import org.egov.fsm.web.model.AuditDetails;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.FSM.StatusEnum;
import org.egov.fsm.web.model.FSMAudit;
import org.egov.fsm.web.model.FSMSearchCriteria;
import org.egov.fsm.web.model.FSMSearchCriteria.FSMSearchCriteriaBuilder;
import org.egov.fsm.web.model.PitDetail;
import org.egov.fsm.web.model.location.Address;
import org.egov.fsm.web.model.location.Boundary;
import org.egov.fsm.web.model.location.GeoLocation;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@Component
public class FSMAuditRowMapper implements ResultSetExtractor<List<FSMAuditUtil>> {

	@Autowired
	private ObjectMapper mapper;

	@SuppressWarnings("rawtypes")
	@Override
	public List<FSMAuditUtil> extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		List<FSMAuditUtil> fsmAuditDataList = new ArrayList<FSMAuditUtil>();
		while (rs.next()) {
			FSMAuditUtil auditUtil = new FSMAuditUtil();
			// TODO fill the FSM object with data in the result set record
			auditUtil.setId(rs.getString("fsm_id"));
			auditUtil.setApplicationNo(rs.getString("applicationno"));
			auditUtil.setAccountId(rs.getString("accountId"));
			auditUtil.setDescription(rs.getString("description"));
			auditUtil.setApplicationStatus(rs.getString("applicationStatus"));
			auditUtil.setSource(rs.getString("source"));
			auditUtil.setSanitationtype(rs.getString("sanitationtype"));
			auditUtil.setPropertyUsage(rs.getString("propertyUsage"));
			auditUtil.setNoOfTrips(rs.getInt("noOfTrips"));		
			auditUtil.setStatus(rs.getString("status"));
			auditUtil.setVehicleId(rs.getString("vehicle_id"));
			auditUtil.setDoorNo(rs.getString("doorno"));
			
			auditUtil.setPlotNo(rs.getString("plotno"));
			auditUtil.setLandmark(rs.getString("landmark"));
			auditUtil.setCity(rs.getString("city"));
			auditUtil.setDistrict(rs.getString("district"));
			auditUtil.setRegion(rs.getString("region"));
			auditUtil.setState(rs.getString("state"));
			auditUtil.setCountry(rs.getString("country"));
			auditUtil.setLocality(rs.getString("locality"));
			auditUtil.setPincode(rs.getString("pincode"));			
			auditUtil.setBuildingName(rs.getString("buildingName"));
			auditUtil.setStreet(rs.getString("street"));
            
			auditUtil.setLatitude(rs.getDouble("latitude"));
			auditUtil.setLongitude(rs.getDouble("longitude"));
			
			auditUtil.setHeight(rs.getDouble("height"));
			auditUtil.setLength(rs.getDouble("length"));
			auditUtil.setWidth(rs.getDouble("width"));
			auditUtil.setDiameter(rs.getDouble("diameter"));
			auditUtil.setDistanceFromRoad(rs.getDouble("distanceFromRoad"));
			
			auditUtil.setModifiedBy(rs.getString("lastmodifiedby"));
			auditUtil.setModifiedTime(rs.getLong("lastmodifiedtime"));
			
			auditUtil.setCreatedBy(rs.getString("createdby"));
			auditUtil.setCreatedTime(rs.getLong("createdtime"));
			
			fsmAuditDataList.add(auditUtil);

		}

		return fsmAuditDataList;

	}

}