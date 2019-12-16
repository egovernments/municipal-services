package org.egov.bpa.service.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.NotificationUtil;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.Event;
import org.egov.bpa.web.models.EventRequest;
import org.egov.bpa.web.models.SMSRequest;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

@Slf4j
@Service
public class BPANotificationService {

    private BPAConfiguration config;

    private ServiceRequestRepository serviceRequestRepository;

    private NotificationUtil util;


    @Autowired
    public BPANotificationService(BPAConfiguration config, ServiceRequestRepository serviceRequestRepository, NotificationUtil util) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
        this.util = util;
    }

	
	
	public void process(BPARequest bpaRequest) {
		List<SMSRequest> smsRequests = new LinkedList<>();
		if (null != config.getIsSMSEnabled()) {
			if (config.getIsSMSEnabled()) {
				enrichSMSRequest(bpaRequest, smsRequests);
				if (!CollectionUtils.isEmpty(smsRequests))
					util.sendSMS(smsRequests);
			}
		}
		if (null != config.getIsUserEventsNotificationEnabled()) {
			if (config.getIsUserEventsNotificationEnabled()) {
				EventRequest eventRequest = getEvents(bpaRequest);
				if (null != eventRequest)
					util.sendEventNotification(eventRequest);
			}
		}
	}



	private EventRequest getEvents(BPARequest bpaRequest) {
		
    	List<Event> events = new ArrayList<>();
        String tenantId = bpaRequest.getBPA().getTenantId();
//        String localizationMessages = util.getLocalizationMessages(tenantId,bpaRequest.getRequestInfo());  --need localization service changes.
        String localizationMessages ="DATA";
//            String message = util.getCustomizedMsg(bpaRequest.getRequestInfo(), bpaRequest.getBPA(), localizationMessages);  --need localization service changes.
        String message = "User creation successfull";
            Map<String,String > mobileNumberToOwner = new HashMap<>();
            bpaRequest.getBPA().getOwners().forEach(owner -> {
                if(owner.getMobileNumber()!=null)
                    mobileNumberToOwner.put(owner.getMobileNumber(),owner.getName());
            });
            List<SMSRequest> smsRequests = util.createSMSRequest(message,mobileNumberToOwner);
        	Set<String> mobileNumbers = smsRequests.stream().map(SMSRequest :: getMobileNumber).collect(Collectors.toSet());
        	Map<String, String> mapOfPhnoAndUUIDs = fetchUserUUIDs(mobileNumbers, bpaRequest.getRequestInfo(), bpaRequest.getBPA().getTenantId());
    		
            Map<String,String > mobileNumberToMsg = smsRequests.stream().collect(Collectors.toMap(SMSRequest::getMobileNumber, SMSRequest::getMessage));		
            for(String mobile: mobileNumbers) {
    			if(null == mapOfPhnoAndUUIDs.get(mobile) || null == mobileNumberToMsg.get(mobile)) {
    				log.error("No UUID/SMS for mobile {} skipping event", mobile);
    				continue;
    			}
    			
    		}
        if(!CollectionUtils.isEmpty(events)) {
    		return EventRequest.builder().requestInfo(bpaRequest.getRequestInfo()).events(events).build();
        }else {
        	return null;
        }
		
	}



	private Map<String, String> fetchUserUUIDs(Set<String> mobileNumbers,
			RequestInfo requestInfo, String tenantId) {

    	Map<String, String> mapOfPhnoAndUUIDs = new HashMap<>();
    	StringBuilder uri = new StringBuilder();
    	uri.append(config.getUserHost()).append(config.getUserSearchEndpoint());
    	Map<String, Object> userSearchRequest = new HashMap<>();
    	userSearchRequest.put("RequestInfo", requestInfo);
		userSearchRequest.put("tenantId", tenantId);
		userSearchRequest.put("userType", "CITIZEN");
    	for(String mobileNo: mobileNumbers) {
    		userSearchRequest.put("userName", mobileNo);
    		try {
    			Object user = serviceRequestRepository.fetchResult(uri, userSearchRequest);
    			if(null != user) {
    				String uuid = JsonPath.read(user, "$.user[0].uuid");
    				mapOfPhnoAndUUIDs.put(mobileNo, uuid);
    			}else {
        			log.error("Service returned null while fetching user for username - "+mobileNo);
    			}
    		}catch(Exception e) {
    			log.error("Exception while fetching user for username - "+mobileNo);
    			log.error("Exception trace: ",e);
    			continue;
    		}
    	}
    	return mapOfPhnoAndUUIDs;
	}



	private void enrichSMSRequest(BPARequest bpaRequest,
			List<SMSRequest> smsRequests) {
        String tenantId = bpaRequest.getBPA().getTenantId();
//      String localizationMessages = util.getLocalizationMessages(tenantId,bpaRequest.getRequestInfo());  //--Localization service changes to be done.
        String localizationMessages ="Checking";
//      String message = util.getCustomizedMsg(bpaRequest.getRequestInfo(),bpaRequest.getBPA(),localizationMessages); //--Localization service changes to be done.
        String message ="Application creation successfull";
            Map<String,String > mobileNumberToOwner = new HashMap<>();

            bpaRequest.getBPA().getOwners().forEach(owner -> {
                if(owner.getMobileNumber()!=null)
                    mobileNumberToOwner.put(owner.getMobileNumber(),owner.getName());
            });
            smsRequests.addAll(util.createSMSRequest(message,mobileNumberToOwner));
	}

}
