package org.egov.swService.util;

public class SWConstants {

	private SWConstants() {

	}

	public static final String JSONPATH_ROOT = "$.MdmsRes.ws-services-masters";

	public static final String JSONPATH_CODE_CONNECTION_TYPE = "connectionType.code";

	public static final String MDMS_SW_MOD_NAME = "ws-services-masters";

	public static final String MDMS_SW_Connection_Type = "connectionType";

	public static final String INVALID_CONNECTION_TYPE = "Invalid Connection Type";
   
	 // TL actions

    public static final String ACTION_INITIATE = "INITIATE";

    public static final String ACTION_APPLY  = "APPLY";

    public static final String ACTION_APPROVE  = "APPROVE";

    public static final String ACTION_REJECT  = "REJECT";

    public static final String ACTION_CANCEL  = "CANCEL";

    public static final String ACTION_PAY  = "PAY";

    public static final String ACTION_ADHOC  = "ADHOC";


    public static final String STATUS_INITIATED = "INITIATED";

    public static final String STATUS_APPLIED  = "APPLIED";

    public static final String STATUS_APPROVED  = "APPROVED";

    public static final String STATUS_REJECTED  = "REJECTED";

    public static final String STATUS_FIELDINSPECTION  = "FIELDINSPECTION";

    public static final String STATUS_CANCELLED  = "CANCELLED";

    public static final String STATUS_PAID  = "PAID";
}
