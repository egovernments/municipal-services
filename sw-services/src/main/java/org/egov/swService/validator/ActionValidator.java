package org.egov.swService.validator;

import java.util.HashMap;
import java.util.Map;

import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.model.workflow.BusinessService;
import org.egov.swService.util.SWConstants;
import org.egov.swService.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
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
	public void validateUpdateRequest(SewerageConnectionRequest request, BusinessService businessService) {
		validateDocumentsForUpdate(request);
		validateIds(request, businessService);
	}

	/**
	 * Validate documents for water connection
	 * 
	 * @param request
	 *            water connection request
	 */
	private void validateDocumentsForUpdate(SewerageConnectionRequest request) {
		Map<String, String> errorMap = new HashMap<>();
		SewerageConnection connection = request.getSewerageConnection();
		if (connection.getAction().equalsIgnoreCase(SWConstants.ACTION_INITIATE) && connection.getDocuments() != null) {
			errorMap.put("INVALID STATUS", "Status cannot be INITIATE when application document are provided");
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	/**
	 * Validate Id's if update is not in updateable state
	 * 
	 * @param request
	 * @param businessService
	 */
	private void validateIds(SewerageConnectionRequest request, BusinessService businessService) {
		Map<String, String> errorMap = new HashMap<>();
		SewerageConnection connection = request.getSewerageConnection();
		if (!workflowService.isStateUpdatable(connection.getApplicationStatus().name(), businessService)) {
			if (connection.getId() == null)
				errorMap.put("INVALID_UPDATE", "Id of sewerageConnection cannot be null");
			if (!CollectionUtils.isEmpty(connection.getDocuments())) {
				connection.getDocuments().forEach(document -> {
					if (document.getId() == null)
						errorMap.put("INVALID UPDATE", "Id of document cannot be null");
				});
			}
			errorMap.put("INVALID_UPDATE", "Id of sewerageConnection cannot be null");
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
}
