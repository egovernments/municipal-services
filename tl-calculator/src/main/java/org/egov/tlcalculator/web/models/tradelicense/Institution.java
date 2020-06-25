package org.egov.tlcalculator.web.models.tradelicense;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Institution {

  @Size(max=64)
  @JsonProperty("id")
  private String id;

  @Size(max=256)
  @JsonProperty("tenantId")
  private String tenantId;

  @Size(max=64)
  @JsonProperty("name")
  private String name;

  @Size(max=64)
  @JsonProperty("type")
  private String type;

  @Size(max=64)
  @JsonProperty("designation")
  private String designation;

  @JsonProperty("active")
  private Boolean active = null;

  @Size(max=256)
  @JsonProperty("instituionName")
  private String instituionName;

  @Size(max=64)
  @JsonProperty("contactNo")
  private String contactNo;


  @Size(max=64)
  @JsonProperty("organisationRegistrationNo")
  private String organisationRegistrationNo;

  @Size(max=512)
  @JsonProperty("address")
  private String address;
}
