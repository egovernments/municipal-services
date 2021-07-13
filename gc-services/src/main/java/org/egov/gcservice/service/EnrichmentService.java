package org.egov.gcservice.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.gcservice.config.GCConfiguration;
import org.egov.gcservice.repository.IdGenRepository;
import org.egov.gcservice.repository.ServiceRequestRepository;
import org.egov.gcservice.repository.SewerageDaoImpl;
import org.egov.gcservice.util.GCConstants;
import org.egov.gcservice.util.GarbageServicesUtil;
import org.egov.gcservice.web.models.*;
import org.egov.gcservice.web.models.GarbageConnection.StatusEnum;
import org.egov.gcservice.web.models.Idgen.IdResponse;
import org.egov.gcservice.web.models.users.User;
import org.egov.gcservice.web.models.users.UserDetailResponse;
import org.egov.gcservice.web.models.users.UserSearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class EnrichmentService {

	@Autowired
	private GarbageServicesUtil garbageServicesUtil;

	@Autowired
	private IdGenRepository idGenRepository;

	@Autowired
	private GCConfiguration config;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private SewerageDaoImpl sewerageDao;

	@Autowired
	private UserService userService;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	/**
	 * 
	 * @param garbageConnectionRequest
	 *            - Sewerage GarbageConnection Requst Object
	 */
	@SuppressWarnings("unchecked")
	public void enrichGarbageConnection(GarbageConnectionRequest garbageConnectionRequest, int reqType) {
		AuditDetails auditDetails = garbageServicesUtil
				.getAuditDetails(garbageConnectionRequest.getRequestInfo().getUserInfo().getUuid(), true);
		garbageConnectionRequest.getGarbageConnection().setAuditDetails(auditDetails);
		garbageConnectionRequest.getGarbageConnection().setId(UUID.randomUUID().toString());
		garbageConnectionRequest.getGarbageConnection().setStatus("ACTIVE");
		JsonNode additionalDetail = null;
//		if (garbageConnectionRequest.getGarbageConnection().getAdditionalDetails() == null) {
//			for (String constValue : GCConstants.ADDITIONAL_OBJECT) {
//				additionalDetail.put(constValue, null);
//			}
//		} else {
//			additionalDetail = mapper.convertValue(
//					garbageConnectionRequest.getGarbageConnection().getAdditionalDetails(), HashMap.class);
//		}
		// Application created date
//		additionalDetail.put(GCConstants.APP_CREATED_DATE, BigDecimal.valueOf(System.currentTimeMillis()));
//		garbageConnectionRequest.getGarbageConnection().setAdditionalDetails(additionalDetail);
		// Setting ApplicationType
		garbageConnectionRequest.getGarbageConnection().setApplicationType(
				reqType == GCConstants.CREATE_APPLICATION ? GCConstants.NEW_SEWERAGE_CONNECTION : GCConstants.MODIFY_SEWERAGE_CONNECTION);
		setSewarageApplicationIdgenIds(garbageConnectionRequest);
		setStatusForCreate(garbageConnectionRequest);

		GarbageConnection connection = garbageConnectionRequest.getGarbageConnection();

//		if (!CollectionUtils.isEmpty(connection.getRoadCuttingInfo())) {
//			connection.getRoadCuttingInfo().forEach(roadCuttingInfo -> {
//				roadCuttingInfo.setId(UUID.randomUUID().toString());
//				roadCuttingInfo.setStatus(Status.ACTIVE);
//				roadCuttingInfo.setAuditDetails(auditDetails);
//			});
//		}
	}

//	@SuppressWarnings("unchecked")
//	public void enrichingAdditionalDetails(GarbageConnectionRequest garbageConnectionRequest) {
//		HashMap<String, Object> additionalDetail = new HashMap<>();
//		if (garbageConnectionRequest.getGarbageConnection().getAdditionalDetails() == null) {
//			GCConstants.ADDITIONAL_OBJECT.forEach(key -> additionalDetail.put(key, null));
//		} else {
//			HashMap<String, Object> addDetail = mapper.convertValue(
//					garbageConnectionRequest.getGarbageConnection().getAdditionalDetails(), HashMap.class);
//			List<String> adhocPenalityAndRebateConst = Arrays.asList(GCConstants.ADHOC_PENALTY,
//					GCConstants.ADHOC_REBATE, GCConstants.APP_CREATED_DATE, GCConstants.ESTIMATION_DATE_CONST);
//			for (String constKey : GCConstants.ADDITIONAL_OBJECT) {
//				if (addDetail.getOrDefault(constKey, null) != null && adhocPenalityAndRebateConst.contains(constKey)) {
//					BigDecimal big = new BigDecimal(String.valueOf(addDetail.get(constKey)));
//					additionalDetail.put(constKey, big);
//				} else {
//					additionalDetail.put(constKey, addDetail.get(constKey));
//				}
//			}
//			if (garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction()
//					.equalsIgnoreCase(GCConstants.APPROVE_CONNECTION_CONST)) {
//				additionalDetail.put(GCConstants.ESTIMATION_DATE_CONST, System.currentTimeMillis());
//			}
//			additionalDetail.put(GCConstants.LOCALITY,addDetail.get(GCConstants.LOCALITY).toString());
//
//			for (Map.Entry<String, Object> entry: addDetail.entrySet()) {
//				if (additionalDetail.getOrDefault(entry.getKey(), null) == null) {
//					additionalDetail.put(entry.getKey(), addDetail.get(entry.getKey()));
//				}
//			}
//		}
//		garbageConnectionRequest.getGarbageConnection().setAdditionalDetails(additionalDetail);
//	}

	/**
	 * Sets status for create request
	 * 
	 * @param garbageConnectionRequest
	 *            Sewerage connection request
	 *
	 */
	private void setStatusForCreate(GarbageConnectionRequest garbageConnectionRequest) {
		
			garbageConnectionRequest.getGarbageConnection().setStatus(GCConstants.STATUS_APPROVED);
	
	}

	/**
	 * Sets the SewarageConnectionId for given GarbageConnectionRequest
	 *
	 * @param request
	 *            GarbageConnectionRequest which is to be created
	 */
	private void setSewarageApplicationIdgenIds(GarbageConnectionRequest request) {
		List<String> applicationNumbers = getIdList(request.getRequestInfo(),
				request.getGarbageConnection().getTenantId(), config.getSewerageApplicationIdGenName(),
				config.getSewerageApplicationIdGenFormat(), 1);

		if (CollectionUtils.isEmpty(applicationNumbers) || applicationNumbers.size() != 1) {
			Map<String, String> errorMap = new HashMap<>();
			errorMap.put("IDGEN ERROR ",
					"The Id of GarbageConnection returned by idgen is not equal to number of GarbageConnection");
			throw new CustomException(errorMap);
		}
		request.getGarbageConnection().setApplicationNo(applicationNumbers.listIterator().next());
	}

	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idFormat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idFormat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN_ERROR", "No ids returned from IdGen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}

	/**
	 * Enrich update sewarage connection
	 * 
	 * @param garbageConnectionRequest
	 *            - Sewerage GarbageConnection Request Object
	 */
	public void enrichUpdateGarbageConnection(GarbageConnectionRequest garbageConnectionRequest) {
		AuditDetails auditDetails = garbageServicesUtil
				.getAuditDetails(garbageConnectionRequest.getRequestInfo().getUserInfo().getUuid(), false);
		garbageConnectionRequest.getGarbageConnection().setAuditDetails(auditDetails);
		GarbageConnection connection = garbageConnectionRequest.getGarbageConnection();
//		if (!CollectionUtils.isEmpty(connection.getDocuments())) {
//			connection.getDocuments().forEach(document -> {
//				if (document.getId() == null) {
//					document.setId(UUID.randomUUID().toString());
//					document.setDocumentUid(UUID.randomUUID().toString());
//					document.setStatus(Status.ACTIVE);
//				}
//				document.setAuditDetails(auditDetails);
//			});
//		}
//		if (!CollectionUtils.isEmpty(connection.getPlumberInfo())) {
//			connection.getPlumberInfo().forEach(plumberInfo -> {
//				if (plumberInfo.getId() == null) {
//					plumberInfo.setId(UUID.randomUUID().toString());
//				}
//				plumberInfo.setAuditDetails(auditDetails);
//			});
//		}
//
//		if (!CollectionUtils.isEmpty(connection.getRoadCuttingInfo())) {
//			connection.getRoadCuttingInfo().forEach(roadCuttingInfo -> {
//				if (roadCuttingInfo.getId() == null) {
//					roadCuttingInfo.setId(UUID.randomUUID().toString());
//					roadCuttingInfo.setStatus(Status.ACTIVE);
//				}
//				roadCuttingInfo.setAuditDetails(auditDetails);
//			});
//		}

	//enrichingAdditionalDetails(garbageConnectionRequest);
	}

	/**
	 * Enrich sewerage connection request and add connection no if status is
	 * approved
	 * 
	 * @param garbageConnectionRequest
	 *            - Sewerage connection request object
	 */
	public void postStatusEnrichment(GarbageConnectionRequest garbageConnectionRequest) {
	
			setConnectionNO(garbageConnectionRequest);
	}

	/**
	 * Enrich sewerage connection request and set sewerage connection no
	 * 
	 * @param request
	 *            Sewerage GarbageConnection Request Object
	 */
	private void setConnectionNO(GarbageConnectionRequest request) {
		List<String> connectionNumbers = getIdList(request.getRequestInfo(),
				request.getGarbageConnection().getTenantId(), config.getSewerageIdGenName(),
				config.getSewerageIdGenFormat(), 1);

		if (CollectionUtils.isEmpty(connectionNumbers) || connectionNumbers.size() != 1) {
			Map<String, String> errorMap = new HashMap<>();
			errorMap.put("IDGEN_ERROR",
					"The Id of WaterConnection returned by idgen is not equal to number of WaterConnection");
			throw new CustomException(errorMap);
		}

		request.getGarbageConnection().setConnectionNo(connectionNumbers.listIterator().next());
	}

	/**
	 * Enrich fileStoreIds
	 * 
	 * @param garbageConnectionRequest
	 *            - Sewerage GarbageConnection Request Object
	 */
