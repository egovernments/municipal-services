package org.egov.swcalculation.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.swcalculation.config.SWCalculationConfiguration;
import org.egov.swcalculation.model.*;
import org.egov.swcalculation.model.workflow.ProcessInstance;
import org.egov.swcalculation.model.workflow.ProcessInstanceResponse;
import org.egov.swcalculation.repository.ServiceRequestRepository;
import org.egov.swcalculation.util.CalculatorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class SWCalculationWorkflowValidator {

    @Autowired
    private SWCalculationConfiguration configs;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private CalculatorUtils util;

    @Autowired
    private ObjectMapper mapper;

    public Boolean nonMeterconnectionValidation(RequestInfo requestInfo, String tenantId, String connectionNo){
        Map<String,String> errorMap = new HashMap<>();
        Boolean genratedemand = true;
        applicationValidation(requestInfo,tenantId,connectionNo,errorMap);
        if(!CollectionUtils.isEmpty(errorMap)){
            log.error("DemandGeneartionError", "Demand cannot be generated as sewerage connection with connection number "+connectionNo+" or property associated with it, is in workflow and not approved yet");
            genratedemand=false;
        }
        return genratedemand;
    }

    public Map<String,String> applicationValidation(RequestInfo requestInfo,String tenantId,String connectionNo, Map<String,String> errorMap){
        SewerageConnection sewerageConnection = util.getSewerageConnection(requestInfo,connectionNo,tenantId);
        String sewerageApplicationNumber = sewerageConnection.getApplicationNo();
        Long dateEffectiveFrom = sewerageConnection.getDateEffectiveFrom();
        sewerageConnectionValidation(requestInfo,tenantId,sewerageApplicationNumber,errorMap);

        String propertyId = sewerageConnection.getPropertyId();
        Property property = util.getProperty(requestInfo,tenantId,propertyId);
        String propertyApplicationNumber = property.getAcknowldgementNumber();
        propertyValidation(requestInfo,tenantId,propertyApplicationNumber,errorMap);

        if(!StringUtils.isEmpty(dateEffectiveFrom))
            dateValidation(dateEffectiveFrom,connectionNo,errorMap);

        return  errorMap;
    }

    public void sewerageConnectionValidation(RequestInfo requestInfo,String tenantId, String sewerageApplicationNumber,Map<String,String> errorMap){
        Boolean isApplicationApproved = workflowValidation(requestInfo,tenantId,sewerageApplicationNumber);
        if(!isApplicationApproved)
            errorMap.put("sewerageApplicationError","Demand cannot be generated as sewerage connection application with application number "+sewerageApplicationNumber+" is in workflow and not approved yet");
    }


    public void propertyValidation(RequestInfo requestInfo,String tenantId, String propertyApplicationNumber,Map<String,String> errorMap){
        Boolean isApplicationApproved = workflowValidation(requestInfo,tenantId,propertyApplicationNumber);
        if(!isApplicationApproved)
            errorMap.put("PropertyApplicationError","Demand cannot be generated as property application with application number "+propertyApplicationNumber+" is in workflow and not approved yet");
    }



    public Boolean workflowValidation(RequestInfo requestInfo,String tenantId, String businessIds){
        List<ProcessInstance> processInstancesList = util.getWorkFlowProcessInstance(requestInfo,tenantId,businessIds);
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
            errormap.put("DateEffectiveFromError","Demand cannot be generated for the sewerage connection "+connectionNo+" ,the modified connection will be in effect from "+effectiveDate.toString());
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
