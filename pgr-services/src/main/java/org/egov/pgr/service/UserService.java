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



    public void upsertUser(ServiceRequest request){

        User userInfo = request.getPgrEntity().getService().getCitizen();
        String tenantId = request.getPgrEntity().getService().getTenantId();

        // Search on mobile number as user name
        UserDetailResponse userDetailResponse = searchUser(null, userInfo.getMobileNumber());
        if (!userDetailResponse.getUser().isEmpty()) {
            User user = userDetailResponse.getUser().get(0);
        }
        else {
            createUser(request.getRequestInfo(),tenantId,userInfo);
        }
    }


    public void createUser(RequestInfo requestInfo,String tenantId, User userInfo) {

        userUtils.addUserDefaultFields(userInfo.getMobileNumber(),tenantId, userInfo);
        StringBuilder uri = new StringBuilder(config.getUserHost())
                .append(config.getUserContextPath())
                .append(config.getUserCreateEndpoint());


        UserDetailResponse userDetailResponse = userUtils.userCall(new CreateUserRequest(requestInfo, userInfo), uri);

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
