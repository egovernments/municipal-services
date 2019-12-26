package org.egov.swCalculation.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.egov.swCalculation.config.SWCalculationConfiguration;
import org.egov.swCalculation.constants.SWCalculationConstant;
import org.egov.swCalculation.model.BillingSlab;
import org.egov.swCalculation.model.DemandNotificationObj;
import org.egov.swCalculation.model.NotificationReceiver;
import org.egov.swCalculation.model.SMSRequest;
import org.egov.swCalculation.model.SewerageConnectionRequest;
import org.egov.swCalculation.repository.ServiceRequestRepository;
import org.egov.swCalculation.util.SWCalculationUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SewerageDemandNotificationService {
	
	
	    private SWCalculationConfiguration config;

	    private ServiceRequestRepository serviceRequestRepository;

	    private SWCalculationUtil util;
	    
	    @Autowired
	    private ObjectMapper mapper;
	    
	    @Autowired
	    private MasterDataService masterDataService;
	    
	    
	    
	    @Autowired
	    public SewerageDemandNotificationService(SWCalculationConfiguration config, ServiceRequestRepository serviceRequestRepository, SWCalculationUtil util) {
	        this.config = config;
	        this.serviceRequestRepository = serviceRequestRepository;
	        this.util = util;
	    }
	
	 /**
     * Creates and send the sms based on the demandNotification object
     * @param request The demandNotification object listenened on the kafka topic
     */
    public void process(DemandNotificationObj request, String topic){
        List<SMSRequest> smsRequests = new LinkedList<>();
        if(null != config.getIsSMSEnabled()) {
        	if(config.getIsSMSEnabled()) {
                enrichSMSRequest(request,smsRequests,topic);
                if(!CollectionUtils.isEmpty(smsRequests))
                	util.sendSMS(smsRequests);
        	}
        }
    }


    /**
     * Enriches the smsRequest with the customized messages
     * @param request The demandNotification object from kafka topic
     * @param smsRequests List of SMSRequests
     */
    private void enrichSMSRequest(DemandNotificationObj request,List<SMSRequest> smsRequests,String topic){
        String tenantId = request.getTenentId();
        String localizationMessages = util.getLocalizationMessages(tenantId,request.getRequestInfo());
       
        List<NotificationReceiver> notificationReceiverList= new ArrayList<>();
        enrichNotificationReceiver(notificationReceiverList,request);
        
        notificationReceiverList.forEach(receiver ->{
        	
        });
        

//        for(TradeLicense license : request.getLicenses()){
//            String message = util.getCustomizedMsg(request.getRequestInfo(),license,localizationMessages);
//            if(message==null) continue;
//
//            Map<String,String > mobileNumberToOwner = new HashMap<>();
//
//            license.getTradeLicenseDetail().getOwners().forEach(owner -> {
//                if(owner.getMobileNumber()!=null)
//                    mobileNumberToOwner.put(owner.getMobileNumber(),owner.getName());
//            });
//            smsRequests.addAll(util.createSMSRequest(message,mobileNumberToOwner));
//        }
    	
    	}
    
    private void enrichNotificationReceiver(List<NotificationReceiver> notificationReceiverList,DemandNotificationObj request){
    	
    	List<BillingSlab> mappingBillingSlab;
		try {
			mappingBillingSlab = mapper.readValue(
					billingSlabMaster.get(SWCalculationConstant.SW_BILLING_SLAB_MASTER).toJSONString(),
					mapper.getTypeFactory().constructCollectionType(List.class, BillingSlab.class));
		} catch (IOException e) {
			throw new CustomException("Parsing Exception", " Billing Slab can not be parsed!");
		}
    	
    	
    }
    
    
    private void getNotificationMasterList(){
    	
    }
    

}
