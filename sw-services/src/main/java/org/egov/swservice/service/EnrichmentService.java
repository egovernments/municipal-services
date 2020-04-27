package org.egov.swservice.service;


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
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.model.AuditDetails;
import org.egov.swservice.model.Connection.ApplicationStatusEnum;
import org.egov.swservice.model.Connection.StatusEnum;
import org.egov.swservice.model.Property;
import org.egov.swservice.model.SearchCriteria;
import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.model.Status;
import org.egov.swservice.model.Idgen.IdResponse;
import org.egov.swservice.repository.IdGenRepository;
import org.egov.swservice.repository.SewarageDaoImpl;
import org.egov.swservice.util.SWConstants;
import org.egov.swservice.util.SewerageServicesUtil;
import org.egov.swservice.validator.ValidateProperty;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class EnrichmentService {

	
	@Autowired
	private SewerageServicesUtil sewerageServicesUtil;


	@Autowired
	private IdGenRepository idGenRepository;

	@Autowired
	private SWConfiguration config;

	@Autowired
	private ValidateProperty validateProperty;

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private PdfFileStoreService pdfFileStroeService;
	
	@Autowired
	private SewarageDaoImpl sewerageDao;


	/**
	 * 
	 * @param sewerageConnectionList
	 *            List of sewerage connection for enriching the sewerage connection
	 *            with property.
	 * @param requestInfo 
	 *            is RequestInfo from request
	 */

	public void enrichSewerageSearch(List<SewerageConnection> sewerageConnectionList, RequestInfo requestInfo,
			SearchCriteria sewerageConnectionSearchCriteria) {

		if (!sewerageConnectionList.isEmpty()) {
			String propertyIdsString = sewerageConnectionList.stream()
					.map(sewerageConnection -> sewerageConnection.getProperty().getPropertyId())
					.collect(Collectors.toSet()).stream().collect(Collectors.joining(","));
			List<Property> propertyList = sewerageServicesUtil
					.searchPropertyOnId(sewerageConnectionSearchCriteria.getTenantId(), propertyIdsString, requestInfo);
			HashMap<String, Property> propertyMap = propertyList.stream()
					.collect(Collectors.toMap(Property::getPropertyId, Function.identity(),
							(oldValue, newValue) -> newValue, LinkedHashMap::new));
			sewerageConnectionList.forEach(sewerageConnection -> {

				String propertyId = sewerageConnection.getProperty().getPropertyId();
				if (propertyMap.containsKey(propertyId)) {
					sewerageConnection.setProperty(propertyMap.get(propertyId));
				} else {
					StringBuilder builder = new StringBuilder();
					builder.append(sewerageConnection.getConnectionNo() == null ? sewerageConnection.getApplicationNo()
							: sewerageConnection.getConnectionNo());
					log.error("", builder.toString());
				}
			});

		}
	}
	

	
	/**
	 * 
	 * @param sewerageConnectionRequest
	 * @param propertyList
	 */

	public void enrichSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest) {
		validateProperty.enrichPropertyForSewerageConnection(sewerageConnectionRequest);
		//TODO - Models need to be updated with AuditDetails
		//AuditDetails auditDetails = sewerageServicesUtil
		//		.getAuditDetails(sewerageConnectionRequest.getRequestInfo().getUserInfo().getUuid(), true);
		sewerageConnectionRequest.getSewerageConnection().setId(UUID.randomUUID().toString());
		sewerageConnectionRequest.getSewerageConnection().setStatus(StatusEnum.ACTIVE);
		//Application created date
		HashMap<String, Object> additionalDetail = new HashMap<>();
	    additionalDetail.put(SWConstants.APP_CREATED_DATE, BigDecimal.valueOf(System.currentTimeMillis()));
	    sewerageConnectionRequest.getSewerageConnection().setAdditionalDetails(additionalDetail);
		setSewarageApplicationIdgenIds(sewerageConnectionRequest);
		setStatusForCreate(sewerageConnectionRequest);
	}
	
	@SuppressWarnings("unchecked")
	public void enrichingAdditionalDetails(SewerageConnectionRequest sewerageConnectionRequest) {
		HashMap<String, Object> additionalDetail = new HashMap<>();
		if (sewerageConnectionRequest.getSewerageConnection().getAdditionalDetails() == null) {
			SWConstants.ADHOC_PENALTY_REBATE.forEach(key -> {
				additionalDetail.put(key, null);
			});
		} else {
			HashMap<String, Object> addDetail = mapper.convertValue(
					sewerageConnectionRequest.getSewerageConnection().getAdditionalDetails(), HashMap.class);
			List<String> adhocPenalityAndRebateConst = Arrays.asList(SWConstants.ADHOC_PENALTY,
					SWConstants.ADHOC_REBATE,SWConstants.APP_CREATED_DATE);
			for (String constKey : SWConstants.ADHOC_PENALTY_REBATE) {
				if (addDetail.getOrDefault(constKey, null) != null && adhocPenalityAndRebateConst.contains(constKey)) {
					BigDecimal big = new BigDecimal(String.valueOf(addDetail.get(constKey)));
					additionalDetail.put(constKey, big);
				} else {
					additionalDetail.put(constKey, addDetail.get(constKey));
				}
			}
		}
		sewerageConnectionRequest.getSewerageConnection().setAdditionalDetails(additionalDetail);
		enrichFileStoreIds(sewerageConnectionRequest);
	}
	
	
	/**
	 * Sets status for create request
	 * 
	 * @param ConnectionRequest
	 *            The create request
	 */
	private void setStatusForCreate(SewerageConnectionRequest sewerageConnectionRequest) {
		if (sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction()
				.equalsIgnoreCase(SWConstants.ACTION_INITIATE)) {
			sewerageConnectionRequest.getSewerageConnection().setApplicationStatus(ApplicationStatusEnum.INITIATED);
		}
		if (sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction().equalsIgnoreCase(SWConstants.ACTION_APPLY)) {
			sewerageConnectionRequest.getSewerageConnection().setApplicationStatus(ApplicationStatusEnum.APPLIED);
		}
	}
	


	/**
	 * Sets the SewarageConnectionId for given SewerageConnectionRequest
	 *
	 * @param request SewerageConnectionRequest which is to be created
	 */
	private void setSewarageApplicationIdgenIds(SewerageConnectionRequest request) {
		List<String> applicationNumbers = getIdList(request.getRequestInfo(), 
				request.getRequestInfo().getUserInfo().getTenantId(), 
				config.getSewerageApplicationIdGenName(),
				config.getSewerageApplicationIdGenFormat(), 1);

		if (CollectionUtils.isEmpty(applicationNumbers) || applicationNumbers.size() != 1) {
			Map<String, String> errorMap = new HashMap<>();
			errorMap.put("IDGEN ERROR ",
					"The Id of SewerageConnection returned by idgen is not equal to number of SewerageConnection");
			throw new CustomException(errorMap);
		}
		request.getSewerageConnection().setApplicationNo(applicationNumbers.listIterator().next());
	}

	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}
	
	/**
	 * Enrich update sewarage connection
	 * 
	 * @param sewarageConnectionRequest
	 */
	public void enrichUpdateSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest) {
		validateProperty.enrichPropertyForSewerageConnection(sewerageConnectionRequest);
		AuditDetails auditDetails = sewerageServicesUtil
				.getAuditDetails(sewerageConnectionRequest.getRequestInfo().getUserInfo().getUuid(), false);
		SewerageConnection connection = sewerageConnectionRequest.getSewerageConnection();
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
		enrichingAdditionalDetails(sewerageConnectionRequest);
	}
	
	/**
	 * Enrich sewerage connection request and add connection no if status is approved
	 * 
	 * @param sewerageConnectionrequest 
	 */
	public void postStatusEnrichment(SewerageConnectionRequest sewerageConnectionRequest) {
		if (SWConstants.STATUS_APPROVED
				.equalsIgnoreCase(sewerageConnectionRequest.getSewerageConnection().getApplicationStatus().name())) {
			setConnectionNO(sewerageConnectionRequest);
		}
	}
    
	/**
	 * Enrich sewergae connection request and set sewerage connection no
	 * 
	 * @param request
	 */
	private void setConnectionNO(SewerageConnectionRequest request) {
		List<String> connectionNumbers = getIdList(request.getRequestInfo(), 
				request.getRequestInfo().getUserInfo().getTenantId(), 
				config.getSewerageIdGenName(),
				config.getSewerageIdGenFormat(), 1);
		
		if (CollectionUtils.isEmpty(connectionNumbers) || connectionNumbers.size() != 1) {
			Map<String, String> errorMap = new HashMap<>();
			errorMap.put("IDGEN_ERROR",
					"The Id of WaterConnection returned by idgen is not equal to number of WaterConnection");
			throw new CustomException(errorMap);
		}
			
		request.getSewerageConnection().setConnectionNo(connectionNumbers.listIterator().next());
	}

	/**
	 * Enrich fileStoreIds
	 * 
	 * @param sewerageConnectionRequest
	 */
	@SuppressWarnings("unchecked")
	public void enrichFileStoreIds(SewerageConnectionRequest sewerageConnectionRequest) {
		try {
			if (sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction()
					.equalsIgnoreCase(SWConstants.ACTIVATE_CONNECTION)) {
				sewerageDao.enrichFileStoreIds(sewerageConnectionRequest);
			}
		} catch (Exception ex) {
			log.debug(ex.toString());
		}
	}
}
