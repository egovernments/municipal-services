package org.egov.swService.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.model.SearchCriteria;
import org.egov.swService.repository.SewarageDao;
import org.egov.swService.util.SewerageServicesUtil;
import org.egov.swService.validator.MDMSValidator;
import org.egov.swService.validator.SewerageConnectionValidator;
import org.egov.swService.validator.ValidateProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class SewarageServiceImpl implements SewarageService {

	Logger logger = LoggerFactory.getLogger(SewarageServiceImpl.class);

	@Autowired
	SewerageServicesUtil sewerageServicesUtil;

	@Autowired
	SewerageConnectionValidator sewerageConnectionValidator;

	@Autowired
	ValidateProperty validateProperty;

	@Autowired
	MDMSValidator mDMSValidator;

	@Autowired
	EnrichmentService enrichmentService;

	@Autowired
	SewarageDao sewarageDao;

	/**
	 * @param sewarageConnectionRequest SewarageConnectionRequest contains sewarage connection to be created
	 * @return List of WaterConnection after create
	 */

	@Override
	public List<SewerageConnection> createSewarageConnection(SewerageConnectionRequest sewarageConnectionRequest) {
		sewerageConnectionValidator.validateSewerageConnection(sewarageConnectionRequest, false);
		mDMSValidator.validateMasterData(sewarageConnectionRequest);
		enrichmentService.enrichSewerageConnection(sewarageConnectionRequest, true);
		sewarageDao.saveSewerageConnection(sewarageConnectionRequest);
		return Arrays.asList(sewarageConnectionRequest.getSewerageConnection());
	}

	/**
	 * 
	 * @param criteria SewarageConnectionSearchCriteria contains search criteria on sewarage connection
	 * @param requestInfo
	 * @return List of matching sewarage connection
	 */
	public List<SewerageConnection> search(SearchCriteria criteria, RequestInfo requestInfo) {
		List<SewerageConnection> sewarageConnectionList;
		sewarageConnectionList = getSewerageConnectionsList(criteria, requestInfo);
		validateProperty.validatePropertyForConnection(sewarageConnectionList);
		enrichmentService.enrichSewerageSearch(sewarageConnectionList, requestInfo,criteria);
		return sewarageConnectionList;
	}

	/**
	 * 
	 * @param criteria SewarageConnectionSearchCriteria contains search criteria on sewarage connection
	 * @param requestInfo
	 * @return List of matching water connection
	 */
	public List<SewerageConnection> getSewerageConnectionsList(SearchCriteria criteria,
			RequestInfo requestInfo) {
		List<SewerageConnection> sewerageConnectionList = sewarageDao.getSewerageConnectionList(criteria, requestInfo);
		if (sewerageConnectionList.isEmpty())
			return Collections.emptyList();
		return sewerageConnectionList;
	}

	/**
	 * 
	 * @param sewarageConnectionRequest SewarageConnectionRequest contains sewarage connection to be updated
	 * @return List of SewarageConnection after update
	 */

	@Override
	public List<SewerageConnection> updateSewarageConnection(SewerageConnectionRequest sewarageConnectionRequest) {
		sewerageConnectionValidator.validateSewerageConnection(sewarageConnectionRequest, true);
		//validateProperty.validatePropertyCriteriaForCreateSewerage(sewarageConnectionRequest);
		enrichmentService.enrichSewerageConnection(sewarageConnectionRequest, false);
		sewarageDao.updatSewerageConnection(sewarageConnectionRequest);
		return Arrays.asList(sewarageConnectionRequest.getSewerageConnection());
	}

}
