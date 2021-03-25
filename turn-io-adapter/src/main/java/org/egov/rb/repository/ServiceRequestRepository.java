package org.egov.rb.repository;

import java.util.Arrays;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.rb.contract.ServiceRequest;
import org.egov.rb.contract.ServiceResponse;
import org.egov.rb.model.MessageRequest;
import org.egov.rb.user.models.RequestInfo;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Repository
@Slf4j
public class ServiceRequestRepository {

	
	@Value("${egov.pgr.host}")
	private String userHost;
	
	@Value("${egov.pgr.create.endpoint}")
	private String userCreateEndPoint;
	
	@Autowired
	private RestTemplate restTemplate;
	

	  public Object fetchResult(StringBuilder uri, ServiceRequest serviceRequest) {
		  RestTemplate template=new RestTemplate();
		 
		  StringBuilder uri1 = new StringBuilder();
		  Object response=null; 
		  ObjectMapper mapper = new ObjectMapper();
	  
	  try {
		  HttpHeaders headers= new HttpHeaders();
		  headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		  HttpEntity<ServiceRequest> httpEntity=new HttpEntity<ServiceRequest>(serviceRequest,headers);
		  String finalUrl =userHost+userCreateEndPoint;
		  //System.out.println("Final url  .... ....... "+finalUrl);
		  
		  ResponseEntity<ServiceRequest>  response1 = restTemplate.postForEntity(userHost+userCreateEndPoint, httpEntity, ServiceRequest.class);
		  
		  System.out.println("response   ...  :"+ serviceRequest);
	  }catch(HttpClientErrorException e) {
		  
	  log.error("External Service threw an Exception: ",e);
	  
	 // throw new ServiceCallException(e.getResponseBodyAsString());
	  }catch(Exception e) {
	 
	  log.error("Exception while fetching from searcher: ",e); }
	  
	  return response;
	  
	  }

	public String getUserHost() {
		return userHost;
	}

	public void setUserHost(String userHost) {
		this.userHost = userHost;
	}

	public String getUserCreateEndPoint() {
		return userCreateEndPoint;
	}

	public void setUserCreateEndPoint(String userCreateEndPoint) {
		this.userCreateEndPoint = userCreateEndPoint;
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	  
	  
	
}
