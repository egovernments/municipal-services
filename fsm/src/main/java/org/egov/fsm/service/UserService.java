package org.egov.fsm.service;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.repository.ServiceRequestRepository;
import org.egov.fsm.util.FSMConstants;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.FSMRequest;
import org.egov.fsm.web.model.FSMSearchCriteria;
import org.egov.fsm.web.model.OwnerInfo;
import org.egov.fsm.web.model.Role;
import org.egov.fsm.web.model.user.CreateUserRequest;
import org.egov.fsm.web.model.user.UserDetailResponse;
import org.egov.fsm.web.model.user.UserSearchRequest;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class UserService {

	@Autowired
	private FSMConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	public void manageUser(FSMRequest fsmRequest) {
		FSM landInfo = fsmRequest.getFsm();
		 @Valid RequestInfo requestInfo = fsmRequest.getRequestInfo();

		landInfo.getOwners().forEach(owner -> {
			UserDetailResponse userDetailResponse = null;
			if (owner.getMobileNumber() != null) {
				if (owner.getTenantId() == null) {
					owner.setTenantId(landInfo.getTenantId().split("\\.")[0]);
				}

				userDetailResponse = userExists(owner, requestInfo);

				if (userDetailResponse == null || CollectionUtils.isEmpty(userDetailResponse.getUser())
						|| !owner.compareWithExistingUser(userDetailResponse.getUser().get(0))) {
					// if no user found with mobileNo or details were changed,
					// creating new one..
					org.egov.fsm.web.model.Role role = getCitizenRole();
					addUserDefaultFields(owner.getTenantId(), role, owner);
					StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserContextPath())
							.append(config.getUserCreateEndpoint());
					setUserName(owner);
					owner.setOwnerType(FSMConstants.EMPLOYEE);
					userDetailResponse = userCall(new CreateUserRequest(requestInfo, owner), uri);
					log.debug("owner created --> " + userDetailResponse.getUser().get(0).getUuid());
				}
				if (userDetailResponse != null)
					setOwnerFields(owner, userDetailResponse, requestInfo);
			} else {
				log.debug("MobileNo is not existed in ownerInfo.");
				throw new CustomException(FSMConstants.INVALID_ONWER_ERROR, "MobileNo is mandatory for ownerInfo");
			}
		});
	}
	private void setUserName(OwnerInfo owner) {
		owner.setUserName(UUID.randomUUID().toString());
	}
	private void setOwnerFields(OwnerInfo owner, UserDetailResponse userDetailResponse, RequestInfo requestInfo) {
		owner.setId(userDetailResponse.getUser().get(0).getId());
		owner.setUuid(userDetailResponse.getUser().get(0).getUuid());
		owner.setUserName((userDetailResponse.getUser().get(0).getUserName()));
	}
	private void addUserDefaultFields(String tenantId, Role role, OwnerInfo owner) {
		owner.setActive(true);
		owner.setTenantId(tenantId);
		owner.setRoles(Collections.singletonList(role));
		owner.setType(FSMConstants.EMPLOYEE);
	}

	private org.egov.fsm.web.model.Role getCitizenRole() {
		Role role = new Role();
		role.setCode(FSMConstants.EMPLOYEE);
		role.setName("Employee");
		return role;
	}
	private UserDetailResponse userExists(org.egov.fsm.web.model.OwnerInfo owner, @Valid RequestInfo requestInfo) {

		UserSearchRequest userSearchRequest = new UserSearchRequest();
		userSearchRequest.setTenantId(owner.getTenantId().split("\\.")[0]);
		userSearchRequest.setMobileNumber(owner.getMobileNumber());
		if(!StringUtils.isEmpty(owner.getUuid())) {
			List<String> uuids = new ArrayList<String>();
			uuids.add(owner.getUuid());
			userSearchRequest.setUuid(uuids);
		}

		StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
		return userCall(userSearchRequest, uri);
	}

	/**
	 * Returns UserDetailResponse by calling user service with given uri and object
	 * 
	 * @param userRequest
	 *            Request object for user service
	 * @param uri
	 *            The address of the end point
	 * @return Response from user service as parsed as userDetailResponse
	 */
	@SuppressWarnings("rawtypes")
	UserDetailResponse userCall(Object userRequest, StringBuilder uri) {
		String dobFormat = null;
		if (uri.toString().contains(config.getUserSearchEndpoint())
				|| uri.toString().contains(config.getUserUpdateEndpoint()))
			dobFormat = "yyyy-MM-dd";
		else if (uri.toString().contains(config.getUserCreateEndpoint()))
			dobFormat = "dd/MM/yyyy";
		try {
//			System.out.println("user search url: " + uri + userRequest);
			LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, userRequest);
			parseResponse(responseMap, dobFormat);
			UserDetailResponse userDetailResponse = mapper.convertValue(responseMap, UserDetailResponse.class);
			return userDetailResponse;
		} catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in userCall");
		}
	}

	/**
	 * Parses date formats to long for all users in responseMap
	 * 
	 * @param responeMap
	 *            LinkedHashMap got from user api response
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void parseResponse(LinkedHashMap responeMap, String dobFormat) {
		List<LinkedHashMap> users = (List<LinkedHashMap>) responeMap.get("user");
		String format1 = "dd-MM-yyyy HH:mm:ss";
		if (users != null) {
			users.forEach(map -> {
				map.put("createdDate", dateTolong((String) map.get("createdDate"), format1));
				if ((String) map.get("lastModifiedDate") != null)
					map.put("lastModifiedDate", dateTolong((String) map.get("lastModifiedDate"), format1));
				if ((String) map.get("dob") != null)
					map.put("dob", dateTolong((String) map.get("dob"), dobFormat));
				if ((String) map.get("pwdExpiryDate") != null)
					map.put("pwdExpiryDate", dateTolong((String) map.get("pwdExpiryDate"), format1));
			});
		}
	}

	/**
	 * Converts date to long
	 * 
	 * @param date
	 *            date to be parsed
	 * @param format
	 *            Format of the date
	 * @return Long value of date
	 */
	private Long dateTolong(String date, String format) {
		SimpleDateFormat f = new SimpleDateFormat(format);
		Date d = null;
		try {
			d = f.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d.getTime();
	}

	/**
	 * Call search in user service based on ownerids from criteria
	 * 
	 * @param criteria
	 *            The search criteria containing the ownerids
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @return Search response from user service based on ownerIds
	 */
	public UserDetailResponse getUser(FSMSearchCriteria criteria, RequestInfo requestInfo) {
		UserSearchRequest userSearchRequest = getUserSearchRequest(criteria, requestInfo);
		StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
		UserDetailResponse userDetailResponse = userCall(userSearchRequest, uri);
		return userDetailResponse;
	}

	/**
	 * Creates userSearchRequest from fsmSearchCriteria
	 * 
	 * @param criteria
	 *            The fsmSearch criteria
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @return The UserSearchRequest based on ownerIds
	 */
	private UserSearchRequest getUserSearchRequest(FSMSearchCriteria criteria, RequestInfo requestInfo) {
		UserSearchRequest userSearchRequest = new UserSearchRequest();
		userSearchRequest.setRequestInfo(requestInfo);
		userSearchRequest.setTenantId(criteria.getTenantId().split("\\.")[0]);
		userSearchRequest.setMobileNumber(criteria.getMobileNumber());
		userSearchRequest.setActive(true);
		userSearchRequest.setUserType(FSMConstants.CITIZEN);
		if (!CollectionUtils.isEmpty(criteria.getOwnerIds()))
			userSearchRequest.setUuid(criteria.getOwnerIds());
		return userSearchRequest;
	}
	private FSM createDesluding(FSMRequest fsmRequest, List<org.egov.common.contract.request.Role> roles) {

		FSM fsm = fsmRequest.getFsm();

		for (org.egov.common.contract.request.Role role : roles) {

			if (role.equals(FSMConstants.CITIZEN)) {

				fsm.getCitizen().getUserName();
				fsm.getCitizen().getMobile();
				fsm.getPropertyUsage();
				fsm.getAddress().getPincode();
				fsm.getAddress().getLocality();
				fsm.getAddress().getBuildingName();
				fsm.getAddress().getStreet();
				fsm.getAddress().getGeoLocation();
				fsm.getPitDetail();
			}

			if (!role.equals(FSMConstants.CITIZEN) || !role.equals(FSMConstants.EMPLOYEE)) {

				fsm.getCitizen().getUserName();
				fsm.getCitizen().getMobile();
				fsm.getPropertyUsage();
				fsm.getAddress().getPincode();
				fsm.getAddress().getLocality();
				fsm.getAddress().getBuildingName();
				fsm.getAddress().getStreet();
				fsm.getAddress().getGeoLocation();
				fsm.getPitDetail();
			}
		}
		return fsm;

	}
}