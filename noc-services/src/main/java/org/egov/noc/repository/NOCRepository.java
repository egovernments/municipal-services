package org.egov.noc.repository;

import org.egov.noc.config.NOCConfiguration;
import org.egov.noc.producer.Producer;
import org.egov.noc.web.model.NOCRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class NOCRepository {
	
	@Autowired
	private Producer producer;
	
	@Autowired
	private NOCConfiguration config;
	
	public void save(NOCRequest nocRequest) {
		producer.push(config.getSaveTopic(), nocRequest);
	}

}
