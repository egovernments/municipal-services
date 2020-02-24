package org.egov.swService.validator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.repository.SewarageDao;
import org.egov.swService.util.SWConstants;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class SewerageConnectionValidator {

	@Autowired
	private SewarageDao sewarageDao;

	/**
	 * 
	 * @param sewarageConnectionRequest
	 *            SewarageConnectionRequest is request for create or update
	 *            sewarage connection
	 * @param isUpdate
	 *            True for update and false for create
	 */
	public void validateSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest, boolean isUpdate) {
		SewerageConnection sewerageConnection = sewerageConnectionRequest.getSewerageConnection();
		Map<String, String> errorMap = new HashMap<>();
		if (sewerageConnectionRequest.getSewerageConnection().getProperty() == null) {
			errorMap.put("INVALID_PROPERTY", "Property should not be empty");
		}
		if (StringUtils.isEmpty(sewerageConnection.getProperty().getUsageCategory())) {
			errorMap.put("INVALID SEWERAGE CONNECTION PROPERTY USAGE TYPE",
					"SewerageConnection cannot be created without property usage type");
		}

		if (isUpdate && sewerageConnectionRequest.getSewerageConnection().getAction()
				.equalsIgnoreCase(SWConstants.APPROVE_CONNECTION_CONST)) {
			if (StringUtils.isEmpty(sewerageConnection.getConnectionType())) {
				errorMap.put("INVALID_SEWERAGE_CONNECTION_TYPE", "Connection type should not be empty");
			}

			if (StringUtils.isEmpty(sewerageConnection.getRoadType())) {
				errorMap.put("INVALID_ROAD_TYPE", "Road type should not be empty");
			}

		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
	
	
	/**
	 * Validate for previous data to current data
	 * 
	 * @param request sewerage connection request
	 * @param searchResult sewerage connection search result
	 */
	public void validateUpdate(SewerageConnectionRequest request, SewerageConnection searchResult) {
		validateAllIds(request.getSewerageConnection(), searchResult);
		validateDuplicateDocuments(request);
		setFieldsFromSearch(request,searchResult);
		
	}
   
	/**
	 * Validates if all ids are same as obtained from search result
	 * 
	 * @param updatesewerageConnection The sewerage connection request from update request 
	 * @param searchResult The sewerage connection from search result
	 */
	private void validateAllIds(SewerageConnection updateSewerageConnection, SewerageConnection searchResult) {
		Map<String, String> errorMap = new HashMap<>();
		if (!searchResult.getApplicationNo().equals(updateSewerageConnection.getApplicationNo()))
			errorMap.put("INVALID UPDATE", "The application number from search: " + searchResult.getApplicationNo()
					+ " and from update: " + updateSewerageConnection.getApplicationNo() + " does not match");
		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}
    
    /**
     * Validates application documents for duplicates
     * 
     * @param request The sewerageConnection Request
     */
	private void validateDuplicateDocuments(SewerageConnectionRequest request) {
		List<String> documentFileStoreIds = new LinkedList<>();
		if (request.getSewerageConnection().getDocuments() != null) {
			request.getSewerageConnection().getDocuments().forEach(document -> {
				if (documentFileStoreIds.contains(document.getFileStoreId()))
					throw new CustomException("DUPLICATE_DOCUMENT ERROR",
							"Same document cannot be used multiple times");
				else
					documentFileStoreIds.add(document.getFileStoreId());
			});
		}
	}
	/**
	 * Enrich Immutable fields
	 * 
	 * @param request Sewerage connection request
	 * @param searchResult sewerage connection search result
	 */
	private void setFieldsFromSearch(SewerageConnectionRequest request, SewerageConnection searchResult) {
		request.getSewerageConnection().setApplicationStatus(searchResult.getApplicationStatus());
		request.getSewerageConnection().setConnectionNo(searchResult.getConnectionNo());
	}
	
	public void validatePropertyForConnection(List<SewerageConnection> sewerageConnectionList) {
		sewerageConnectionList.forEach(sewerageConnection -> {
			if (sewerageConnection.getProperty().getPropertyId() == null
					|| sewerageConnection.getProperty().getPropertyId().isEmpty()) {
				throw new CustomException("INVALID SEARCH",
						"PROPERTY ID NOT FOUND FOR " + sewerageConnection.getConnectionNo() + " SEWERAGE CONNECTION NO");
			}
		});
	}

}
