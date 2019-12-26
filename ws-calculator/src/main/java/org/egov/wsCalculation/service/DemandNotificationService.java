package org.egov.wsCalculation.service;

import java.util.LinkedList;
import java.util.List;

import org.egov.wsCalculation.model.DemandNotificationObj;
import org.egov.wsCalculation.model.SMSRequest;
import org.egov.wsCalculation.util.WSCalculationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DemandNotificationService {
	
	@Autowired
	WSCalculationUtil util;
	
	public void process(DemandNotificationObj noiticationObj, String topic) {
		List<SMSRequest> smsRequestList = new LinkedList<>();
		
	}
}
