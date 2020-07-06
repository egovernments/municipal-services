package org.egov.wscalculation.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.egov.wscalculation.model.*;
import org.egov.wscalculation.model.workflow.ProcessInstance;
import org.egov.wscalculation.model.workflow.ProcessInstanceResponse;
import org.egov.wscalculation.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class WSCalulationWorkflowValidator {

    @Autowired
    private WSCalculationConfiguration configs;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ObjectMapper mapper;

    public void meterconnectionValidation(MeterConnectionRequest meterConnectionRequest){
        String tenantId = meterConnectionRequest.getMeterReading().getTenantId();
        RequestInfo requestInfo = meterConnectionRequest.getRequestInfo();
        String connectionNo = meterConnectionRequest.getMeterReading().getConnectionNo();
        Map<String,String> errorMap = new HashMap<>();
        applicationValidation(requestInfo,tenantId,connectionNo,errorMap);
        if(!CollectionUtils.isEmpty(errorMap))
            throw new CustomException(errorMap);
    }
    public Boolean nonMeterconnectionValidation(RequestInfo requestInfo,String tenantId,String connectionNo){
        Map<String,String> errorMap = new HashMap<>();
        Boolean genratedemand = true;
        applicationValidation(requestInfo,tenantId,connectionNo,errorMap);
        if(!CollectionUtils.isEmpty(errorMap)){
            log.error("DemandGeneartionError", "Demand cannot be generated as water connection with connection number "+connectionNo+" or property associated with it, is in workflow and not approved yet");
            genratedemand=false;
        }
        return genratedemand;
    }

    public Map<String,String> applicationValidation(RequestInfo requestInfo,String tenantId,String connectionNo, Map<String,String> errorMap){
        WaterConnection waterConnection = getWaterConnection(requestInfo,tenantId,connectionNo);
        String waterApplicationNumber = waterConnection.getApplicationNo();
        Long dateEffectiveFrom = waterConnection.getDateEffectiveFrom();
        waterConnectionValidation(requestInfo,tenantId,waterApplicationNumber,errorMap);
        if(!StringUtils.isEmpty(dateEffectiveFrom))
            dateValidation(dateEffectiveFrom,connectionNo,errorMap);
        /*String propertyId = waterConnection.getPropertyId();
        String propertyApplicationNumber = getPropertyApplicationNumber(requestInfo,tenantId,propertyId);
        propertyValidation(requestInfo,tenantId,propertyApplicationNumber,errorMap);*/
        return  errorMap;
    }

    public void waterConnectionValidation(RequestInfo requestInfo,String tenantId, String waterApplicationNumber,Map<String,String> errorMap){
        Boolean isApplicationApproved = workflowValidation(requestInfo,tenantId,waterApplicationNumber);
        if(!isApplicationApproved)
            errorMap.put("WaterApplicationError","Demand cannot be generated as water connection application with application number "+waterApplicationNumber+" is in workflow and not approved yet");
    }

    public WaterConnection getWaterConnection(RequestInfo requestInfo, String tenantId, String connectionNo){
        String waterConnectionSearchURL = getWaterConnectionSearchURL(connectionNo,tenantId);
        Object connectionResult = serviceRequestRepository.fetchResult(new StringBuilder(waterConnectionSearchURL),
                RequestInfoWrapper.builder().requestInfo(requestInfo).build());
        WaterConnectionResponse waterConnectionResponse = mapper.convertValue(connectionResult, WaterConnectionResponse.class);
        return waterConnectionResponse.getWaterConnection().get(0);
    }

    public String getPropertyApplicationNumber(RequestInfo requestInfo,String tenantId,String propertyId){
        String propertySearchURL = getPropertySearchURL(propertyId,tenantId);
        Object propertyResult = serviceRequestRepository.fetchResult(new StringBuilder(propertySearchURL),
                RequestInfoWrapper.builder().requestInfo(requestInfo).build());
        PropertyResponse properties = mapper.convertValue(propertyResult, PropertyResponse.class);
        return properties.getProperties().get(0).getAcknowldgementNumber();
    }

    public void propertyValidation(RequestInfo requestInfo,String tenantId, String propertyApplicationNumber,Map<String,String> errorMap){
        Boolean isApplicationApproved = workflowValidation(requestInfo,tenantId,propertyApplicationNumber);
        if(!isApplicationApproved)
            errorMap.put("PropertyApplicationError","Demand cannot be generated as property application with application number "+propertyApplicationNumber+" is in workflow and not approved yet");
    }

    public String getWaterConnectionSearchURL(String connectionNo,String tenantId){
        StringBuilder url = new StringBuilder(configs.getWaterConnectionHost());
        url.append(configs.getWaterConnectionSearchEndPoint()).append("?");
        url.append("tenantId=").append(tenantId).append("&");
        url.append("connectionNumber=").append(connectionNo);
        return url.toString();
    }
    public String getPropertySearchURL(String propertyId,String tenantId){
        StringBuilder url = new StringBuilder(configs.getPropertyHost());
        url.append(configs.getSearchPropertyEndPoint()).append("?");
        url.append("tenantId=").append(tenantId).append("&");
        url.append("uuids=").append(propertyId);
        return url.toString();
    }



    public Boolean workflowValidation(RequestInfo requestInfo,String tenantId, String businessIds){
        StringBuilder url = new StringBuilder(configs.getWorkflowHost());
        url.append(configs.getSearchWorkflowProcessEndPoint()).append("?");
        url.append("tenantId=").append(tenantId).append("&");
        url.append("businessIds=").append(businessIds);

        Object result = serviceRequestRepository.fetchResult(new StringBuilder(url.toString()),
                RequestInfoWrapper.builder().requestInfo(requestInfo).build());

        ProcessInstanceResponse processInstanceResponse = mapper.convertValue(result, ProcessInstanceResponse.class);
        List<ProcessInstance> processInstancesList = processInstanceResponse.getProcessInstances();
        Boolean isApplicationApproved = false;

        for(ProcessInstance processInstances : processInstancesList){
            if(processInstances.getState().getIsTerminateState()){
                isApplicationApproved=true;
            }
        }

        return isApplicationApproved;
    }

    public Map<String,String> dateValidation(Long dateEffectiveFrom, String connectionNo, Map<String,String> errormap){
        if(System.currentTimeMillis() < dateEffectiveFrom){
            String effectiveDate = getDate(dateEffectiveFrom);
            errormap.put("DateEffectiveFromError","Demand cannot be generated for the water connection "+connectionNo+" ,the modified connection will be in effect from "+effectiveDate.toString());
        }

        return errormap;
    }

    public String getDate(Long dateEffectiveFrom){
        Date date = new Date(dateEffectiveFrom);
        DateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
        dateformat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        return dateformat.format(date);
    }

}