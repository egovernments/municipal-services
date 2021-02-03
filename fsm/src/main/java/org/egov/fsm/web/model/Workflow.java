package org.egov.fsm.web.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * BPA application object to capture the details of land, land owners, and address of the land.
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-12-23T12:08:13.326Z[GMT]")

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Workflow   {
  @JsonProperty("action")
  private String action = null;

  @JsonProperty("assignes")
  @Valid
  private List<String> assignes = null;

  @JsonProperty("comments")
  private String comments = null;

  @JsonProperty("verificationDocuments")
  @Valid
  private List<Document> verificationDocuments = null;

  public Workflow action(String action) {
    this.action = action;
    return this;
  }

  /**
   * Action on the application in certain
   * @return action
   **/
  
  @Size(min=1,max=64)   public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public Workflow assignes(List<String> assignes) {
    this.assignes = assignes;
    return this;
  }

  public Workflow addAssignesItem(String assignesItem) {
    if (this.assignes == null) {
      this.assignes = new ArrayList<String>();
    }
    this.assignes.add(assignesItem);
    return this;
  }

  /**
   * Get assignes
   * @return assignes
   **/
  
    public List<String> getAssignes() {
    return assignes;
  }

  public void setAssignes(List<String> assignes) {
    this.assignes = assignes;
  }

  public Workflow comments(String comments) {
    this.comments = comments;
    return this;
  }

  /**
   * Unique Identifier scrutinized number
   * @return comments
   **/
  
  @Size(min=1,max=64)   public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public Workflow varificationDocuments(List<Document> verificationDocuments) {
    this.verificationDocuments = verificationDocuments;
    return this;
  }

  public Workflow addVerificationDocumentsItem(Document verificationDocuments) {
    if (this.verificationDocuments == null) {
      this.verificationDocuments = new ArrayList<Document>();
    }
    this.verificationDocuments.add(verificationDocuments);
    return this;
  }

  /**
   * Attach the workflow varification documents.
   * @return verificationDocuments
   **/
      @Valid
    public List<Document> getVerificationDocuments() {
    return verificationDocuments;
  }

  public void setVerificationDocuments(List<Document> verificationDocuments) {
    this.verificationDocuments = verificationDocuments;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Workflow workflow = (Workflow) o;
    return Objects.equals(this.action, workflow.action) &&
        Objects.equals(this.assignes, workflow.assignes) &&
        Objects.equals(this.comments, workflow.comments) &&
        Objects.equals(this.verificationDocuments, workflow.verificationDocuments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(action, assignes, comments, verificationDocuments);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Workflow {\n");
    
    sb.append("    action: ").append(toIndentedString(action)).append("\n");
    sb.append("    assignes: ").append(toIndentedString(assignes)).append("\n");
    sb.append("    comments: ").append(toIndentedString(comments)).append("\n");
    sb.append("    verificationDocuments: ").append(toIndentedString(verificationDocuments)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
