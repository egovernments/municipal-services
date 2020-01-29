package org.egov.waterConnection.constants;

public class WCConstants {

	private WCConstants() {

	}
	
	public static final String JSONPATH_ROOT = "$.MdmsRes.ws-services-masters";
	

	public static final String JSONPATH_CODE_CONNECTION_CATEGORY = "connectionCategory.code";
	
	public static final String JSONPATH_CODE_CONNECTION_TYPE = "connectionType.code";
	
	public static final String JSONPATH_CODE_WATER_SOURCE= "waterSource.code";

	public static final String MDMS_WC_MOD_NAME = "ws-services-masters";

	public static final String MDMS_WC_Connection_Category = "connectionCategory";

	public static final String MDMS_WC_Connection_Type = "connectionType";

	public static final String MDMS_WC_Water_Source = "waterSource";

	public static final String INVALID_CONNECTION_CATEGORY = "Invalid Connection Category";

	public static final String INVALID_CONNECTION_TYPE = "Invalid Connection Type";
	
	public static final String METERED_CONNECTION = "Metered";
	
	  // WS actions

    public static final String ACTION_INITIATE = "INITIATE";

    public static final String ACTION_APPLY  = "APPLY";

    public static final String ACTION_APPROVE  = "ACTIVATE_CONNECTION";

    public static final String ACTION_REJECT  = "REJECT";

    public static final String ACTION_CANCEL  = "CANCEL";

    public static final String ACTION_PAY  = "PAY";



    public static final String STATUS_INITIATED = "INITIATED";

    public static final String STATUS_APPLIED  = "APPLIED";

    public static final String STATUS_APPROVED  = "CONNECTION_ACTIVATED";

    public static final String STATUS_REJECTED  = "REJECTED";

    public static final String STATUS_FIELDINSPECTION  = "FIELDINSPECTION";

    public static final String STATUS_CANCELLED  = "CANCELLED";

    public static final String STATUS_PAID  = "PAID";

}
