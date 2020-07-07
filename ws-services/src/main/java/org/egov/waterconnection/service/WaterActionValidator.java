package org.egov.waterconnection.service;

import org.egov.waterconnection.model.ValidatorResult;
import org.egov.waterconnection.model.WaterConnectionRequest;

public interface WaterActionValidator {

	public ValidatorResult validate(WaterConnectionRequest waterConnectionRequest, int reqType);

}
