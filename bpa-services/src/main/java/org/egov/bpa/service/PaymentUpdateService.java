package org.egov.bpa.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.BPARepository;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.collection.PaymentDetail;
import org.egov.bpa.web.models.collection.PaymentRequest;
import org.egov.bpa.web.models.workflow.BusinessService;
import org.egov.bpa.workflow.WorkflowIntegrator;
import org.egov.bpa.workflow.WorkflowService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;

import lombok.extern.slf4j.Slf4j;

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
	 * @param record The incoming message from receipt create consumer
	 */
	public void process(HashMap<String, Object> record) {

		try {
			PaymentRequest paymentRequest = mapper.convertValue(record,PaymentRequest.class);
			RequestInfo requestInfo = paymentRequest.getRequestInfo();
			List<PaymentDetail> paymentDetails = paymentRequest.getPayment().getPaymentDetails();
			String tenantId = paymentRequest.getPayment().getTenantId();

			for(PaymentDetail paymentDetail : paymentDetails){
				
				List<String> businessServices = new ArrayList<String>(Arrays.asList(config.getBusinessService().split(",")));
				if (businessServices.contains(paymentDetail.getBusinessService())) {
					BPASearchCriteria searchCriteria = new BPASearchCriteria();
					searchCriteria.setTenantId(tenantId);
					List<String> codes = Arrays.asList(paymentDetail.getBill().getConsumerCode());
					searchCriteria.setApplicationNos(codes);
					List<BPA> bpas = repository.getBPAData(searchCriteria); //bpaService.getBPAWithOwnerInfo(searchCriteria, requestInfo);

					BusinessService businessService = workflowService.getBusinessService(bpas.get(0).getTenantId(), requestInfo,codes.get(0));


					if (CollectionUtils.isEmpty(bpas))
						throw new CustomException("INVALID RECEIPT",
								"No tradeLicense found for the comsumerCode " + searchCriteria.getApplicationNos());

					bpas.forEach(bpa -> bpa.setAction("PAY"));

					// FIXME check if the update call to repository can be avoided
					// FIXME check why aniket is not using request info from consumer
					// REMOVE SYSTEM HARDCODING AFTER ALTERING THE CONFIG IN WF FOR TL

					Role role = Role.builder().code("SYSTEM_PAYMENT").tenantId(bpas.get(0).getTenantId()).build();
					requestInfo.getUserInfo().getRoles().add(role);
					BPARequest updateRequest = BPARequest.builder().requestInfo(requestInfo)
							.BPA(bpas.get(0)).build();
					

					/*
					 * calling workflow to update status
					 */
					wfIntegrator.callWorkFlow(updateRequest);

					log.info(" the status of the application is : " + updateRequest.getBPA().getStatus());

					enrichmentService.postStatusEnrichment(updateRequest);

					
					repository.update(updateRequest,false);
			}
		 }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
}
