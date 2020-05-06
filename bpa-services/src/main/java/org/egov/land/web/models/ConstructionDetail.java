package org.egov.land.web.models;

import java.math.BigDecimal;

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
public class ConstructionDetail {
	@JsonProperty("id")
	  private String id;

	  @JsonProperty	("carpetArea")
	  private BigDecimal carpetArea;

	  @JsonProperty("builtUpArea")
	  private BigDecimal builtUpArea;

	  @JsonProperty("plinthArea")
	  private BigDecimal plinthArea;

	  @JsonProperty("superBuiltUpArea")
	  private BigDecimal superBuiltUpArea;

	  @JsonProperty("constructionType")
	  private String constructionType;

	  @JsonProperty("constructionDate")
	  private Long constructionDate;

	  @JsonProperty("dimensions")
	  private Object dimensions;

	  @JsonProperty("auditDetails")
	  private AuditDetails auditDetails;

	  @JsonProperty("additionalDetails")
	  private Object additionalDetails;

}
