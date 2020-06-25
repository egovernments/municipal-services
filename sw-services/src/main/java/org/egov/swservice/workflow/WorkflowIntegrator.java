package org.egov.swservice.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.model.Property;
import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.model.workflow.ProcessInstance;
import org.egov.swservice.model.workflow.ProcessInstanceRequest;
import org.egov.swservice.model.workflow.ProcessInstanceResponse;
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

	private static final String MODULENAMEVALUE = "SW";

	private RestTemplate rest;

	private SWConfiguration config;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	public WorkflowIntegrator(RestTemplate rest, SWConfiguration config) {
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
	 * @param sewerageRequest
	 */
	public void callWorkFlow(SewerageConnectionRequest sewerageConnectionRequest, Property property) {

		SewerageConnection connection = sewerageConnectionRequest.getSewerageConnection();
		ProcessInstance processInstance = ProcessInstance.builder()
				.businessId(sewerageConnectionRequest.getSewerageConnection().getApplicationNo())
				.tenantId(property.getTenantId())
				.businessService(config.getBusinessServiceValue()).moduleName(MODULENAMEVALUE)
				.action(connection.getProcessInstance().getAction()).build();

		if (!StringUtils.isEmpty(sewerageConnectionRequest.getSewerageConnection().getProcessInstance())) {
			if (!CollectionUtils
					.isEmpty(sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAssignes())) {
				processInstance.setAssignes(
						sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAssignes());
			}
			if (!CollectionUtils
					.isEmpty(sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getDocuments())) {
				processInstance.setDocuments(
						sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getDocuments());
			}
			if (!StringUtils
					.isEmpty(sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getComment())) {
				processInstance.setComment(
						sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getComment());
			}

		}
		List<ProcessInstance> processInstances = new ArrayList<>();
		processInstances.add(processInstance);
		ProcessInstanceResponse processInstanceResponse = null;

		try {
			processInstanceResponse = mapper.convertValue(
					rest.postForObject(config.getWfHost().concat(config.getWfTransitionPath()),
							ProcessInstanceRequest.builder().requestInfo(sewerageConnectionRequest.getRequestInfo())
									.processInstances(processInstances).build(),
							Map.class),
					ProcessInstanceResponse.class);
		} catch (HttpClientErrorException e) {
			/*
			 * extracting message from client error exception
			 */
			DocumentContext responseContext = JsonPath.parse(e.getResponseBodyAsString());
			List<Object> errros = null;
			try {
				errros = responseContext.read("$.Errors");
			} catch (PathNotFoundException pnfe) {
				StringBuilder builder = new StringBuilder();
				builder.append(" Unable to read the json path in error object : ").append(pnfe.getMessage());
				log.error("EG_SW_WF_ERROR_KEY_NOT_FOUND", builder.toString());
				builder = new StringBuilder();
				builder.append(" Unable to read the json path in error object : ").append(pnfe.getMessage());
				throw new CustomException("EG_SW_WF_ERROR_KEY_NOT_FOUND", builder.toString());
			}
			throw new CustomException("EG_WF_ERROR", errros.toString());
		} catch (Exception e) {
			throw new CustomException("EG_WF_ERROR",
					" Exception occured while integrating with workflow : " + e.getMessage());
		}

		/*
		 * on success result from work-flow read the data and set the status
		 * back to SW object
		 */
		processInstanceResponse.getProcessInstances().forEach(pInstance -> {
			if (sewerageConnectionRequest.getSewerageConnection().getApplicationNo()
					.equals(pInstance.getBusinessId())) {
				sewerageConnectionRequest.getSewerageConnection()
						.setApplicationStatus(pInstance.getState().getApplicationStatus());
			}
		});

	}
}