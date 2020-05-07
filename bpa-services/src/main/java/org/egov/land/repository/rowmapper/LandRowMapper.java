package org.egov.land.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.egov.bpa.web.model.BPA;
import org.egov.land.web.models.LandInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class LandRowMapper implements ResultSetExtractor<List<LandInfo>>  {

	@Autowired
	private ObjectMapper mapper;
	
	public List<LandInfo> extractData(ResultSet rs) throws SQLException, DataAccessException {
		return null;
		
	}
	
	private void addChildrenToProperty(ResultSet rs, LandInfo landInfo) throws SQLException {
		
	}
}
