package org.egov.pt.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.pt.models.hrms.HRMSResponse;
import org.egov.pt.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HrmsService {

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Value("${egov.hrms.host}")
	private String hrmsHost;

	@Value("${egov.hrms.search.path}")
	private String hrmsSearchEndpoint;
	
	@Autowired
	ObjectMapper objectMapper;

	/**
	 * Performs an API call to the searchHRMS endpoint.
	 *
	 * @param requestInfo RequestInfo for the API call
	 * @param tenantId    Tenant ID
	 * @param roles       List of roles
	 * @param boundary    Boundary parameter
	 * @return Response from the HRMS searchHRMS endpoint
	 */
	public HRMSResponse searchHRMS(RequestInfo requestInfo, String tenantId, List<Role> roles, String boundary) {
		// Create the API request body
		String requestBody = "{\"RequestInfo\":" + requestInfo.toString() + "}"; 
		
		 List<String> roleCodes = roles.stream()
		            .map(Role::getCode)
		            .collect(Collectors.toList());
		
		 String rolesString = String.join(",", roleCodes);
		// Create the API URL
		String apiUrl = hrmsHost + hrmsSearchEndpoint + "?tenantId=" + tenantId + "&roles=" + rolesString + "&boundary="
				+ boundary; // Modify as needed

		try {
			
			Optional<Object> response = serviceRequestRepository.fetchResult(new StringBuilder(apiUrl), requestBody);
			if (response.isPresent()) {
	            String responseString = response.get().toString();
	            // Parse the response and map it to HRMSResponse object
	            HRMSResponse hrmsResponse = objectMapper.readValue(responseString, HRMSResponse.class);
	            return hrmsResponse;
	        } else {  
	            return null; 
	        }
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
