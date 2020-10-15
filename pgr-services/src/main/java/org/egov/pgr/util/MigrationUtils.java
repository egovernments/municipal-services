package org.egov.pgr.util;


import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.web.models.User;
import org.egov.pgr.web.models.user.UserDetailResponse;
import org.egov.pgr.web.models.user.UserSearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.egov.pgr.util.PGRConstants.USERTYPE_CITIZEN;

@Component
public class MigrationUtils {


    private UserUtils userUtils;

    private PGRConfiguration config;

    @Autowired
    public MigrationUtils(UserUtils userUtils, PGRConfiguration config) {
        this.userUtils = userUtils;
        this.config = config;
    }

    public Map<Long,String> getIdtoUUIDMap(List<String> ids){

        /**
         * calls the user search API based on the given list of user uuids
         * @param uuids
         * @return
         */

            UserSearchRequest userSearchRequest =new UserSearchRequest();
            userSearchRequest.setActive(true);
            userSearchRequest.setUserType(USERTYPE_CITIZEN);


            if(!CollectionUtils.isEmpty(ids))
                userSearchRequest.setId(ids);


            StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
            UserDetailResponse userDetailResponse = userUtils.userCall(userSearchRequest,uri);
            List<User> users = userDetailResponse.getUser();

            if(CollectionUtils.isEmpty(users))
                throw new CustomException("USER_NOT_FOUND","No user found for the uuids");

            Map<Long,String> idToUuidMap = users.stream().collect(Collectors.toMap(User::getId, User::getUuid));

            if(idToUuidMap.keySet().size()!=ids.size())
                throw new CustomException("UUID_NOT_FOUND","Number of ids searched: "+ids.size()+" uuids returned: "+idToUuidMap.keySet());

            return idToUuidMap;

    }

}
