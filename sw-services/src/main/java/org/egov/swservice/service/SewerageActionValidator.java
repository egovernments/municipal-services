package org.egov.swservice.service;

import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.model.ValidatorResult;

public interface SewerageActionValidator {

	public ValidatorResult validate(SewerageConnectionRequest sewerageConnectionRequest, boolean isUpdate);

}
