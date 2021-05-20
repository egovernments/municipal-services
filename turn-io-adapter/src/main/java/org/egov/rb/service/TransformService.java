package org.egov.rb.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
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
import org.egov.rb.util.MDMSUtils;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.egov.rb.util.Constants.MDMS_SERVICECODE_SEARCH;

@org.springframework.stereotype.Service
@Slf4j
public class TransformService {

	@Value("${egov.pgr.host}")
	private String pgrBasePath;

	@Value("${egov.pgr.create.endpoint}")
	private String pgrCreateEndPoint;


	@Autowired
	ServiceRequestRepository serviceRequestRepository;

	@Autowired
	TurnIoService turnIoService;
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
			turnIoService.sendTurnMessage(message, mobileNumber);
		}
		else{
			try {
				message = turnIoService.prepareMessage(serviceResponse.getServices().get(0), mobileNumber);
				turnIoService.sendTurnMessage(message, mobileNumber);
			} catch (Exception e) {
				message = "There is some issue in our server.\n we will revert when we get the Complaint Id of your complaint";
				turnIoService.sendTurnMessage(message, mobileNumber);
				throw new CustomException("PGR_CREATE_ERROR", "Exception while creating PGR complaint ");
			}
		}
		turnIoService.setProfileField(mobileNumber);
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
		RequestInfo requestInfo = messageRequest.getRequestInfo();

		String complaintName = messageRequest.getThreadContact().getContact().getComplaint_sub_category();
		String serviceCode = turnIoService.getServiceCode(requestInfo,complaintName);
		String mobileNumber = messageRequest.getContacts().get(0).getWa_id().substring(2);

		Service service = new Service();
		Citizen citizen = new Citizen();
		citizen.setMobileNumber(mobileNumber);
		citizen.setName(messageRequest.getContacts().get(0).getProfile().getName());
		service.setCitizen(citizen);
		service.setServiceCode(serviceCode);
		Address addressDetail = new Address();
		addressDetail.setCity(messageRequest.getThreadContact().getContact().getCity());
		addressDetail.setMohalla(messageRequest.getThreadContact().getContact().getLocality());
		service.setTenantId(messageRequest.getThreadContact().getContact().getCity());
		service.setPhone(mobileNumber);
		service.setSource(SourceEnum.WHATSAPP);
		service.setAddressDetail(addressDetail);

		List<Service> serviceList = new ArrayList<Service>();
		serviceList.add(service);
		serviceRequest.setRequestInfo(requestInfo);
		serviceRequest.setServices(serviceList);
		return serviceRequest;

	}
}
