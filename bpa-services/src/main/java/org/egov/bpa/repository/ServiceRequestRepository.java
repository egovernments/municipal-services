package org.egov.bpa.repository;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
@Slf4j
public class ServiceRequestRepository {

	private ObjectMapper mapper;

	private RestTemplate restTemplate;

	@Autowired
	public ServiceRequestRepository(ObjectMapper mapper, RestTemplate restTemplate) {
		this.mapper = mapper;
		this.restTemplate = restTemplate;
	}

	public Object fetchResult(StringBuilder uri, Object request) {
		Object response = null;
		log.info("URI: " + uri.toString());
		try {
			log.info("Request: " + mapper.writeValueAsString(request));
			response = restTemplate.postForObject(uri.toString(), request, Map.class);
		} catch (HttpClientErrorException e) {
			log.error("External Service threw an Exception: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			log.error("Exception while fetching from searcher: ", e);
		}

		return response;
	}
}
