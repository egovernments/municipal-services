package org.egov.rb.repository;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.rb.contract.ServiceRequest;
import org.egov.rb.contract.ServiceResponse;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
		
	@Autowired
	private RestTemplate restTemplate;
		
	/**
	 * Fetches results from searcher framework based on the uri and request that define what is to be searched.
	 * 
	 * @param requestInfo
	 * @param serviceReqSearchCriteria
	 * @return Object
	 * @author vishal
	 */
	public Object fetchResult(StringBuilder uri, Object request) {
		Object response=null;
		ServiceRequest serviceRequest= new ServiceRequest();
		System.out.println("method calling");
		serviceRequest.setServices(new LinkedList<>());
		HttpHeaders headers=new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<ServiceRequest> httpEntity=new HttpEntity<ServiceRequest>(serviceRequest,headers);
		
		/*
		 * ObjectMapper mapper = new ObjectMapper();
		 * mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); Object
		 * response = null;
		 */
		try {
			System.out.println("request");
			response = restTemplate.postForObject("http://localhost:8083/rainmaker-pgr/v1/requests/_create", httpEntity, ServiceResponse.class);
			System.out.println("response");
		}catch(HttpClientErrorException e) {
			//log.error("External Service threw an Exception: ",e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		}catch(Exception e) {
			System.out.println("requestexception" + e);
			//log.error("Exception while fetching from searcher: ",e);
		}
		
		return response;
		
	}
	
	
}
