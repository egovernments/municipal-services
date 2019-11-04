package org.egov.waterConnection.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.SewerageConnection;
import org.egov.waterConnection.model.SewerageConnectionRequest;
import org.egov.waterConnection.model.WaterConnectionSearchCriteria;
import org.egov.waterConnection.repository.SewarageDao;
import org.egov.waterConnection.util.SewerageServicesUtil;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.egov.waterConnection.validator.MDMSValidator;
import org.egov.waterConnection.validator.SewerageConnectionValidator;
import org.egov.waterConnection.validator.ValidateProperty;
import org.egov.waterConnection.validator.WaterConnectionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class SewarageServiceImpl implements SewarageService {

	Logger logger = LoggerFactory.getLogger(WaterServiceImpl.class);

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
	 * 
	 * @param waterConnectionRequest
	 *            WaterConnectionRequest contains water connection to be created
	 * @return List of WaterConnection after create
	 */

	@Override
	public List<SewerageConnection> createSewarageConnection(SewerageConnectionRequest sewarageConnectionRequest) {
		List<Property> propertyList;
		sewerageConnectionValidator.validateSewerageConnection(sewarageConnectionRequest, false);
		validateProperty.validatePropertyCriteriaForCreateSewerage(sewarageConnectionRequest);
		// mDMSValidator.validateMasterData(sewarageConnectionRequest);
		if (!validateProperty.isPropertyIdPresentForSewerage(sewarageConnectionRequest)) {
			propertyList = sewerageServicesUtil.propertySearch(sewarageConnectionRequest);
		} else {
			propertyList = sewerageServicesUtil.createPropertyRequest(sewarageConnectionRequest);
		}
		enrichmentService.enrichSewerageConnection(sewarageConnectionRequest, propertyList);
		sewarageDao.saveSewerageConnection(sewarageConnectionRequest);
		return Arrays.asList(sewarageConnectionRequest.getSewerageConnection());

	}

	/**
	 * 
	 * @param criteria
	 *            WaterConnectionSearchCriteria contains search criteria on
	 *            water connection
	 * @param requestInfo
	 * @return List of matching water connection
	 */
	public List<SewerageConnection> search(WaterConnectionSearchCriteria criteria, RequestInfo requestInfo) {
		List<SewerageConnection> sewarageConnectionList;
		sewarageConnectionList = getSewerageConnectionsList(criteria, requestInfo);
		enrichmentService.enrichSewerageSearch(sewarageConnectionList, requestInfo);
		return sewarageConnectionList;
	}

	/**
	 * 
	 * @param criteria
	 *            WaterConnectionSearchCriteria contains search criteria on
	 *            water connection
	 * @param requestInfo
	 * @return List of matching water connection
	 */
	public List<SewerageConnection> getSewerageConnectionsList(WaterConnectionSearchCriteria criteria,
			RequestInfo requestInfo) {
		List<SewerageConnection> sewerageConnectionList = sewarageDao.getSewerageConnectionList(criteria, requestInfo);
		if (sewerageConnectionList.isEmpty())
			return Collections.emptyList();
		return sewerageConnectionList;
	}

	/**
	 * 
	 * @param waterConnectionRequest
	 *            WaterConnectionRequest contains water connection to be updated
	 * @return List of WaterConnection after update
	 */

	@Override
	public List<SewerageConnection> updateSewarageConnection(SewerageConnectionRequest sewarageConnectionRequest) {
		sewerageConnectionValidator.validateSewerageConnection(sewarageConnectionRequest, true);
		validateProperty.validatePropertyCriteriaForCreateSewerage(sewarageConnectionRequest);
		// mDMSValidator.validateMasterData(sewarageConnectionRequest);
		sewarageDao.updatSewerageConnection(sewarageConnectionRequest);
		return Arrays.asList(sewarageConnectionRequest.getSewerageConnection());
	}

}
