package org.egov.bpa.repository;

import org.egov.bpa.config.BpaConfiguration;
import org.egov.bpa.producer.Producer;
import org.egov.bpa.web.models.BPARequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BPARepository {

	@Autowired
	private BpaConfiguration config;

	@Autowired
	private Producer producer;

	/**
	 * Pushes the request on save topic
	 *
	 * @param tradeLicenseRequest
	 *            The tradeLciense create request
	 */
	public void save(BPARequest bpaRequest) {
		producer.push(config.getSaveTopic(), bpaRequest);
	}
}
