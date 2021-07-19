package org.egov.echallan.expense.service;

import java.util.List;

import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.model.Challan;
import org.egov.echallan.model.Challan.StatusEnum;
import org.egov.echallan.model.ChallanRequest;
import org.egov.echallan.model.RequestInfoWrapper;
import org.egov.echallan.repository.ServiceRequestRepository;
import org.egov.echallan.web.models.collection.Bill;
import org.egov.echallan.web.models.collection.BillResponse;
import org.egov.echallan.web.models.collection.Payment;
import org.egov.echallan.web.models.collection.PaymentDetail;
import org.egov.echallan.web.models.collection.PaymentRequest;
import org.egov.echallan.web.models.collection.PaymentResponse;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentService {

	@Autowired
	private ChallanConfiguration config;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	public PaymentResponse createPayment(ChallanRequest request) {
		Challan challan = request.getChallan();
		PaymentResponse response = null;
		if (challan.getIsBillPaid()) {

			Payment payment = Payment.builder().tenantId(challan.getTenantId()).paymentMode("CASH")
					.mobileNumber(challan.getCitizen().getMobileNumber()).paidBy("Deepa").build();

			List<Bill> billList = fetchBill(request);
			if (!billList.isEmpty()) {
				payment.setTotalAmountPaid(billList.get(0).getTotalAmount());
				payment.addpaymentDetailsItem(PaymentDetail.builder().billId(billList.get(0).getId())
						.businessService(billList.get(0).getBusinessService())
						.totalAmountPaid(billList.get(0).getTotalAmount()).build());
			}

			// Call Collection

			StringBuilder uri = new StringBuilder("http://localhost:8092/collection-services/payments/_create");
			Object result = serviceRequestRepository.fetchResult(uri,
					PaymentRequest.builder().payment(payment).requestInfo(request.getRequestInfo()).build());

			try {
				response = mapper.convertValue(result, PaymentResponse.class);
				challan.setApplicationStatus(StatusEnum.PAID);
			} catch (IllegalArgumentException e) {
				log.error("Error parsing payment response Challan id : " + challan.getId());
				throw new CustomException("PARSING ERROR", "Unable to parse payment response");
			}

		}
		return response;
	}

	public List<Bill> fetchBill(ChallanRequest request) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		Challan challan = request.getChallan();
		queryParams.add("tenantId", challan.getTenantId());
		queryParams.add("service", challan.getBusinessService());
		queryParams.add("consumerCode", challan.getChallanNo());

		String uri = UriComponentsBuilder.fromHttpUrl(config.getBillingHost()).path(config.getSearchBill())
				.queryParams(queryParams).build().toUriString();

		RequestInfoWrapper wrapper = new RequestInfoWrapper(request.getRequestInfo());

		try {
			BillResponse response = restTemplate.postForObject(uri, wrapper, BillResponse.class);
			return response.getBill();
		} catch (HttpClientErrorException e) {
			log.error("Unable to fetch bill for Challan: {} in tenant {}", challan.getChallanNo(),
					challan.getTenantId(), e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			log.error("Unable to fetch bill for Bill ID: {} in tenant {}", challan.getChallanNo(),
					challan.getTenantId(), e);
			throw new CustomException("BILLING_SERVICE_ERROR", "Failed to fetch bill, unknown error occurred");
		}
	}

}
