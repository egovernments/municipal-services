package org.egov.bpa.repository;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.producer.Producer;
import org.egov.bpa.repository.querybuilder.BPAQueryBuilder;
import org.egov.bpa.repository.rowmapper.BPARowMapper;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.egov.common.contract.request.RequestInfo;
import org.egov.land.web.models.LandInfo;
import org.egov.land.web.models.LandRequest;
import org.egov.land.web.models.LandSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Repository
public class BPARepository {

	@Autowired
	private BPAConfiguration config;

	@Autowired
	private Producer producer;

	@Autowired
	private BPAQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private BPARowMapper rowMapper;

	/**
	 * Pushes the request on save topic through kafka
	 *
	 * @param bpaRequest
	 *            The bpa create request
	 */
	public void save(BPARequest bpaRequest) {
		producer.push(config.getSaveTopic(), bpaRequest);
	}
	
//	public void saveLand(LandRequest landRequest) {
//		producer.push(config.getSaveTopic(), landRequest);
//	}

	public void update(BPARequest bpaRequest, boolean isStateUpdatable) {
		RequestInfo requestInfo = bpaRequest.getRequestInfo();

		BPA bpaForStatusUpdate = null;
		BPA bpaForUpdate = null;

		BPA bpa = bpaRequest.getBPA();

		if (isStateUpdatable) {
			bpaForUpdate = bpa;
		} else {
			bpaForStatusUpdate = bpa;
		}
		if (bpaForUpdate != null)
			producer.push(config.getUpdateTopic(), new BPARequest(requestInfo, bpaForUpdate));

		if (bpaForStatusUpdate != null)
			producer.push(config.getUpdateWorkflowTopic(), new BPARequest(requestInfo, bpaForStatusUpdate));

	}

	/**
	 * BPA search in database
	 *
	 * @param criteria
	 *            The BPA Search criteria
	 * @return List of BPA from search
	 */
	public List<BPA> getBPAData(BPASearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getBPASearchQuery(criteria, preparedStmtList);
		List<BPA> BPAData = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
		sortChildObjectsById(BPAData);
		return BPAData;
	}

	/**
	 * Sorts the child objects by there id's
	 * 
	 * @param bpa of BPA DATA
	 */
	private void sortChildObjectsById(List<BPA> bpaData) {
		if (CollectionUtils.isEmpty(bpaData))
			return;
		/*bpaData.forEach(bpa -> {
			bpa.getOwners().sort(Comparator.comparing(User::getUuid));
			if (!CollectionUtils.isEmpty(bpa.getDocuments()))
				bpa.getDocuments().sort(Comparator.comparing(Document::getId));
		});*/
	}

	public LandInfo getLandInfoData(@Valid LandSearchCriteria criteria) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveLand(@Valid LandRequest landRequest) {
		// TODO Auto-generated method stub
		
	}
}
