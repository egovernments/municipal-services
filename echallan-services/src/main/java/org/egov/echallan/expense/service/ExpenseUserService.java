package org.egov.echallan.expense.service;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.echallan.expense.model.Expense;
import org.egov.echallan.expense.model.ExpenseRequest;
import org.egov.echallan.expense.model.UserInfo;
import org.egov.echallan.expense.web.models.user.UserDetailResponse;
import org.egov.echallan.model.SearchCriteria;
import org.egov.echallan.repository.ServiceRequestRepository;
import org.egov.echallan.web.models.user.UserSearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ExpenseUserService {

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

	public void setAccountUser(ExpenseRequest request) {

		Expense expense = request.getExpense();
		RequestInfo requestInfo = request.getRequestInfo();
		if(null == expense.getCitizen())
			expense.setCitizen(new UserInfo());
		UserInfo userInfo = expense.getCitizen();
		if (expense.getAccountId() != null) {
			UserDetailResponse userDetailResponse = userExists(expense, requestInfo);
			if (userDetailResponse.getUser().isEmpty())
				throw new CustomException("INVALID Vendor", "The uuid " + expense.getAccountId() + " does not exists");
			// update needs to be added
			setOwnerFields(userInfo, userDetailResponse, requestInfo);
		}

	}
	
	
	private UserDetailResponse userExists(Expense expense,RequestInfo requestInfo){
        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setTenantId(expense.getTenantId().split("\\.")[0]);
        userSearchRequest.setRequestInfo(requestInfo);
        userSearchRequest.setActive(true);
        userSearchRequest.setUuid(Arrays.asList(expense.getAccountId()));
        StringBuilder uri = new StringBuilder(userHost).append(userSearchEndpoint);
        return userCall(userSearchRequest,uri);
    }
	
	 private void setOwnerFields(UserInfo owner, UserDetailResponse userDetailResponse,RequestInfo requestInfo){
	        owner.setUuid(userDetailResponse.getUser().get(0).getUuid());
	        owner.setId(userDetailResponse.getUser().get(0).getId());
	        owner.setUserName((userDetailResponse.getUser().get(0).getUserName()));
	        owner.setCreatedBy(requestInfo.getUserInfo().getUuid());
	        owner.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
	        owner.setCreatedDate(System.currentTimeMillis());
	        owner.setLastModifiedDate(System.currentTimeMillis());
	        owner.setActive(userDetailResponse.getUser().get(0).getActive());
	        owner.setName(userDetailResponse.getUser().get(0).getName());
	        owner.setMobileNumber(userDetailResponse.getUser().get(0).getMobileNumber());
	    } 
	 
	 private void addUserDefaultFields(String tenantId,Role role, UserInfo userInfo){
		 userInfo.setActive(true);
		 userInfo.setTenantId(tenantId.split("\\.")[0]);
		 userInfo.setRoles(Collections.singletonList(role));
		 userInfo.setType("CITIZEN");
	    }
	
	 public  UserDetailResponse searchByUserName(String userName,String tenantId){
	        UserSearchRequest userSearchRequest = new UserSearchRequest();
	        userSearchRequest.setUserType("CITIZEN");
	        userSearchRequest.setUserName(userName);
	        userSearchRequest.setTenantId(tenantId);
	        StringBuilder uri = new StringBuilder(userHost).append(userSearchEndpoint);
	        return userCall(userSearchRequest,uri);

	    }
	 
	 public UserDetailResponse getUser(SearchCriteria criteria,RequestInfo requestInfo){
	        UserSearchRequest userSearchRequest = getUserSearchRequest(criteria,requestInfo);
	        StringBuilder uri = new StringBuilder(userHost).append(userSearchEndpoint);
	        UserDetailResponse userDetailResponse = userCall(userSearchRequest,uri);
	        return userDetailResponse;
	    }

	    private UserSearchRequest getUserSearchRequest(SearchCriteria criteria, RequestInfo requestInfo){
	        UserSearchRequest userSearchRequest = new UserSearchRequest();
	        userSearchRequest.setRequestInfo(requestInfo);
	        userSearchRequest.setTenantId(criteria.getTenantId());
	        userSearchRequest.setMobileNumber(criteria.getMobileNumber());
	        userSearchRequest.setActive(true);
	        userSearchRequest.setUserType("CITIZEN");
	        if(!CollectionUtils.isEmpty(criteria.getUserIds()))
	            userSearchRequest.setUuid(criteria.getUserIds());
	        return userSearchRequest;
	    }
	 
	 
	 @SuppressWarnings("unchecked")
		private UserDetailResponse userCall(Object userRequest, StringBuilder url) {
	    	
			String dobFormat = null;
			if (url.indexOf(userSearchEndpoint) != -1 || url.indexOf(userUpdateEndpoint) != -1)
				dobFormat = "yyyy-MM-dd";
			else if (url.indexOf(userCreateEndpoint) != -1)
				dobFormat = "dd/MM/yyyy";
			try{
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
	 
	   
}
