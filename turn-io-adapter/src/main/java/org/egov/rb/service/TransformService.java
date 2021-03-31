package org.egov.rb.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.rb.contract.MessageRequest;
import org.egov.rb.pgrmodels.AddressDetail;
import org.egov.rb.pgrmodels.Citizen;
import org.egov.rb.pgrmodels.Service;
import org.egov.rb.pgrmodels.Service.SourceEnum;
import org.egov.rb.pgrmodels.ServiceRequest;
import org.egov.rb.pgrmodels.ServiceResponse;
import org.egov.rb.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;

@org.springframework.stereotype.Service
@Slf4j
public class TransformService {

	@Value("${egov.pgr.host}")
	private String pgrBasePath;

	@Value("${egov.pgr.create.endpoint}")
	private String pgrCreateEndPoint;

	@Autowired
	ServiceRequestRepository serviceRequestRepository;

	
	public ServiceResponse transform(MessageRequest messageRequest) {
		ServiceRequest servicerequest = prepareServiceRequest(messageRequest);
		StringBuilder url = new StringBuilder(pgrBasePath + pgrCreateEndPoint);
		ServiceResponse serviceResponse = serviceRequestRepository.fetchResult(url, servicerequest);
		return serviceResponse;

	}

	private ServiceRequest prepareServiceRequest(MessageRequest messageRequest) {

		ServiceRequest serviceRequest = new ServiceRequest();
		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setApiId("rainmaker");
		requestInfo.setVer("1.0");
		requestInfo.setAction("_create");
		requestInfo.setTs(System.currentTimeMillis());

		Service service = new Service();
		Citizen citizen = new Citizen();
		citizen.setMobileNumber(messageRequest.getContacts().get(0).getWa_id());
		citizen.setName(messageRequest.getContacts().get(0).getProfile().getName());
		service.setCitizen(citizen);
		service.setServiceCode(messageRequest.getThreadContact().getContact().getIssueCategory());

		AddressDetail addressDetail = new AddressDetail();
		addressDetail.setCity(messageRequest.getThreadContact().getContact().getCity());
		addressDetail.setMohalla(messageRequest.getThreadContact().getContact().getWard());
		service.setTenantId(messageRequest.getThreadContact().getContact().getCity());
		service.setPhone(messageRequest.getContacts().get(0).getWa_id());
		service.setSource(SourceEnum.IVR);

		List<Service> serviceList = new ArrayList<Service>();
		serviceList.add(service);
		serviceRequest.setRequestInfo(requestInfo);
		serviceRequest.setServices(serviceList);
		return serviceRequest;

	}

}
