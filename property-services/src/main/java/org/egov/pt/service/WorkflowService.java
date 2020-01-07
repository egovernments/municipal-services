package org.egov.pt.service;

import static org.egov.pt.util.PTConstants.FIELDS_FOR_OWNER_MUTATION;
import static org.egov.pt.util.PTConstants.FIELDS_FOR_PROPERTY_MUTATION;
import static org.egov.pt.util.PTConstants.VARIABLE_OWNER;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.Difference;
import org.egov.pt.models.Property;
import org.egov.pt.models.workflow.BusinessService;
import org.egov.pt.models.workflow.BusinessServiceResponse;
import org.egov.pt.models.workflow.ProcessInstanceRequest;
import org.egov.pt.models.workflow.ProcessInstanceResponse;
import org.egov.pt.repository.ServiceRequestRepository;
import org.egov.pt.web.contracts.PropertyRequest;
import org.egov.pt.web.contracts.RequestInfoWrapper;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WorkflowService {

	@Autowired
	private RestTemplate rest;

	@Autowired
	private PropertyConfiguration configs;

	@Autowired
	private ServiceRequestRepository restRepo;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private DiffService diffService;;

	/**
	 * Method to integrate with workflow
	 *
	 * takes the trade-license request as parameter constructs the work-flow request
	 *
	 * and sets the resultant status from wf-response back to trade-license object
	 *
	 * @param PropertyRequest
	 */
	public String callWorkFlow(ProcessInstanceRequest workflowReq) {

		ProcessInstanceResponse response = null;

		try {

			response = rest.postForObject(configs.getWfHost().concat(configs.getWfTransitionPath()), workflowReq, ProcessInstanceResponse.class);
		} catch (HttpClientErrorException ex) {

			throw new ServiceCallException(ex.getMessage());
		} catch (Exception e) {
			throw new CustomException("EG_WF_ERROR", "Exception occured while integrating with workflow : " + e.getMessage());
		}

		/*
		 * on success result from work-flow read the data and set the status back to TL
		 * object
		 */

		return response.getProcessInstances().get(0).getState().getApplicationStatus();
	}
	
	
    /**
     * Get the workflow config for the given tenant
     * @param tenantId    The tenantId for which businessService is requested
     * @param requestInfo The RequestInfo object of the request
     * @return BusinessService for the the given tenantId
     */
    public BusinessService getBusinessService(String tenantId, String businessService, RequestInfo requestInfo) {

		StringBuilder url = getSearchURLWithParams(tenantId, businessService);
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = restRepo.fetchResult(url, requestInfoWrapper);
		BusinessServiceResponse response = null;
		try {
			response = mapper.convertValue(result, BusinessServiceResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response of calculate");
		}
		return response.getBusinessServices().get(0);
	}
    
    /**
     * Creates url for search based on given tenantId
     *
     * @param tenantId The tenantId for which url is generated
     * @return The search url
     */
    private StringBuilder getSearchURLWithParams(String tenantId, String businessService) {
    	
        StringBuilder url = new StringBuilder(configs.getWfHost());
        url.append(configs.getWfBusinessServiceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&businessservices=");
        url.append(businessService);
        return url;
    }
    
    /**
     * method to process requests for workflow
     * @param request
     */
    public void processWorkflowAndPersistData(PropertyRequest request, Property propertyFromDb) {

        Boolean isDiffOnWorkflowFields = false;

        Difference difference =  diffService.getDifference(request, propertyFromDb);
        /*
         *
         * 1. is record active or not
         *
         * 2. if inactive get workflow information
         *
         * 3. check if update is possible, if yes the do update else throw error
         *
         * 4. if record is active and changes are there , then trigger the workflow they are asking for
         * then persist the record
         *
         * 5.
         */


    }


	private List<String> getSwitches(Difference difference) {

		List<String> switches = new LinkedList<>();

		if (!CollectionUtils.isEmpty(difference.getFieldsChanged())) {

			if (Collections.disjoint(difference.getFieldsChanged(), FIELDS_FOR_OWNER_MUTATION))
				switches.add("OWNERMUTATION");

			if (Collections.disjoint(difference.getFieldsChanged(), FIELDS_FOR_PROPERTY_MUTATION))
				switches.add("PROPERTYMUTATION");
		}

		if (!CollectionUtils.isEmpty(difference.getClassesRemoved())) {
			if (difference.getClassesRemoved().contains(VARIABLE_OWNER))
				switches.add("OWNERMUTATION");
		}

		if (!CollectionUtils.isEmpty(difference.getClassesAdded())) {
			if (difference.getClassesAdded().contains(VARIABLE_OWNER))
				switches.add("OWNERMUTATION");
		}

		return switches;
	}

}