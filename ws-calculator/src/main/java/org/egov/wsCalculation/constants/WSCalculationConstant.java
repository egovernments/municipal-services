package org.egov.wsCalculation.constants;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.egov.wsCalculation.model.DemandStatus;

public class WSCalculationConstant {

	public static final String TAXPERIOD_MASTER_KEY = "TAXPERIOD";

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

	public static final String MDMS_STARTDATE = "startingDate";

	public static final String MDMS_ENDDATE = "endingDate";
	
	 public static final String MDMS_FINANCIALYEAR  = "FinancialYear";
	 
	 
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

	public static final String MDMS_FINACIALYEAR_PATH = "$.MdmsRes.egf-master.FinancialYear[?(@.code==\"{}\")]";

	public static final String EG_WS_FINANCIAL_MASTER_NOT_FOUND = "EG_WS_FINANCIAL_MASTER_NOT_FOUND";
	public static final String EG_WS_FINANCIAL_MASTER_NOT_FOUND_MSG = "No Financial Year data is available for the given year value of : ";

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


	/*
	 * exceptions
	 */
	public static final String CONNECT_EXCEPTION_KEY = "CONNECTION_FAILED";

	public static final String EG_WS_INVALID_DEMAND_ERROR = "EG_WS_INVALID_DEMAND_ERROR";
	public static final String EG_WS_INVALID_DEMAND_ERROR_MSG = " Bill cannot be generated for previous assessments in a year, please use the latest assesmment to pay";

	public static final String Assesment_Year = "assessmentYear";

	/**
	 * Time Taxes Config
	 */
	public static final String WS_TIME_REBATE = "WS_TIME_REBATE";

	public static final String WS_TIME_INTEREST = "WS_TIME_INTEREST";

	public static final String WS_TIME_PENALTY = "WS_TIME_PENALTY";

	public static final String WS_WATER_CESS = "WS_WATER_CESS";

	public static final String WS_CHARGE = "WS_CHARGE";

	/**
	 * data fields
	 */
	public static final String FROMFY_FIELD_NAME = "fromFY";

	public static final String ENDING_DATE_APPLICABLES = "endingDay";

	public static final String STARTING_DATE_APPLICABLES = "startingDay";

	public static final String MAX_AMOUNT_FIELD_NAME = "maxAmount";

	public static final String MIN_AMOUNT_FIELD_NAME = "minAmount";

	public static final String FLAT_AMOUNT_FIELD_NAME = "flatAmount";

	public static final String RATE_FIELD_NAME = "rate";
	
	public static final String DAYA_APPLICABLE_NAME = "applicableAfterDays";

	/*
	 * bigdecimal values
	 */

	public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	public static final String WC_REBATE_MASTER = "Rebate";

	public static final String FINANCIALYEAR_MASTER_KEY = "2019-20";

	public static final String FINANCIAL_YEAR_STARTING_DATE = "startingDate";

	public static final String FINANCIAL_YEAR_ENDING_DATE = "endingDate";

	public static final String TAXHEADMASTER_MASTER_KEY = "WS_TAX";

	public static final String WS_Round_Off = "WS_Round_Off";

	public static final String WS_TAX_MODULE = "ws-services-calculation";

	public static final String WC_PENANLTY_MASTER = "Penalty";

	public static final String WC_WATER_CESS_MASTER = "WaterCess";

	public static final String WC_INTEREST_MASTER = "Interest";

	public static final String WC_BILLING_SLAB_MASTER = "WCBillingSlab";

	public static final String CODE_FIELD_NAME = "code";

	public static final List<String> WS_BASED_EXEMPTION_MASTERS = Collections
			.unmodifiableList(Arrays.asList(USAGE_MAJOR_MASTER));

	public static final List<String> WS_BILLING_SLAB_MASTERS = Collections
			.unmodifiableList(Arrays.asList(WC_BILLING_SLAB_MASTER));

	
	public static final Long TIMEZONE_OFFSET = 19800000l;
	public static final List<String> TAX_APPLICABLE = Collections.unmodifiableList(Arrays.asList(WS_CHARGE));
	
	public static final String flatRateCalculationAttribute = "Flat";
	
	public static final String meteredConnectionType = "Metered";
	
	public static final String nonMeterdConnection = "Non Metered";
	
	public static final String noOfTapsConst = "No. of taps";
	
	public static final String pipeSizeConst = "Pipe Size";

}
