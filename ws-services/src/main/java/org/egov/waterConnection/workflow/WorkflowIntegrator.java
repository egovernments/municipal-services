package org.egov.waterConnection.workflow;

import java.util.ArrayList;
import java.util.List;

import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.config.WSConfiguration;
import org.egov.waterConnection.model.Connection.ApplicationStatusEnum;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.workflow.ProcessInstance;
import org.egov.waterConnection.model.workflow.ProcessInstanceRequest;
import org.egov.waterConnection.model.workflow.ProcessInstanceResponse;
import org.egov.waterConnection.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkflowIntegrator {

	private static final String MODULENAMEVALUE = "WS";

	@Autowired
	private WSConfiguration config;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	/**
	 * Method to integrate with workflow
	 *
	 * takes the water connection request as parameter constructs the work-flow
	 * request
	 *
	 * and sets the resultant status from wf-response back to water-connection
	 * request object
	 *
	 * @param waterConnectionRequest
	 */
	public void callWorkFlow(WaterConnectionRequest waterConnectionRequest) {
		WaterConnection connection = waterConnectionRequest.getWaterConnection();
		ProcessInstance processInstance = ProcessInstance.builder()
				.businessId(waterConnectionRequest.getWaterConnection().getApplicationNo())
				.tenantId(waterConnectionRequest.getWaterConnection().getProperty().getTenantId())
				.businessService(config.getBusinessServiceValue()).moduleName(MODULENAMEVALUE)
				.action(connection.getAction()).build();
		if (!StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getProcessInstance())) {
			if (!CollectionUtils
					.isEmpty(waterConnectionRequest.getWaterConnection().getProcessInstance().getAssignes())) {
				processInstance
						.setAssignes(waterConnectionRequest.getWaterConnection().getProcessInstance().getAssignes());
			}
			if (!CollectionUtils
					.isEmpty(waterConnectionRequest.getWaterConnection().getProcessInstance().getDocuments())) {
				processInstance
						.setDocuments(waterConnectionRequest.getWaterConnection().getProcessInstance().getDocuments());
			}
			if (!StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getProcessInstance().getComment())) {
				processInstance
						.setComment(waterConnectionRequest.getWaterConnection().getProcessInstance().getComment());
			}

		}
		List<ProcessInstance> processInstances = new ArrayList<>();
		processInstances.add(processInstance);
		ProcessInstanceRequest request = ProcessInstanceRequest.builder()
				.requestInfo(waterConnectionRequest.getRequestInfo()).processInstances(processInstances).build();
		ProcessInstanceResponse processInstanceResponse = null;
		try {
			processInstanceResponse = mapper.convertValue(
					serviceRequestRepository.fetchResult(
							new StringBuilder(config.getWfHost().concat(config.getWfTransitionPath())), request),
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
				log.error("EG_WS_WF_ERROR_KEY_NOT_FOUND",
						" Unable to read the json path in error object : " + pnfe.getMessage());
				throw new CustomException("EG_WS_WF_ERROR_KEY_NOT_FOUND",
						" Unable to read the json path in error object : " + pnfe.getMessage());
			}
			throw new CustomException("EG_WF_ERROR", errros.toString());
		} catch (Exception e) {
			throw new CustomException("EG_WF_ERROR",
					" Exception occured while integrating with workflow : " + e.getMessage());
		}

		/*
		 * on success result from work-flow read the data and set the status back to WS
		 * object
		 */
		processInstanceResponse.getProcessInstances().forEach(pInstance -> {
			if (waterConnectionRequest.getWaterConnection().getApplicationNo().equals(pInstance.getBusinessId())) {
				waterConnectionRequest.getWaterConnection().setApplicationStatus(
						ApplicationStatusEnum.fromValue(pInstance.getState().getApplicationStatus()));
			}
		});
	}
}