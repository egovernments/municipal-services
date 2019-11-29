package org.egov.bpa.util;

import org.springframework.stereotype.Component;

@Component
public class BPAConstants {
	
	private static final String ACTION_APPLY = "APPLY";

	// MDMS

	public static final String BPA_MODULE = "BPA";

	public static final String BPA_MODULE_CODE = "BP";

	public static final String COMMON_MASTERS_MODULE = "common-masters";

	
	
	// mdms path codes

	public static final String BPA_JSONPATH_CODE = "$.MdmsRes";
	
	public static final String COMMON_MASTER_JSONPATH_CODE = "$.MdmsRes.common-masters";
	
	// error constants

    public static final String INVALID_TENANT_ID_MDMS_KEY = "INVALID TENANTID";

    public static final String INVALID_TENANT_ID_MDMS_MSG = "No data found for this tenentID";
	
	// mdms master names

	public static final String APPLICATION_TYPE = "ApplicationType";
	public static final String OWNER_TYPE = "OwnerType";
	public static final String OWNERSHIP_CATEGORY = "OwnerShipCategory";
	public static final String DOCUMENT_TYPE = "DocumentType";

	// FINANCIAL YEAR

	public static final String MDMS_EGF_MASTER = "egf-master";

	public static final String MDMS_FINANCIALYEAR = "FinancialYear";

	public static final String MDMS_FINACIALYEAR_PATH = "$.MdmsRes.egf-master.FinancialYear[?(@.code==\"{}\")]";

	public static final String MDMS_STARTDATE = "startingDate";

	public static final String MDMS_ENDDATE = "endingDate";
}
