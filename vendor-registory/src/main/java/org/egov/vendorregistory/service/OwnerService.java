package org.egov.vendorregistory.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.egov.vendorregistory.config.VendorConfiguration;
import org.egov.vendorregistory.repository.ServiceRequestRepository;
import org.egov.vendorregistory.util.VendorConstants;
import org.egov.vendorregistory.web.model.Vendor;
import org.egov.vendorregistory.web.model.VendorRequest;
import org.egov.vendorregistory.web.model.VendorSearchCriteria;
import org.egov.vendorregistory.web.model.owner.CreateOwnerRequest;
import org.egov.vendorregistory.web.model.owner.OwnerSearchRequest;
import org.egov.vendorregistory.web.model.owner.VendorDetailResponse;
import org.egov.vendorregistory.web.model.owner.VendorSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OwnerService {

	@Autowired
	VendorConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	public void manageOwner(VendorRequest vendorRequest) {

		Vendor vendor = vendorRequest.getVendor();

		@Valid
		RequestInfo requestInfo = vendorRequest.getRequestInfo();
		User vendorDetails = vendor.getOwner();
		String tenantid = vendor.getTenantId();

		VendorDetailResponse vendorDetailResponse = null;
		vendorDetailResponse = ownerExists(vendorDetails, requestInfo);
		List<User> ownres = vendorDetailResponse.getOwner();
		String role = "EMPLOYEE";
		isRoleAvailale(ownres.get(0), role, tenantid);
		
		//I have doute need to create an role is not exist
		/*
		 * Boolean isRoleExists = isRoleAvailale(ownres.get(0), role, tenantid);
		 * System.out.println("role>>>>>>>>>>> "+isRoleExists); if(isRoleExists.FALSE) {
		 * createApplicant(vendorDetails, requestInfo); }
		 */
		

		/*
		 * if (vendorDetails.getMobileNumber() != null) { if
		 * (vendorDetails.getTenantId() == null) {
		 * vendorDetails.setTenantId(vendor.getTenantId().split("\\.")[0]); }
		 * vendorDetailResponse = ownerExists(vendorDetails, requestInfo);
		 * 
		 * if (vendorDetailResponse != null ||
		 * !CollectionUtils.isEmpty(vendorDetailResponse.getOwner())) {
		 * 
		 * if (vendorDetailResponse.getOwner().size() > 0) { Boolean foundOwner =
		 * Boolean.FALSE; for (int j = 0; j < vendorDetailResponse.getOwner().size();
		 * j++) { User user = vendorDetailResponse.getOwner().get(j); if
		 * (!user.getUserName().equalsIgnoreCase(user.getMobileNumber()) &&
		 * user.getName().equalsIgnoreCase(vendorDetails.getName())) { // found user
		 * with mobilenumber and username not same and name as equal to the // applicnat
		 * name provided by ui // then consider that user as applicant vendorDetails =
		 * user; foundOwner = Boolean.TRUE; break; } } // users exists with mobile
		 * number but non of them have the same name, then // create new user if
		 * (foundOwner == Boolean.FALSE) { vendorDetailResp =
		 * createVendor(vendorDetails, vendorRequest.getRequestInfo()); vendorDetails =
		 * vendorDetailResp.getOwner().get(0);
		 * 
		 * } } else { // User exists but only one user with the mobile number and
		 * username as same, So // create new user
		 * 
		 * vendorDetailResp = createVendor(vendorDetails,
		 * vendorRequest.getRequestInfo()); vendorDetails =
		 * vendorDetailResp.getOwner().get(0);
		 * 
		 * }
		 * 
		 * } else { // User with mobile number ifself not found then create new user and
		 * consider // the new user as applicant. vendorDetailResp =
		 * createVendor(vendorDetails, vendorRequest.getRequestInfo()); vendorDetails =
		 * vendorDetailResp.getOwner().get(0); } } else {
		 * log.debug("MobileNo is not existed in Application."); throw new
		 * CustomException(VendorErrorConstants.INVALID_APPLICANT_ERROR,
		 * "MobileNo is mandatory for ownerInfo"); }
		 */
	}

	/*
	 * private VendorDetailResponse createVendor(User owner, RequestInfo
	 * requestInfo) { Role role = getEmployeeRole();
	 * addOwnerDefaultFields(owner.getTenantId(), role, owner); StringBuilder uri =
	 * new StringBuilder(config.getOwnerHost()).append(config.getOwnerContextPath())
	 * .append(config.getOwnerCreateEndpoint()); // I am in doute
	 * setUserName(owner);
	 * 
	 * owner.setType(VendorConstants.EMPLOYEE); // Employee Or DSO I am In Doute
	 * VendorDetailResponse ownerDetailsResponse = ownerCall(new
	 * CreateOwnerRequest(requestInfo, owner), uri); return ownerDetailsResponse; }
	 */

	/*
	 * private Role getEmployeeRole() { Role role = new Role();
	 * role.setCode(VendorConstants.EMPLOYEE); // DSO need to cross verify
	 * role.setName("Employee"); // DSO return role; }
	 */

	//Need to Change to createDSO
	private VendorDetailResponse createApplicant(User owner, RequestInfo requestInfo) {
		Role role = getDSORole();
		addDsoDefaultFields(owner.getTenantId(), role, owner);
		StringBuilder uri = new StringBuilder(config.getOwnerHost()).append(config.getOwnerContextPath())
				.append(config.getOwnerCreateEndpoint());
		//As you said not needed
		//setUserName(applicant);
		owner.setType(VendorConstants.EMPLOYEE);
		VendorDetailResponse ownerDetailResponse = ownerCall(new CreateOwnerRequest(requestInfo, owner), uri);
		log.debug("owner created --> " + ownerDetailResponse.getOwner().get(0).getUuid());
		return ownerDetailResponse;
	}

	private Role getDSORole() {
		Role role = new Role();
		role.setCode(VendorConstants.EMPLOYEE);
		role.setName("FSMDSO");
		return role;
	}
	
	private void addDsoDefaultFields(String tenantId, Role role, User owner) {
		owner.setTenantId(tenantId);
		owner.setRoles(Collections.singletonList(role));
		owner.setType(VendorConstants.EMPLOYEE);
	}

	private VendorDetailResponse ownerExists(User owner, @Valid RequestInfo requestInfo) {

		OwnerSearchRequest ownerSearchRequest = new OwnerSearchRequest();
		ownerSearchRequest.setTenantId(owner.getTenantId().split("\\.")[0]);
		ownerSearchRequest.setMobileNumber(owner.getMobileNumber());
		if (!StringUtils.isEmpty(owner.getMobileNumber())) {
			ownerSearchRequest.setMobileNumber(owner.getMobileNumber());
		}
		StringBuilder uri = new StringBuilder(config.getOwnerHost()).append(config.getOwnerSearchEndpoint());
		VendorDetailResponse vendorDetailResponse = ownerCall(ownerSearchRequest, uri);

		return vendorDetailResponse;

	}

	public Boolean isRoleAvailale(User user, String role, String tenantId) {
		Boolean flag = false;
		Map<String, List<String>> tenantIdToOwnerRoles = getTenantIdToOwnerRolesMap(user);
		flag = isRoleAvailable(tenantIdToOwnerRoles.get(tenantId), role);
		return flag;
	}

	public Map<String, List<String>> getTenantIdToOwnerRolesMap(User user) {
		Map<String, List<String>> tenantIdToOwnerRoles = new HashMap<>();
		user.getRoles().forEach(role -> {
			if (tenantIdToOwnerRoles.containsKey(role.getTenantId())) {
				tenantIdToOwnerRoles.get(role.getTenantId()).add(role.getCode());
			} else {
				List<String> roleCodes = new LinkedList<>();
				roleCodes.add(role.getCode());
				tenantIdToOwnerRoles.put(role.getTenantId(), roleCodes);
			}

		});
		return tenantIdToOwnerRoles;
	}

	private Boolean isRoleAvailable(List<String> ownerRoles, String role) {
		Boolean flag = false;
		// List<String> allowedRoles = Arrays.asList(actionRoles.get(0).split(","));
		if (CollectionUtils.isEmpty(ownerRoles)) {
			return false;
		}
		ownerRoles.contains(role);
		return flag;
	}

	// Might be this is not requried
	/**
	 * Sets the username as uuid
	 * 
	 * @param owner The owner to whom the username is to assigned
	 */
	/*
	 * private void setUserName(User owner) { String uuid =
	 * UUID.randomUUID().toString(); owner.setUserName(uuid); owner.setUuid(uuid); }
	 */

	/*
	 * private void addOwnerDefaultFields(String tenantId, Role role, User owner) {
	 * // owner.setActive(true); owner.setTenantId(tenantId);
	 * owner.setRoles(Collections.singletonList(role));
	 * owner.setType(VendorConstants.EMPLOYEE); // Employee DSO }
	 */

	@SuppressWarnings("rawtypes")
	VendorDetailResponse ownerCall(Object ownerRequest, StringBuilder uri) {
		String dobFormat = null;
		if (uri.toString().contains(config.getOwnerSearchEndpoint())
				|| uri.toString().contains(config.getOwnerUpdateEndpoint()))
			dobFormat = "yyyy-MM-dd";
		else if (uri.toString().contains(config.getOwnerCreateEndpoint()))
			dobFormat = "dd/MM/yyyy";
		try {
			LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, ownerRequest);
			parseResponse(responseMap, dobFormat);
			VendorDetailResponse ownerDetailResponse = mapper.convertValue(responseMap, VendorDetailResponse.class);
			return ownerDetailResponse;
		} catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in ownerCall");
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void parseResponse(LinkedHashMap responeMap, String dobFormat) {
		List<LinkedHashMap> owners = (List<LinkedHashMap>) responeMap.get("owner");
		String format1 = "dd-MM-yyyy HH:mm:ss";
		if (owners != null) {
			owners.forEach(map -> {
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

	public VendorDetailResponse getOwner(VendorSearchCriteria criteria, RequestInfo requestInfo) {
		VendorSearchRequest ownerSearchRequest = getOwnerSearchRequest(criteria, requestInfo);
		StringBuilder uri = new StringBuilder(config.getOwnerHost()).append(config.getOwnerSearchEndpoint());
		VendorDetailResponse ownerDetailResponse = ownerCall(ownerSearchRequest, uri);
		return ownerDetailResponse;
	}

	// Dont Know what and all parameters need to be add in
	// VendorSearchRequest mean while i am adding same as UserSearchRequest
	private VendorSearchRequest getOwnerSearchRequest(VendorSearchCriteria criteria, RequestInfo requestInfo) {
		VendorSearchRequest userSearchRequest = new VendorSearchRequest();
		userSearchRequest.setRequestInfo(requestInfo);
		userSearchRequest.setTenantId(criteria.getTenantId().split("\\.")[0]);
		userSearchRequest.setMobileNumber(criteria.getMobileNumber());
		userSearchRequest.setActive(true);
		userSearchRequest.setUserType(VendorConstants.EMPLOYEE);
		if (!CollectionUtils.isEmpty(criteria.getOwnerIds()))
			userSearchRequest.setUuid(criteria.getOwnerIds());
		return userSearchRequest;
	}
}
