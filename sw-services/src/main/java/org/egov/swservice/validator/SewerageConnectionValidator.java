package org.egov.swservice.validator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.util.SWConstants;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SewerageConnectionValidator {

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
		
		if (sewerageConnectionRequest.getSewerageConnection().getProcessInstance() == null || StringUtils
				.isEmpty(sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction())) {
			errorMap.put("INVALID_ACTION", "Workflow obj can not be null or action can not be empty!!");
		}
		if (isUpdate && (sewerageConnectionRequest.getSewerageConnection().getProcessInstance() != null)
				&& !StringUtils
						.isEmpty(sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction())
				&& SWConstants.APPROVE_CONNECTION_CONST.equalsIgnoreCase(
						sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction())) {
			if (StringUtils.isEmpty(sewerageConnection.getConnectionType())) {
				errorMap.put("INVALID_SEWERAGE_CONNECTION_TYPE", "Connection type should not be empty");
			}

			if (StringUtils.isEmpty(sewerageConnection.getRoadType())) {
				errorMap.put("INVALID_ROAD_TYPE", "Road type should not be empty");
			}

		}
		if (isUpdate
				&& (!StringUtils.isEmpty(sewerageConnectionRequest.getSewerageConnection().getProcessInstance())
						&& !StringUtils.isEmpty(
								sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction()))
				&& SWConstants.ACTIVATE_CONNECTION_CONST.equalsIgnoreCase(
						sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction())) {
			if (StringUtils.isEmpty(sewerageConnection.getConnectionExecutionDate())) {
				errorMap.put("INVALID_CONNECTION_EXECUTION_DATE", "Connection execution date should not be empty");
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
		setStatusForDocuments(request, searchResult);
		
	}
   
	/**
	 * Validates if all ids are same as obtained from search result
	 * 
	 * @param updatesewerageConnection The sewerage connection request from update request 
	 * @param searchResult The sewerage connection from search result
	 */
	private void validateAllIds(SewerageConnection updateSewerageConnection, SewerageConnection searchResult) {
		Map<String, String> errorMap = new HashMap<>();
		if (!searchResult.getApplicationNo().equals(updateSewerageConnection.getApplicationNo())) {
			StringBuilder builder = new StringBuilder();
			builder.append("The application number from search: ").append(searchResult.getApplicationNo())
					.append(" and from update: ").append(updateSewerageConnection.getApplicationNo())
					.append(" does not match");
			errorMap.put("INVALID UPDATE", builder.toString());
		}
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
	
	/**
	 * 
	 * @param request
	 * @param searchResult
	 */
	private void setStatusForDocuments(SewerageConnectionRequest request, SewerageConnection searchResult) {
//		if (!CollectionUtils.isEmpty(searchResult.getDocuments())) {
//			ArrayList<String> fileStoreIds = new ArrayList<>();
//			if (!CollectionUtils.isEmpty(request.getSewerageConnection().getDocuments())) {
//				request.getSewerageConnection().getDocuments().forEach(document -> {
//					fileStoreIds.add(document.getFileStoreId());
//				});
//			}
//			searchResult.getDocuments().forEach(document -> {
//				if (!fileStoreIds.contains(document.getFileStoreId())) {
//					document.setStatus(Status.INACTIVE);
//					request.getSewerageConnection().getDocuments().add(document);
//				}
//			});
//		}
	}

}
