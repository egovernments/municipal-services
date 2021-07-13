package org.egov.gcservice.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.gcservice.config.GCConfiguration;
import org.egov.gcservice.repository.SewerageDao;
import org.egov.gcservice.repository.SewerageDaoImpl;
import org.egov.gcservice.util.GCConstants;
import org.egov.gcservice.util.GarbageServicesUtil;
import org.egov.gcservice.validator.ActionValidator;
import org.egov.gcservice.validator.MDMSValidator;
import org.egov.gcservice.validator.GarbageConnectionValidator;
import org.egov.gcservice.validator.ValidateProperty;
import org.egov.gcservice.web.models.Property;
import org.egov.gcservice.web.models.SearchCriteria;
import org.egov.gcservice.web.models.GarbageConnection;
import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.gcservice.web.models.workflow.BusinessService;
import org.egov.gcservice.workflow.WorkflowIntegrator;
import org.egov.gcservice.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import static org.egov.gcservice.util.GCConstants.APPROVE_CONNECTION;

@Component
public class GarbageServiceImpl implements GarbageService {

	Logger logger = LoggerFactory.getLogger(GarbageServiceImpl.class);

	@Autowired
	GarbageServicesUtil garbageServicesUtil;

	@Autowired
	GarbageConnectionValidator GarbageConnectionValidator;

	@Autowired
	ValidateProperty validateProperty;

	@Autowired
	MDMSValidator mDMSValidator;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	private GCConfiguration config;

	@Autowired
	EnrichmentService enrichmentService;

	@Autowired
	SewerageDao sewerageDao;

	@Autowired
	private ActionValidator actionValidator;

	@Autowired
	private WorkflowService workflowService;
    
	@Autowired
	private SewerageDaoImpl sewerageDaoImpl;
    

	@Autowired
	private CalculationService calculationService;
	
	@Autowired
	private UserService userService;
	
	/**
	 * @param garbageConnectionRequest
	 *            GarbageConnectionRequest contains sewerage connection to be
	 *            created
	 * @return List of WaterConnection after create
	 */

	@Override
	public List<GarbageConnection> createGarbageConnection(GarbageConnectionRequest garbageConnectionRequest) {
		int reqType = GCConstants.CREATE_APPLICATION;
		if (garbageServicesUtil.isModifyConnectionRequest(garbageConnectionRequest)) {
			List<GarbageConnection> GarbageConnectionList = getAllSewerageApplications(garbageConnectionRequest);
			if (!CollectionUtils.isEmpty(GarbageConnectionList)) {
				workflowService.validateInProgressWF(GarbageConnectionList, garbageConnectionRequest.getRequestInfo(),
						garbageConnectionRequest.getGarbageConnection().getTenantId());
			}
			reqType = GCConstants.MODIFY_CONNECTION;
		}
		GarbageConnectionValidator.validateGarbageConnection(garbageConnectionRequest, reqType);
		Property property = validateProperty.getOrValidateProperty(garbageConnectionRequest);
		validateProperty.validatePropertyFields(property,garbageConnectionRequest.getRequestInfo());
		mDMSValidator.validateMasterForCreateRequest(garbageConnectionRequest);
		enrichmentService.enrichGarbageConnection(garbageConnectionRequest, reqType);
		userService.createUser(garbageConnectionRequest);
		sewerageDao.saveGarbageConnection(garbageConnectionRequest);
		// call work-flow
		if (config.getIsExternalWorkFlowEnabled()) {}
			//wfIntegrator.callWorkFlow(garbageConnectionRequest, property);
		return Arrays.asList(garbageConnectionRequest.getGarbageConnection());
	}

	/**
	 * 
	 * @param criteria
	 *            GarbageConnectionSearchCriteria contains search criteria on
	 *            sewerage connection
	 * @param requestInfo - Request Info
	 * @return List of matching sewerage connection
	 */
	public List<GarbageConnection> search(SearchCriteria criteria, RequestInfo requestInfo) {
		List<GarbageConnection> GarbageConnectionList = getGarbageConnectionsList(criteria, requestInfo);
		if(!StringUtils.isEmpty(criteria.getSearchType()) &&
				criteria.getSearchType().equals(GCConstants.SEARCH_TYPE_CONNECTION)){
			GarbageConnectionList = enrichmentService.filterConnections(GarbageConnectionList);
			if(criteria.getIsPropertyDetailsRequired()){
				GarbageConnectionList = enrichmentService.enrichPropertyDetails(GarbageConnectionList, criteria, requestInfo);

			}
		}
		validateProperty.validatePropertyForConnection(GarbageConnectionList);
		enrichmentService.enrichConnectionHolderDeatils(GarbageConnectionList, criteria, requestInfo);
		return GarbageConnectionList;
	}

