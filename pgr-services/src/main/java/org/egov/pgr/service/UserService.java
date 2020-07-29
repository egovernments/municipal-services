package org.egov.pgr.service;


import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.util.UserUtils;
import org.egov.pgr.web.models.ServiceRequest;
import org.egov.pgr.web.models.user.CreateUserRequest;
import org.egov.pgr.web.models.user.UserDetailResponse;
import org.egov.pgr.web.models.user.UserSearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.util.StringUtils;

import java.util.Collections;

import static org.egov.pgr.util.PGRConstants.USERTYPE_CITIZEN;

@org.springframework.stereotype.Service
public class UserService {





    private UserUtils userUtils;

    private PGRConfiguration config;



    public void callUserService(ServiceRequest request){

        if(!StringUtils.isEmpty(request.getPgrEntity().getService().getAccountId()))
            enrichUser(request);
        else if(request.getPgrEntity().getService().getCitizen()!=null)
            upsertUser(request);

    }


    public void upsertUser(ServiceRequest request){

        User user = request.getPgrEntity().getService().getCitizen();
        String tenantId = request.getPgrEntity().getService().getTenantId();
        User userServiceResponse = user;

        // Search on mobile number as user name
        UserDetailResponse userDetailResponse = searchUser(null, user.getMobileNumber());
        if (!userDetailResponse.getUser().isEmpty()) {
            User userFromSearch = userDetailResponse.getUser().get(0);
            if(!user.getName().equalsIgnoreCase(userFromSearch.getName())){
                userServiceResponse = updateUser(request.getRequestInfo(),user,userFromSearch);
            }
        }
        else {
            userServiceResponse = createUser(request.getRequestInfo(),tenantId,user);
        }

        // Enrich the accountId
        request.getPgrEntity().getService().setAccountId(userServiceResponse.getUuid());
    }


    public void enrichUser(ServiceRequest request){

        RequestInfo requestInfo = request.getRequestInfo();
        String accountId = request.getPgrEntity().getService().getAccountId();

        UserDetailResponse userDetailResponse = searchUser(accountId,null);

        if(userDetailResponse.getUser().isEmpty())
            throw new CustomException("INVALID_ACCOUNTID","No user exist for the given accountId");

        else request.getPgrEntity().getService().setCitizen(userDetailResponse.getUser().get(0));

    }


    public User createUser(RequestInfo requestInfo,String tenantId, User userInfo) {

        userUtils.addUserDefaultFields(userInfo.getMobileNumber(),tenantId, userInfo);
        StringBuilder uri = new StringBuilder(config.getUserHost())
                .append(config.getUserContextPath())
                .append(config.getUserCreateEndpoint());


        UserDetailResponse userDetailResponse = userUtils.userCall(new CreateUserRequest(requestInfo, userInfo), uri);

        return userDetailResponse.getUser().get(0);

    }

    public User updateUser(RequestInfo requestInfo,User user,User userFromSearch) {

        userFromSearch.setName(user.getName());

        StringBuilder uri = new StringBuilder(config.getUserHost())
                .append(config.getUserContextPath())
                .append(config.getUserUpdateEndpoint());


        UserDetailResponse userDetailResponse = userUtils.userCall(new CreateUserRequest(requestInfo, userFromSearch), uri);

        return userDetailResponse.getUser().get(0);

    }

    private UserDetailResponse searchUser(String accountId, String userName){

        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType(USERTYPE_CITIZEN);

        if(StringUtils.isEmpty(accountId) && StringUtils.isEmpty(userName))
            return null;

        if(!StringUtils.isEmpty(accountId))
            userSearchRequest.setUuid(Collections.singletonList(accountId));

        if(!StringUtils.isEmpty(userName))
            userSearchRequest.setUserName(userName);

        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        return userUtils.userCall(userSearchRequest,uri);

    }











}
