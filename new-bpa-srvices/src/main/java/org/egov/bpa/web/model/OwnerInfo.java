package org.egov.bpa.web.model;

import java.math.BigDecimal;
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
public class OwnerInfo {
	 @JsonProperty("name")
	  private String name;

	  @JsonProperty("mobileNumber")
	  private String mobileNumber;

	  @JsonProperty("gender")
	  private String gender;

	  @JsonProperty("fatherOrHusbandName")
	  private String fatherOrHusbandName;

	  @JsonProperty("correspondenceAddress")
	  private String correspondenceAddress;

	  @JsonProperty("isPrimaryOwner")
	  private Boolean isPrimaryOwner;

	  @JsonProperty("ownerShipPercentage")
	  private BigDecimal ownerShipPercentage;

	  @JsonProperty("ownerType")
	  private String ownerType;

	  @JsonProperty("institutionId")
	  private String institutionId;

	  @JsonProperty("documents")
	  private List<Document> documents;

	  @JsonProperty("relationship")
	  private Relationship relationship;

	  @JsonProperty("additionalDetails")
	  private Object additionalDetails;

}
