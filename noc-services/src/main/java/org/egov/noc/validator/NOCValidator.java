package org.egov.noc.validator;

import org.egov.noc.web.model.NOCRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NOCValidator {

	@Autowired
	private MDMSValidator mdmsValidator;

	
	public void validateCreate(NOCRequest nocRequest, Object mdmsData) {
		mdmsValidator.validateMdmsData(nocRequest, mdmsData);
	}
	
}
