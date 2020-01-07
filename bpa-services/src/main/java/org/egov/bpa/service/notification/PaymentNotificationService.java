package org.egov.bpa.service.notification;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.service.BPAService;
import org.egov.bpa.util.NotificationUtil;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.SMSRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@Service
public class PaymentNotificationService {


    private BPAConfiguration config;

    private BPAService bpaService;

    private NotificationUtil util;
    
    private ObjectMapper mapper;


    @Autowired
    public PaymentNotificationService(BPAConfiguration config, BPAService bpaService,
                                      NotificationUtil util,ObjectMapper mapper) {
        this.config = config;
        this.bpaService = bpaService;
        this.util = util;
        this.mapper = mapper;
    }


    final String tenantIdKey = "tenantId";

    final String businessServiceKey = "businessService";

    final String consumerCodeKey = "consumerCode";

    final String payerMobileNumberKey = "mobileNumber";

    final String paidByKey = "paidBy";

    final String amountPaidKey = "amountPaid";

    final String receiptNumberKey = "receiptNumber";

    
    /**
     * Generates sms from the input record and Sends smsRequest to SMSService
     * @param record The kafka message from receipt create topic
     */
    /**
     * Generates sms from the input record and Sends smsRequest to SMSService
     * @param record The kafka message from receipt create topic
     */
    public void process(HashMap<String, Object> record){
        try{
            String jsonString = new JSONObject(record).toString();
            DocumentContext documentContext = JsonPath.parse(jsonString);
            Map<String,String> valMap = enrichValMap(documentContext);
            Map<String, Object> info = documentContext.read("$.RequestInfo");
            RequestInfo requestInfo = mapper.convertValue(info, RequestInfo.class);

            if(valMap.get(businessServiceKey).equalsIgnoreCase(config.getBusinessService())){
                BPA bpa = getBPAFromConsumerCode(valMap.get(tenantIdKey),valMap.get(consumerCodeKey),
                                                                       requestInfo);
                String localizationMessages = util.getLocalizationMessages(bpa.getTenantId(),requestInfo);
                List<SMSRequest> smsRequests = getSMSRequests(bpa,valMap,localizationMessages);
                util.sendSMS(smsRequests);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Creates the SMSRequest
     * @param license The TradeLicense for which the receipt is generated
     * @param valMap The valMap containing the values from receipt
     * @param localizationMessages The localization message to be sent
     * @return
     */
    private List<SMSRequest> getSMSRequests(BPA bpa, Map<String,String> valMap,String localizationMessages){
            List<SMSRequest> ownersSMSRequest = getOwnerSMSRequest(bpa,valMap,localizationMessages);
            SMSRequest payerSMSRequest = getPayerSMSRequest(bpa,valMap,localizationMessages);

            List<SMSRequest> totalSMS = new LinkedList<>();
            totalSMS.addAll(ownersSMSRequest);
            totalSMS.add(payerSMSRequest);

            return totalSMS;
    }


    /**
     * Creates SMSRequest for the owners
     * @param license The tradeLicense for which the receipt is created
     * @param valMap The Map containing the values from receipt
     * @param localizationMessages The localization message to be sent
     * @return The list of the SMS Requests
     */
    private List<SMSRequest> getOwnerSMSRequest(BPA bpa, Map<String,String> valMap,String localizationMessages){
        String message = util.getOwnerPaymentMsg(bpa,valMap,localizationMessages);

        HashMap<String,String> mobileNumberToOwnerName = new HashMap<>();
        bpa.getOwners().forEach(owner -> {
            if(owner.getMobileNumber()!=null)
                mobileNumberToOwnerName.put(owner.getMobileNumber(),owner.getName());
        });

        List<SMSRequest> smsRequests = new LinkedList<>();

        for(Map.Entry<String,String> entrySet : mobileNumberToOwnerName.entrySet()){
            String customizedMsg = message.replace("<1>",entrySet.getValue());
            smsRequests.add(new SMSRequest(entrySet.getKey(),customizedMsg));
        }
        return smsRequests;
    }


    /**
     * Creates SMSRequest to be send to the payer
     * @param valMap The Map containing the values from receipt
     * @param localizationMessages The localization message to be sent
     * @return
     */
    private SMSRequest getPayerSMSRequest(BPA bpa,Map<String,String> valMap,String localizationMessages){
        String message = util.getPayerPaymentMsg(bpa,valMap,localizationMessages);
        String customizedMsg = message.replace("<1>",valMap.get(paidByKey));
        SMSRequest smsRequest = new SMSRequest(valMap.get(payerMobileNumberKey),customizedMsg);
        return smsRequest;
    }


    /**
     * Enriches the map with values from receipt
     * @param context The documentContext of the receipt
     * @return The map containing required fields from receipt
     */
    private Map<String,String> enrichValMap(DocumentContext context){
        Map<String,String> valMap = new HashMap<>();
        try{
            valMap.put(businessServiceKey,context.read("$.Payments.*.paymentDetails[?(@.businessService=='TL')].businessService"));
            valMap.put(consumerCodeKey,context.read("$.Payments.*.paymentDetails[?(@.businessService=='TL')].bill.consumerCode"));
            valMap.put(tenantIdKey,context.read("$.Payments[0].tenantId"));
            valMap.put(payerMobileNumberKey,context.read("$.Payments.*.paymentDetails[?(@.businessService=='TL')].bill.mobileNumber"));
            valMap.put(paidByKey,context.read("$.Payments[0].paidBy"));
            Integer amountPaid = context.read("$.Payments.*.paymentDetails[?(@.businessService=='TL')].bill.amountPaid");
            valMap.put(amountPaidKey,amountPaid.toString());
            valMap.put(receiptNumberKey,context.read("$.Payments.*.paymentDetails[?(@.businessService=='TL')].receiptNumber"));

        }
        catch (Exception e){
            e.printStackTrace();
            throw new CustomException("RECEIPT ERROR","Unable to fetch values from receipt");
        }
        return valMap;
    }


    /**
     * Searches the tradeLicense based on the consumer code as applicationNumber
     * @param tenantId tenantId of the tradeLicense
     * @param consumerCode The consumerCode of the receipt
     * @param requestInfo The requestInfo of the request
     * @return TradeLicense for the particular consumerCode
     */
    private BPA getBPAFromConsumerCode(String tenantId,String consumerCode,RequestInfo requestInfo){

    	BPASearchCriteria searchCriteria = new BPASearchCriteria();
    	List<String> codes = Arrays.asList(consumerCode);
        searchCriteria.setApplicationNos(codes);
        searchCriteria.setTenantId(tenantId);
        List<BPA> bpas = bpaService.getBPAWithOwnerInfo(searchCriteria,requestInfo);

        if(CollectionUtils.isEmpty(bpas))
            throw new CustomException("INVALID RECEIPT","No Appllication found for the consumerCode: "
                    +consumerCode+" and tenantId: "+tenantId);

        if(bpas.size()!=1)
            throw new CustomException("INVALID RECEIPT","Multiple Application found for the consumerCode: "
                    +consumerCode+" and tenantId: "+tenantId);

        return bpas.get(0);

    }
    
}
