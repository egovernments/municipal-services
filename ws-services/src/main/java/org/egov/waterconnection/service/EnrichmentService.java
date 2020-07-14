package org.egov.waterconnection.service;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterconnection.config.WSConfiguration;
import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.repository.IdGenRepository;
import org.egov.waterconnection.repository.WaterDaoImpl;
import org.egov.waterconnection.util.WaterServicesUtil;
import org.egov.waterconnection.web.models.AuditDetails;
import org.egov.waterconnection.web.models.Connection.StatusEnum;
import org.egov.waterconnection.web.models.ConnectionHolderInfo;
import org.egov.waterconnection.web.models.SearchCriteria;
import org.egov.waterconnection.web.models.Status;
import org.egov.waterconnection.web.models.WaterConnection;
import org.egov.waterconnection.web.models.WaterConnectionRequest;
import org.egov.waterconnection.web.models.Idgen.IdResponse;
import org.egov.waterconnection.web.models.users.UserDetailResponse;
import org.egov.waterconnection.web.models.users.UserSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;


@Service
@Slf4j
public class EnrichmentService {

	@Autowired
	private WaterServicesUtil waterServicesUtil;

	@Autowired
	private IdGenRepository idGenRepository;

	@Autowired
	private WSConfiguration config;

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private WaterDaoImpl waterDao;
	
	@Autowired
	private UserService userService;

	@Autowired
	private WaterServiceImpl waterService;

	/**
	 * Enrich water connection
	 * 
	 * @param waterConnectionRequest WaterConnection Object
	 */
	@SuppressWarnings("unchecked")
	public void enrichWaterConnection(WaterConnectionRequest waterConnectionRequest, int reqType) {
		AuditDetails auditDetails = waterServicesUtil
				.getAuditDetails(waterConnectionRequest.getRequestInfo().getUserInfo().getUuid(), true);
		waterConnectionRequest.getWaterConnection().setAuditDetails(auditDetails);
		waterConnectionRequest.getWaterConnection().setId(UUID.randomUUID().toString());
		waterConnectionRequest.getWaterConnection().setStatus(StatusEnum.ACTIVE);
		//Application creation date
		HashMap<String, Object> additionalDetail = new HashMap<>();
		if (waterConnectionRequest.getWaterConnection().getAdditionalDetails() == null) {
			for (String constValue : WCConstants.ADDITIONAL_OBJ_CONSTANT) {
				additionalDetail.put(constValue, null);
			}
		} else {
			additionalDetail = mapper
					.convertValue(waterConnectionRequest.getWaterConnection().getAdditionalDetails(), HashMap.class);
		}
		additionalDetail.put(WCConstants.APP_CREATED_DATE, BigDecimal.valueOf(System.currentTimeMillis()));
		waterConnectionRequest.getWaterConnection().setAdditionalDetails(additionalDetail);
	    //Setting ApplicationType
	  	waterConnectionRequest.getWaterConnection().setApplicationType(
	  			reqType == WCConstants.MODIFY_CONNECTION ? WCConstants.MODIFY_WATER_CONNECTION :  WCConstants.NEW_WATER_CONNECTION);
		setApplicationIdGenIds(waterConnectionRequest);
		setStatusForCreate(waterConnectionRequest);
		
	}
	@SuppressWarnings("unchecked")
	public void enrichingAdditionalDetails(WaterConnectionRequest waterConnectionRequest) {
		HashMap<String, Object> additionalDetail = new HashMap<>();
		if (waterConnectionRequest.getWaterConnection().getAdditionalDetails() == null) {
			WCConstants.ADDITIONAL_OBJ_CONSTANT.forEach(key -> {
				additionalDetail.put(key, null);
			});
		} else {
			HashMap<String, Object> addDetail = mapper
					.convertValue(waterConnectionRequest.getWaterConnection().getAdditionalDetails(), HashMap.class);
			List<String> numberConstants = Arrays.asList(WCConstants.ADHOC_PENALTY, WCConstants.ADHOC_REBATE,
					WCConstants.INITIAL_METER_READING_CONST, WCConstants.APP_CREATED_DATE,
					WCConstants.ESTIMATION_DATE_CONST);
			for (String constKey : WCConstants.ADDITIONAL_OBJ_CONSTANT) {
				if (addDetail.getOrDefault(constKey, null) != null && numberConstants.contains(constKey)) {
					BigDecimal big = new BigDecimal(String.valueOf(addDetail.get(constKey)));
					additionalDetail.put(constKey, big);
				} else {
					additionalDetail.put(constKey, addDetail.get(constKey));
				}
			}
			if (waterConnectionRequest.getWaterConnection().getProcessInstance().getAction()
					.equalsIgnoreCase(WCConstants.APPROVE_CONNECTION_CONST)) {
				additionalDetail.put(WCConstants.ESTIMATION_DATE_CONST, System.currentTimeMillis());
			}
		}
		waterConnectionRequest.getWaterConnection().setAdditionalDetails(additionalDetail);
	}
	

