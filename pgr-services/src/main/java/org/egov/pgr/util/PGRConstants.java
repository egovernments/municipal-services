package org.egov.pgr.util;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class PGRConstants {


    public static final String PGR_BUSINESSSERVICE = "PGR";

    public static final String USERTYPE_EMPLOYEE = "EMPLOYEE";

    public static final String USERTYPE_CITIZEN = "CITIZEN";

    public static final String WORKFLOW_START_ACTION = "INITIATE";

    public static final String PGR_WORKFLOW_CODE = "PGR";

    public static final String PGR_MODULENAME = "PGR";

    public static final String MDMS_SERVICEDEF = "ServiceDefs";

    public static final String MDMS_MODULE_NAME = "RAINMAKER-PGR";

    public static final String MDMS_SERVICEDEF_SEARCH = "$.MdmsRes.RAINMAKER-PGR.ServiceDefs[?(@.serviceCode=='{SERVICEDEF}')]";



}
