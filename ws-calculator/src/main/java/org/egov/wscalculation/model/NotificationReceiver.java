package org.egov.wscalculation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationReceiver {

	private String firstName;
	private String lastName;
	private String serviceName;
	private String ulbName;
	private String mobileNumber;
	
}
