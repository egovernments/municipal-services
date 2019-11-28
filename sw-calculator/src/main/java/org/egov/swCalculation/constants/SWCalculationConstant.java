package org.egov.swCalculation.constants;

<<<<<<< HEAD
import org.egov.swCalculation.model.DemandStatus;
=======
import java.math.BigDecimal;
>>>>>>> 06a67bd080b67b9a0edc67086f2b92a8ffdb650f

public class SWCalculationConstant {

	public static final String TAXPERIOD_MASTER_KEY = "TAXPERIOD";
	
	public static final String TAXHEADMASTER_MASTER_KEY = "WS_TAX";
	
	public static final String FINANCIALYEAR_MASTER_KEY = "2019-20";
	
	public static final String FINANCIAL_YEAR_STARTING_DATE = "startingDate";

	public static final String FINANCIAL_YEAR_ENDING_DATE = "endingDate";
	
	public static final String URL_PARAMS_SEPARATER = "?";
	
	public static final String MDMS_ROUNDOFF_TAXHEAD= "WS_ROUNDOFF";
	
	public static final String TENANT_ID_FIELD_FOR_SEARCH_URL = "tenantId=";
	
	public static final String SEPARATER = "&";
	
	public static final String SERVICE_FIELD_FOR_SEARCH_URL = "service=";
	
	public static final String SERVICE_FIELD_VALUE_WS = "SW";
	
	public static final String MDMS_FINACIALYEAR_PATH = "$.MdmsRes.egf-master.FinancialYear[?(@.code==\"{}\")]";
	
	
	public static final String EG_WS_FINANCIAL_MASTER_NOT_FOUND = "EG_WS_FINANCIAL_MASTER_NOT_FOUND";
	
	public static final String EG_WS_FINANCIAL_MASTER_NOT_FOUND_MSG = "No Financial Year data is available for the given year value of : ";
	
	public static final String FINANCIAL_YEAR_MASTER = "FinancialYear";
	
	public static final String FINANCIAL_YEAR_RANGE_FEILD_NAME = "finYearRange";
	
	public static final String FINANCIAL_MODULE = "egf-master";
	
	public static final String SW_TAX_MODULE = "sw-services-calculation";
	
	public static final String SW_REBATE_MASTER = "Rebate";
	
	public static final String SW_SEWERAGE_CESS_MASTER = "SewerageCess";
	
	public static final String SW_PENANLTY_MASTER = "Penalty";

	public static final String SW_INTEREST_MASTER = "Interest";

	public static final String SW_BILLING_SLAB_MASTER = "SCBillingSlab";
	
<<<<<<< HEAD
	public static final String EMPTY_DEMAND_ERROR_CODE = "EMPTY_DEMANDS";

	public static final String EMPTY_DEMAND_ERROR_MESSAGE = "No demands found for the given bill generate criteria";
	
	public static final String DEMAND_CANCELLED_STATUS = DemandStatus.CANCELLED.toString();
	
	public static final String CONSUMER_CODE_SEARCH_FIELD_NAME = "consumerCode=";
	
	public static final String WS_CONSUMER_CODE_SEPARATOR = ":";
	
	public static final String EG_WS_INVALID_DEMAND_ERROR = "EG_WS_INVALID_DEMAND_ERROR";
	public static final String EG_WS_INVALID_DEMAND_ERROR_MSG = " Bill cannot be generated for previous assessments in a year, please use the latest assesmment to pay";
	
=======
	/**
	 * Time Taxes Config
	 */
	public static final String SW_TIME_REBATE = "SW_TIME_REBATE";

	public static final String SW_TIME_INTEREST = "SW_TIME_INTEREST";

	public static final String SW_TIME_PENALTY = "SW_TIME_PENALTY";

	public static final String SW_WATER_CESS = "SW_WATER_CESS";

	public static final String SW_CHARGE = "SW_CHARGE";
	
	
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
	
	public static final String FINANCIAL_YEAR_STARTING_DATE = "startingDate";

	public static final String FINANCIAL_YEAR_ENDING_DATE = "endingDate";
	
	/*
	 * bigdecimal values
	 */

	public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
	
	
	public static final Long TIMEZONE_OFFSET = 19800000l;
	
	public static final String SW_Round_Off = "SW_Round_Off";
>>>>>>> 06a67bd080b67b9a0edc67086f2b92a8ffdb650f

}
