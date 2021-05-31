package org.egov.fsm.plantmapping.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.validation.Valid;

import org.egov.fsm.plantmapping.config.PlantMappingConfiguration;
import org.egov.fsm.plantmapping.web.model.PlantMappingRequest;
import org.egov.fsm.plantmapping.web.model.PlantMappingUserSearchRequest;
import org.egov.fsm.plantmapping.web.model.UserDetailResponse;
import org.egov.fsm.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PlantMappingUserService {
	

	
	@Autowired
	private PlantMappingConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * Checks if the user exists in the database
	 * 
	 * @param applicant
	 *            The applicant from the FSM Application
	 * @param request
	 *            The requestInfo of the request
	 * @return The search response from the user service
	 */
	public UserDetailResponse userExists(@Valid PlantMappingRequest request) {

		
		  PlantMappingUserSearchRequest userSearchRequest = new PlantMappingUserSearchRequest();
//		  userSearchRequest.setTenantId(request.getPlantMapping().getTenantId().split("\\.")[0]);
		  List<String> uuid = new ArrayList<String>();
				 uuid.add(request.getPlantMapping().getEmployeeUuid());
		  userSearchRequest.setUuid(uuid);		  
		  
		  StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
		  return userCall(userSearchRequest, uri);
		 
	}
	/**
	 * Returns UserDetailResponse by calling user service with given uri and object
	 * 
	 * @param userRequest
	 *            Request object for user service
	 * @param uri
	 *            The address of the end point
	 * @return Response from user service as parsed as userDetailResponse
	 */
	@SuppressWarnings("rawtypes")
	private UserDetailResponse userCall(Object userRequest, StringBuilder uri) {
		String dobFormat = null;
		if (uri.toString().contains(config.getUserSearchEndpoint())
				|| uri.toString().contains(config.getUserUpdateEndpoint()))
			dobFormat = "yyyy-MM-dd";
		else if (uri.toString().contains(config.getUserCreateEndpoint()))
			dobFormat = "dd/MM/yyyy";
		try {
//			System.out.println("user search url: " + uri + userRequest);
			
			LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, userRequest);
			parseResponse(responseMap, dobFormat);
			UserDetailResponse userDetailResponse = mapper.convertValue(responseMap, UserDetailResponse.class);
			return userDetailResponse;
		} catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in userCall");
		}
	}
	/**
	 * Parses date formats to long for all users in responseMap
	 * 
	 * @param responeMap
	 *            LinkedHashMap got from user api response
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void parseResponse(LinkedHashMap responeMap, String dobFormat) {
		List<LinkedHashMap> users = (List<LinkedHashMap>) responeMap.get("user");
		String format1 = "dd-MM-yyyy HH:mm:ss";
		if (users != null) {
			users.forEach(map -> {
				map.put("createdDate", dateTolong((String) map.get("createdDate"), format1));
				if ((String) map.get("lastModifiedDate") != null)
					map.put("lastModifiedDate", dateTolong((String) map.get("lastModifiedDate"), format1));
				if ((String) map.get("dob") != null)
					map.put("dob", dateTolong((String) map.get("dob"), dobFormat));
				if ((String) map.get("pwdExpiryDate") != null)
					map.put("pwdExpiryDate", dateTolong((String) map.get("pwdExpiryDate"), format1));
			});
		}
	}
	
	/**
	 * Converts date to long
	 * 
	 * @param date
	 *            date to be parsed
	 * @param format
	 *            Format of the date
	 * @return Long value of date
	 */
	private Long dateTolong(String date, String format) {
		SimpleDateFormat f = new SimpleDateFormat(format);
		Date d = null;
		try {
			d = f.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d.getTime();
	}
}
