package org.egov.bpa.web.model.landInfo;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import org.egov.bpa.web.model.AuditDetails;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.Document;
import org.egov.bpa.web.model.Workflow;
import org.egov.bpa.web.model.BPA.BPABuilder;
import org.egov.bpa.web.model.landInfo.Relationship;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * OwnerInfo
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-06-23T05:52:32.717Z[GMT]")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerInfo   {
  @JsonProperty("name")
  private String name = null;
  
  @JsonProperty("Uuid")
  private String Uuid = null;

  @JsonProperty("mobileNumber")
  private String mobileNumber = null;

  @JsonProperty("gender")
  private String gender = null;

  @JsonProperty("fatherOrHusbandName")
  private String fatherOrHusbandName = null;

  @JsonProperty("correspondenceAddress")
  private String correspondenceAddress = null;

  @JsonProperty("isPrimaryOwner")
  private Boolean isPrimaryOwner = null;

  @JsonProperty("ownerShipPercentage")
  private BigDecimal ownerShipPercentage = null;

  @JsonProperty("ownerType")
  private String ownerType = null;

  @JsonProperty("institutionId")
  private String institutionId = null;

  @JsonProperty("documents")
  @Valid
  private List<Document> documents = null;

  @JsonProperty("relationship")
  private Relationship relationship = null;

  @JsonProperty("additionalDetails")
  private Object additionalDetails = null;

  public OwnerInfo name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the owner.
   * @return name
  **/
  @ApiModelProperty(required = true, value = "The name of the owner.")
      @NotNull

  @Size(max=256)   public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  
  public OwnerInfo Uuid(String Uuid) {
	    this.Uuid = Uuid;
	    return this;
	  }

	  /**
	   * The name of the owner.
	   * @return name
	  **/
	  @ApiModelProperty(required = true, value = "Uuid of the user search of owner.")
	      @NotNull

	  @Size(max=256)   public String getUuid() {
	    return Uuid;
	  }

	  public void setUuid(String Uuid) {
	    this.Uuid = Uuid;
	  }
	  
	  
  public OwnerInfo mobileNumber(String mobileNumber) {
    this.mobileNumber = mobileNumber;
    return this;
  }

  /**
   * MobileNumber of the owner.
   * @return mobileNumber
  **/
  @ApiModelProperty(required = true, value = "MobileNumber of the owner.")
      @NotNull

  @Size(max=256)   public String getMobileNumber() {
    return mobileNumber;
  }

  public void setMobileNumber(String mobileNumber) {
    this.mobileNumber = mobileNumber;
  }

  public OwnerInfo gender(String gender) {
    this.gender = gender;
    return this;
  }

  /**
   * Gender of the owner.
   * @return gender
  **/
  @ApiModelProperty(required = true, value = "Gender of the owner.")
      @NotNull

  @Size(max=256)   public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public OwnerInfo fatherOrHusbandName(String fatherOrHusbandName) {
    this.fatherOrHusbandName = fatherOrHusbandName;
    return this;
  }

  /**
   * Father or Husband name of the owner.
   * @return fatherOrHusbandName
  **/
  @ApiModelProperty(required = true, value = "Father or Husband name of the owner.")
      @NotNull

  @Size(max=256)   public String getFatherOrHusbandName() {
    return fatherOrHusbandName;
  }

  public void setFatherOrHusbandName(String fatherOrHusbandName) {
    this.fatherOrHusbandName = fatherOrHusbandName;
  }

  public OwnerInfo correspondenceAddress(String correspondenceAddress) {
    this.correspondenceAddress = correspondenceAddress;
    return this;
  }

  /**
   * The current address of the owner for correspondence.
   * @return correspondenceAddress
  **/
  @ApiModelProperty(value = "The current address of the owner for correspondence.")
  
  @Size(max=1024)   public String getCorrespondenceAddress() {
    return correspondenceAddress;
  }

  public void setCorrespondenceAddress(String correspondenceAddress) {
    this.correspondenceAddress = correspondenceAddress;
  }

  public OwnerInfo isPrimaryOwner(Boolean isPrimaryOwner) {
    this.isPrimaryOwner = isPrimaryOwner;
    return this;
  }

  /**
   * The owner is primary or not
   * @return isPrimaryOwner
  **/
  @ApiModelProperty(value = "The owner is primary or not")
  
    public Boolean isIsPrimaryOwner() {
    return isPrimaryOwner;
  }

  public void setIsPrimaryOwner(Boolean isPrimaryOwner) {
    this.isPrimaryOwner = isPrimaryOwner;
  }

  public OwnerInfo ownerShipPercentage(BigDecimal ownerShipPercentage) {
    this.ownerShipPercentage = ownerShipPercentage;
    return this;
  }

  /**
   * Ownership percentage.
   * @return ownerShipPercentage
  **/
  @ApiModelProperty(value = "Ownership percentage.")
  
    @Valid
    public BigDecimal getOwnerShipPercentage() {
    return ownerShipPercentage;
  }

  public void setOwnerShipPercentage(BigDecimal ownerShipPercentage) {
    this.ownerShipPercentage = ownerShipPercentage;
  }

  public OwnerInfo ownerType(String ownerType) {
    this.ownerType = ownerType;
    return this;
  }

  /**
   * Type of owner, based on this option Exemptions will be applied. This is master data defined in mdms.
   * @return ownerType
  **/
  @ApiModelProperty(value = "Type of owner, based on this option Exemptions will be applied. This is master data defined in mdms.")
  
  @Size(max=256)   public String getOwnerType() {
    return ownerType;
  }

  public void setOwnerType(String ownerType) {
    this.ownerType = ownerType;
  }

  public OwnerInfo institutionId(String institutionId) {
    this.institutionId = institutionId;
    return this;
  }

  /**
   * The id of the institution if the owner is the authorized person for one
   * @return institutionId
  **/
  @ApiModelProperty(value = "The id of the institution if the owner is the authorized person for one")
  
  @Size(max=64)   public String getInstitutionId() {
    return institutionId;
  }

  public void setInstitutionId(String institutionId) {
    this.institutionId = institutionId;
  }

  public OwnerInfo documents(List<Document> documents) {
    this.documents = documents;
    return this;
  }

  public OwnerInfo addDocumentsItem(Document documentsItem) {
    if (this.documents == null) {
      this.documents = new ArrayList<Document>();
    }
    this.documents.add(documentsItem);
    return this;
  }

  /**
   * The documents attached by owner for exemption.
   * @return documents
  **/
  @ApiModelProperty(value = "The documents attached by owner for exemption.")
      @Valid
    public List<Document> getDocuments() {
    return documents;
  }

  public void setDocuments(List<Document> documents) {
    this.documents = documents;
  }

  public OwnerInfo relationship(Relationship relationship) {
    this.relationship = relationship;
    return this;
  }

  /**
   * Get relationship
   * @return relationship
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    @Valid
    public Relationship getRelationship() {
    return relationship;
  }

  public void setRelationship(Relationship relationship) {
    this.relationship = relationship;
  }

  public OwnerInfo additionalDetails(Object additionalDetails) {
    this.additionalDetails = additionalDetails;
    return this;
  }

  /**
   * Json object to capture any extra information which is not accommodated of model
   * @return additionalDetails
  **/
  @ApiModelProperty(value = "Json object to capture any extra information which is not accommodated of model")
  
    public Object getAdditionalDetails() {
    return additionalDetails;
  }

  public void setAdditionalDetails(Object additionalDetails) {
    this.additionalDetails = additionalDetails;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OwnerInfo ownerInfo = (OwnerInfo) o;
    return Objects.equals(this.name, ownerInfo.name) &&
        Objects.equals(this.mobileNumber, ownerInfo.mobileNumber) &&
        Objects.equals(this.gender, ownerInfo.gender) &&
        Objects.equals(this.fatherOrHusbandName, ownerInfo.fatherOrHusbandName) &&
        Objects.equals(this.correspondenceAddress, ownerInfo.correspondenceAddress) &&
        Objects.equals(this.isPrimaryOwner, ownerInfo.isPrimaryOwner) &&
        Objects.equals(this.ownerShipPercentage, ownerInfo.ownerShipPercentage) &&
        Objects.equals(this.ownerType, ownerInfo.ownerType) &&
        Objects.equals(this.institutionId, ownerInfo.institutionId) &&
        Objects.equals(this.documents, ownerInfo.documents) &&
        Objects.equals(this.relationship, ownerInfo.relationship) &&
        Objects.equals(this.additionalDetails, ownerInfo.additionalDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, mobileNumber, gender, fatherOrHusbandName, correspondenceAddress, isPrimaryOwner, ownerShipPercentage, ownerType, institutionId, documents, relationship, additionalDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OwnerInfo {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    mobileNumber: ").append(toIndentedString(mobileNumber)).append("\n");
    sb.append("    gender: ").append(toIndentedString(gender)).append("\n");
    sb.append("    fatherOrHusbandName: ").append(toIndentedString(fatherOrHusbandName)).append("\n");
    sb.append("    correspondenceAddress: ").append(toIndentedString(correspondenceAddress)).append("\n");
    sb.append("    isPrimaryOwner: ").append(toIndentedString(isPrimaryOwner)).append("\n");
    sb.append("    ownerShipPercentage: ").append(toIndentedString(ownerShipPercentage)).append("\n");
    sb.append("    ownerType: ").append(toIndentedString(ownerType)).append("\n");
    sb.append("    institutionId: ").append(toIndentedString(institutionId)).append("\n");
    sb.append("    documents: ").append(toIndentedString(documents)).append("\n");
    sb.append("    relationship: ").append(toIndentedString(relationship)).append("\n");
    sb.append("    additionalDetails: ").append(toIndentedString(additionalDetails)).append("\n");
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
