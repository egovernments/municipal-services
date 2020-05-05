package org.egov.bpa.web.model;

import java.util.List;

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
public class Workflow {
	 @JsonProperty("action")
	  private String action = null;

	  @JsonProperty("assignes")
	  private List<String> assignes = null;

	  @JsonProperty("comments")
	  private String comments = null;

	  @JsonProperty("varificationDocuments")
	  private List<Document> varificationDocuments = null;

}
