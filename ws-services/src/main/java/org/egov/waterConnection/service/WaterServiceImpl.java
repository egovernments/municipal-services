package org.egov.waterConnection.service;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.SearchCriteria;
import org.egov.waterConnection.repository.WaterDao;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.egov.waterConnection.validator.MDMSValidator;
import org.egov.waterConnection.validator.ValidateProperty;
import org.egov.waterConnection.validator.WaterConnectionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WaterServiceImpl implements WaterService {

	Logger logger = LoggerFactory.getLogger(WaterServiceImpl.class);

	@Autowired
	WaterDao waterDao;

	@Autowired
	WaterServicesUtil waterServicesUtil;
	
	@Autowired
	WaterConnectionValidator waterConnectionValidator;

	@Autowired
	ValidateProperty validateProperty;
	
	@Autowired
	MDMSValidator mDMSValidator;

	@Autowired
	EnrichmentService enrichmentService;
	
	/**
	 * 
	 * @param waterConnectionRequest WaterConnectionRequest contains water connection to be created
	 * @return List of WaterConnection after create
	 */
	@Override
	public List<WaterConnection> createWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		waterConnectionValidator.validateWaterConnection(waterConnectionRequest, false);
		mDMSValidator.validateMasterData(waterConnectionRequest);
		enrichmentService.enrichWaterConnection(waterConnectionRequest, true);
		waterDao.saveWaterConnection(waterConnectionRequest);
		return Arrays.asList(waterConnectionRequest.getWaterConnection());
	}
	/**
	 * 
	 * @param criteria WaterConnectionSearchCriteria contains search criteria on water connection
	 * @param requestInfo 
	 * @return List of matching water connection
	 */
	public List<WaterConnection> search(SearchCriteria criteria, RequestInfo requestInfo) {
		List<WaterConnection> waterConnectionList;
		waterConnectionList = getWaterConnectionsList(criteria, requestInfo);
		waterConnectionValidator.validatePropertyForConnection(waterConnectionList);
		enrichmentService.enrichWaterSearch(waterConnectionList, requestInfo,criteria);
		return waterConnectionList;
	}
	/**
	 * 
	 * @param criteria WaterConnectionSearchCriteria contains search criteria on water connection
	 * @param requestInfo 
	 * @return List of matching water connection
	 */
	public List<WaterConnection> getWaterConnectionsList(SearchCriteria criteria,
			RequestInfo requestInfo) {
		List<WaterConnection> waterConnectionList = waterDao.getWaterConnectionList(criteria, requestInfo);
		if (waterConnectionList.isEmpty())
			return Collections.emptyList();
		return waterConnectionList;
	}
	/**
	 * 
	 * @param waterConnectionRequest WaterConnectionRequest contains water connection to be updated
	 * @return List of WaterConnection after update
	 */
	@Override
	public List<WaterConnection> updateWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		waterConnectionValidator.validateWaterConnection(waterConnectionRequest, true);
		validateProperty.validatePropertyCriteria(waterConnectionRequest);
		mDMSValidator.validateMasterData(waterConnectionRequest);
		enrichmentService.enrichWaterConnection(waterConnectionRequest, false);
		waterDao.updateWaterConnection(waterConnectionRequest);
		return Arrays.asList(waterConnectionRequest.getWaterConnection());
	}
}
