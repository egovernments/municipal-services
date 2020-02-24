package org.egov.waterConnection.validator;


import java.util.HashMap;
import java.util.Map;

import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.constants.WCConstants;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.workflow.BusinessService;
import org.egov.waterConnection.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class ActionValidator {
	
	@Autowired
	private WorkflowService workflowService;

	/**
	 * Validate update request
	 * 
	 * @param request
	 * @param businessService
	 */
	public void validateUpdateRequest(WaterConnectionRequest request, BusinessService businessService) {
		validateDocumentsForUpdate(request);
		validateIds(request, businessService);
	}

	/**
	 * Validate documents for water connection
	 * 
	 * @param request water connection request
	 */
	private void validateDocumentsForUpdate(WaterConnectionRequest request) {
		if (request.getWaterConnection().getAction().equalsIgnoreCase(WCConstants.ACTION_INITIATE)
				&& request.getWaterConnection().getDocuments() != null) {
			throw new CustomException("INVALID STATUS",
					"Status cannot be INITIATE when application document are provided");
		}
	}
	
	/**
	 * Validate Id's if update is not in updateable state
	 * 
	 * @param request
	 * @param businessService
	 */
	private void validateIds(WaterConnectionRequest request, BusinessService businessService) {
		WaterConnection connection = request.getWaterConnection();
		Map<String, String> errorMap = new HashMap<>();
		if (!workflowService.isStateUpdatable(connection.getApplicationStatus().name(), businessService)) {
			if (connection.getId() == null)
				errorMap.put("INVALID_UPDATE", "Id of waterConnection cannot be null");
			if (!CollectionUtils.isEmpty(connection.getDocuments())) {
				connection.getDocuments().forEach(document -> {
					if (document.getId() == null)
						errorMap.put("INVALID_UPDATE", "Id of document cannot be null");
				});
			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
}
