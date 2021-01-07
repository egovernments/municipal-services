package org.egov.fsm.calculator.utils;

public class CalculatorConstants {

	

    public static final String MDMS_EGF_MASTER = "egf-master";

    public static final String MDMS_FINANCIALYEAR  = "FinancialYear";

    public static final String MDMS_FINACIALYEAR_PATH = "$.MdmsRes.egf-master.FinancialYear[?(@.code==\"{}\")]";

    public static final String MDMS_STARTDATE  = "startingDate";

    public static final String MDMS_ENDDATE  = "endingDate";

    public static final String MDMS_CALCULATIONTYPE_FINANCIALYEAR_PATH = "$.MdmsRes.BPA.CalculationType[?(@.financialYear=='{}')]";
    
    public static final String APPLICATION_FEE="APPLICATION_FEE";
	
	// Error messages in FSM Calculator
	
	public static final String PARSING_ERROR = "PARSING ERROR";
	
	public static final String INVALID_AMOUNT = "INVALID AMOUNT";
	
	public static final String INVALID_UPDATE = "INVALID UPDATE";
	
	public static final String INVALID_ERROR = "INVALID ERROR";
	
	public static final String INVALID_APPLICATION_NUMBER = "INVALID APPLICATION NUMBER";
	
	public static final String CALCULATION_ERROR = "CALCULATION ERROR";
	
}
