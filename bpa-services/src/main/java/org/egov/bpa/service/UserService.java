package org.egov.bpa.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.OwnerInfo;
import org.egov.bpa.web.models.User;
import org.egov.bpa.web.models.user.CreateUserRequest;
import org.egov.bpa.web.models.user.UserDetailResponse;
import org.egov.bpa.web.models.user.UserSearchRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class UserService {

	@Autowired
	private BPAConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	public void createUser(BPARequest bpaRequest) {
		BPA bpa = bpaRequest.getBPA();
		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		Role role = getCitizenRole();

		bpa.getOwners().forEach(owner ->
		{
							if (owner.getUuid() == null) {
								addUserDefaultFields(bpa.getTenantId(), role,
										owner);
								StringBuilder uri = new StringBuilder(config
										.getUserHost()).append(
										config.getUserContextPath()).append(
										config.getUserCreateEndpoint());
								setUserName(owner);

								UserDetailResponse userDetailResponse = userCall(
										new CreateUserRequest(requestInfo,
												owner), uri);
								if (userDetailResponse.getUser().get(0)
										.getUuid() == null) {
									throw new CustomException(
											"INVALID USER RESPONSE",
											"The user created has uuid as null");
								}
								log.info("owner created --> "
										+ userDetailResponse.getUser().get(0)
												.getUuid());
								log.info("owner created Id --> "
										+ userDetailResponse.getUser().get(0)
												.getId());
								setOwnerFields(owner, userDetailResponse,
										requestInfo);
							} else {
								log.info("owner Exists ========>");
								UserDetailResponse userDetailResponse = userExists(
										owner, requestInfo);
								if (userDetailResponse.getUser().isEmpty())
									throw new CustomException("INVALID USER",
											"The uuid " + owner.getUuid()
													+ " does not exists");
								StringBuilder uri = new StringBuilder(config
										.getUserHost());
								uri = uri.append(config.getUserContextPath())
										.append(config.getUserUpdateEndpoint());
								OwnerInfo user = new OwnerInfo();
								user.addUserWithoutAuditDetail(owner);
								addNonUpdatableFields(user, userDetailResponse
										.getUser().get(0));
								userDetailResponse = userCall(
										new CreateUserRequest(requestInfo, user),
										uri);
								setOwnerFields(owner, userDetailResponse,
										requestInfo);
							}
						});
	}

	/**
	 * Sets the immutable fields from search to update request
	 * 
	 * @param user
	 *            The user to be updated
	 * @param userFromSearchResult
	 *            The current user details according to searcvh
	 */
	private void addNonUpdatableFields(User user, User userFromSearchResult) {
		user.setUserName(userFromSearchResult.getUserName());
		user.setId(userFromSearchResult.getId());
		user.setActive(userFromSearchResult.getActive());
		user.setPassword(userFromSearchResult.getPassword());
	}

	/**
	 * Checks if the user exists in the database
	 * 
	 * @param owner
	 *            The owner from the bpa
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @return The search response from the user service
	 */
	private UserDetailResponse userExists(OwnerInfo owner,RequestInfo requestInfo) {
		UserSearchRequest userSearchRequest = new UserSearchRequest();
		userSearchRequest.setTenantId(owner.getTenantId());
		userSearchRequest.setMobileNumber(owner.getMobileNumber());
		userSearchRequest.setName(owner.getName());
		userSearchRequest.setRequestInfo(requestInfo);
		userSearchRequest.setActive(true);
		userSearchRequest.setUserType(owner.getType());
		if (owner.getUuid() != null)
			userSearchRequest.setUuid(Arrays.asList(owner.getUuid()));
		StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
		return userCall(userSearchRequest, uri);
	}


	/**
	 * Sets the username as uuid
	 * 
	 * @param owner The owner to whom the username is to assigned
	 */
	private void setUserName(OwnerInfo owner) {
		// String username = UUID.randomUUID().toString();
		String username = owner.getMobileNumber();
		owner.setUserName(username);
	}

	/**
	 * Sets ownerfields from the userResponse
	 * 
	 * @param owner
	 *            The owner from bpa
	 * @param userDetailResponse
	 *            The response from user search
	 * @param requestInfo
	 *            The requestInfo of the request
	 */
	private void setOwnerFields(OwnerInfo owner,
			UserDetailResponse userDetailResponse, RequestInfo requestInfo) {
		owner.setUuid(userDetailResponse.getUser().get(0).getUuid());
		owner.setId(userDetailResponse.getUser().get(0).getId());
		owner.setUserName((userDetailResponse.getUser().get(0).getUserName()));
		owner.setCreatedBy(requestInfo.getUserInfo().getUuid());
		owner.setCreatedDate(System.currentTimeMillis());
		owner.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
		owner.setLastModifiedDate(System.currentTimeMillis());
		owner.setActive(userDetailResponse.getUser().get(0).getActive());
	}

	/**
	 * Sets the role,type,active and tenantId for a Citizen
	 * 
	 * @param tenantId
	 *            TenantId of the property
	 * @param role
	 *            The role of the user set in this case to CITIZEN
	 * @param owner
	 *            The user whose fields are to be set
	 */
	private void addUserDefaultFields(String tenantId, Role role,
			OwnerInfo owner) {
		owner.setActive(true);
		owner.setTenantId(tenantId);
		owner.setRoles(Collections.singletonList(role));
		owner.setType("CITIZEN");
	}

	/**
	 * Creates citizen role
	 * 
	 * @return Role object for citizen
	 */
	private Role getCitizenRole() {
		Role role = new Role();
		role.setCode("CITIZEN");
		role.setName("Citizen");
		return role;
	}

	/**
	 * Returns UserDetailResponse by calling user service with given uri and
	 * object
	 * 
	 * @param userRequest
	 *            Request object for user service
	 * @param uri
	 *            The address of the endpoint
	 * @return Response from user service as parsed as userDetailResponse
	 */
	private UserDetailResponse userCall(Object userRequest, StringBuilder uri) {
		String dobFormat = null;
		if (uri.toString().contains(config.getUserSearchEndpoint())
				|| uri.toString().contains(config.getUserUpdateEndpoint()))
			dobFormat = "yyyy-MM-dd";
		else if (uri.toString().contains(config.getUserCreateEndpoint()))
			dobFormat = "dd/MM/yyyy";
		try {
			LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository
					.fetchResult(uri, userRequest);
			parseResponse(responseMap, dobFormat);
			UserDetailResponse userDetailResponse = mapper.convertValue(
					responseMap, UserDetailResponse.class);
			return userDetailResponse;
		} catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException",
					"ObjectMapper not able to convertValue in userCall");
		}
	}

	/**
	 * Parses date formats to long for all users in responseMap
	 * 
	 * @param responeMap
	 *            LinkedHashMap got from user api response
	 */
	private void parseResponse(LinkedHashMap responeMap, String dobFormat) {
		List<LinkedHashMap> users = (List<LinkedHashMap>) responeMap
				.get("user");
		String format1 = "dd-MM-yyyy HH:mm:ss";
		if (users != null) {
			users.forEach(map -> {
				map.put("createdDate",
						dateTolong((String) map.get("createdDate"), format1));
				if ((String) map.get("lastModifiedDate") != null)
					map.put("lastModifiedDate",
							dateTolong((String) map.get("lastModifiedDate"),
									format1));
				if ((String) map.get("dob") != null)
					map.put("dob",
							dateTolong((String) map.get("dob"), dobFormat));
				if ((String) map.get("pwdExpiryDate") != null)
					map.put("pwdExpiryDate",
							dateTolong((String) map.get("pwdExpiryDate"),
									format1));
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
	public UserDetailResponse getUser(BPASearchCriteria criteria,
			RequestInfo requestInfo) {
		UserSearchRequest userSearchRequest = getUserSearchRequest(criteria,
				requestInfo);
		StringBuilder uri = new StringBuilder(config.getUserHost())
				.append(config.getUserSearchEndpoint());
		UserDetailResponse userDetailResponse = userCall(userSearchRequest, uri);
		return userDetailResponse;
	}

	/**
	 * Creates userSearchRequest from bpaSearchCriteria
	 * 
	 * @param criteria
	 *            The bpaSearch criteria
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @return The UserSearchRequest based on ownerIds
	 */
	private UserSearchRequest getUserSearchRequest(BPASearchCriteria criteria,
			RequestInfo requestInfo) {
		UserSearchRequest userSearchRequest = new UserSearchRequest();
		userSearchRequest.setRequestInfo(requestInfo);
		userSearchRequest.setTenantId(criteria.getTenantId());
		userSearchRequest.setMobileNumber(criteria.getMobileNumber());
		userSearchRequest.setActive(true);
		userSearchRequest.setUserType("CITIZEN");
		if (!CollectionUtils.isEmpty(criteria.getOwnerIds()))
			userSearchRequest.setUuid(criteria.getOwnerIds());
		return userSearchRequest;
	}

	/**
	 * Updates user if present else creates new user
	 * 
	 * @param request
	 *            bpaRequest received from update
	 */
	public void updateUser(BPARequest request) {
		// TO DO update
	}

}
