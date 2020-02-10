package org.egov.waterConnection.validator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.constants.WCConstants;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


@Component
public class WaterConnectionValidator {

	/**
	 * 
	 * @param waterConnectionRequest
	 *            WaterConnectionRequest is request for create or update water
	 *            connection
	 * @param isUpdate
	 *            True for update and false for create
	 */
	public void validateWaterConnection(WaterConnectionRequest waterConnectionRequest, boolean isUpdate) {
		WaterConnection waterConnection = waterConnectionRequest.getWaterConnection();
		Map<String, String> errorMap = new HashMap<>();
		if (waterConnection.getProperty().getUsageCategory() == null || waterConnection.getProperty().getUsageCategory().isEmpty()) {
			errorMap.put("INVALID_WATER_CONNECTION_PROPERTY_USAGE_TYPE", "Water connection cannot be created without property usage type");
		}
//		if ((waterConnection.getPipeSize() == null || waterConnection.getPipeSize() == 0.0) && (waterConnection.getNoOfTaps() == null || waterConnection.getNoOfTaps() == 0)) {
//			errorMap.put("INVALID_WATER_CONNECTION_PIPE_SIZE_OR_TAPS", "Pipe size or no of taps should not be null!!");
//		}
//		if (waterConnection.getConnectionType() == WCConstants.METERED_CONNECTION) {
//			if (waterConnection.getMeterId() == null) {
//				errorMap.put("INVALID WATER CONNECTION TYPE", "Meter Id cannot be null !!");
//			}
//			if (waterConnection.getMeterInstallationDate() < 0 || waterConnection.getMeterInstallationDate() == null
//					|| waterConnection.getMeterInstallationDate() == 0) {
//				errorMap.put("INVALID METER INSTALLATION DATE", "Meter Installation date cannot be null or negative !!");
//			}
//		}
//			
////		if (isUpdate && waterConnection.getConnectionNo() != null && !waterConnection.getConnectionNo().isEmpty()) {
////			int n = waterDao.isWaterConnectionExist(Arrays.asList(waterConnection.getConnectionNo()));
////			if (n == 0) {
////				errorMap.put("INVALID WATER CONNECTION NUMBER", "Water Id not present");
////			}
////		}
//		if (waterConnection.getConnectionType() == null || waterConnection.getConnectionType().isEmpty()) {
//			errorMap.put("INVALID WATER CONNECTION TYPE", "WaterConnection cannot be created  without connection type");
//		}
//		if (waterConnection.getConnectionCategory() == null || waterConnection.getConnectionCategory().isEmpty()) {
//			errorMap.put("INVALID WATER CONNECTION CATEGORY", "WaterConnection cannot be created without connection category");
//		}
//		if (waterConnection.getWaterSource() == null || waterConnection.getWaterSource().isEmpty()) {
//			errorMap.put("INVALID WATER SOURCE", "WaterConnection cannot be created without water source");
//		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
	
	public void validatePropertyForConnection(List<WaterConnection> waterConnectionList) {
		waterConnectionList.forEach(waterConnection -> {
			if (waterConnection.getProperty().getPropertyId() == null
					|| waterConnection.getProperty().getPropertyId().isEmpty()) {
				throw new CustomException("INVALID SEARCH",
						"PROPERTY ID NOT FOUND FOR " + waterConnection.getConnectionNo() + " WATER CONNECTION NO");
			}
		});
	}
	
	/**
	 * Validate for previous data to current data
	 * 
	 * @param request water connection request
	 * @param searchResult water connection search result
	 */
	public void validateUpdate(WaterConnectionRequest request, WaterConnection searchResult) {
		validateAllIds(request.getWaterConnection(), searchResult);
		validateDuplicateDocuments(request);
		setFieldsFromSearch(request,searchResult);
		
	}
   
	/**
	 * Validates if all ids are same as obtained from search result
	 * 
	 * @param updateWaterConnection The water connection request from update request 
	 * @param searchResult The water connection from search result
	 */
	private void validateAllIds(WaterConnection updateWaterConnection, WaterConnection searchResult) {
		Map<String, String> errorMap = new HashMap<>();
		if (!searchResult.getApplicationNo().equals(updateWaterConnection.getApplicationNo()))
			errorMap.put("INVALID UPDATE", "The application number from search: " + searchResult.getApplicationNo()
					+ " and from update: " + updateWaterConnection.getApplicationNo() + " does not match");
		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}
    
    /**
     * Validates application documents for duplicates
     * 
     * @param request The waterConnection Request
     */
	private void validateDuplicateDocuments(WaterConnectionRequest request) {
		List<String> documentFileStoreIds = new LinkedList<>();
		if (request.getWaterConnection().getDocuments() != null) {
			request.getWaterConnection().getDocuments().forEach(document -> {
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
	 * @param request Water connection request
	 * @param searchResult water connection search result
	 */
	private void setFieldsFromSearch(WaterConnectionRequest request, WaterConnection searchResult) {
		request.getWaterConnection().setApplicationStatus(searchResult.getApplicationStatus());
		request.getWaterConnection().setConnectionNo(searchResult.getConnectionNo());
	}
}
