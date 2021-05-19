package org.egov.rb.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.rb.contract.MessageRequest;
import org.egov.rb.pgrmodels.Address;
import org.egov.rb.pgrmodels.AddressDetail;
import org.egov.rb.pgrmodels.Citizen;
import org.egov.rb.pgrmodels.Service;
import org.egov.rb.pgrmodels.Service.SourceEnum;
import org.egov.rb.pgrmodels.ServiceRequest;
import org.egov.rb.pgrmodels.ServiceResponse;
import org.egov.rb.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@org.springframework.stereotype.Service
@Slf4j
public class TransformService {

	@Value("${egov.pgr.host}")
	private String pgrBasePath;

	@Value("${egov.pgr.create.endpoint}")
	private String pgrCreateEndPoint;

	@Value("${egov.external.host}")
	private String egovExternalHost;

	@Value("${authorization.token}")
	private String authorizationToken;

	@Value("${turn.io.message.api}")
	private String turnIoMessageAPI;

	@Value("${turn.io.profile.api}")
	private String turnIoProfileUpdateAPI;

	private String successMessage = "Thank you\uD83D\uDE4F\uD83D\uDE4F for lodging your complaint in *Public Grievance Complaint portal*.\n\n" +
			"Your Complaint No is : *{{complaintNumber}}*\n\nYou can view and track your complaint  through the link below:\n{{complaintLink}}\n\n" +
			"To lodge another complaint. Please type and send *PGR-PUNJAB*";

	@Autowired
	ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	URLShorteningSevice urlShorteningSevice;

	/***
	 * Method performs business logic to transform the data and call the pgr data
	 * we are binding the pgr uri to transform
	 * 
	 * @param messageRequest
	 * @return serviceResponse
	 */
	
	public ServiceResponse transform(MessageRequest messageRequest) {
		String message = null;
		String mobileNumber = messageRequest.getContacts().get(0).getWa_id();
		ServiceRequest servicerequest = prepareServiceRequest(messageRequest);
		StringBuilder url = new StringBuilder(pgrBasePath + pgrCreateEndPoint);
		ServiceResponse serviceResponse = serviceRequestRepository.fetchResult(url, servicerequest);
		if(serviceResponse == null){
			message = "There is some issue in our server.\n we will revert when we get the Complaint Id of your complaint";
			sendTurnMessage(message, mobileNumber);
		}
		else{
			try {
				message = prepareMessage(serviceResponse.getServices().get(0), mobileNumber);
				sendTurnMessage(message, mobileNumber);
			} catch (Exception e) {
				message = "There is some issue in our server.\n we will revert when we get the Complaint Id of your complaint";
				sendTurnMessage(message, mobileNumber);
				throw new CustomException("PGR_CREATE_ERROR", "Exception while creating PGR complaint ");
			}
		}
		setProfileField(mobileNumber);
		return serviceResponse;

	}
	
	/***
	 *  method performs mapping the data messageRequest to serviceReguest fields and get the 
	 *  data from the serviceRequest
	 * 
	 * @param messageRequest
	 * @return serviceRequest
	 */

	private ServiceRequest prepareServiceRequest(MessageRequest messageRequest) {

		ServiceRequest serviceRequest = new ServiceRequest();
		
		String mobileNumber = messageRequest.getContacts().get(0).getWa_id().substring(2);
		Service service = new Service();
		Citizen citizen = new Citizen();
		citizen.setMobileNumber(mobileNumber);
		citizen.setName(messageRequest.getContacts().get(0).getProfile().getName());
		service.setCitizen(citizen);
		//service.setServiceCode(messageRequest.getThreadContact().getContact().getComplaint_sub_category());
		service.setServiceCode("BurningOfGarbage");
		Address addressDetail = new Address();
		addressDetail.setCity(messageRequest.getThreadContact().getContact().getCity());
		addressDetail.setMohalla(messageRequest.getThreadContact().getContact().getLocality());
		service.setTenantId(messageRequest.getThreadContact().getContact().getCity());
		service.setPhone(mobileNumber);
		service.setSource(SourceEnum.WHATSAPP);
		service.setAddressDetail(addressDetail);

		List<Service> serviceList = new ArrayList<Service>();
		serviceList.add(service);
		serviceRequest.setRequestInfo(messageRequest.getRequestInfo());
		serviceRequest.setServices(serviceList);
		return serviceRequest;

	}

	private void sendTurnMessage(String message, String mobileNumber){
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

	private void setProfileField(String mobileNumber){
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

	private String prepareMessage(Service service, String mobileNumber) throws Exception {
		String message = successMessage;
		String complaintNumber = service.getServiceRequestId();
		String encodedPath = URLEncoder.encode(complaintNumber, "UTF-8");
		String url = egovExternalHost + "citizen/otpLogin?mobileNo=" + mobileNumber + "&redirectTo=complaint-details/" + encodedPath;
		String shortenedURL = urlShorteningSevice.shortenURL(url);
		message = message.replace("{{complaintNumber}}",complaintNumber).replace("{{complaintLink}}",shortenedURL);
		return message;
	}

}
