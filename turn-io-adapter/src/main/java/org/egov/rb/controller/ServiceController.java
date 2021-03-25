package org.egov.rb.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;

import org.egov.rb.contract.ServiceRequest;
import org.egov.rb.contract.ServiceResponse;
import org.egov.rb.model.MessageRequest;

import org.egov.rb.repository.ServiceRequestRepository;
import org.egov.rb.service.TransformService;
import org.egov.rb.user.models.ActionInfo;
import org.egov.rb.user.models.AddressDetail;
import org.egov.rb.user.models.Media;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.rb.user.models.Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
@RequestMapping(value = "/v1/requests/")
public class ServiceController {
	
	@Autowired
	private TransformService transformService;
	
	@Autowired
	ServiceRequestRepository serviceRequestRepository;	
	

	
	@PostMapping("_transform")
	@ResponseBody
	public ResponseEntity<?> transformService(@RequestBody MessageRequest messageRequest) {	
	
		ServiceResponse response = transformService.transform(messageRequest);
		
		
		ServiceRequest request=new ServiceRequest();
		RequestInfo requestInfo=new RequestInfo();
		requestInfo.setApiId("rainmaker");
		requestInfo.setVer("1.0");
		requestInfo.setAction("_create");
		requestInfo.setDid("1");
		requestInfo.setKey("null");
		requestInfo.setMsgId("987665");
		requestInfo.setAuthToken("988776ghg");
		
		//requestInfo.getUserInfo()
		User user = new User();
		user.setEmailId("asrgmail.com");
		user.setId(123L);
		user.setMobileNumber("7013352479");
		user.setName("xty");
		
		user.setTenantId("123");
		user.setType("User");
		user.setUserName("xyz");
		user.setUuid("1111");
		
		Role role = new Role();
		role.setCode("CITIZEN");
		role.setName("Citizen");
		List< Role> roles = new ArrayList<>();
		roles.add(role);
		user.setRoles(roles);
		
		
		requestInfo.setUserInfo(user);
		
		ActionInfo actionInfo= new ActionInfo();
	
		 
		Services services=new Services();
		services.setTenantId("pb.amritsar");
		services.setSource("hyd");
		services.setServiceCode("08987");
		services.setPhone("0987654323");
		services.setDescription("complaint");
		services.setAddress("hcjsdghs");
		
		AddressDetail addressDetail=new AddressDetail();
		addressDetail.setMohalla("amritsar");
		addressDetail.setCity("hyd");
		addressDetail.setHouseNoAndStreetName("shfshdfg678");
		addressDetail.setLandmark("kphb");
		addressDetail.setLatitude("987.78");
		addressDetail.setLongitude("765.789");
		
		services.setAddressDetail(addressDetail);
		
		List<Services> serviceList = new LinkedList<>();
		serviceList.add(services);
		
		List<ActionInfo> actionInfoList = new LinkedList<>();
		actionInfoList.add(actionInfo);
		
		
		request.setRequestInfo(requestInfo);
		request.setServices(serviceList);
		request.setActionInfo(actionInfoList);
		
	
		//ServiceRequestRepository serviceRequestRepository=new ServiceRequestRepository();
		serviceRequestRepository.fetchResult(null, request);
		
		
		//System.out.println(serviceRequestRepository);
		System.out.println(messageRequest );
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