	/**
	 * 
	 * @param criteria
	 *            GarbageConnectionSearchCriteria contains search criteria on
	 *            sewerage connection
	 * @param requestInfo - Request Info Object
	 * @return List of matching water connection
	 */

	public List<GarbageConnection> getGarbageConnectionsList(SearchCriteria criteria, RequestInfo requestInfo) {
		return sewerageDao.getGarbageConnectionList(criteria, requestInfo);
	}

	/**
	 * 
	 * @param garbageConnectionRequest
	 *            GarbageConnectionRequest contains sewerage connection to be
	 *            updated
	 * @return List of GarbageConnection after update
	 */
	@Override
	public List<GarbageConnection> updateGarbageConnection(GarbageConnectionRequest garbageConnectionRequest) {
		if(garbageServicesUtil.isModifyConnectionRequest(garbageConnectionRequest)){
			return modifyGarbageConnection(garbageConnectionRequest);
		}
		GarbageConnectionValidator.validateGarbageConnection(garbageConnectionRequest, GCConstants.UPDATE_APPLICATION);
		mDMSValidator.validateMasterData(garbageConnectionRequest, GCConstants.UPDATE_APPLICATION);
		Property property = validateProperty.getOrValidateProperty(garbageConnectionRequest);
		validateProperty.validatePropertyFields(property,garbageConnectionRequest.getRequestInfo());
		String previousApplicationStatus = workflowService.getApplicationStatus(
				garbageConnectionRequest.getRequestInfo(),
				garbageConnectionRequest.getGarbageConnection().getApplicationNo(),
				garbageConnectionRequest.getGarbageConnection().getTenantId(), config.getBusinessServiceValue());
		BusinessService businessService = workflowService.getBusinessService(config.getBusinessServiceValue(),
				garbageConnectionRequest.getGarbageConnection().getTenantId(),
				garbageConnectionRequest.getRequestInfo());
		GarbageConnection searchResult = getConnectionForUpdateRequest(
				garbageConnectionRequest.getGarbageConnection().getId(), garbageConnectionRequest.getRequestInfo());
		enrichmentService.enrichUpdateGarbageConnection(garbageConnectionRequest);
		actionValidator.validateUpdateRequest(garbageConnectionRequest, businessService, previousApplicationStatus);
		GarbageConnectionValidator.validateUpdate(garbageConnectionRequest, searchResult);
		calculationService.calculateFeeAndGenerateDemand(garbageConnectionRequest, property);
		sewerageDaoImpl.pushForEditNotification(garbageConnectionRequest);
		userService.updateUser(garbageConnectionRequest, searchResult);
		// Call workflow
		//wfIntegrator.callWorkFlow(garbageConnectionRequest, property);
		// Enrich file store Id After payment
		//enrichmentService.enrichFileStoreIds(garbageConnectionRequest);
		enrichmentService.postStatusEnrichment(garbageConnectionRequest);
		sewerageDao.updateGarbageConnection(garbageConnectionRequest,
				garbageServicesUtil.getStatusForUpdate(businessService, previousApplicationStatus));
		return Arrays.asList(garbageConnectionRequest.getGarbageConnection());
	}

	/**
	 * Search Sewerage connection to be update
	 * 
	 * @param id - Sewerage GarbageConnection Id
	 * @param requestInfo - Request Info Object
	 * @return sewerage connection
	 */
	public GarbageConnection getConnectionForUpdateRequest(String id, RequestInfo requestInfo) {
		SearchCriteria criteria = new SearchCriteria();
		Set<String> ids = new HashSet<>(Arrays.asList(id));
		criteria.setIds(ids);
		List<GarbageConnection> connections = getGarbageConnectionsList(criteria, requestInfo);
		if (CollectionUtils.isEmpty(connections)) {
			StringBuilder builder = new StringBuilder();
			builder.append("Sewerage GarbageConnection not found for Id - ").append(id);
			throw new CustomException("INVALID_SEWERAGE_CONNECTION_SEARCH", builder.toString());
		}
		return connections.get(0);
	}

