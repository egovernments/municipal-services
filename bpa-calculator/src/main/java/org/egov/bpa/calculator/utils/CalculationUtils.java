package org.egov.bpa.calculator.utils;

import org.egov.bpa.calculator.config.BPACalculatorConfig;
import org.egov.bpa.calculator.repository.ServiceRequestRepository;
import org.egov.bpa.calculator.web.models.AuditDetails;
import org.egov.bpa.calculator.web.models.RequestInfoWrapper;
import org.egov.bpa.calculator.web.models.bpa.BPA;
import org.egov.bpa.calculator.web.models.bpa.BPAResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CalculationUtils {

	


    @Autowired
    private BPACalculatorConfig config;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ObjectMapper mapper;


    /**
     * Creates tradeLicense search url based on tenantId and applicationNumber
     * @return tradeLicense search url
     */
  private String getBPASearchURL(){
      StringBuilder url = new StringBuilder(config.getBpaHost());
      url.append(config.getBpaContextPath());
      url.append(config.getBpaSearchEndpoint());
      url.append("?");
      url.append("tenantId=");
      url.append("{1}");
      url.append("&");
      url.append("applicationNumber=");
      url.append("{2}");
      return url.toString();
  }


    /**
     * Creates demand Search url based on tenanatId,businessService and ConsumerCode
     * @return demand search url
     */
    public String getDemandSearchURL(){
        StringBuilder url = new StringBuilder(config.getBillingHost());
        url.append(config.getDemandSearchEndpoint());
        url.append("?");
        url.append("tenantId=");
        url.append("{1}");
        url.append("&");
        url.append("businessService=");
        url.append("{2}");
        url.append("&");
        url.append("consumerCode=");
        url.append("{3}");
        return url.toString();
    }


    /**
     * Creates generate bill url using tenantId,consumerCode and businessService
     * @return Bill Generate url
     */
    public String getBillGenerateURI(){
        StringBuilder url = new StringBuilder(config.getBillingHost());
        url.append(config.getBillGenerateEndpoint());
        url.append("?");
        url.append("tenantId=");
        url.append("{1}");
        url.append("&");
        url.append("consumerCode=");
        url.append("{2}");
        url.append("&");
        url.append("businessService=");
        url.append("{3}");

        return url.toString();
    }

    public AuditDetails getAuditDetails(String by, Boolean isCreate) {
        Long time = System.currentTimeMillis();
        if(isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
    }


    /**
     * Call bpa-services to get BPA for the given applicationNumber and tenantID
     * @param requestInfo The RequestInfo of the incoming request
     * @param applicationNo The applicationNo whose BPA has to be fetched
     * @param tenantId The tenantId of the tradeLicense
     * @return The tradeLicense fo the particular applicationNumber
     */
    public BPA getBuildingPlan(RequestInfo requestInfo, String applicationNumber, String tenantId){
        String url = getBPASearchURL();
        url = url.replace("{1}",tenantId).replace("{2}",applicationNumber);
        log.info("Bpa search URL is " + url);
        Object result =serviceRequestRepository.fetchResult(new StringBuilder(url),RequestInfoWrapper.builder().
                requestInfo(requestInfo).build());

        BPAResponse response =null;
        try {
                response = mapper.convertValue(result,BPAResponse.class);
        }
        catch (IllegalArgumentException e){
            throw new CustomException("PARSING ERROR","Error while parsing response of TradeLicense Search");
        }

        if(response==null || CollectionUtils.isEmpty(response.getBpa()))
            return null;

        log.info("Bpa search response exists ? " + (response==null || CollectionUtils.isEmpty(response.getBpa())));
        return response.getBpa().get(0);
    }
}
