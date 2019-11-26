package org.egov.bpa.util;

import org.springframework.stereotype.Component;

@Component
public class BPAConstants {

	

    // MDMS

    public static final String BPA_MODULE = "BPA";

    public static final String BPA_MODULE_CODE = "BP";

    public static final String COMMON_MASTERS_MODULE = "common-masters";


    // mdms master names

    public static final String APPLICATION_TYPE = "ApplicationType";
    
	
	
    //FINANCIAL YEAR

    public static final String MDMS_EGF_MASTER = "egf-master";

    public static final String MDMS_FINANCIALYEAR  = "FinancialYear";

    public static final String MDMS_FINACIALYEAR_PATH = "$.MdmsRes.egf-master.FinancialYear[?(@.code==\"{}\")]";

    public static final String MDMS_STARTDATE  = "startingDate";

    public static final String MDMS_ENDDATE  = "endingDate";
}
