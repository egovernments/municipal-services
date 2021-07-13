package org.egov.gcservice.service;

import java.util.HashMap;
import java.util.Map;

import org.egov.gcservice.web.models.RoadCuttingInfo;
import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.gcservice.web.models.ValidatorResult;
import org.egov.gcservice.util.GCConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GarbageFieldValidator implements GarbageActionValidator {

	@Override
	public ValidatorResult validate(GarbageConnectionRequest garbageConnectionRequest, int reqType) {
		Map<String, String> errorMap = new HashMap<>();
		switch (reqType){
			case GCConstants.UPDATE_APPLICATION:
				validateUpdateRequest(garbageConnectionRequest, errorMap);
			    break;
			case GCConstants.MODIFY_CONNECTION:
				validateModifyRequest(garbageConnectionRequest, errorMap);
			default:
				break;
		}
		if (!errorMap.isEmpty())
			return new ValidatorResult(false, errorMap);
		return new ValidatorResult(true, errorMap);
	}

	public void validateUpdateRequest(GarbageConnectionRequest garbageConnectionRequest, Map<String, String> errorMap) {
		if (GCConstants.ACTIVATE_CONNECTION_CONST.equalsIgnoreCase(
				garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction())) {
			if (StringUtils.isEmpty(garbageConnectionRequest.getGarbageConnection().getPropertyType())) {
				errorMap.put("INVALID_SEWERAGE_CONNECTION_TYPE", "GarbageConnection type should not be empty");
			}
			
		}
	
	}

	public void validateModifyRequest(GarbageConnectionRequest garbageConnectionRequest, Map<String, String> errorMap) {
		if (GCConstants.APPROVE_CONNECTION.equalsIgnoreCase(
				garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction())) {
			
//			if (StringUtils.isEmpty(garbageConnectionRequest.getGarbageConnection().getConnectionExecutionDate()) ||
//					garbageConnectionRequest.getGarbageConnection().getConnectionExecutionDate().equals(GCConstants.INVALID_CONEECTION_EXECUTION_DATE)) {
//				errorMap.put("INVALID_CONNECTION_EXECUTION_DATE", "GarbageConnection execution date should not be empty");
//
//			}
			if (garbageConnectionRequest.getGarbageConnection().getEffectiveFrom() != null) {
				if (System.currentTimeMillis() > garbageConnectionRequest.getGarbageConnection().getEffectiveFrom()) {
					errorMap.put("DATE_EFFECTIVE_FROM_IN_PAST", "Date effective from cannot be past");
				}
//				if ((garbageConnectionRequest.getGarbageConnection().getConnectionExecutionDate() != null)
//						&& (garbageConnectionRequest.getGarbageConnection()
//						.getConnectionExecutionDate() > garbageConnectionRequest.getGarbageConnection()
//						.getDateEffectiveFrom())) {
//
//					errorMap.put("DATE_EFFECTIVE_FROM_LESS_THAN_EXCECUTION_DATE",
//							"Date effective from cannot be before connection execution date");
//				}

			}
		}
		if (GCConstants.SUBMIT_APPLICATION_CONST
				.equals(garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction())
				|| GCConstants.APPROVE_CONNECTION.equalsIgnoreCase(
				garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction())) {
			if (garbageConnectionRequest.getGarbageConnection().getEffectiveFrom() == null
					|| garbageConnectionRequest.getGarbageConnection().getEffectiveFrom() < 0
					|| garbageConnectionRequest.getGarbageConnection().getEffectiveFrom() == 0) {
				errorMap.put("INVALID_DATE_EFFECTIVE_FROM", "Date effective from cannot be null or negative");
			}
		}
	}

}
