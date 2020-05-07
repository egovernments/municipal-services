package org.egov.land.repository;

import javax.validation.Valid;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.producer.Producer;
import org.egov.common.contract.request.RequestInfo;
import org.egov.land.web.models.LandInfo;
import org.egov.land.web.models.LandRequest;
import org.egov.land.web.models.LandSearchCriteria;
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
	 *            The bpa create request
	 */
	public void save(LandRequest landRequest) {
		producer.push(config.getSaveTopic(), landRequest);
	}
	
//	public void saveLand(LandRequest landRequest) {
//		producer.push(config.getSaveTopic(), landRequest);
//	}

	public void update(LandRequest landRequest, boolean isStateUpdatable) {
		RequestInfo requestInfo = landRequest.getRequestInfo();

		LandInfo landForStatusUpdate = null;
		LandInfo landForUpdate = null;

		LandInfo landInfo = landRequest.getLandInfo();

		if (isStateUpdatable) {
			landForUpdate = landInfo;
		} else {
			landForStatusUpdate = landInfo;
		}
		if (landForUpdate != null)
			producer.push(config.getUpdateTopic(), new LandRequest(requestInfo, landForUpdate));

		if (landForStatusUpdate != null)
			producer.push(config.getUpdateWorkflowTopic(), new LandRequest(requestInfo, landForStatusUpdate));

	}


	public LandInfo getLandInfoData(@Valid LandSearchCriteria criteria) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveLand(@Valid LandRequest landRequest) {
		// TODO Auto-generated method stub
		
	}
}
