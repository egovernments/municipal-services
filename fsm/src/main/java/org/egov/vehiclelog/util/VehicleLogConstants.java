package org.egov.vehiclelog.util;

import org.springframework.stereotype.Component;

@Component
public class VehicleLogConstants {
	
	//Error Constants
	public static final String INVALID_VEHICLELOG_ERROR = "INVALID_VEHICLELOG_ERROR";
	public static final String CREATE_VEHICLELOG_ERROR = "CREATE_VEHICLELOG_ERROR";	
	public static final String UPDATE_VEHICLELOG_ERROR = "UPDATE_VEHICLELOG_ERROR";
	public static final String INVALID_TENANT = "INVALID TENANT";
	public static final String INVALID_DSO = "INVALID DSO";
	public static final String INVALID_VEHICLE = "INVALID VEHICLE";
	public static final String INVALID_APPLICATION_NOS = "INVALID APPLICATION NUMBER/NUMBERS";
	public static final String IDGEN_ERROR = "IDGEN ERROR";
	public static final String INVALID_SEARCH = "INVALID SEARCH";
	
	//VehicleLog Application Status Constants
	public static final String VEHICLE_LOG_APPLICATION_CREATED_STATUS = "CREATED";
	public static final String VEHICLE_LOG_APPLICATION_UPDATED_STATUS = "UPDATED";
}
