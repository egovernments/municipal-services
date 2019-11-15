package org.egov.wsCalculation.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.egov.wsCalculation.model.DemandStatus;

public class WSCalculationConstant {

	public static final String FINANCIALYEAR_MASTER_KEY = "FINANCIALYEAR";

	public static final String TAXPERIOD_MASTER_KEY = "TAXPERIOD";

	public static final String TAXHEADMASTER_MASTER_KEY = "TAXHEADMASTER";

	public static final String URL_PARAMS_SEPARATER = "?";

	public static final String TENANT_ID_FIELD_FOR_SEARCH_URL = "tenantId=";

	public static final String SEPARATER = "&";

	public static final String SERVICE_FIELD_FOR_SEARCH_URL = "service=";

	public static final String SERVICE_FIELD_VALUE_WS = "WS";

	public static final String WS_TAX = "WS_TAX";

	public static final String WS_CONSUMER_CODE_SEPARATOR = ":";

	public static final String EG_WS_DEPRECIATING_ASSESSMENT_ERROR = "EG_WS_DEPRECIATING_ASSESSMENT_ERROR";

	public static final String EG_WS_DEPRECIATING_ASSESSMENT_ERROR_MSG = "Depreciating assessments are not allowed for the same assessment year,"
			+ "please kindly update the values for the following properties with assessmentNumbers : ";

	public static final String WS_ADVANCE_CARRYFORWARD = "WS_ADVANCE_CARRYFORWARD";

	public static final String FINANCIAL_YEAR_MASTER = "FinancialYear";

	public static final String FINANCIAL_YEAR_RANGE_FEILD_NAME = "finYearRange";

	/*
	 * Module names
	 */

	public static final String FINANCIAL_MODULE = "egf-master";

	/*
	 * billing service field names
	 */

	public static final String CONSUMER_CODE_SEARCH_FIELD_NAME = "consumerCode=";

	public static final String DEMAND_ID_SEARCH_FIELD_NAME = "demandId=";

	public static final String DEMAND_CANCELLED_STATUS = DemandStatus.CANCELLED.toString();

	/*
	 * queries
	 */

	public static final String QUERY_ASSESSMENT_INSERT = "INSERT INTO eg_pt_assessment (uuid, assessmentnumber, assessmentyear, demandid,"

			+ " propertyid, tenantid, createdby, createdtime, lastmodifiedby, lastmodifiedtime)"

			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	public static final String BUSINESSSERVICE_FIELD_FOR_SEARCH_URL = "businessService=";
	public static final String WATER_TAX_SERVICE_CODE = "WS";

	public static final String EMPTY_DEMAND_ERROR_CODE = "EMPTY_DEMANDS";

	public static final String EMPTY_DEMAND_ERROR_MESSAGE = "No demands found for the given bill generate criteria";

	public static final String WATER_TAX_MODULE = "WaterTax";

	/*
	 * Master names
	 */

	public static final String USAGE_MAJOR_MASTER = "UsageCategoryMajor";

	public static final String USAGE_MINOR_MASTER = "UsageCategoryMinor";

	public static final String USAGE_SUB_MINOR_MASTER = "UsageCategorySubMinor";

	public static final String USAGE_DETAIL_MASTER = "UsageCategoryDetail";

	public static final String OWNER_TYPE_MASTER = "OwnerType";

	public static final List<String> PROPERTY_BASED_EXEMPTION_MASTERS = Collections.unmodifiableList(Arrays.asList(
			USAGE_MAJOR_MASTER, USAGE_MINOR_MASTER, USAGE_SUB_MINOR_MASTER, USAGE_DETAIL_MASTER, OWNER_TYPE_MASTER));

	public static final String WS_ROUNDOFF = "WS_ROUNDOFF";

	/*
	 * exceptions
	 */
	public static final String CONNECT_EXCEPTION_KEY = "CONNECTION_FAILED";

	public static final String EG_WS_INVALID_DEMAND_ERROR = "EG_WS_INVALID_DEMAND_ERROR";
	public static final String EG_WS_INVALID_DEMAND_ERROR_MSG = " Bill cannot be generated for previous assessments in a year, please use the latest assesmment to pay";
	
	/**
	 * Time Taxes Config
	 */
	public static final String WS_TIME_REBATE = "WATER_CONNECTION_REBATE";

	public static final String WS_TIME_INTEREST = "WS_TIME_INTEREST";

	public static final String WS_TIME_PENALTY = "WS_TIME_PENALTY";
	
	public static final String WS_FIRE_CESS = "WS_FIRE_CESS";
}
