package org.egov.pgr.validator;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.UserInfo;
import org.egov.pgr.web.models.ServiceRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static org.egov.pgr.util.PGRConstants.USERTYPE_CITIZEN;
import static org.egov.pgr.util.PGRConstants.USERTYPE_EMPLOYEE;

@Component
public class ServiceRequestValidator {






    public void validateCreate(ServiceRequest request){
        Map<String,String> errorMap = new HashMap<>();
        validateUserData(request,errorMap);

        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }



    private void validateUserData(ServiceRequest request,Map<String, String> errorMap){

        RequestInfo requestInfo = request.getRequestInfo();

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_CITIZEN)
            && StringUtils.isEmpty(request.getService().getAccountId())){
            errorMap.put("INVALID_REQUEST","AccountId cannot be null");
        }

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_EMPLOYEE)){
            UserInfo citizen = request.getService().getCitizen();
            if(citizen == null)
                errorMap.put("INVALID_REQUEST","Citizen object cannot be null");
            else if(citizen.getMobileNumber()==null || citizen.getName()==null)
                errorMap.put("INVALID_REQUEST","Name and Mobile Number is mandatory in citizen object");
        }

    }

}
