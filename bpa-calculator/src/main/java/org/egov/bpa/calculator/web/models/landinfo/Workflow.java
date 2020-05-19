package org.egov.bpa.calculator.web.models.landinfo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Workflow {
	 @JsonProperty("action")
	  private String action;

	  @JsonProperty("assignes")
	  private List<String> assignes;

	  @JsonProperty("comments")
	  private String comments;

	  @JsonProperty("varificationDocuments")
	  private List<Document> varificationDocuments;

}
