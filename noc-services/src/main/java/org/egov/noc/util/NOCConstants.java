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
	
	public static final String MODE = "mode";	
	
	public static final String ONLINE_MODE = "online";	
	
	public static final String OFFLINE_MODE = "offline";	
	
	public static final String ONLINE_WF = "onlineWF";	

	public static final String OFFLINE_WF = "offlineWF";
	
	public static final String ACTION_REJECT = "REJECT";	
	
	public static final String WORKFLOWCODE = "workflowCode";	
	
    public static final String NOCTYPE_JSONPATH_CODE = "$.MdmsRes.NOC.NocType";

	
}
