package org.egov.swCalculation.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.swCalculation.config.SWCalculationConfiguration;
import org.egov.swCalculation.model.DemandNotificationObj;
import org.egov.swCalculation.model.SMSRequest;
import org.egov.swCalculation.model.SewerageConnectionRequest;
import org.egov.swCalculation.repository.ServiceRequestRepository;
import org.egov.swCalculation.util.SWCalculationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public class SewerageDemandNotificationService {
	
	
	    private SWCalculationConfiguration config;

	    private ServiceRequestRepository serviceRequestRepository;

	    private SWCalculationUtil util;
	    
	    @Autowired
	    public SewerageDemandNotificationService(SWCalculationConfiguration config, ServiceRequestRepository serviceRequestRepository, SWCalculationUtil util) {
	        this.config = config;
	        this.serviceRequestRepository = serviceRequestRepository;
	        this.util = util;
	    }
	
	 /**
     * Creates and send the sms based on the demandNotification object
     * @param request The demandNotification object listenend on the kafka topic
     */
    public void process(DemandNotificationObj request, String topic){
        List<SMSRequest> smsRequests = new LinkedList<>();
        if(null != config.getIsSMSEnabled()) {
        	if(config.getIsSMSEnabled()) {
                enrichSMSRequest(request,smsRequests);
                if(!CollectionUtils.isEmpty(smsRequests))
                	util.sendSMS(smsRequests);
        	}
        }
    }


    /**
     * Enriches the smsRequest with the customized messages
     * @param request The demandNotification object from kafka topic
     * @param smsRequests List of SMSRequets
     */
    private void enrichSMSRequest(DemandNotificationObj request,List<SMSRequest> smsRequests){

    }
    

}
