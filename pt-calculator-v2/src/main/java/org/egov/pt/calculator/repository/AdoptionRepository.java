package org.egov.pt.calculator.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.pt.calculator.repository.querybuilder.AdoptionQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class AdoptionRepository {

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public List<String> getTotalPropertiesCount() {
		String query =  AdoptionQueryBuilder.TOTAL_PROPERTIES_IDS_QUERY;
		Map<String, Object> preparedStatementValues = new HashMap<>();
		preparedStatementValues.put("status", "ACTIVE");
		List<String> properties = namedParameterJdbcTemplate.queryForList(query,preparedStatementValues, String.class);
		log.info("Result of total properties count: " + properties.size());

		return properties;
	}
	public List<String> generateAssessmentReport(List<String> propertiesList) {
		String query = AdoptionQueryBuilder.PT_ASSESSMENT_DATA_QUERY;
//		log.info("generateTotalReport: " + query);
		Map<String, Object> preparedStatementValues = new HashMap<>();
		preparedStatementValues.put("propertyid", propertiesList);

		List<String> result = namedParameterJdbcTemplate.queryForList(query, preparedStatementValues, String.class);
		log.info("Result of whatsapp chatbot adoption data: " + result.size());
		return result;

	}
	
	public List<String> generatePaymentReport(List<String> propertiesList) {
		String query = AdoptionQueryBuilder.PT_PAYMENT_DATA_QUERY;
//		log.info("generateTotalReport: " + query);
		Map<String, Object> preparedStatementValues = new HashMap<>();
		preparedStatementValues.put("propertyid", propertiesList);

		List<String> result = namedParameterJdbcTemplate.queryForList(query, preparedStatementValues, String.class);
		log.info("Result of whatsapp chatbot adoption data: " + result.size());
		return result;

	}

	public List<String> getPropertiesFromAssessmentJob(int daysForIncrement) {
		
		daysForIncrement = daysForIncrement < 1 ? 1 : daysForIncrement;
		
		String query =  AdoptionQueryBuilder.ASSESS_JOB_PROPERTIES_IDS_QUERY;
		
		query = query.replaceAll(":days", "'"+ daysForIncrement +" DAY'");
		
		Map<String, Object> preparedStatementValues = new HashMap<>();
		List<String> properties = namedParameterJdbcTemplate.queryForList(query,preparedStatementValues, String.class);
		log.info("Result of assessment job and payment properties count: " + properties.size());

		return properties;
	}

}