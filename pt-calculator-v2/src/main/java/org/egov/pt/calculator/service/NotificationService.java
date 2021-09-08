package org.egov.pt.calculator.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.pt.calculator.producer.Producer;
import org.egov.pt.calculator.util.Configurations;
import org.egov.pt.calculator.web.models.DefaultersInfo;
import org.egov.pt.calculator.web.models.OwnerDetails;
import org.egov.pt.calculator.web.models.SMSRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

	private static final String MESSAGE = "Dear {citizen} , Pay your Property tax before {rebatedate} and get a 10% rebate! Click on this link to pay: https://wa.me/+918750975975?text=mSeva";

	@Autowired
	private Producer producer;

	@Autowired
	private Configurations configs;

	@Autowired
	private RestTemplate restTemplate;

	public void prepareAndSendSMS(List<DefaultersInfo> defaulterDetails) {
		List<SMSRequest> smsRequestList = new ArrayList<>();
		for (DefaultersInfo defaulter : defaulterDetails) {

			SMSRequest sms = getSMSRequest(defaulter);
			smsRequestList.add(sms);
		}
		sendSMS(smsRequestList);
	}

	private SMSRequest getSMSRequest(DefaultersInfo defaulter) {

		OwnerDetails owner = OwnerDetails.builder().name(defaulter.getOwnerName())
				.mobileNumber(defaulter.getMobileNumber()).build();
		owner = getDecryptedValues(owner);
		String message = MESSAGE.replace("{citizen}", owner.getName());

		message = message.replace("{rebatedate}", defaulter.getRebateEndDate());

		return SMSRequest.builder().message(message).mobileNumber(owner.getMobileNumber()).build();

	}

	private OwnerDetails getDecryptedValues(OwnerDetails encryptedDetails) {

		StringBuilder decryptURL = new StringBuilder(configs.getDecryptServiceHost());
		decryptURL.append(configs.getDecryptEndPoint());

		try {
			return restTemplate.postForObject(decryptURL.toString(), encryptedDetails, OwnerDetails.class);
		} catch (Exception e) {
			log.info("Exception while decrypting user details");
			log.error(e.getMessage());
		}
		return encryptedDetails;
	}

	public void sendSMS(List<SMSRequest> smsRequestList) {

		for (SMSRequest smsRequest : smsRequestList) {
			//producer.push(configs.getSmsNotifTopic(), smsRequest);
			log.info("MobileNumber: " + smsRequest.getMobileNumber() + " Messages: " + smsRequest.getMessage());
		}
	}

}
