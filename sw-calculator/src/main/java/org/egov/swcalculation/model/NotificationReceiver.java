package org.egov.swcalculation.model;

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
