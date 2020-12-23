package org.egov.fsm.util;

import org.springframework.stereotype.Component;

@Component
public class FSMConstants {


	// MDMS

	public static final String EMPLOYEE="Employee";
	
	public static final String FSM_MODULE = "FSM";

	public static final String FSM_BusinessService = "FSM";

	public static final String FSM_MODULE_CODE = "FSM";

	public static final String COMMON_MASTERS_MODULE = "common-masters";
	
	public static final String PROPERTY_MASTER_MODULE="PropertyTax";

	public static final String NOTIFICATION_LOCALE = "en_IN";

	// mdms path codes

	public static final String FSM_JSONPATH_CODE = "$.MdmsRes.FSM";

	public static final String COMMON_MASTER_JSONPATH_CODE = "$.MdmsRes.common-masters";
	public static final String PROPERTY_MASTER_JSONPATH_CODE = "$.MdmsRes.PropertyTax";

	// error constants
	public static final String INVALID_PROPERTY_TYPE ="INVALID_PROPERTY_TYPE";
	public static final String INVALID_UPDATE ="Invlaid Update";
	
	
	// mdms master names

	public static final String PROPERTY_TYPE = "PropertyType";



	// FINANCIAL YEAR

	public static final String MDMS_EGF_MASTER = "egf-master";

	public static final String MDMS_FINANCIALYEAR = "FinancialYear";

	public static final String MDMS_FINACIALYEAR_PATH = "$.MdmsRes.egf-master.FinancialYear[?(@.code==\"{}\")]";

	public static final String MDMS_STARTDATE = "startingDate";

	public static final String MDMS_ENDDATE = "endingDate";

	

	

	public static final String USREVENTS_EVENT_TYPE = "SYSTEMGENERATED";
	public static final String USREVENTS_EVENT_NAME = "FSM";
	public static final String USREVENTS_EVENT_POSTEDBY = "SYSTEM-FSM";
	public static final String SEARCH_MODULE = "rainmaker-fsm";
	
	
	

	public static final String FILESTOREID = "fileStoreId";


	public static final String CITIZEN = "CITIZEN";
	
	public static final String ACTION_SENDBACKTOCITIZEN = "SEND_BACK_TO_CITIZEN";


}
