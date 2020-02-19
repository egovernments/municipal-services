package org.egov.swService.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.model.workflow.BusinessService;
import org.egov.swService.config.SWConfiguration;
import org.egov.swService.model.Difference;
import org.egov.swService.model.SearchCriteria;
import org.egov.swService.repository.SewarageDao;
import org.egov.swService.util.SewerageServicesUtil;
import org.egov.swService.validator.ActionValidator;
import org.egov.swService.validator.MDMSValidator;
import org.egov.swService.validator.SewerageConnectionValidator;
import org.egov.swService.validator.ValidateProperty;
import org.egov.swService.workflow.WorkflowIntegrator;
import org.egov.swService.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


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
	private WorkflowIntegrator wfIntegrator;
	
	@Autowired
	private SWConfiguration config;
	

	@Autowired
	EnrichmentService enrichmentService;

	@Autowired
	SewarageDao sewarageDao;
	
	@Autowired
	private ActionValidator actionValidator;
	
	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private DiffService diffService;

	/**
	 * @param sewarageConnectionRequest SewarageConnectionRequest contains sewarage connection to be created
	 * @return List of WaterConnection after create
	 */

	@Override
	public List<SewerageConnection> createSewarageConnection(SewerageConnectionRequest sewarageConnectionRequest) {
		sewerageConnectionValidator.validateSewerageConnection(sewarageConnectionRequest, false);
	//	mDMSValidator.validateMasterData(sewarageConnectionRequest);
		enrichmentService.enrichSewerageConnection(sewarageConnectionRequest);
		sewarageDao.saveSewerageConnection(sewarageConnectionRequest);
		//call work-flow
		if (config.getIsExternalWorkFlowEnabled())
			wfIntegrator.callWorkFlow(sewarageConnectionRequest);
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
		mDMSValidator.validateMasterData(sewarageConnectionRequest);
		BusinessService businessService = workflowService.getBusinessService(
				sewarageConnectionRequest.getRequestInfo().getUserInfo().getTenantId(),
				sewarageConnectionRequest.getRequestInfo());
		SewerageConnection searchResult = getConnectionForUpdateRequest(
				sewarageConnectionRequest.getSewerageConnection().getId(), sewarageConnectionRequest.getRequestInfo());

		actionValidator.validateUpdateRequest(sewarageConnectionRequest, businessService);

		validateProperty.validatePropertyCriteriaForCreateSewerage(sewarageConnectionRequest);
		enrichmentService.enrichUpdateSewerageConnection(sewarageConnectionRequest);
		Map<String, Difference> diffMap = diffService.getDifference(sewarageConnectionRequest, searchResult);
		// Call workflow
		wfIntegrator.callWorkFlow(sewarageConnectionRequest);
		enrichmentService.postStatusEnrichment(sewarageConnectionRequest);
		boolean isStateUpdatable = sewerageServicesUtil.getStatusForUpdate(businessService, searchResult);
		sewarageDao.updateSewerageConnection(sewarageConnectionRequest, isStateUpdatable);
		return Arrays.asList(sewarageConnectionRequest.getSewerageConnection());
	}
	
	/**
	 * Search Sewerage connection to be update
	 * 
	 * @param id
	 * @param requestInfo
	 * @return sewerage connection
	 */
	private SewerageConnection getConnectionForUpdateRequest(String id, RequestInfo requestInfo) {
		SearchCriteria criteria = new SearchCriteria();
		Set<String> ids = new HashSet<>(Arrays.asList(id));
		criteria.setIds(ids);
		List<SewerageConnection> connections = getSewerageConnectionsList(criteria, requestInfo);
		if (CollectionUtils.isEmpty(connections))
			throw new CustomException("INVALID_SEWERAGECONNECTION_SEARCH",
					"SEWERAGE CONNECTION NOT FOUND FOR: " + id + " :ID");
		return connections.get(0);
	}

}
