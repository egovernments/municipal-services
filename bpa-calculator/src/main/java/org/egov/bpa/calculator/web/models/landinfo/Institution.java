package org.egov.bpa.calculator.web.models.landinfo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Institution {
	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("type")
	private String type;

	@JsonProperty("designation")
	private String designation;

	@JsonProperty("nameOfAuthorizedPerson")
	private String nameOfAuthorizedPerson;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

}
