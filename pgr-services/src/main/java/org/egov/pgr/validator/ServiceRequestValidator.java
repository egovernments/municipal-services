package org.egov.pgr.validator;

import com.jayway.jsonpath.JsonPath;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.repository.PGRRepository;
import org.egov.pgr.util.HRMSUtil;
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

import static org.egov.pgr.util.PGRConstants.*;

@Component
public class ServiceRequestValidator {


    private PGRConfiguration config;

    private PGRRepository repository;

    private HRMSUtil hrmsUtil;

    @Autowired
    public ServiceRequestValidator(PGRConfiguration config, PGRRepository repository, HRMSUtil hrmsUtil) {
        this.config = config;
        this.repository = repository;
        this.hrmsUtil = hrmsUtil;
    }


    public void validateCreate(ServiceRequest request, Object mdmsData){
        Map<String,String> errorMap = new HashMap<>();
        validateUserData(request,errorMap);
        validateMDMS(request, mdmsData);
        validateDepartment(request, mdmsData);
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }



    public void validateUpdate(ServiceRequest request, Object mdmsData){

        String id = request.getPgrEntity().getService().getId();
        validateMDMS(request, mdmsData);
        validateDepartment(request, mdmsData);
        validateIdleTime(request);
        RequestSearchCriteria criteria = RequestSearchCriteria.builder().ids(Collections.singleton(id)).build();
        List<PGREntity> pgrEntities = repository.getPGREntities(criteria);

        if(CollectionUtils.isEmpty(pgrEntities))
            throw new CustomException("INVALID_UPDATE","The record that you are trying to update does not exists");

        // TO DO

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


    private void validateMDMS(ServiceRequest request, Object mdmsData){

        String serviceCode = request.getPgrEntity().getService().getServiceCode();
        String jsonPath = MDMS_SERVICEDEF_SEARCH.replace("{SERVICEDEF}",serviceCode);

        List<Object> res = null;

        try{
            res = JsonPath.read(mdmsData,jsonPath);
        }
        catch (Exception e){
            e.printStackTrace();
            throw new CustomException("JSONPATH_ERROR","Failed to parse mdms response");
        }

        if(CollectionUtils.isEmpty(res))
            throw new CustomException("INVALID_SERVICECODE","The service code: "+serviceCode+" is not present in MDMS");


    }


    private void validateDepartment(ServiceRequest request, Object mdmsData){

        String tenantId = request.getPgrEntity().getService().getTenantId();
        String serviceCode = request.getPgrEntity().getService().getServiceCode();
        List<String> assignes = request.getPgrEntity().getWorkflow().getAssignes();

        if(CollectionUtils.isEmpty(assignes))
            return;

        List<String> departments = hrmsUtil.getDepartment(tenantId, assignes, request.getRequestInfo());

        String jsonPath = MDMS_DEPARTMENT_SEARCH.replace("{SERVICEDEF}",serviceCode);

        List<String> res = null;
        String departmentFromMDMS;

        try{
            res = JsonPath.read(mdmsData,jsonPath);
        }
        catch (Exception e){
            e.printStackTrace();
            throw new CustomException("JSONPATH_ERROR","Failed to parse mdms response for department");
        }

        if(CollectionUtils.isEmpty(res))
            throw new CustomException("PARSING_ERROR","Failed to fetch department from mdms data for serviceCode: "+serviceCode);
        else departmentFromMDMS = res.get(0);

        Map<String, String> errorMap = new HashMap<>();

        if(!departments.contains(departmentFromMDMS))
            errorMap.put("INVALID_ASSIGNMENT","The application cannot be assigned to employee of department: "+departments.toString());


        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);

    }


    private void validateIdleTime(ServiceRequest request){
        Service service = request.getPgrEntity().getService();
        Long lastModifiedTime = service.getAuditDetails().getLastModifiedTime();

        if(!request.getPgrEntity().getWorkflow().getAction().equalsIgnoreCase(PGR_WF_REOPEN))
            return;

        else if(System.currentTimeMillis()-lastModifiedTime > config.getComplainMaxIdleTime())
            throw new CustomException("INVALID_ACTION","Complaint is closed");

    }



    public void validateSearch(RequestSearchCriteria criteria){

        if(criteria.getMobileNumber()!=null && criteria.getTenantId()==null)
            throw new CustomException("INVALID_SEARCH","TenantId is mandatory to search on mobileNumber");

        // TO DO

    }



}
