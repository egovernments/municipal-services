package org.egov.gcservice.service;

import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.gcservice.web.models.ValidatorResult;

public interface GarbageActionValidator {

	ValidatorResult validate(GarbageConnectionRequest garbageConnectionRequest, int reqType);
}
