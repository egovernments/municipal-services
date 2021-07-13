package org.egov.echallan.expense.service;


import java.util.HashMap;
import java.util.List;
import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.expense.model.Expense;
import org.egov.echallan.expense.model.AuditDetails;
import org.egov.echallan.model.Challan;
import org.egov.echallan.expense.model.Expense.StatusEnum;
import org.egov.echallan.expense.model.ExpenseRequest;
import org.egov.echallan.model.ChallanRequest;
import org.egov.echallan.expense.model.SearchCriteria;
import org.egov.echallan.producer.Producer;
import org.egov.echallan.expense.util.ExpenseCommonUtils;
import org.egov.echallan.web.models.collection.Payment;
import org.egov.echallan.web.models.collection.PaymentDetail;
import org.egov.echallan.web.models.collection.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
 

@Service
@Slf4j
public class ExpensePaymentUpdateService {
	
	@Autowired
	private ObjectMapper mapper; 
	
	@Autowired
	private ExpenseService expenseService;
	
	@Autowired
	private Producer producer;
	
	@Autowired
	private ChallanConfiguration config;
	
	@Autowired
	 private ExpenseCommonUtils commUtils;
	
	
	
	public void process(HashMap<String, Object> record) {

		try {
			log.info("Process for object"+ record);
			PaymentRequest paymentRequest = mapper.convertValue(record, PaymentRequest.class);
			RequestInfo requestInfo = paymentRequest.getRequestInfo();
			//Update the challan only when the payment is fully done.
			if( paymentRequest.getPayment().getTotalAmountPaid().compareTo(paymentRequest.getPayment().getTotalDue())!=0) 
				return;
			List<PaymentDetail> paymentDetails = paymentRequest.getPayment().getPaymentDetails();
			for (PaymentDetail paymentDetail : paymentDetails) {
				SearchCriteria criteria = new SearchCriteria();
				criteria.setTenantId(paymentRequest.getPayment().getTenantId());
				criteria.setChallanNo(paymentDetail.getBill().getConsumerCode());
				criteria.setBusinessService(paymentDetail.getBusinessService());
				List<Expense> expneses = expenseService.search(criteria, requestInfo);
				//update challan only if payment is done for challan. 
				if(!CollectionUtils.isEmpty(expneses) ) {
					String uuid = requestInfo.getUserInfo().getUuid();
				    AuditDetails auditDetails = commUtils.getAuditDetails(uuid, true);
					expneses.forEach(expense -> expense.setApplicationStatus(StatusEnum.PAID));
					expneses.get(0).setAuditDetails(auditDetails);
					ExpenseRequest request = ExpenseRequest.builder().requestInfo(requestInfo).expense(expneses.get(0)).build();
					producer.push(config.getUpdateChallanTopic(), request);
				}
			}
		} catch (Exception e) {
			log.error("Exception while processing payment update: ",e);
		}

	}

}