//	public void enrichFileStoreIds(GarbageConnectionRequest garbageConnectionRequest) {
//		try {
//			log.info("ACTION "+garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction());
//			log.info("ApplicationStatus "+garbageConnectionRequest.getGarbageConnection().getApplicationStatus());
//			if (garbageConnectionRequest.getGarbageConnection().getApplicationStatus()
//					.equalsIgnoreCase(GCConstants.PENDING_APPROVAL_FOR_CONNECTION_CODE)
//					|| garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction()
//							.equalsIgnoreCase(GCConstants.ACTION_PAY)) {
//				sewerageDao.enrichFileStoreIds(garbageConnectionRequest);
//			}
//		} catch (Exception ex) {
//			log.debug(ex.toString());
//		}
//	}

	/**
	 * Enrich sewerage connection list
	 *
	 * @param GarbageConnectionList
	 * @param requestInfo
	 */
	public void enrichConnectionHolderDeatils(List<GarbageConnection> GarbageConnectionList, SearchCriteria criteria,
			RequestInfo requestInfo) {
		if (CollectionUtils.isEmpty(GarbageConnectionList))
			return;
		Set<String> connectionHolderIds = new HashSet<>();
		for (GarbageConnection GarbageConnection : GarbageConnectionList) {
			if (!CollectionUtils.isEmpty(GarbageConnection.getConnectionHolders())) {
				connectionHolderIds.addAll(GarbageConnection.getConnectionHolders().stream()
						.map(OwnerInfo::getUuid).collect(Collectors.toSet()));
			}
		}
		if (CollectionUtils.isEmpty(connectionHolderIds))
			return;
		UserSearchRequest userSearchRequest = userService.getBaseUserSearchRequest(criteria.getTenantId(), requestInfo);
		userSearchRequest.setUuid(connectionHolderIds);
		UserDetailResponse userDetailResponse = userService.getUser(userSearchRequest);
		enrichConnectionHolderInfo(userDetailResponse, GarbageConnectionList,requestInfo);
	}

	/**
	 * Populates the owner fields inside of the sewerage connection objects from the
	 * response got from calling user api
	 * 
	 * @param userDetailResponse
	 * @param GarbageConnectionList
	 *            List of water connection whose owner's are to be populated from
	 *            userDetailsResponse
	 */
	public void enrichConnectionHolderInfo(UserDetailResponse userDetailResponse,
			List<GarbageConnection> GarbageConnectionList,RequestInfo requestInfo) {
		List<OwnerInfo> connectionHolderInfos = userDetailResponse.getUser();
		Map<String, OwnerInfo> userIdToConnectionHolderMap = new HashMap<>();
		connectionHolderInfos.forEach(user -> userIdToConnectionHolderMap.put(user.getUuid(), user));
		GarbageConnectionList.forEach(GarbageConnection -> {
			if (!CollectionUtils.isEmpty(GarbageConnection.getConnectionHolders())) {
				GarbageConnection.getConnectionHolders().forEach(holderInfo -> {
					if (userIdToConnectionHolderMap.get(holderInfo.getUuid()) == null)
						throw new CustomException("OWNER_SEARCH_ERROR", "The owner of the sewerage application"
								+ GarbageConnection.getApplicationNo() + " is not coming in user search");
					else{
						Boolean isOpenSearch = isSearchOpen(requestInfo.getUserInfo());
						if(isOpenSearch)
							holderInfo.addUserDetail(getMaskedOwnerInfo(userIdToConnectionHolderMap.get(holderInfo.getUuid())));
						else
							holderInfo.addUserDetail(userIdToConnectionHolderMap.get(holderInfo.getUuid()));
					}

				});
			}
		});
	}

	public Boolean isSearchOpen(org.egov.common.contract.request.User userInfo) {

		return userInfo.getType().equalsIgnoreCase("SYSTEM")
				&& userInfo.getRoles().stream().map(Role::getCode).collect(Collectors.toSet()).contains("ANONYMOUS");
	}

	private User getMaskedOwnerInfo(OwnerInfo info) {

		info.setMobileNumber(null);
		info.setUuid(null);
		info.setUserName(null);
		info.setGender(null);
		info.setAltContactNumber(null);
		info.setPwdExpiryDate(null);

		return info;
	}

	/**
	 * Filter the connection from connection activated or modified state
	 *
	 * @param connectionList
	 * @return
	 */
	public List<GarbageConnection> filterConnections(List<GarbageConnection> connectionList) {
		HashMap<String, GarbageConnection> connectionHashMap = new HashMap<>();
		connectionList.forEach(connection -> {
			if (!StringUtils.isEmpty(connection.getConnectionNo())) {
				if (connectionHashMap.get(connection.getConnectionNo()) == null &&
						GCConstants.FINAL_CONNECTION_STATES.contains(connection.getStatus())) {
					connectionHashMap.put(connection.getConnectionNo(), connection);
				} else if (connectionHashMap.get(connection.getConnectionNo()) != null &&
						GCConstants.FINAL_CONNECTION_STATES.contains(connection.getStatus())) {
					if (connectionHashMap.get(connection.getConnectionNo()).getStatus().
							equals(connection.getStatus())) {
						HashMap additionalDetail1 = new HashMap<>();
						HashMap additionalDetail2 = new HashMap<>();
						additionalDetail1 = mapper
								.convertValue(connectionHashMap.get(connection.getConnectionNo()).getAdditionalDetails(), HashMap.class);
						additionalDetail2 = mapper
								.convertValue(connection.getAdditionalDetails(), HashMap.class);
						BigDecimal creationDate1 = (BigDecimal) additionalDetail1.get(GCConstants.APP_CREATED_DATE);
						BigDecimal creationDate2 = (BigDecimal) additionalDetail2.get(GCConstants.APP_CREATED_DATE);
						if (creationDate1.compareTo(creationDate2) == -1) {
							connectionHashMap.put(connection.getConnectionNo(), connection);
						}
					} else {
						if (connection.getStatus().equals(GCConstants
								.MODIFIED_FINAL_STATE)) {
							connectionHashMap.put(connection.getConnectionNo(), connection);
						}
					}
				}
			}
		});
		return  new ArrayList(connectionHashMap.values());
	}

	public List<GarbageConnection> enrichPropertyDetails(List<GarbageConnection> GarbageConnectionList, SearchCriteria criteria, RequestInfo requestInfo){
		List<GarbageConnection> finalConnectionList = new ArrayList<>();
		if (CollectionUtils.isEmpty(GarbageConnectionList))
			return finalConnectionList;

		Set<String> propertyIds = new HashSet<>();
		Map<String,List<OwnerInfo>> propertyToOwner = new HashMap<>();
		for(GarbageConnection GarbageConnection : GarbageConnectionList){
			if(!StringUtils.isEmpty(GarbageConnection.getPropertyId()))
				propertyIds.add(GarbageConnection.getPropertyId());
		}
		if(!CollectionUtils.isEmpty(propertyIds)){
			PropertyCriteria propertyCriteria = new PropertyCriteria();
			if (!StringUtils.isEmpty(criteria.getTenantId())) {
				propertyCriteria.setTenantId(criteria.getTenantId());
			}
			propertyCriteria.setPropertyIds(propertyIds);
			List<Property> propertyList = garbageServicesUtil.getPropertyDetails(serviceRequestRepository.fetchResult(garbageServicesUtil.getPropertyURL(propertyCriteria),
					RequestInfoWrapper.builder().requestInfo(requestInfo).build()));

			if(!CollectionUtils.isEmpty(propertyList)){
				for(Property property: propertyList){
					propertyToOwner.put(property.getPropertyId(),property.getOwners());
				}
			}

//			for(GarbageConnection GarbageConnection : GarbageConnectionList){
//				HashMap<String, Object> additionalDetail = new HashMap<>();
//				HashMap<String, Object> addDetail = mapper
//						.convertValue(GarbageConnection.getAdditionalDetails(), HashMap.class);
//
//				for (Map.Entry<String, Object> entry: addDetail.entrySet()) {
//					if (additionalDetail.getOrDefault(entry.getKey(), null) == null) {
//						additionalDetail.put(entry.getKey(), addDetail.get(entry.getKey()));
//					}
//				}
//				List<OwnerInfo> ownerInfoList = propertyToOwner.get(GarbageConnection.getPropertyId());
//				if(!CollectionUtils.isEmpty(ownerInfoList)){
//					additionalDetail.put("ownerName",ownerInfoList.get(0).getName());
//				}
//				GarbageConnection.setAdditionalDetails(additionalDetail);
//				finalConnectionList.add(GarbageConnection);
//			}


		}
		return finalConnectionList;
	}
}
