package org.egov.swCalculation.util;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.swCalculation.config.SWCalculationConfiguration;
import org.egov.swCalculation.constants.SWCalculationConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class CalculatorUtils {
	
	@Autowired
	SWCalculationConfiguration configurations;
	
	/**
	 * Prepares and returns Mdms search request with financial master criteria
	 *
	 * @param requestInfo
	 * @param assesmentYears
	 * @return
	 */
	public MdmsCriteriaReq getFinancialYearRequest(RequestInfo requestInfo, Set<String> assesmentYears,
			String tenantId) {

		String assessmentYearStr = StringUtils.join(assesmentYears, ",");
		MasterDetail mstrDetail = MasterDetail.builder().name(SWCalculationConstant.FINANCIAL_YEAR_MASTER)
				.filter("[?(@." + SWCalculationConstant.FINANCIAL_YEAR_RANGE_FEILD_NAME + " IN [" + assessmentYearStr
						+ "]" + " && @.module== '" + SWCalculationConstant.SERVICE_FIELD_VALUE_WS + "')]")
				.build();
		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(SWCalculationConstant.FINANCIAL_MODULE)
				.masterDetails(Arrays.asList(mstrDetail)).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(moduleDetail)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}
	
	
	/**
	 * Returns the url for mdms search endpoint
	 *
	 * @return
	 */
	public StringBuilder getMdmsSearchUrl() {
		return new StringBuilder().append(configurations.getMdmsHost()).append(configurations.getMdmsEndPoint());
	}

}
