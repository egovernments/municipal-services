package org.egov.swCalculation.model;

import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swCalculation.model.DemandNotificationObj.DemandNotificationObjBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationReceiver {

	private String firstName;

	private String lastName;

	private String serviceName;

	private String ulbName;

	private String mobileNumber;

}
