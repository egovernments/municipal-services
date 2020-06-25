package org.egov.swcalculation.model;

import java.util.Set;

import org.egov.common.contract.request.RequestInfo;

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
