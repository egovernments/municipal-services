package org.egov.swservice.validator;

import java.util.HashMap;
import java.util.Map;

import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.model.workflow.BusinessService;
import org.egov.swservice.util.SWConstants;
import org.egov.swservice.workflow.WorkflowService;
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
		if (SWConstants.ACTION_INITIATE.equalsIgnoreCase(request.getSewerageConnection().getAction())
				&& request.getSewerageConnection().getDocuments() != null) {
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
	private void validateIds(SewerageConnectionRequest request, BusinessService businessService) {
		Map<String, String> errorMap = new HashMap<>();
		if (!workflowService.isStateUpdatable(request.getSewerageConnection().getApplicationStatus().name(), businessService)) {
			if (request.getSewerageConnection().getId() == null)
				errorMap.put("INVALID_UPDATE", "Id of sewerageConnection cannot be null");
			if (!CollectionUtils.isEmpty(request.getSewerageConnection().getDocuments())) {
				request.getSewerageConnection().getDocuments().forEach(document -> {
					if (document.getId() == null)
						errorMap.put("INVALID UPDATE", "Id of document cannot be null");
				});
			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
}
