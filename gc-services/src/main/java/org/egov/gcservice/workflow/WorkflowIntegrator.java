package org.egov.gcservice.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.egov.gcservice.config.GCConfiguration;
import org.egov.gcservice.util.GarbageServicesUtil;
import org.egov.gcservice.web.models.Property;
import org.egov.gcservice.web.models.GarbageConnection;
import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.gcservice.web.models.workflow.ProcessInstance;
import org.egov.gcservice.web.models.workflow.ProcessInstanceRequest;
import org.egov.gcservice.web.models.workflow.ProcessInstanceResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkflowIntegrator {

	private static final String MODULE_NAME_VALUE = "SW";

	private RestTemplate rest;

	private GCConfiguration config;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private GarbageServicesUtil servicesUtil;
	
	@Autowired
	public WorkflowIntegrator(RestTemplate rest, GCConfiguration config) {
		this.rest = rest;
		this.config = config;
	}

	/**
	 * Method to integrate with workflow
	 *
	 * takes the sewerage request as parameter constructs the work-flow request
	 *
	 * and sets the resultant status from wf-response back to sewerage object
	 *
	 * @param garbageConnectionRequest - Sewerage GarbageConnection Request
	 * @param property - Property Object
	 */
//	public void callWorkFlow(GarbageConnectionRequest garbageConnectionRequest, Property property) {
//		String wfBusinessServiceName = config.getBusinessServiceValue();
//		if(servicesUtil.isModifyConnectionRequest(garbageConnectionRequest)){
//			wfBusinessServiceName = config.getModifySWBusinessServiceName();
//		}
//		GarbageConnection connection = garbageConnectionRequest.getGarbageConnection();
//		ProcessInstance processInstance = ProcessInstance.builder()
//				.businessId(garbageConnectionRequest.getGarbageConnection().getApplicationNo())
//				.tenantId(property.getTenantId())
//				.businessService(wfBusinessServiceName).moduleName(MODULE_NAME_VALUE)
//				.action(connection.getProcessInstance().getAction()).build();
//
//		if (!StringUtils.isEmpty(garbageConnectionRequest.getGarbageConnection().getProcessInstance())) {
//			if (!CollectionUtils
//					.isEmpty(garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAssignes())) {
//				processInstance.setAssignes(
//						garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAssignes());
//			}
//			if (!CollectionUtils
//					.isEmpty(garbageConnectionRequest.getGarbageConnection().getProcessInstance().getDocuments())) {
//				processInstance.setDocuments(
//						garbageConnectionRequest.getGarbageConnection().getProcessInstance().getDocuments());
//			}
//			if (!StringUtils
//					.isEmpty(garbageConnectionRequest.getGarbageConnection().getProcessInstance().getComment())) {
//				processInstance.setComment(
//						garbageConnectionRequest.getGarbageConnection().getProcessInstance().getComment());
//			}
//
//		}
//		List<ProcessInstance> processInstances = new ArrayList<>();
//		processInstances.add(processInstance);
//		ProcessInstanceResponse processInstanceResponse;
//
//		try {
//			processInstanceResponse = mapper.convertValue(
//					rest.postForObject(config.getWfHost().concat(config.getWfTransitionPath()),
//							ProcessInstanceRequest.builder().requestInfo(garbageConnectionRequest.getRequestInfo())
//									.processInstances(processInstances).build(),
//							Map.class),
//					ProcessInstanceResponse.class);
//		} catch (HttpClientErrorException e) {
//			/*
//			 * extracting message from client error exception
//			 */
//			DocumentContext responseContext = JsonPath.parse(e.getResponseBodyAsString());
//			List<Object> errorList;
//			try {
//				errorList = responseContext.read("$.Errors");
//			} catch (PathNotFoundException ex1) {
//				StringBuilder builder = new StringBuilder();
//				builder.append(" Unable to read the json path in error object : ").append(ex1.getMessage());
//				log.error(builder.toString());
//				throw new CustomException("EG_SW_WF_ERROR_KEY_NOT_FOUND", builder.toString());
//			}
//			throw new CustomException("EG_WF_ERROR", errorList.toString());
//		} catch (Exception e) {
//			throw new CustomException("EG_WF_ERROR",
//					" Exception occurred while integrating with workflow : " + e.getMessage());
//		}
//
//		/*
//		 * on success result from work-flow read the data and set the status
//		 * back to SW object
//		 */
//		processInstanceResponse.getProcessInstances().forEach(pInstance -> {
//			if (garbageConnectionRequest.getGarbageConnection().getApplicationNo()
//					.equals(pInstance.getBusinessId())) {
//				garbageConnectionRequest.getGarbageConnection()
//						.setApplicationStatus(pInstance.getState().getApplicationStatus());
//			}
//		});
//
//	}
}