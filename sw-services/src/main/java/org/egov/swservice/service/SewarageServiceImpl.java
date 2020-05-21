package org.egov.swservice.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.model.Property;
import org.egov.swservice.model.SearchCriteria;
import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.model.workflow.BusinessService;
import org.egov.swservice.repository.SewarageDao;
import org.egov.swservice.repository.SewarageDaoImpl;
import org.egov.swservice.util.SewerageServicesUtil;
import org.egov.swservice.validator.ActionValidator;
import org.egov.swservice.validator.MDMSValidator;
import org.egov.swservice.validator.SewerageConnectionValidator;
import org.egov.swservice.validator.ValidateProperty;
import org.egov.swservice.workflow.WorkflowIntegrator;
import org.egov.swservice.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
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
	private SewarageDaoImpl sewarageDaoImpl;
    

	@Autowired
	private CalculationService calculationService;
	
	@Autowired
	private ObjectMapper mapper;

	/**
	 * @param sewarageConnectionRequest
	 *            SewarageConnectionRequest contains sewarage connection to be
	 *            created
	 * @return List of WaterConnection after create
	 */

	@Override
	public List<SewerageConnection> createSewarageConnection(SewerageConnectionRequest sewarageConnectionRequest) {
		sewerageConnectionValidator.validateSewerageConnection(sewarageConnectionRequest, false);
		mDMSValidator.validateMasterData(sewarageConnectionRequest);
		enrichmentService.enrichSewerageConnection(sewarageConnectionRequest);
		Property property = validateProperty.getOrValidateProperty(sewarageConnectionRequest);
		sewarageDao.saveSewerageConnection(sewarageConnectionRequest);
		// call work-flow
		if (config.getIsExternalWorkFlowEnabled())
			wfIntegrator.callWorkFlow(sewarageConnectionRequest, property);
		return Arrays.asList(sewarageConnectionRequest.getSewerageConnection());
	}

	/**
	 * 
	 * @param criteria
	 *            SewarageConnectionSearchCriteria contains search criteria on
	 *            sewarage connection
	 * @param requestInfo
	 * @return List of matching sewarage connection
	 */
	public List<SewerageConnection> search(SearchCriteria criteria, RequestInfo requestInfo) {
		List<SewerageConnection> sewarageConnectionList = getSewerageConnectionsList(criteria, requestInfo);
		validateProperty.validatePropertyForConnection(sewarageConnectionList);
		return sewarageConnectionList;
	}

	/**
	 * 
	 * @param criteria
	 *            SewarageConnectionSearchCriteria contains search criteria on
	 *            sewarage connection
	 * @param requestInfo
	 * @return List of matching water connection
	 */

	public List<SewerageConnection> getSewerageConnectionsList(SearchCriteria criteria, RequestInfo requestInfo) {
		return sewarageDao.getSewerageConnectionList(criteria, requestInfo);
	}

	/**
	 * 
	 * @param sewarageConnectionRequest
	 *            SewarageConnectionRequest contains sewarage connection to be
	 *            updated
	 * @return List of SewarageConnection after update
	 */

	@Override
	public List<SewerageConnection> updateSewarageConnection(SewerageConnectionRequest sewarageConnectionRequest) {
		StringBuilder str = new StringBuilder();
		try {
			str.append("Sewerage Connection Update Request: ")
					.append(mapper.writeValueAsString(sewarageConnectionRequest));
			log.info(str.toString());
		} catch (JsonProcessingException e) {
			log.debug(e.toString());
		}
		sewerageConnectionValidator.validateSewerageConnection(sewarageConnectionRequest, true);
		mDMSValidator.validateMasterData(sewarageConnectionRequest);
		Property property = validateProperty.getOrValidateProperty(sewarageConnectionRequest);
		validateProperty.validatePropertyCriteriaForCreateSewerage(property);
		BusinessService businessService = workflowService.getBusinessService(
				sewarageConnectionRequest.getRequestInfo().getUserInfo().getTenantId(),
				sewarageConnectionRequest.getRequestInfo());
		SewerageConnection searchResult = getConnectionForUpdateRequest(
				sewarageConnectionRequest.getSewerageConnection().getId(), sewarageConnectionRequest.getRequestInfo());
		enrichmentService.enrichUpdateSewerageConnection(sewarageConnectionRequest);
		actionValidator.validateUpdateRequest(sewarageConnectionRequest, businessService);
		sewerageConnectionValidator.validateUpdate(sewarageConnectionRequest, searchResult);
		calculationService.calculateFeeAndGenerateDemand(sewarageConnectionRequest, property);
		sewarageDaoImpl.pushForEditNotification(sewarageConnectionRequest);
		//Enrich file store Id After payment
		enrichmentService.enrichFileStoreIds(sewarageConnectionRequest);
		// Call workflow
		wfIntegrator.callWorkFlow(sewarageConnectionRequest, property);
		enrichmentService.postStatusEnrichment(sewarageConnectionRequest);
		sewarageDao.updateSewerageConnection(sewarageConnectionRequest, 
				sewerageServicesUtil.getStatusForUpdate(businessService, searchResult));
		return Arrays.asList(sewarageConnectionRequest.getSewerageConnection());
	}

	/**
	 * Search Sewerage connection to be update
	 * 
	 * @param id
	 * @param requestInfo
	 * @return sewerage connection
	 */
	public SewerageConnection getConnectionForUpdateRequest(String id, RequestInfo requestInfo) {
		SearchCriteria criteria = new SearchCriteria();
		Set<String> ids = new HashSet<>(Arrays.asList(id));
		criteria.setIds(ids);
		List<SewerageConnection> connections = getSewerageConnectionsList(criteria, requestInfo);
		if (CollectionUtils.isEmpty(connections)) {
			StringBuilder builder = new StringBuilder();
			builder.append("SEWERAGE CONNECTION NOT FOUND FOR: ").append(id).append(" :ID");
			throw new CustomException("INVALID_SEWERAGECONNECTION_SEARCH", builder.toString());
		}
		return connections.get(0);
	}

}
