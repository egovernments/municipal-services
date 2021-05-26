package org.egov.rb.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.rb.repository.ServiceRequestRepository;
import org.egov.rb.util.MDMSUtils;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.rb.util.Constants.MDMS_SERVICECODE_SEARCH;

@Service
@Slf4j
public class TurnIoService {

    @Value("${egov.external.host}")
    private String egovExternalHost;

    @Value("${authorization.token}")
    private String authorizationToken;

    @Value("${turn.io.message.api}")
    private String turnIoMessageAPI;

    @Value("${turn.io.profile.api}")
    private String turnIoProfileUpdateAPI;

    @Value("${state.level.tenant.id}")
    private String stateLevelTenantId;

    private String successMessage = "Your Complaint No is : *{{complaintNumber}}*\n\nYou can view and track your complaint through the "+
            "link given below:\n{{complaintLink}}\n\n To lodge another complaint. Please type and send *PGR PUNJAB*";


    @Autowired
    ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    URLShorteningSevice urlShorteningSevice;

    @Autowired
    MDMSUtils mdmsUtils;

    public String getServiceCode(RequestInfo requestInfo, String complaintName){
        String jsonPath = MDMS_SERVICECODE_SEARCH.replace("{COMPLAINT_NAME}",complaintName);
        Object mdmsData = mdmsUtils.mDMSCall(requestInfo, stateLevelTenantId);

        List<String> res = null;
        try{
            res = JsonPath.read(mdmsData,jsonPath);
        }
        catch (Exception e){
            throw new CustomException("JSONPATH_ERROR","Failed to parse mdms response for department");
        }

        if(CollectionUtils.isEmpty(res))
            throw new CustomException("PARSING_ERROR","Failed to fetch service code from mdms data for complaint type: "+complaintName);

        return res.get(0);
    }

    public void sendTurnMessage(String message, String mobileNumber){
        Map<String, Object> request = new HashMap<>();
        Map<String,String> textBody = new HashMap<>();
        Object response = null;

        textBody.put("body",message);
        request.put("preview_url", true);
        request.put("recipient_type", "individual");
        request.put("to", mobileNumber);
        request.put("type", "text");
        request.put("text", textBody);


        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization" , "Bearer "+ authorizationToken);
        headers.add("Content-Type","application/json");

        HttpEntity requestEntity = new HttpEntity<>(request, headers);

        try {
            response = restTemplate.exchange(turnIoMessageAPI, HttpMethod.POST, requestEntity, Map.class);
        }catch(HttpClientErrorException e) {
            log.error("External Service threw an Exception: ",e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        }catch(Exception e) {
            log.error("Exception while fetching from searcher: ",e);
        }
    }

    public void setProfileField(String mobileNumber){
        String url = turnIoProfileUpdateAPI;
        Map<String, Object> request = new HashMap<>();
        Object response = null;

        request.put("city", null);
        request.put("cityset", false);
        request.put("locality", null);
        request.put("localityset", false);
        request.put("complaint_category", null);
        request.put("complaint_image", null);
        request.put("complaint_sub_category", null);
        request.put("complaintsetvalue", null);
        request.put("complaintset", false);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization" , "Bearer "+ authorizationToken);
        headers.add("Content-Type","application/json");
        headers.add("Accept","application/vnd.v1+json");

        HttpEntity requestEntity = new HttpEntity<>(request, headers);


        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate.setRequestFactory(httpRequestFactory);

        url = url.replace("{phoneNumber}",mobileNumber);
        try {
            response = restTemplate.exchange(url, HttpMethod.PATCH, requestEntity, Map.class);
        }catch(HttpClientErrorException e) {
            log.error("External Service threw an Exception: ",e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        }catch(Exception e) {
            log.error("Exception while fetching from searcher: ",e);
        }

    }

    public String prepareMessage(org.egov.rb.pgrmodels.Service service, String mobileNumber) throws Exception {
        String message = successMessage;
        String complaintNumber = service.getServiceRequestId();
        String encodedPath = URLEncoder.encode(complaintNumber, "UTF-8");
        String url = egovExternalHost + "citizen/otpLogin?mobileNo=" + mobileNumber + "&redirectTo=complaint-details/" + encodedPath;
        String shortenedURL = urlShorteningSevice.shortenURL(url);
        message = message.replace("{{complaintNumber}}",complaintNumber).replace("{{complaintLink}}",shortenedURL);
        return message;
    }
}