	/**
	 *
	 * @param garbageConnectionRequest
	 * @return list of sewerage connection list
	 */
	private List<GarbageConnection> getAllSewerageApplications(GarbageConnectionRequest garbageConnectionRequest) {
		SearchCriteria criteria = SearchCriteria.builder()
				.connectionNumber(garbageConnectionRequest.getGarbageConnection().getConnectionNo()).build();
		return search(criteria, garbageConnectionRequest.getRequestInfo());
	}

	/**
	 *
	 * @param garbageConnectionRequest
	 * @return list of sewerage connection
	 */
	private List<GarbageConnection> modifyGarbageConnection(GarbageConnectionRequest garbageConnectionRequest) {
		GarbageConnectionValidator.validateGarbageConnection(garbageConnectionRequest, GCConstants.MODIFY_CONNECTION);
		mDMSValidator.validateMasterData(garbageConnectionRequest, GCConstants.MODIFY_CONNECTION);
		Property property = validateProperty.getOrValidateProperty(garbageConnectionRequest);
		validateProperty.validatePropertyFields(property,garbageConnectionRequest.getRequestInfo());
		String previousApplicationStatus = workflowService.getApplicationStatus(
				garbageConnectionRequest.getRequestInfo(),
				garbageConnectionRequest.getGarbageConnection().getApplicationNo(),
				garbageConnectionRequest.getGarbageConnection().getTenantId(), config.getModifySWBusinessServiceName());
		BusinessService businessService = workflowService.getBusinessService(config.getModifySWBusinessServiceName(),
				garbageConnectionRequest.getGarbageConnection().getTenantId(),
				garbageConnectionRequest.getRequestInfo());
		GarbageConnection searchResult = getConnectionForUpdateRequest(
				garbageConnectionRequest.getGarbageConnection().getId(), garbageConnectionRequest.getRequestInfo());
		enrichmentService.enrichUpdateGarbageConnection(garbageConnectionRequest);
		actionValidator.validateUpdateRequest(garbageConnectionRequest, businessService, previousApplicationStatus);
		GarbageConnectionValidator.validateUpdate(garbageConnectionRequest, searchResult);
		userService.updateUser(garbageConnectionRequest, searchResult);
		sewerageDaoImpl.pushForEditNotification(garbageConnectionRequest);
		// Call workflow
		//wfIntegrator.callWorkFlow(garbageConnectionRequest, property);
		sewerageDaoImpl.updateGarbageConnection(garbageConnectionRequest, garbageServicesUtil.getStatusForUpdate(businessService, previousApplicationStatus));
		// setting oldApplication Flag
		markOldApplication(garbageConnectionRequest);
		return Arrays.asList(garbageConnectionRequest.getGarbageConnection());
	}

	public void markOldApplication(GarbageConnectionRequest garbageConnectionRequest) {
		if (garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction().equalsIgnoreCase(APPROVE_CONNECTION)) {
			String currentModifiedApplicationNo = garbageConnectionRequest.getGarbageConnection().getApplicationNo();
			List<GarbageConnection> GarbageConnectionList = getAllSewerageApplications(garbageConnectionRequest);

			for(GarbageConnection GarbageConnection:GarbageConnectionList){
				if(!GarbageConnection.getIslegacy() && !(GarbageConnection.getApplicationNo().equalsIgnoreCase(currentModifiedApplicationNo))){
					GarbageConnection.setIslegacy(Boolean.TRUE);
					GarbageConnectionRequest previousGarbageConnectionRequest = GarbageConnectionRequest.builder().requestInfo(garbageConnectionRequest.getRequestInfo())
							.garbageConnection(GarbageConnection).build();
					sewerageDaoImpl.updateGarbageConnection(previousGarbageConnectionRequest,Boolean.TRUE);
				}
			}
		}
	}
}
