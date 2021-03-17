package org.egov.echallan.util;

import org.springframework.stereotype.Component;

@Component
public class ChallanConstants {

    public static final String STATUS_ACTIVE = "ACTIVE";

    public static final String STATUS_CANCELLED  = "CANCELLED";

    public static final String STATUS_PAID  = "PAID";
    

    public static final String KEY_ID = "id";

    public static final String KEY_FILESTOREID = "filestoreid";

    public static final String KEY_PDF_JOBS = "jobs";

    public static final String KEY_PDF_ENTITY_ID = "entityid";

    public static final String KEY_PDF_FILESTOREID = "filestoreids";
    
    public static final String KEY_NAME = "key";
    
	public static final String GL_CODE_JSONPATH_CODE = "$.MdmsRes.BillingService.GLCode[?(@.code==\"{}\")]";

	public static final String GL_CODE = "glcode";
	
	public static final String GL_CODE_MASTER = "GLCode";

	public static final String BILLING_SERVICE = "BillingService";

    public ChallanConstants() {}

}
