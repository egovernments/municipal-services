package org.egov.gcservice.validator;

import java.util.HashMap;
import java.util.Map;

import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.gcservice.web.models.workflow.BusinessService;
import org.egov.gcservice.util.GCConstants;
import org.egov.gcservice.workflow.WorkflowService;
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
	 * @param request - Sewerage GarbageConnection Request
	 * @param businessService - BusinessService Object
	 */
	public void validateUpdateRequest(GarbageConnectionRequest request, BusinessService businessService, String applicationStatus) {
		//validateDocumentsForUpdate(request);
		validateIds(request, businessService, applicationStatus);
	}

	/**
	 * Validate documents for water connection
	 * 
	 * @param request
	 *            Sewerage GarbageConnection Request
	 */
//	private void validateDocumentsForUpdate(GarbageConnectionRequest request) {
//		if (GCConstants.ACTION_INITIATE.equalsIgnoreCase(request.getGarbageConnection().getProcessInstance().getAction())
//				&& request.getGarbageConnection().getDocuments() != null) {
//			throw new CustomException("INVALID_STATUS",
//					"Status cannot be INITIATE when application document are provided");
//		}
//	}

	/**
	 * Validate Id's if update is not in updatable state
	 * 
	 * @param request Sewerage GarbageConnection Request
	 * @param businessService BusinessService Object
	 */
	private void validateIds(GarbageConnectionRequest request, BusinessService businessService, String previousApplicationStatus) {
		Map<String, String> errorMap = new HashMap<>();
		if (!workflowService.isStateUpdatable(previousApplicationStatus, businessService)) {
			if (request.getGarbageConnection().getId() == null)
				errorMap.put("INVALID_UPDATE", "Id of GarbageConnection cannot be null");
//			if (!CollectionUtils.isEmpty(request.getGarbageConnection().getDocuments())) {
//				request.getGarbageConnection().getDocuments().forEach(document -> {
//					if (document.getId() == null)
//						errorMap.put("INVALID_UPDATE", "Id of document cannot be null");
//				});
//			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
}
