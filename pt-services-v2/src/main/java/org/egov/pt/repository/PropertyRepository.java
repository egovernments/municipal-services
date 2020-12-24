package org.egov.pt.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.pt.repository.builder.PropertyQueryBuilder;
import org.egov.pt.repository.rowmapper.PropertyRowMapper;
import org.egov.pt.web.models.Property;
import org.egov.pt.web.models.PropertyCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class PropertyRepository {

	@Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	@Autowired
	private PropertyQueryBuilder queryBuilder;
	
	@Autowired
	private PropertyRowMapper rowMapper;
	
	public List<Property> getProperties(PropertyCriteria criteria){
		Map<String,Object> preparedStmtList = new HashMap<>();
		String query = queryBuilder.getPropertySearchQuery(criteria, preparedStmtList);
		log.info("Query: " + query);
		log.info("PS: " + preparedStmtList);
		return namedParameterJdbcTemplate.query(query, preparedStmtList, rowMapper);
	}
	
	public List<Property> getPropertiesPlainSearch(PropertyCriteria criteria){
		Map<String,Object> preparedStmtList = new HashMap<>();
		String query = queryBuilder.getPropertyLikeQuery(criteria, preparedStmtList);
		log.info("Query: " + query);
		log.info("PS: " + preparedStmtList);
		return namedParameterJdbcTemplate.query(query, preparedStmtList, rowMapper);
	}
}