	/**
	 * Sets the WaterConnectionId for given WaterConnectionRequest
	 *
	 * @param request
	 *            WaterConnectionRequest which is to be created
	 */
	private void setApplicationIdGenIds(WaterConnectionRequest request) {
		WaterConnection waterConnection = request.getWaterConnection();
		List<String> applicationNumbers = getIdList(request.getRequestInfo(),
				request.getWaterConnection().getTenantId(), config.getWaterApplicationIdGenName(),
				config.getWaterApplicationIdGenFormat());
		if (applicationNumbers.size() != 1) {
			Map<String, String> errorMap = new HashMap<>();
			errorMap.put("IDGEN_ERROR",
					"The Id of WaterConnection returned by IdGen is not equal to number of WaterConnection");
			throw new CustomException(errorMap);
		}
		waterConnection.setApplicationNo(applicationNumbers.get(0));
	}

	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idFormat) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idFormat, 1)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException(WCConstants.IDGEN_ERROR_CONST, "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}
	
	
	/**
	 * Enrich update water connection
	 * 
	 * @param waterConnectionRequest WaterConnectionRequest Object
	 */
	public void enrichUpdateWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		AuditDetails auditDetails = waterServicesUtil
				.getAuditDetails(waterConnectionRequest.getRequestInfo().getUserInfo().getUuid(), false);
		waterConnectionRequest.getWaterConnection().setAuditDetails(auditDetails);
		WaterConnection connection = waterConnectionRequest.getWaterConnection();
		if (!CollectionUtils.isEmpty(connection.getDocuments())) {
			connection.getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
					document.setDocumentUid(UUID.randomUUID().toString());
					document.setStatus(Status.ACTIVE);
				}
				document.setAuditDetails(auditDetails);
			});
		}
		if (!CollectionUtils.isEmpty(connection.getPlumberInfo())) {
			connection.getPlumberInfo().forEach(plumberInfo -> {
				if (plumberInfo.getId() == null) {
					plumberInfo.setId(UUID.randomUUID().toString());
				}
				plumberInfo.setAuditDetails(auditDetails);
			});
		}
		enrichingAdditionalDetails(waterConnectionRequest);
	}
	
	/**
	 * Enrich water connection request and add connection no if status is approved
	 * 
	 * @param waterConnectionRequest WaterConnectionRequest Object
	 */
	public void postStatusEnrichment(WaterConnectionRequest waterConnectionRequest) {
		if (WCConstants.ACTIVATE_CONNECTION
				.equalsIgnoreCase(waterConnectionRequest.getWaterConnection().getProcessInstance().getAction())) {
			setConnectionNO(waterConnectionRequest);
		}
	}

	/**
	 * Create meter reading for meter connection
	 *
	 * @param waterConnectionrequest
	 */
	public void postForMeterReading(WaterConnectionRequest waterConnectionrequest, int reqType) {
		if (!StringUtils.isEmpty(waterConnectionrequest.getWaterConnection().getConnectionType())
				&& WCConstants.METERED_CONNECTION
				.equalsIgnoreCase(waterConnectionrequest.getWaterConnection().getConnectionType())) {
			if (reqType == WCConstants.UPDATE_APPLICATION && WCConstants.ACTIVATE_CONNECTION
					.equalsIgnoreCase(waterConnectionrequest.getWaterConnection().getProcessInstance().getAction())) {
				waterDao.postForMeterReading(waterConnectionrequest);
			} else if (WCConstants.MODIFY_CONNECTION == reqType && WCConstants.APPROVE_CONNECTION.
					equals(waterConnectionrequest.getWaterConnection().getProcessInstance().getAction())) {
				SearchCriteria criteria = SearchCriteria.builder()
						.tenantId(waterConnectionrequest.getWaterConnection().getTenantId())
						.connectionNumber(waterConnectionrequest.getWaterConnection().getConnectionNo()).build();
				List<WaterConnection> connections = waterService.search(criteria, waterConnectionrequest.getRequestInfo());
				if (!CollectionUtils.isEmpty(connections)) {
					WaterConnection connection = connections.get(connections.size() - 1);
					if (!connection.getConnectionType().equals(WCConstants.METERED_CONNECTION)) {
						waterDao.postForMeterReading(waterConnectionrequest);
					}
				}
			}
		}
	}
    
    
    /**
     * Enrich water connection request and set water connection no
     * @param request WaterConnectionRequest Object
     */
	private void setConnectionNO(WaterConnectionRequest request) {
		List<String> connectionNumbers = getIdList(request.getRequestInfo(), request.getWaterConnection().getTenantId(),
				config.getWaterConnectionIdGenName(), config.getWaterConnectionIdGenFormat());
		if (connectionNumbers.size() != 1) {
			throw new CustomException("IDGEN_ERROR",
					"The Id of WaterConnection returned by IdGen is not equal to number of WaterConnection");
		}
		request.getWaterConnection().setConnectionNo(connectionNumbers.get(0));
	}
	/**
	 * Enrich fileStoreIds
	 * 
	 * @param waterConnectionRequest WaterConnectionRequest Object
	 */
	public void enrichFileStoreIds(WaterConnectionRequest waterConnectionRequest) {
		try {
			if (waterConnectionRequest.getWaterConnection().getProcessInstance().getAction()
					.equalsIgnoreCase(WCConstants.APPROVE_CONNECTION_CONST)
					|| waterConnectionRequest.getWaterConnection().getProcessInstance().getAction()
							.equalsIgnoreCase(WCConstants.ACTION_PAY)) {
				waterDao.enrichFileStoreIds(waterConnectionRequest);
			}
		} catch (Exception ex) {
			log.debug(ex.toString());
		}
	}
	
	/**
	 * Sets status for create request
	 * 
	 * @param waterConnectionRequest
	 *            The create request
	 */
	private void setStatusForCreate(WaterConnectionRequest waterConnectionRequest) {
		if (waterConnectionRequest.getWaterConnection().getProcessInstance().getAction()
				.equalsIgnoreCase(WCConstants.ACTION_INITIATE)) {
			waterConnectionRequest.getWaterConnection().setApplicationStatus(WCConstants.STATUS_INITIATED);
		}
	}

	/**
	 * Enrich
	 * 
	 * @param waterConnectionList - List Of WaterConnectionObject
	 * @param criteria - Search Criteria
	 * @param requestInfo - RequestInfo Object
	 */
	public void enrichConnectionHolderDeatils(List<WaterConnection> waterConnectionList, SearchCriteria criteria,
			RequestInfo requestInfo) {
		if (CollectionUtils.isEmpty(waterConnectionList))
			return;
		Set<String> connectionHolderIds = new HashSet<>();
		for (WaterConnection waterConnection : waterConnectionList) {
			if (!CollectionUtils.isEmpty(waterConnection.getConnectionHolders())) {
				connectionHolderIds.addAll(waterConnection.getConnectionHolders().stream()
						.map(ConnectionHolderInfo::getUuid).collect(Collectors.toSet()));
			}
		}
		if (CollectionUtils.isEmpty(connectionHolderIds))
			return;
		UserSearchRequest userSearchRequest = userService.getBaseUserSearchRequest(criteria.getTenantId(), requestInfo);
		userSearchRequest.setUuid(connectionHolderIds);
		UserDetailResponse userDetailResponse = userService.getUser(userSearchRequest);
		enrichConnectionHolderInfo(userDetailResponse, waterConnectionList);
	}

	/**
	 *Populates the owner fields inside of the water connection objects from the response got from calling user api
	 * @param userDetailResponse
	 * @param waterConnectionList List of water connection whose owner's are to be populated from userDetailsResponse
	 */
	public void enrichConnectionHolderInfo(UserDetailResponse userDetailResponse, List<WaterConnection> waterConnectionList) {
		List<ConnectionHolderInfo> connectionHolderInfos = userDetailResponse.getUser();
		Map<String, ConnectionHolderInfo> userIdToConnectionHolderMap = new HashMap<>();
		connectionHolderInfos.forEach(user -> userIdToConnectionHolderMap.put(user.getUuid(), user));
		waterConnectionList.forEach(waterConnection -> {
			if(!CollectionUtils.isEmpty(waterConnection.getConnectionHolders())){
				waterConnection.getConnectionHolders().forEach(holderInfo -> {
					if (userIdToConnectionHolderMap.get(holderInfo.getUuid()) == null)
						throw new CustomException("OWNER SEARCH ERROR", "The owner of the water application"
								+ waterConnection.getApplicationNo() + " is not coming in user search");
					else
						holderInfo.addUserDetail(userIdToConnectionHolderMap.get(holderInfo.getUuid()));
				});
			}
		});
	}
}
