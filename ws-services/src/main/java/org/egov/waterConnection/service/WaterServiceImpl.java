package org.egov.waterConnection.service;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.workflow.BusinessService;
import org.egov.waterConnection.config.WSConfiguration;
import org.egov.waterConnection.model.Difference;
import org.egov.waterConnection.model.SearchCriteria;
import org.egov.waterConnection.repository.WaterDao;
import org.egov.waterConnection.validator.ActionValidator;
import org.egov.waterConnection.validator.MDMSValidator;
import org.egov.waterConnection.validator.ValidateProperty;
import org.egov.waterConnection.validator.WaterConnectionValidator;
import org.egov.waterConnection.workflow.WorkflowIntegrator;
import org.egov.waterConnection.workflow.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class WaterServiceImpl implements WaterService {

	Logger logger = LoggerFactory.getLogger(WaterServiceImpl.class);

	@Autowired
	private WaterDao waterDao;
	
	@Autowired
	private WaterConnectionValidator waterConnectionValidator;

	@Autowired
	private ValidateProperty validateProperty;
	
	@Autowired
	private MDMSValidator mDMSValidator;

	@Autowired
	private EnrichmentService enrichmentService;
	
	@Autowired
	private WorkflowIntegrator wfIntegrator;
	
	@Autowired
	private WSConfiguration config;
	
	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private ActionValidator actionValidator;
	
	@Autowired
	private DiffService diffService;
	
	
	
	
	
	/**
	 * 
	 * @param waterConnectionRequest WaterConnectionRequest contains water connection to be created
	 * @return List of WaterConnection after create
	 */
	@Override
	public List<WaterConnection> createWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		waterConnectionValidator.validateWaterConnection(waterConnectionRequest, false);
		mDMSValidator.validateMasterData(waterConnectionRequest);
		enrichmentService.enrichWaterConnection(waterConnectionRequest);
		waterDao.saveWaterConnection(waterConnectionRequest);
		//call work-flow
		if (config.getIsExternalWorkFlowEnabled())
			wfIntegrator.callWorkFlow(waterConnectionRequest);
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
		mDMSValidator.validateMasterData(waterConnectionRequest);
		waterConnectionValidator.validateWaterConnection(waterConnectionRequest, true);
		BusinessService businessService = workflowService.getBusinessService(waterConnectionRequest.getRequestInfo().getUserInfo().getTenantId(), waterConnectionRequest.getRequestInfo());
		WaterConnection searchResult = getConnectionForUpdateRequest(waterConnectionRequest.getWaterConnection().getId(), waterConnectionRequest.getRequestInfo());
		actionValidator.validateUpdateRequest(waterConnectionRequest, businessService);
		enrichmentService.enrichUpdateWaterConnection(waterConnectionRequest);
		validateProperty.validatePropertyCriteria(waterConnectionRequest);
		waterConnectionValidator.validateUpdate(waterConnectionRequest, searchResult);
		 Map<String, Difference> diffMap = diffService.getDifference(waterConnectionRequest, searchResult);
		waterDao.updateWaterConnection(waterConnectionRequest);
		return Arrays.asList(waterConnectionRequest.getWaterConnection());
	}
	
	/**
	 * Search Water connection to be update
	 * 
	 * @param id
	 * @param requestInfo
	 * @return water connection
	 */
	private WaterConnection getConnectionForUpdateRequest(String id, RequestInfo requestInfo) {
		SearchCriteria criteria = new SearchCriteria();
		Set<String> ids = new HashSet<>(Arrays.asList(id));
		criteria.setIds(ids);
		List<WaterConnection> connections = getWaterConnectionsList(criteria, requestInfo);
		if (CollectionUtils.isEmpty(connections))
			throw new CustomException("INVALID_WATERCONNECTION_SEARCH",
					"WATER CONNECTION NOT FOUND FOR: " + id + " :ID");
		return connections.get(0);
	}
}
