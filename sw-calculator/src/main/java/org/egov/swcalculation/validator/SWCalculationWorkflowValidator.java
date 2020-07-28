package org.egov.swcalculation.validator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swcalculation.util.CalculatorUtils;
import org.egov.swcalculation.web.models.Property;
import org.egov.swcalculation.web.models.SewerageConnection;
import org.egov.swcalculation.web.models.workflow.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SWCalculationWorkflowValidator {

    @Autowired
    private CalculatorUtils util;

    public Boolean nonMeterconnectionValidation(RequestInfo requestInfo, String tenantId, String connectionNo){
        Map<String,String> errorMap = new HashMap<>();
        Boolean genratedemand = true;
        applicationValidation(requestInfo,tenantId,connectionNo,errorMap);
        if(!CollectionUtils.isEmpty(errorMap)){
            log.error("DEMAND_GENERATION_ERROR", "Demand cannot be generated as sewerage connection with connection number "+connectionNo+" or property associated with it, is in workflow and not approved yet");
            genratedemand=false;
        }
        return genratedemand;
    }

    public Map<String,String> applicationValidation(RequestInfo requestInfo,String tenantId,String connectionNo, Map<String,String> errorMap){
        List<SewerageConnection> sewerageConnectionList = util.getSewerageConnection(requestInfo,connectionNo,tenantId);
        int size = sewerageConnectionList.size();
        SewerageConnection sewerageConnection = sewerageConnectionList.get(size-1);
        String sewerageApplicationNumber = sewerageConnection.getApplicationNo();
        sewerageConnectionValidation(requestInfo,tenantId,sewerageApplicationNumber,errorMap);
        String propertyId = sewerageConnection.getPropertyId();
        Property property = util.getProperty(requestInfo,tenantId,propertyId);
        String propertyApplicationNumber = property.getAcknowldgementNumber();
        propertyValidation(requestInfo,tenantId,propertyApplicationNumber,errorMap);
        return  errorMap;
    }

    public void sewerageConnectionValidation(RequestInfo requestInfo,String tenantId, String sewerageApplicationNumber,Map<String,String> errorMap){
        Boolean isApplicationApproved = workflowValidation(requestInfo,tenantId,sewerageApplicationNumber);
        if(!isApplicationApproved)
            errorMap.put("SEWERAGE_APPLICATION_ERROR","Demand cannot be generated as sewerage connection application with application number "+sewerageApplicationNumber+" is in workflow and not approved yet");
    }

    public void propertyValidation(RequestInfo requestInfo,String tenantId, String propertyApplicationNumber,Map<String,String> errorMap){
        Boolean isApplicationApproved = workflowValidation(requestInfo,tenantId,propertyApplicationNumber);
        if(!isApplicationApproved)
            errorMap.put("PROPERTY_APPLICATION_ERROR","Demand cannot be generated as property application with application number "+propertyApplicationNumber+" is in workflow and not approved yet");
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
}