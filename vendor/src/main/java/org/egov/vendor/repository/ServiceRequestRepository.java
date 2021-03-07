package org.egov.vendor.repository;

import java.util.Map;

import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Repository
public class ServiceRequestRepository {

	private RestTemplate restTemplate;

	@Autowired
	public ServiceRequestRepository(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	/**
	 * fetchResult form the different services based on the url and request object
	 * 
	 * @param uri
	 * @param request
	 * @return
	 */
	public Object fetchResult(StringBuilder uri, Object request) {
		Object response = null;
		try {
			response = restTemplate.postForObject(uri.toString(), request, Map.class);
		} catch (HttpClientErrorException e) {
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (CustomException e) {
			throw new ServiceCallException(e.getMessage());
		}

		return response;
	}
}
