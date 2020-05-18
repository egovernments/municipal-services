package org.egov.waterconnection.service;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterconnection.config.WSConfiguration;
import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.model.AuditDetails;
import org.egov.waterconnection.model.Connection.ApplicationStatusEnum;
import org.egov.waterconnection.model.Connection.StatusEnum;
import org.egov.waterconnection.model.Property;
import org.egov.waterconnection.model.SearchCriteria;
import org.egov.waterconnection.model.Status;
import org.egov.waterconnection.model.WaterConnection;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.egov.waterconnection.model.Idgen.IdResponse;
import org.egov.waterconnection.repository.IdGenRepository;
import org.egov.waterconnection.repository.WaterDaoImpl;
import org.egov.waterconnection.util.WaterServicesUtil;
import org.egov.waterconnection.validator.ValidateProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


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
	private ValidateProperty validateProperty;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private WaterDaoImpl waterDao;
	

	/**
	 * 
	 * @param waterConnectionList List of water connection for enriching the water connection with property.
	 * @param requestInfo is RequestInfo from request
	 */
	public void enrichWaterSearch(List<WaterConnection> waterConnectionList, RequestInfo requestInfo,
			SearchCriteria waterConnectionSearchCriteria) {
		
		if(!CollectionUtils.isEmpty(waterConnectionList)) {
			String propertyIdsString = waterConnectionList.stream()
					.map(waterConnection -> waterConnection.getProperty().getPropertyId()).collect(Collectors.toSet())
					.stream().collect(Collectors.joining(","));
			List<Property> propertyList = waterServicesUtil.searchPropertyOnId(waterConnectionSearchCriteria.getTenantId(),
					propertyIdsString, requestInfo);
			HashMap<String, Property> propertyMap = propertyList.stream().collect(Collectors.toMap(Property::getPropertyId,
					Function.identity(), (oldValue, newValue) -> newValue, LinkedHashMap::new));
			waterConnectionList.forEach(waterConnection -> {
				String propertyId = waterConnection.getProperty().getPropertyId();
				if (propertyMap.containsKey(propertyId)) {
					waterConnection.setProperty(propertyMap.get(propertyId));
				} else {
					StringBuilder builder = new StringBuilder("NO PROPERTY FOUND FOR ");
					builder.append(waterConnection.getConnectionNo() == null ? waterConnection.getApplicationNo() : waterConnection.getConnectionNo());
					log.error(builder.toString());
				}
			});
		}
	}

	/**
	 * Enrich water connection
	 * 
	 * @param waterConnectionRequest
	 */
	public void enrichWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		validateProperty.enrichPropertyForWaterConnection(waterConnectionRequest);
		AuditDetails auditDetails = waterServicesUtil
				.getAuditDetails(waterConnectionRequest.getRequestInfo().getUserInfo().getUuid(), true);
		waterConnectionRequest.getWaterConnection().setAuditDetails(auditDetails);
		waterConnectionRequest.getWaterConnection().setId(UUID.randomUUID().toString());
		waterConnectionRequest.getWaterConnection().setStatus(StatusEnum.ACTIVE);
		//Application creation date
		HashMap<String, Object> additionalDetail = new HashMap<>();
	    additionalDetail.put(WCConstants.APP_CREATED_DATE, BigDecimal.valueOf(System.currentTimeMillis()));
	    waterConnectionRequest.getWaterConnection().setAdditionalDetails(additionalDetail);
		setApplicationIdgenIds(waterConnectionRequest);
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
			List<String> numberConstants = Arrays.asList(WCConstants.ADHOC_PENALTY,
					WCConstants.ADHOC_REBATE, WCConstants.INITIAL_METER_READING_CONST, WCConstants.APP_CREATED_DATE);
			for (String constKey : WCConstants.ADDITIONAL_OBJ_CONSTANT) {
				if (addDetail.getOrDefault(constKey, null) != null && numberConstants.contains(constKey)) {
					BigDecimal big = new BigDecimal(String.valueOf(addDetail.get(constKey)));
					additionalDetail.put(constKey, big);
				} else {
					additionalDetail.put(constKey, addDetail.get(constKey));
				}
			}
		}
		waterConnectionRequest.getWaterConnection().setAdditionalDetails(additionalDetail);
		enrichFileStoreIds(waterConnectionRequest);
	}
	

	/**
	 * Sets the WaterConnectionId for given WaterConnectionRequest
	 *
	 * @param request
	 *            WaterConnectionRequest which is to be created
	 */
	private void setApplicationIdgenIds(WaterConnectionRequest request) {
		String tenantId = request.getRequestInfo().getUserInfo().getTenantId();
		WaterConnection waterConnection = request.getWaterConnection();
		List<String> applicationNumbers = getIdList(request.getRequestInfo(), tenantId,
				config.getWaterApplicationIdGenName(), config.getWaterApplicationIdGenFormat());
		if (applicationNumbers.size() != 1) {
			Map<String, String> errorMap = new HashMap<>();
			errorMap.put("IDGEN_ERROR",
					"The Id of WaterConnection returned by idgen is not equal to number of WaterConnection");
			throw new CustomException(errorMap);
		}
		waterConnection.setApplicationNo(applicationNumbers.get(0));
	}

	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, 1)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException(WCConstants.IDGEN_ERROR_CONST, "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}
	
	 /**
     * Sets status for create request
     * @param WaterConnectionRequest The create request
     */
	private void setStatusForCreate(WaterConnectionRequest waterConnectionRequest) {
		if (waterConnectionRequest.getWaterConnection().getProcessInstance().getAction().equalsIgnoreCase(WCConstants.ACTION_INITIATE)) {
			waterConnectionRequest.getWaterConnection().setApplicationStatus(ApplicationStatusEnum.INITIATED);
		}
		if (waterConnectionRequest.getWaterConnection().getProcessInstance().getAction().equalsIgnoreCase(WCConstants.ACTION_APPLY)) {
			waterConnectionRequest.getWaterConnection().setApplicationStatus(ApplicationStatusEnum.APPLIED);
		}
	}
	
	/**
	 * Enrich update water connection
	 * 
	 * @param waterConnectionRequest
	 */
	public void enrichUpdateWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		validateProperty.enrichPropertyForWaterConnection(waterConnectionRequest);
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
					document.setAuditDetails(auditDetails);
				}
			});
		}
		if (!CollectionUtils.isEmpty(connection.getPlumberInfo())) {
			connection.getPlumberInfo().forEach(plumberInfo -> {
				if (plumberInfo.getId() == null) {
					plumberInfo.setId(UUID.randomUUID().toString());
				}
			});
		}
		enrichingAdditionalDetails(waterConnectionRequest);
	}
	
	/**
	 * Enrich water connection request and add connection no if status is approved
	 * 
	 * @param waterConnectionrequest 
	 */
	public void postStatusEnrichment(WaterConnectionRequest waterConnectionrequest) {
		String applicationStatus = waterConnectionrequest.getWaterConnection().getApplicationStatus().name();
		if (WCConstants.STATUS_APPROVED.equalsIgnoreCase(applicationStatus)) {
			setConnectionNO(waterConnectionrequest);
		}
	}
	
	/**
	 * Create meter reading for meter connection
	 * 
	 * @param waterConnectionrequest
	 */
	public void postForMeterReading(WaterConnectionRequest waterConnectionrequest) {
		if (WCConstants.STATUS_APPROVED
				.equalsIgnoreCase(waterConnectionrequest.getWaterConnection().getApplicationStatus().name())) {
			waterDao.postForMeterReading(waterConnectionrequest);
		}
	}
    
    
    /**
     * Enrich water connection request and set water connection no
     * @param request
     */
	private void setConnectionNO(WaterConnectionRequest request) {
		String tenantId = request.getRequestInfo().getUserInfo().getTenantId();
		List<String> connectionNumbers = getIdList(request.getRequestInfo(), tenantId,
				config.getWaterConnectionIdGenName(), config.getWaterConnectionIdGenFormat());
		if (connectionNumbers.size() != 1) {
			throw new CustomException("IDGEN_ERROR",
					"The Id of WaterConnection returned by idgen is not equal to number of WaterConnection");
		}
		request.getWaterConnection().setConnectionNo(connectionNumbers.get(0));
	}
	/**
	 * Enrich fileStoreIds
	 * 
	 * @param waterConnectionRequest
	 */
	public void enrichFileStoreIds(WaterConnectionRequest waterConnectionRequest) {
		try {
			if (waterConnectionRequest.getWaterConnection().getProcessInstance().getAction()
					.equalsIgnoreCase(WCConstants.ACTIVATE_CONNECTION)) {
				waterDao.enrichFileStoreIds(waterConnectionRequest);
			}
		} catch (Exception ex) {
			log.debug(ex.toString());
		}
	}
}
