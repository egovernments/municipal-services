package org.egov.bpa.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.BPARepository;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.workflow.BusinessService;
import org.egov.bpa.workflow.WorkflowIntegrator;
import org.egov.bpa.workflow.WorkflowService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@Service
@Slf4j
public class PaymentUpdateService {

	private BPAService bpaService;

	private BPAConfiguration config;

	private BPARepository repository;

	private WorkflowIntegrator wfIntegrator;

	private EnrichmentService enrichmentService;

	private ObjectMapper mapper;

	private WorkflowService workflowService;

	private BPAUtil util;

	@Autowired
	public PaymentUpdateService(BPAService bpaService, BPAConfiguration config,
			BPARepository repository, WorkflowIntegrator wfIntegrator,
			EnrichmentService enrichmentService, ObjectMapper mapper,
			WorkflowService workflowService, BPAUtil util) {
		this.bpaService = bpaService;
		this.config = config;
		this.repository = repository;
		this.wfIntegrator = wfIntegrator;
		this.enrichmentService = enrichmentService;
		this.mapper = mapper;
		this.workflowService = workflowService;
		this.util = util;
	}

	final String tenantId = "tenantId";

	final String businessService = "businessService";

	final String consumerCode = "consumerCode";

	/**
	 * Process the message from kafka and updates the status to paid
	 * 
	 * @param record
	 *            The incoming message from receipt create consumer
	 */
	public void process(HashMap<String, Object> record) {

		try {
			String jsonString = new JSONObject(record).toString();
			DocumentContext documentContext = JsonPath.parse(jsonString);
			Map<String, String> valMap = enrichValMap(documentContext);

			Map<String, Object> info = documentContext.read("$.RequestInfo");
			RequestInfo requestInfo = mapper.convertValue(info,
					RequestInfo.class);

			if (valMap.get(businessService).equalsIgnoreCase(
					config.getBusinessService())) {
				BPASearchCriteria searchCriteria = new BPASearchCriteria();
				searchCriteria.setTenantId(valMap.get(tenantId));
				List<String> code = Arrays.asList(valMap.get(consumerCode));
				searchCriteria.setApplicationNos(code);
				List<BPA> bpa = bpaService.getBPAWithOwnerInfo(searchCriteria,
						requestInfo);

				BusinessService businessService = workflowService
						.getBusinessService(bpa.get(0).getTenantId(),
								requestInfo,null);

				if (CollectionUtils.isEmpty(bpa))
					throw new CustomException("INVALID RECEIPT",
							"No tradeLicense found for the comsumerCode "
									+ searchCriteria.getApplicationNos());

				bpa.forEach(license -> license
						.setAction(BPAConstants.ACTION_PAY));

				// FIXME check if the update call to repository can be avoided
				// FIXME check why aniket is not using request info from
				// consumer
				// REMOVE SYSTEM HARDCODING AFTER ALTERING THE CONFIG IN WF FOR
				// TL
				Role role = Role.builder().code("SYSTEM_PAYMENT")
						.tenantId(bpa.get(0).getTenantId()).build();
				requestInfo.getUserInfo().getRoles().add(role);

				BPARequest updateRequest = BPARequest.builder()
						.requestInfo(requestInfo).BPA((BPA) bpa).build();

				/*
				 * calling workflow to update status
				 */
				wfIntegrator.callWorkFlow(updateRequest);

				log.info(" the status of the application is : "
						+ updateRequest.getBPA().getStatus());

				enrichmentService.postStatusEnrichment(updateRequest);

				
				repository.update(updateRequest, workflowService.isStateUpdatable(updateRequest.getBPA().getStatus(), businessService));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Extracts the required fields as map
	 * 
	 * @param context
	 *            The documentcontext of the incoming receipt
	 * @return Map containing values of required fields
	 */
	private Map<String, String> enrichValMap(DocumentContext context) {
		Map<String, String> valMap = new HashMap<>();
		try {
			valMap.put(
					businessService,
					context.read("$.Receipt[0].Bill[0].billDetails[0].businessService"));
			valMap.put(consumerCode, context
					.read("$.Receipt[0].Bill[0].billDetails[0].consumerCode"));
			valMap.put(tenantId, context.read("$.Receipt[0].tenantId"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("RECEIPT ERROR",
					"Unable to fetch values from receipt");
		}
		return valMap;
	}
}
