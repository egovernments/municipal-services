package org.egov.fsm.web.model;

import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.egov.common.contract.request.User;
import org.egov.fsm.web.model.AuditDetails;
import org.egov.fsm.web.model.location.Address;
import org.egov.fsm.web.model.location.Boundary;
import org.egov.fsm.web.model.location.Boundary.BoundaryBuilder;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * cature the pit details 
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-12-23T12:08:13.326Z[GMT]")

@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@ToString
@Builder
@Getter
@Setter
public class PitDetail   {
	
  @JsonProperty("type")
  private String type = null;

  @JsonProperty("id")
  private String id = null;

  @JsonProperty("tenantId")
  private String tenantId = null;
  
  @JsonProperty("height")
  private Double height = null;

  @JsonProperty("length")
  private Double length = null;

  @JsonProperty("width")
  private Double width = null;

  @JsonProperty("distanceFromRoad")
  private Double distanceFromRoad = null;

  @JsonProperty("auditDetails")
  private AuditDetails auditDetails = null;
}
