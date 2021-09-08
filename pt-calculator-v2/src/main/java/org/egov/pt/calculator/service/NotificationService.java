package org.egov.pt.calculator.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.pt.calculator.producer.Producer;
import org.egov.pt.calculator.util.Configurations;
import org.egov.pt.calculator.web.models.DefaultersInfo;
import org.egov.pt.calculator.web.models.OwnerDetails;
import org.egov.pt.calculator.web.models.SMSRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

	private static final String MESSAGE = "Dear {citizen} , Pay your Property tax before {rebatedate} and get a 10% rebate! Click on this link to pay: https://wa.me/+918750975975?text=mSeva";

	private static final String DEFAULTER_SMS_INSERT_QUERY = "Insert into eg_pt_due_sms (propertyid,finyear,ownername,mobilenumber,due,createdtime,tenantid) values (:propertyid,:finyear,:ownername,:mobilenumber,:due,:createdtime,:tenantid)";

	@Autowired
	private Producer producer;

	@Autowired
	private Configurations configs;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public void prepareAndSendSMS(List<DefaultersInfo> defaulterDetails) {

		for (DefaultersInfo defaulter : defaulterDetails) {

			SMSRequest smsRequest = getSMSRequest(defaulter);
			producer.push(configs.getSmsNotifTopic(), smsRequest);
			log.info("MobileNumber: " + smsRequest.getMobileNumber() + " Messages: " + smsRequest.getMessage());
			saveNotificationDetails(defaulter, "SUCCESS", null);
		}

	}

	private SMSRequest getSMSRequest(DefaultersInfo defaulter) {

		OwnerDetails owner = getDecryptedValues(defaulter);
		String message = MESSAGE.replace("{citizen}", owner.getName());

		message = message.replace("{rebatedate}", defaulter.getRebateEndDate());

		return SMSRequest.builder().message(message).mobileNumber(owner.getMobileNumber()).build();

	}

	private OwnerDetails getDecryptedValues(DefaultersInfo defaulter) {
		OwnerDetails ownerDetails = OwnerDetails.builder().name(defaulter.getOwnerName())
				.mobileNumber(defaulter.getMobileNumber()).build();
		StringBuilder decryptURL = new StringBuilder(configs.getDecryptServiceHost());
		decryptURL.append(configs.getDecryptEndPoint());

		try {
			return restTemplate.postForObject(decryptURL.toString(), ownerDetails, OwnerDetails.class);
		} catch (Exception e) {
			log.info("Exception while decrypting user details");
			log.error(e.getMessage());
			saveNotificationDetails(defaulter, "FAILED", "Failed to decrypt owner details.");
		}
		return ownerDetails;
	}

	private void saveNotificationDetails(DefaultersInfo defaulter, String status, String error) {

		StringBuilder query = new StringBuilder(DEFAULTER_SMS_INSERT_QUERY);
		final Map<String, Object> params = new HashMap<>();
		params.put("propertyid", defaulter.getPropertyId());
		params.put("ownername", defaulter.getOwnerName());
		params.put("mobilenumber", defaulter.getMobileNumber());
		params.put("financialyear", defaulter.getFinYear());
		params.put("createdtime", System.currentTimeMillis());
		params.put("dueamount", defaulter.getDueAmount());
		params.put("status", status);
		params.put("error", error);
		params.put("tenantid", defaulter.getTenantId());
		try {
			namedParameterJdbcTemplate.update(query.toString(), params);
		} catch (Exception e) {
			log.info("Due sms request pushed to kafka, but details not saved in the sms table.");
		}

	}

}
