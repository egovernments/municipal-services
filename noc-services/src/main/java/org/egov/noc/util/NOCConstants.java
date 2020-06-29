package org.egov.noc.util;

import org.springframework.stereotype.Component;

@Component
public class NOCConstants {

	public static final String SEARCH_MODULE = "rainmaker-nocsrv";
	
	public static final String NOC_MODULE = "NOC";
	
	public static final String NOC_TYPE = "NocType";
	
	// mdms path codes

    public static final String NOC_JSONPATH_CODE = "$.MdmsRes.NOC";

    // error constants

	public static final String INVALID_TENANT_ID_MDMS_KEY = "INVALID TENANTID";

	public static final String INVALID_TENANT_ID_MDMS_MSG = "No data found for this tenentID";

	public static final String APPROVED_STATE = "APPROVED";	
	
	public static final String ACTION_APPROVE = "APPROVE";	

}
