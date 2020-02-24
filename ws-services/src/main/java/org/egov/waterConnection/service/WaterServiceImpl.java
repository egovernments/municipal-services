package org.egov.waterConnection.service;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.config.WSConfiguration;
import org.egov.waterConnection.model.SearchCriteria;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.workflow.BusinessService;
import org.egov.waterConnection.repository.WaterDao;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.egov.waterConnection.validator.ActionValidator;
import org.egov.waterConnection.validator.MDMSValidator;
import org.egov.waterConnection.validator.ValidateProperty;
import org.egov.waterConnection.validator.WaterConnectionValidator;
import org.egov.waterConnection.workflow.WorkflowIntegrator;
import org.egov.waterConnection.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class WaterServiceImpl implements WaterService {

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
	
	@Autowired
	private WaterServicesUtil waterServiceUtil;
	
	@Autowired
	private CalculationService calculationService;
	
	
	
	
	
	/**
	 * 
	 * @param waterConnectionRequest WaterConnectionRequest contains water connection to be created
	 * @return List of WaterConnection after create
	 */
	@Override
	public List<WaterConnection> createWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		waterConnectionValidator.validateWaterConnection(waterConnectionRequest, false);
		//mDMSValidator.validateMasterData(waterConnectionRequest);
		enrichmentService.enrichWaterConnection(waterConnectionRequest);
		//call work-flow
		if (config.getIsExternalWorkFlowEnabled())
			wfIntegrator.callWorkFlow(waterConnectionRequest);
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
		return waterDao.getWaterConnectionList(criteria, requestInfo);
	}
	/**
	 * 
	 * @param waterConnectionRequest WaterConnectionRequest contains water connection to be updated
	 * @return List of WaterConnection after update
	 */
	@Override
	public List<WaterConnection> updateWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		waterConnectionValidator.validateWaterConnection(waterConnectionRequest, true);
		mDMSValidator.validateMasterData(waterConnectionRequest);
		BusinessService businessService = workflowService.getBusinessService(waterConnectionRequest.getRequestInfo().getUserInfo().getTenantId(), waterConnectionRequest.getRequestInfo());
		WaterConnection searchResult = getConnectionForUpdateRequest(waterConnectionRequest.getWaterConnection().getId(), waterConnectionRequest.getRequestInfo());
		actionValidator.validateUpdateRequest(waterConnectionRequest, businessService);
		enrichmentService.enrichUpdateWaterConnection(waterConnectionRequest);
		validateProperty.validatePropertyCriteria(waterConnectionRequest);
		waterConnectionValidator.validateUpdate(waterConnectionRequest, searchResult);
		calculationService.calculateFeeAndGenerateDemand(waterConnectionRequest);
		
		//check for edit and send edit notification
		diffService.checkDifferenceAndSendEditNotification(waterConnectionRequest, searchResult);
		//Call workflow
		wfIntegrator.callWorkFlow(waterConnectionRequest);
		enrichmentService.postStatusEnrichment(waterConnectionRequest);
		boolean isStateUpdatable = waterServiceUtil.getStatusForUpdate(businessService, searchResult);
		waterDao.updateWaterConnection(waterConnectionRequest, isStateUpdatable);
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
		Set<String> ids = new HashSet<>(Arrays.asList(id));
		SearchCriteria criteria = new SearchCriteria();
		criteria.setIds(ids);
		List<WaterConnection> connections = getWaterConnectionsList(criteria, requestInfo);
		if (CollectionUtils.isEmpty(connections)) {
			StringBuilder builder = new StringBuilder();
			builder.append("WATER CONNECTION NOT FOUND FOR: ").append(id).append(" :ID");
			throw new CustomException("INVALID_WATERCONNECTION_SEARCH", builder.toString());
		}
			
		return connections.get(0);
	}
}
