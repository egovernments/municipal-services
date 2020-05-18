package org.egov.bpa.calculator.web.models.bpa;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.egov.bpa.calculator.web.models.AuditDetails;
import org.egov.bpa.calculator.web.models.Document;
import org.egov.bpa.calculator.web.models.landinfo.LandInfo;
import org.egov.bpa.calculator.web.models.landinfo.Workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BPA {

	@JsonIgnore
	private ArrayList<String> docIds;

	@JsonProperty("id")
	private String id;

	@JsonProperty("applicationNo")
	private String applicationNo;

	@JsonProperty("approvalNo")
	private String approvalNo;

	@JsonProperty("accountId")
	private String accountId;

	@JsonProperty("edcrNumber")
	private String edcrNumber;

	@JsonProperty("riskType")
	private String riskType;

	@JsonProperty("landId")
	private String landId;

	@NotNull
	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("status")
	private String status;

	@JsonProperty("documents")
	private List<Document> documents;

	@JsonProperty("landInfo")
	private LandInfo landInfo;

	@JsonProperty("approvalDate")
	private Long approvalDate;

	@JsonProperty("workflow")
	private Workflow workflow;

	@JsonProperty("businessService")
	private String businessService;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;
}
