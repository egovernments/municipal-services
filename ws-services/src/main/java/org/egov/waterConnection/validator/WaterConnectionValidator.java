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
import org.springframework.util.StringUtils;


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
		if (StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getProperty())) {
			errorMap.put("INVALID_PROPERTY", "Property should not be empty");
		}
		if (!StringUtils.isEmpty(waterConnectionRequest.getWaterConnection().getProperty())
				&& (StringUtils.isEmpty(waterConnection.getProperty().getUsageCategory()))) {
			errorMap.put("INVALID_WATER_CONNECTION_PROPERTY_USAGE_TYPE", "Property usage type should not be empty");
		}
		if (isUpdate && WCConstants.APPROVE_CONNECTION_CONST
				.equalsIgnoreCase(waterConnectionRequest.getWaterConnection().getAction())) {
			if (StringUtils.isEmpty(waterConnection.getConnectionType())) {
				errorMap.put("INVALID_WATER_CONNECTION_TYPE", "Connection type should not be empty");
			}
			if (!StringUtils.isEmpty(waterConnection.getConnectionType())
					&& WCConstants.METERED_CONNECTION.equalsIgnoreCase(waterConnection.getConnectionType())) {
				if (waterConnection.getMeterId() == null) {
					errorMap.put("INVALID_METER_ID", "Meter Id cannot be empty");
				}
				if (waterConnection.getMeterInstallationDate() < 0 || waterConnection.getMeterInstallationDate() == null
						|| waterConnection.getMeterInstallationDate() == 0) {
					errorMap.put("INVALID_METER_INSTALLATION_DATE",
							"Meter Installation date cannot be null or negative");
				}
			}
			if (StringUtils.isEmpty(waterConnection.getConnectionCategory())) {
				errorMap.put("INVALID_WATER_CONNECTION_CATEGORY",
						"WaterConnection cannot be created without connection category");
			}
			if (StringUtils.isEmpty(waterConnection.getWaterSource())) {
				errorMap.put("INVALID_WATER_SOURCE", "WaterConnection cannot be created  without water source");
			}
			if (StringUtils.isEmpty(waterConnection.getRoadType())) {
				errorMap.put("INVALID_ROAD_TYPE", "Road type should not be empty");
			}

		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
	
	public void validatePropertyForConnection(List<WaterConnection> waterConnectionList) {
		waterConnectionList.forEach(waterConnection -> {
			if (StringUtils.isEmpty(waterConnection.getProperty().getPropertyId())) {
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
		if (request.getWaterConnection().getDocuments() != null) {
			List<String> documentFileStoreIds = new LinkedList<>();
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
