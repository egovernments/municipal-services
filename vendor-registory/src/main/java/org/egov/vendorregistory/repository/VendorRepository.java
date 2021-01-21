package org.egov.vendorregistory.repository;

import java.util.ArrayList;
import java.util.List;


import org.egov.vendorregistory.config.VendorConfiguration;
import org.egov.vendorregistory.producer.Producer;
import org.egov.vendorregistory.repository.querybuilder.VendorQueryBuilder;
import org.egov.vendorregistory.repository.rowmapper.VendorRowMapper;
import org.egov.vendorregistory.web.model.Vendor;
import org.egov.vendorregistory.web.model.VendorRequest;
import org.egov.vendorregistory.web.model.VendorSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VendorRepository {

	@Autowired
	private Producer producer;

	@Autowired
	private VendorConfiguration configuration;

	@Autowired
	private VendorQueryBuilder vendorQueryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private VendorRowMapper vendorrowMapper;

	public void save(VendorRequest vendorRequest) {
		producer.push(configuration.getSaveTopic(), vendorRequest);
	}

	public List<Vendor> getVendorData(VendorSearchCriteria vendorSearchCriteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = vendorQueryBuilder.getVendorSearchQuery(vendorSearchCriteria, preparedStmtList);
		List<Vendor> vendorData = jdbcTemplate.query(query, preparedStmtList.toArray(), vendorrowMapper);
		System.out.println("query is "+query);
		return vendorData;
	}

	
}
