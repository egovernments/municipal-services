package org.egov.waterConnection.service;


import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.config.WSConfiguration;
import org.egov.waterConnection.constants.WCConstants;
import org.egov.waterConnection.model.AuditDetails;
import org.egov.waterConnection.model.Connection.ApplicationStatusEnum;
import org.egov.waterConnection.model.Connection.StatusEnum;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.SearchCriteria;
import org.egov.waterConnection.model.Status;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.Idgen.IdResponse;
import org.egov.waterConnection.repository.IdGenRepository;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.egov.waterConnection.validator.ValidateProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EnrichmentService {

	@Autowired
	WaterServicesUtil waterServicesUtil;

	@Autowired
	IdGenRepository idGenRepository;

	@Autowired
	WSConfiguration config;

	@Autowired
	ValidateProperty validateProperty;

	/**
	 * 
	 * @param waterConnectionList List of water connection for enriching the water connection with property.
	 * @param requestInfo is RequestInfo from request
	 */
	public void enrichWaterSearch(List<WaterConnection> waterConnectionList, RequestInfo requestInfo,
			SearchCriteria waterConnectionSearchCriteria) {
		
		if(!CollectionUtils.isEmpty(waterConnectionList)) {
			String propertyIdsString = waterConnectionList.stream()
					.map(waterConnection -> waterConnection.getProperty().getPropertyId()).collect(Collectors.toList())
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
					builder.append(waterConnection.getConnectionNo()).append(" WATER CONNECTION No");
					throw new CustomException("INVALID SEARCH ", builder.toString());
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
//		AuditDetails auditDetails = waterServicesUtil
//				.getAuditDetails(waterConnectionRequest.getRequestInfo().getUserInfo().getUuid(), true);
		waterConnectionRequest.getWaterConnection().setId(UUID.randomUUID().toString());
		waterConnectionRequest.getWaterConnection().setConnectionExecutionDate(Instant.now().getEpochSecond() * 1000);
		waterConnectionRequest.getWaterConnection().setStatus(StatusEnum.ACTIVE);
		setApplicationIdgenIds(waterConnectionRequest);
		setStatusForCreate(waterConnectionRequest);
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
		if (waterConnectionRequest.getWaterConnection().getAction().equalsIgnoreCase(WCConstants.ACTION_INITIATE)) {
			waterConnectionRequest.getWaterConnection().setApplicationStatus(ApplicationStatusEnum.INITIATED);
		}
		if (waterConnectionRequest.getWaterConnection().getAction().equalsIgnoreCase(WCConstants.ACTION_APPLY)) {
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
		
	}
	
	/**
	 * Enrich water connection request and add connection no if status is approved
	 * 
	 * @param waterConnectionrequest 
	 */
    public void postStatusEnrichment(WaterConnectionRequest waterConnectionrequest){
    	String applicationStatus = waterConnectionrequest.getWaterConnection().getApplicationStatus().name();
        if(WCConstants.STATUS_APPROVED.equalsIgnoreCase(applicationStatus)) {
        	setConnectionNO(waterConnectionrequest);
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
}
