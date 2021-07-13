package org.egov.gcservice.validator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.gcservice.web.models.GarbageConnection;
import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.gcservice.web.models.ValidatorResult;
import org.egov.gcservice.service.PropertyValidator;
import org.egov.gcservice.service.GarbageFieldValidator;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class GarbageConnectionValidator {
	
	@Autowired
	private PropertyValidator propertyValidator;
	
	@Autowired
	private GarbageFieldValidator garbageFieldValidator;

	/**Used strategy pattern for avoiding multiple if else condition
	 * 
	 * @param garbageConnectionRequest SewarageConnectionRequest is request for create or update
	 * @param reqType True for update and false for create
	 */
	public void validateGarbageConnection(GarbageConnectionRequest garbageConnectionRequest, int reqType) {
		Map<String, String> errorMap = new HashMap<>();
//		if (garbageConnectionRequest.getGarbageConnection().getProcessInstance() == null || StringUtils
//				.isEmpty(garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction())) {
//			errorMap.put("INVALID_ACTION", "Workflow obj can not be null or action can not be empty!!");
//			throw new CustomException(errorMap);
//		}
		ValidatorResult isPropertyValidated = propertyValidator.validate(garbageConnectionRequest, reqType);
		if (!isPropertyValidated.isStatus()) {
			errorMap.putAll(isPropertyValidated.getErrorMessage());
		}
		ValidatorResult isSewerageFieldValidated = garbageFieldValidator.validate(garbageConnectionRequest, reqType);
		if (!isSewerageFieldValidated.isStatus()) {
			errorMap.putAll(isSewerageFieldValidated.getErrorMessage());
		}
//		if(garbageConnectionRequest.getGarbageConnection().getProcessInstance().getAction().equalsIgnoreCase("PAY"))
//			errorMap.put("INVALID_ACTION","Pay action cannot perform directly");
//		
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
	
	
	/**
	 * Validate for previous data to current data
	 * 
	 * @param request sewerage connection request
	 * @param searchResult sewerage connection search result
	 */
	public void validateUpdate(GarbageConnectionRequest request, GarbageConnection searchResult) {
		validateAllIds(request.getGarbageConnection(), searchResult);
		validateDuplicateDocuments(request);
		setFieldsFromSearch(request,searchResult);
		
	}
   
	/**
	 * Validates if all ids are same as obtained from search result
	 * 
	 * @param updateGarbageConnection The sewerage connection request from update request
	 * @param searchResult The sewerage connection from search result
	 */
	private void validateAllIds(GarbageConnection updateGarbageConnection, GarbageConnection searchResult) {
		Map<String, String> errorMap = new HashMap<>();
		if (!searchResult.getApplicationNo().equals(updateGarbageConnection.getApplicationNo())) {
			StringBuilder builder = new StringBuilder();
			builder.append("The application number from search: ").append(searchResult.getApplicationNo())
					.append(" and from update: ").append(updateGarbageConnection.getApplicationNo())
					.append(" does not match");
			errorMap.put("INVALID_UPDATE", builder.toString());
		}
		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}
    
    /**
     * Validates application documents for duplicates
     * 
     * @param request The GarbageConnection Request
     */
	private void validateDuplicateDocuments(GarbageConnectionRequest request) {
		List<String> documentFileStoreIds = new LinkedList<>();
//		if (request.getGarbageConnection().getDocuments() != null) {
//			request.getGarbageConnection().getDocuments().forEach(document -> {
//				if (documentFileStoreIds.contains(document.getFileStoreId()))
//					throw new CustomException("DUPLICATE_DOCUMENT ERROR",
//							"Same document cannot be used multiple times");
//				else
//					documentFileStoreIds.add(document.getFileStoreId());
//			});
//		}
	}
	/**
	 * Enrich Immutable fields
	 * 
	 * @param request Sewerage connection request
	 * @param searchResult sewerage connection search result
	 */
	private void setFieldsFromSearch(GarbageConnectionRequest request, GarbageConnection searchResult) {
		request.getGarbageConnection().setConnectionNo(searchResult.getConnectionNo());
	}
}
