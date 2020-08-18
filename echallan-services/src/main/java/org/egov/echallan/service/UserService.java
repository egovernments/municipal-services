package org.egov.echallan.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.echallan.model.Challan;
import org.egov.echallan.model.ChallanRequest;
import org.egov.echallan.model.UserInfo;
import org.egov.echallan.repository.ServiceRequestRepository;
import org.egov.echallan.web.models.user.CreateUserRequest;
import org.egov.echallan.web.models.user.UserDetailResponse;
import org.egov.echallan.web.models.user.UserSearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	
	@Value("${egov.user.host}")
	private String userHost;

	@Value("${egov.user.context.path}")
	private String userContextPath;

	@Value("${egov.user.create.path}")
	private String userCreateEndpoint;

	@Value("${egov.user.search.path}")
	private String userSearchEndpoint;

	@Value("${egov.user.update.path}")
	private String userUpdateEndpoint;

	/**
	 * Creates user of the owners of property if it is not created already
	 * 
	 * @param request PropertyRequest received for creating properties
	 */
	public void createUser(ChallanRequest request) {

		Challan challan = request.getChallan();
		RequestInfo requestInfo = request.getRequestInfo();
		UserInfo userInfo = challan.getCitizen();
		Role role = Role.builder()
				.code("CITIZEN")
				.name("Citizen")
				.build();
		System.out.println("userInfo:::"+userInfo.getMobileNumber());
		addUserDefaultFields(challan.getTenantId(), role, userInfo);
		UserDetailResponse userDetailResponse = searchByUserName(userInfo.getMobileNumber(), challan.getTenantId());
		if (CollectionUtils.isEmpty(userDetailResponse.getUser())) {
			System.out.println("new user :::");
		StringBuilder uri = new StringBuilder(userHost).append(userContextPath).append(userCreateEndpoint);
		userInfo.setUserName(userInfo.getMobileNumber());
		CreateUserRequest userRequest = CreateUserRequest.builder()
				.requestInfo(requestInfo)
				.user(userInfo)
				.build();

		userDetailResponse = userCall(userRequest, uri);
		}
		else {
			//Update existing user with the input values
			System.out.println("already exists::"+userDetailResponse.getUser().get(0).getFatherOrHusbandName());
		}
		setOwnerFields(userInfo, userDetailResponse, requestInfo);
		

	}
	
	
	 private void addUserDefaultFields(String tenantId,Role role, UserInfo userInfo){
		 userInfo.setMobileNumber(userInfo.getMobileNumber());
		 userInfo.setUserName(userInfo.getMobileNumber());
		 userInfo.setActive(true);
		 userInfo.setTenantId(tenantId);
		 userInfo.setRoles(Collections.singletonList(role));
		 userInfo.setType("CITIZEN");
		 userInfo.setCreatedDate(null);
		 userInfo.setCreatedBy(null );
		 userInfo.setLastModifiedDate(null);
		 userInfo.setLastModifiedBy(null );
		 userInfo.setName(userInfo.getName());
	    }
	
	 private UserDetailResponse searchByUserName(String userName,String tenantId){
	        UserSearchRequest userSearchRequest = new UserSearchRequest();
	        userSearchRequest.setUserType("CITIZEN");
	        userSearchRequest.setUserName(userName);
	        userSearchRequest.setTenantId(tenantId);
	        StringBuilder uri = new StringBuilder(userHost).append(userSearchEndpoint);
	        return userCall(userSearchRequest,uri);

	    }
	 
	 @SuppressWarnings("unchecked")
		private UserDetailResponse userCall(Object userRequest, StringBuilder url) {
	    	
			String dobFormat = null;
			if (url.indexOf(userSearchEndpoint) != -1 || url.indexOf(userUpdateEndpoint) != -1)
				dobFormat = "yyyy-MM-dd";
			else if (url.indexOf(userCreateEndpoint) != -1)
				dobFormat = "dd/MM/yyyy";
			try{
	        	System.out.println(userRequest);
	            LinkedHashMap responseMap = (LinkedHashMap)serviceRequestRepository.fetchResult(url, userRequest);
	            parseResponse(responseMap,dobFormat);
	            UserDetailResponse userDetailResponse = mapper.convertValue(responseMap,UserDetailResponse.class);
	            return userDetailResponse;
	        }
	        catch(IllegalArgumentException  e)
	        {
	            throw new CustomException("IllegalArgumentException","ObjectMapper not able to convertValue in userCall");
	        }
	    }
	 
	 @SuppressWarnings("unchecked")
		private void parseResponse(LinkedHashMap<String, Object> responeMap,String dobFormat) {
	    	
	        List<LinkedHashMap<String, Object>> users = (List<LinkedHashMap<String, Object>>)responeMap.get("user");
	        String format1 = "dd-MM-yyyy HH:mm:ss";
	        
	        if(null != users) {
	        	
	            users.forEach( map -> {
	            	
	                        map.put("createdDate",dateTolong((String)map.get("createdDate"),format1));
	                        if((String)map.get("lastModifiedDate")!=null)
	                            map.put("lastModifiedDate",dateTolong((String)map.get("lastModifiedDate"),format1));
	                        if((String)map.get("dob")!=null)
	                            map.put("dob",dateTolong((String)map.get("dob"),dobFormat));
	                        if((String)map.get("pwdExpiryDate")!=null)
	                            map.put("pwdExpiryDate",dateTolong((String)map.get("pwdExpiryDate"),format1));
	                    }
	            );
	        }
	    }
	 
	 
	  /**
	     * Converts date to long
	     * @param date date to be parsed
	     * @param format Format of the date
	     * @return Long value of date
	     */
	    private Long dateTolong(String date,String format){
	        SimpleDateFormat f = new SimpleDateFormat(format);
	        Date d = null;
	        try {
	            d = f.parse(date);
	        } catch (ParseException e) {
	            e.printStackTrace();
	        }
	        return  d.getTime();
	    }
	 
	    private void setOwnerFields(UserInfo userInfo, UserDetailResponse userDetailResponse,RequestInfo requestInfo){
	    	
	    	userInfo.setUuid(userDetailResponse.getUser().get(0).getUuid());
	    	userInfo.setId(userDetailResponse.getUser().get(0).getId());
	    	userInfo.setUserName((userDetailResponse.getUser().get(0).getUserName()));
	    	userInfo.setCreatedBy(requestInfo.getUserInfo().getUuid());
	    	userInfo.setCreatedDate(System.currentTimeMillis());
	    	userInfo.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
	    	userInfo.setLastModifiedDate(System.currentTimeMillis());
	    	userInfo.setActive(userDetailResponse.getUser().get(0).getActive());
	    }
}
