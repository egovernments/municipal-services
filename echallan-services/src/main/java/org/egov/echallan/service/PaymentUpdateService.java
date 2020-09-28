package org.egov.echallan.service;


import java.util.HashMap;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.model.AuditDetails;
import org.egov.echallan.model.Challan;
import org.egov.echallan.model.ChallanRequest;
import org.egov.echallan.model.SearchCriteria;
import org.egov.echallan.producer.Producer;
import org.egov.echallan.util.CommonUtils;
import org.egov.echallan.web.models.collection.PaymentDetail;
import org.egov.echallan.web.models.collection.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.egov.echallan.util.ChallanConstants.*;

@Service
public class PaymentUpdateService {
	
	@Autowired
	private ObjectMapper mapper; 
	
	@Autowired
	private ChallanService challanService;
	
	@Autowired
	private Producer producer;
	
	@Autowired
	private ChallanConfiguration config;
	
	@Autowired
	 private CommonUtils commUtils;
	
	
	
	public void process(HashMap<String, Object> record) {

		try {

			PaymentRequest paymentRequest = mapper.convertValue(record, PaymentRequest.class);
			RequestInfo requestInfo = paymentRequest.getRequestInfo();

			List<PaymentDetail> paymentDetails = paymentRequest.getPayment().getPaymentDetails();
			String tenantId = paymentRequest.getPayment().getTenantId();

			for (PaymentDetail paymentDetail : paymentDetails) {
				SearchCriteria criteria = new SearchCriteria();
				criteria.setTenantId(tenantId);
				criteria.setChallanNo(paymentDetail.getBill().getConsumerCode());
				criteria.setBusinessService(paymentDetail.getBusinessService());
				List<Challan> challans = challanService.search(criteria, requestInfo);
				String uuid = requestInfo.getUserInfo().getUuid();
			    AuditDetails auditDetails = commUtils.getAuditDetails(uuid, true);
				challans.forEach(challan -> challan.setApplicationStatus(STATUS_PAID));
				challans.get(0).setAuditDetails(auditDetails);
				ChallanRequest request = ChallanRequest.builder().requestInfo(requestInfo).challan(challans.get(0)).build();
				producer.push(config.getUpdateChallanTopic(), request);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
