package org.egov.bpa.web.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.egov.bpa.web.model.landInfo.LandInfo;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	
	@JsonProperty("applicationDate")
	private Long applicationDate;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

	public BPA addDocumentsItem(Document documentsItem) {
		if (this.documents == null) {
			this.documents = new ArrayList<>();
		}
		if (this.docIds == null) {
			this.docIds = new ArrayList<String>();
		}

		if (!this.docIds.contains(documentsItem.getId())) {
			this.documents.add(documentsItem);
			this.docIds.add(documentsItem.getId());
		}

		return this;
	}

}
