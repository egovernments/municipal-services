package org.egov.land.repository;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.producer.Producer;
import org.egov.land.web.models.LandRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class LandRepository {

	@Autowired
	private BPAConfiguration config;

	@Autowired
	private Producer producer;

	/**
	 * Pushes the request on save topic through kafka
	 *
	 * @param bpaRequest
	 *            The landinfo create request
	 */
	public void save(LandRequest landRequest) {
		producer.push(config.getSaveLandInfoTopic(), landRequest);
	}

	public void update(LandRequest landRequest) {
		producer.push(config.getUpdateLandInfoTopic(), landRequest);
	}
}
