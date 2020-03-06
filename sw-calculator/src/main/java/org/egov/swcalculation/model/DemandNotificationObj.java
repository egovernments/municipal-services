package org.egov.swcalculation.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.swcalculation.model.Demand.DemandBuilder;
import org.egov.swcalculation.model.Demand.StatusEnum;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandNotificationObj {

	private String billingCycle;

	private Set<String> sewerageConnetionIds;

	private boolean isSuccess;

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo = null;

	private String tenantId;

}
