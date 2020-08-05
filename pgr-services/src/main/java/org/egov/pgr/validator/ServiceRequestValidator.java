package org.egov.pgr.validator;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.repository.PGRRepository;
import org.egov.pgr.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.pgr.util.PGRConstants.USERTYPE_CITIZEN;
import static org.egov.pgr.util.PGRConstants.USERTYPE_EMPLOYEE;

@Component
public class ServiceRequestValidator {


    private PGRConfiguration config;

    private PGRRepository repository;

    @Autowired
    public ServiceRequestValidator(PGRConfiguration config, PGRRepository repository) {
        this.config = config;
        this.repository = repository;
    }


    public void validateCreate(ServiceRequest request){
        Map<String,String> errorMap = new HashMap<>();
        validateUserData(request,errorMap);

        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }



    private void validateUserData(ServiceRequest request,Map<String, String> errorMap){

        RequestInfo requestInfo = request.getRequestInfo();
        String accountId = request.getPgrEntity().getService().getAccountId();

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_CITIZEN)
            && StringUtils.isEmpty(accountId)){
            errorMap.put("INVALID_REQUEST","AccountId cannot be null");
        }
        else if(requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_CITIZEN)
                && !StringUtils.isEmpty(accountId)
                && !accountId.equalsIgnoreCase(requestInfo.getUserInfo().getUuid())){
            errorMap.put("INVALID_ACCOUNTID","The accountId is different from the user logged in");
        }

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_EMPLOYEE)){
            User citizen = request.getPgrEntity().getService().getCitizen();
            if(citizen == null)
                errorMap.put("INVALID_REQUEST","Citizen object cannot be null");
            else if(citizen.getMobileNumber()==null || citizen.getName()==null)
                errorMap.put("INVALID_REQUEST","Name and Mobile Number is mandatory in citizen object");
        }

    }


    private void validateIdleTime(ServiceRequest request){
        Service service = request.getPgrEntity().getService();
        Long lastModifiedTime = service.getAuditDetails().getLastModifiedTime();

        if(System.currentTimeMillis()-lastModifiedTime > config.getComplainMaxIdleTime())
            throw new CustomException("INVALID_ACTION","Complaint is closed");

    }



    public void validateSearch(RequestSearchCriteria criteria){

        if(criteria.getMobileNumber()!=null && criteria.getTenantId()==null)
            throw new CustomException("INVALID_SEARCH","TenantId is mandatory to search on mobileNumber");

        // TO DO

    }

    public void validateUpdate(ServiceRequest request){

        String id = request.getPgrEntity().getService().getId();

        RequestSearchCriteria criteria = RequestSearchCriteria.builder().ids(Collections.singleton(id)).build();
        List<PGREntity> pgrEntities = repository.getPGREntities(criteria);

        if(CollectionUtils.isEmpty(pgrEntities))
            throw new CustomException("INVALID_UPDATE","The record that you are trying to update does not exists");

        // TO DO

    }

}
