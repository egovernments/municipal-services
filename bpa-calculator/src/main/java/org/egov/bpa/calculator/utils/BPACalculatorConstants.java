package org.egov.bpa.calculator.utils;

public class BPACalculatorConstants {

	

    public static final String MDMS_EGF_MASTER = "egf-master";

    public static final String MDMS_FINANCIALYEAR  = "FinancialYear";

    public static final String MDMS_FINACIALYEAR_PATH = "$.MdmsRes.egf-master.FinancialYear[?(@.code==\"{}\")]";

    public static final String MDMS_STARTDATE  = "startingDate";

    public static final String MDMS_ENDDATE  = "endingDate";

    public static final String MDMS_CALCULATIONTYPE = "CalculationType";
    
    public static final String MDMS_TENANT_PATH = "$.MdmsRes.tenant.tenants";

    public static final String MDMS_CALCULATIONTYPE_PATH = "$.MdmsRes.BPA.CalculationType";
    
    public static final String MDMS_TENAT_ULB_GRADE = "$.MdmsRes.BPA.CalculationType";

    public static final String MDMS_BPA_PATH = "$.MdmsRes.BPA";

    public static final String MDMS_BPA = "BPA";
    
    public static final String MDMS_MODULE_TENANT = "tenant";
    public static final String MDMS_MASTER_TENANT = "tenants";

    public static final String MDMS_CALCULATIONTYPE_FINANCIALYEAR= "financialYear";

    public static final String MDMS_CALCULATIONTYPE_FINANCIALYEAR_PATH = "$.MdmsRes.BPA.CalculationType[?(@.financialYear=='{}')]";

	public static final Object MDMS_CALCULATIONTYPE_SERVICETYPE = "serviceType";

	public static final Object MDMS_CALCULATIONTYPE_RISKTYPE = "riskType";

	public static final String MDMS_ROUNDOFF_TAXHEAD = "TL_ROUNDOFF";

	public static final String MDMS_CALCULATIONTYPE_AMOUNT = "amount";
	
	public static final String MDMS_CALCULATIONTYPE_APL_FEETYPE = "ApplicationFee";
	
	public static final String MDMS_CALCULATIONTYPE_SANC_FEETYPE = "SanctionFee";
	
	public static final String MDMS_CALCULATIONTYPE_LOW_APL_FEETYPE = "Low_ApplicationFee";
	
	public static final String MDMS_CALCULATIONTYPE_LOW_SANC_FEETYPE = "Low_SanctionFee";
	
	public static final String DEVELOPENT_CHARGE="Development";
	public static final String SCRUTINY_FEE="Scrutiny";
	public static final String SHELTER_FUND="Shelter";
	public static final String LABOUR_CESS="Labour";
	
	public static final String MDMS_CALCULATIONTYPE_LOW_SANC_FEETYPE_DEVELOPENT_CHARGE = "Low_SanctionFee_Development";
	public static final String MDMS_CALCULATIONTYPE_LOW_SANC_FEETYPE_SCRUTINY_FEE = "Low_SanctionFee_Scrutiny";
	public static final String MDMS_CALCULATIONTYPE_LOW_SANC_FEETYPE_SHELTER_FUND = "Low_SanctionFee_Shelter";
	public static final String MDMS_CALCULATIONTYPE_LOW_SANC_FEETYPE_LABOUR_CESS = "Low_SanctionFee_Labour";
	
	public static final String MDMS_CALCULATIONTYPE_SANC_FEETYPE_DEVELOPENT_CHARGE = "SanctionFee_Development";
	public static final String MDMS_CALCULATIONTYPE_SANC_FEETYPE_SCRUTINY_FEE = "SanctionFee_Scrutiny";
	public static final String MDMS_CALCULATIONTYPE_SANC_FEETYPE_SHELTER_FUND = "SanctionFee_Shelter";
	public static final String MDMS_CALCULATIONTYPE_SANC_FEETYPE_LABOUR_CESS = "SanctionFee_Labour";
	
	public static final String LOW_RISK_PERMIT_FEE_TYPE="LOW_RISK_PERMIT_FEE";
	
	public static final String DEFAULT_ULB_GRADE="GP";
	
	
	
	public static final String BUILT_UP_AREA="builtuparea";
	
	public static final String BUILDING_HEIGHT="buildingheight";



	
}
