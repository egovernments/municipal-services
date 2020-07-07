package org.egov.swservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.model.Property;
import org.egov.swservice.model.SearchCriteria;
import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.model.workflow.BusinessService;
import org.egov.swservice.repository.SewarageDao;
import org.egov.swservice.repository.SewarageDaoImpl;
import org.egov.swservice.util.SWConstants;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class SewarageServiceImpl implements SewarageService {

	Logger logger = LoggerFactory.getLogger(SewarageServiceImpl.class);

	@Autowired
	private SewerageServicesUtil sewerageServicesUtil;

	@Autowired
	private SewerageConnectionValidator sewerageConnectionValidator;

	@Autowired
	private ValidateProperty validateProperty;

	@Autowired
	private MDMSValidator mDMSValidator;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	private SWConfiguration config;

	@Autowired
	private EnrichmentService enrichmentService;

	@Autowired
	private SewarageDao sewarageDao;

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

	@Autowired
	private UserService userService;

	/**
	 * @param sewarageConnectionRequest
	 *            SewarageConnectionRequest contains sewarage connection to be
	 *            created
	 * @return List of WaterConnection after create
	 */

	@Override
	public List<SewerageConnection> createSewarageConnection(SewerageConnectionRequest sewarageConnectionRequest) {
		if (sewerageServicesUtil.isModifyConnectionRequest(sewarageConnectionRequest)) {
			List<SewerageConnection> sewerageConnectionList = getAllSewerageApplications(sewarageConnectionRequest);
			if (!CollectionUtils.isEmpty(sewerageConnectionList)) {
				workflowService.validateInProgressWF(sewerageConnectionList, sewarageConnectionRequest.getRequestInfo(),
						sewarageConnectionRequest.getSewerageConnection().getTenantId());
			}
		}
		sewerageConnectionValidator.validateSewerageConnection(sewarageConnectionRequest, SWConstants.CREATE_APPLICATION);
		Property property = validateProperty.getOrValidateProperty(sewarageConnectionRequest);
		validateProperty.validatePropertyFields(property);
		mDMSValidator.validateMasterForCreateRequest(sewarageConnectionRequest);
		enrichmentService.enrichSewerageConnection(sewarageConnectionRequest);
		userService.createUser(sewarageConnectionRequest);
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
		enrichmentService.enrichConnectionHolderDeatils(sewarageConnectionList, criteria, requestInfo);
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
	 * @param sewerageConnectionRequest
	 *            SewarageConnectionRequest contains sewarage connection to be
	 *            updated
	 * @return List of SewarageConnection after update
	 */

	@Override
	public List<SewerageConnection> updateSewarageConnection(SewerageConnectionRequest sewerageConnectionRequest) {
		if(sewerageServicesUtil.isModifyConnectionRequest(sewerageConnectionRequest)){
			return modifySewerageConnection(sewerageConnectionRequest);
		}
		sewerageConnectionValidator.validateSewerageConnection(sewerageConnectionRequest, SWConstants.UPDATE_APPLICATION);
		mDMSValidator.validateMasterData(sewerageConnectionRequest, SWConstants.UPDATE_APPLICATION);
		Property property = validateProperty.getOrValidateProperty(sewerageConnectionRequest);
		validateProperty.validatePropertyFields(property);
		String previousApplicationStatus = workflowService.getApplicationStatus(
				sewerageConnectionRequest.getRequestInfo(),
				sewerageConnectionRequest.getSewerageConnection().getApplicationNo(),
				sewerageConnectionRequest.getSewerageConnection().getTenantId(), config.getBusinessServiceValue());
		BusinessService businessService = workflowService.getBusinessService(config.getModifySWBusinessServiceName(),
				sewerageConnectionRequest.getSewerageConnection().getTenantId(),
				sewerageConnectionRequest.getRequestInfo());
		SewerageConnection searchResult = getConnectionForUpdateRequest(
				sewerageConnectionRequest.getSewerageConnection().getId(), sewerageConnectionRequest.getRequestInfo());
		enrichmentService.enrichUpdateSewerageConnection(sewerageConnectionRequest);
		actionValidator.validateUpdateRequest(sewerageConnectionRequest, businessService, previousApplicationStatus);
		sewerageConnectionValidator.validateUpdate(sewerageConnectionRequest, searchResult);
		calculationService.calculateFeeAndGenerateDemand(sewerageConnectionRequest, property);
		sewarageDaoImpl.pushForEditNotification(sewerageConnectionRequest);
		// Enrich file store Id After payment
		enrichmentService.enrichFileStoreIds(sewerageConnectionRequest);
		// Call workflow
		wfIntegrator.callWorkFlow(sewerageConnectionRequest, property);
		enrichmentService.postStatusEnrichment(sewerageConnectionRequest);
		sewarageDao.updateSewerageConnection(sewerageConnectionRequest,
				sewerageServicesUtil.getStatusForUpdate(businessService, previousApplicationStatus));
		return Arrays.asList(sewerageConnectionRequest.getSewerageConnection());
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

	/**
	 *
	 * @param sewerageConnectionRequest
	 * @return list of sewerage connection list
	 */
	private List<SewerageConnection> getAllSewerageApplications(SewerageConnectionRequest sewerageConnectionRequest) {
		SearchCriteria criteria = SearchCriteria.builder()
				.connectionNumber(sewerageConnectionRequest.getSewerageConnection().getConnectionNo()).build();
		return search(criteria, sewerageConnectionRequest.getRequestInfo());
	}

	/**
	 *
	 * @param sewerageConnectionRequest
	 * @return list of sewerage connection
	 */
	private List<SewerageConnection> modifySewerageConnection(SewerageConnectionRequest sewerageConnectionRequest) {
		sewerageConnectionValidator.validateSewerageConnection(sewerageConnectionRequest, SWConstants.MODIFY_CONNECTION);
		mDMSValidator.validateMasterData(sewerageConnectionRequest, SWConstants.MODIFY_CONNECTION);
		Property property = validateProperty.getOrValidateProperty(sewerageConnectionRequest);
		validateProperty.validatePropertyFields(property);
		String previousApplicationStatus = workflowService.getApplicationStatus(
				sewerageConnectionRequest.getRequestInfo(),
				sewerageConnectionRequest.getSewerageConnection().getApplicationNo(),
				sewerageConnectionRequest.getSewerageConnection().getTenantId(), config.getModifySWBusinessServiceName());
		BusinessService businessService = workflowService.getBusinessService(config.getModifySWBusinessServiceName(),
				sewerageConnectionRequest.getSewerageConnection().getTenantId(),
				sewerageConnectionRequest.getRequestInfo());
		SewerageConnection searchResult = getConnectionForUpdateRequest(
				sewerageConnectionRequest.getSewerageConnection().getId(), sewerageConnectionRequest.getRequestInfo());
		enrichmentService.enrichUpdateSewerageConnection(sewerageConnectionRequest);
		actionValidator.validateUpdateRequest(sewerageConnectionRequest, businessService, previousApplicationStatus);
		sewerageConnectionValidator.validateUpdate(sewerageConnectionRequest, searchResult);
		sewarageDaoImpl.pushForEditNotification(sewerageConnectionRequest);
		// Call workflow
		wfIntegrator.callWorkFlow(sewerageConnectionRequest, property);
		sewarageDao.updateSewerageConnection(sewerageConnectionRequest, sewerageServicesUtil.getStatusForUpdate(businessService, previousApplicationStatus));
		return Arrays.asList(sewerageConnectionRequest.getSewerageConnection());
	}
}
