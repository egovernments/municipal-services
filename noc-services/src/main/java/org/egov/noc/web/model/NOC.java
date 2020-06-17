package org.egov.noc.web.model;

import javax.validation.constraints.NotNull;
import org.egov.noc.web.model.enums.ApplicationType;
import org.egov.noc.web.model.enums.Status;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NOC {
   
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("applicationNo")
	private String applicationNo;
	
	@JsonProperty("nocNo")
	private String nocNo;
	
	@JsonProperty("applicationType")
	private ApplicationType applicationType;
	
	@JsonProperty("nocType")
	private String nocType;
	
	@JsonProperty("accountId")
	private String accountId;
	
	@JsonProperty("source")
	private String source;
	
	@JsonProperty("sourceRefId")
	private String sourceRefId;
	
	@JsonProperty("landId")
	private String landId;
	
	@JsonProperty("status")
	private Status status;
	
	@JsonProperty("applicationStatus")
	private String applicationStatus;
	
	@NotNull
	@JsonProperty("tenantId")
	private String tenantId;
	
	@JsonProperty("workflow")
	private Workflow workflow;
	
	@JsonProperty("applicationDate")
	private Long applicationDate;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;
	
}
