package org.egov.land.repository;

import java.util.Map;

import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ServiceRequestRepository {

	private RestTemplate restTemplate;

	@Autowired
	public ServiceRequestRepository(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public Object fetchResult(StringBuilder uri, Object request) {
		Object response = null;
		log.debug("URI: " + uri.toString());
		try {
			response = restTemplate.postForObject(uri.toString(), request, Map.class);
		} catch (HttpClientErrorException e) {
			log.error("External Service threw an Exception: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (CustomException e) {
			log.error("Exception while fetching from searcher: ", e);
		}

		return response;
	}
}
