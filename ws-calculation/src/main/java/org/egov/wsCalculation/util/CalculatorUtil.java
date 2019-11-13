package org.egov.wsCalculation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class CalculatorUtil {

	/**
	 * Methods provides all the usage category master for Water Service module
	 */
	public MdmsCriteriaReq getPropertyModuleRequest(RequestInfo requestInfo, String tenantId) {
		List<MasterDetail> details = new ArrayList<>();
		details.add(MasterDetail.builder().name(WSCalculationConfiguration.WC_REBATE_MASTER).build());
		details.add(MasterDetail.builder().name(WSCalculationConfiguration.WC_WATER_CESS_MASTER).build());
		details.add(MasterDetail.builder().name(WSCalculationConfiguration.WC_PENANLTY_MASTER).build());
		details.add(MasterDetail.builder().name(WSCalculationConfiguration.WC_INTEREST_MASTER).build());
		ModuleDetail mdDtl = ModuleDetail.builder().masterDetails(details)
				.moduleName(WSCalculationConfiguration.WS_TAX_MODULE).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(mdDtl)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}
	
	/**
	 * Returns the url for mdms search endpoint
	 *
	 * @return
	 */
	public StringBuilder getMdmsSearchUrl() {
		return new StringBuilder().append(WSCalculationConfiguration.getMdmsHost()).append(WSCalculationConfiguration.getMdmsEndpoint());
	}

}
