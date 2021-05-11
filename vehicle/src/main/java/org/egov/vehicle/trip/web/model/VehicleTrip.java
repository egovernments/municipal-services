package org.egov.vehicle.trip.web.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.common.contract.request.User;
import org.egov.vehicle.web.model.AuditDetails;
import org.egov.vehicle.web.model.Vehicle;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request schema of VehicleTrip.  
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-12-23T12:08:13.326Z[GMT]")

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VehicleTrip   {

  @SafeHtml
  @JsonProperty("id")
  private String id = null;

  @NotNull
  @NotBlank
  @SafeHtml
  @Size(max=64)
  @JsonProperty("tenantId")
  private String tenantId = null;
  
  @JsonProperty("tripOwner")
  @Valid
  private User tripOwner = null;
  
  @SafeHtml
  @JsonProperty("tripOwnerId")
  private String tripOwnerId = null;
  
  @JsonProperty("driver")
  @Valid
  private User driver = null;
  
  @JsonProperty("driverId")
  @SafeHtml
  @Size(max=64)
  private String driverId = null;
  

  @NotNull
  @JsonProperty("vehicle")
  @Valid
  private Vehicle vehicle;
  
  @SafeHtml
  @JsonProperty("vehicleId")
  private String vehicleId = null;

  @SafeHtml
  @JsonProperty("applicationNo")
  private String applicationNo = null;
  
  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    ACTIVE("ACTIVE"),
    
    INACTIVE("INACTIVE");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("status")
  private StatusEnum status = null;

  @NotNull
  @NotBlank
  @SafeHtml
  @JsonProperty("businessService")
  private String businessService = null;

  @SafeHtml
  @JsonProperty("applicationStatus")
  private String applicationStatus = null;


  @JsonProperty("additionalDetails")
  private Object additionalDetails = null;
  
  @NotNull
  @NotEmpty
  @Valid
  @JsonProperty("tripDetails")
  private List<VehicleTripDetail> tripDetails = null;


  @JsonProperty("tripStartTime")
  private Long tripStartTime = null;
  
  @JsonProperty("tripEndTime")
  private Long tripEndTime = null;
  
  @JsonProperty("volumeCarried")
  private Double volumeCarried = null;
  
  @JsonProperty("auditDetails")
  private AuditDetails auditDetails = null;
  
  @JsonProperty("fstpEntryTime")
  private Long fstpEntryTime = null;
  
  @JsonProperty("fstpExitTime")
  private Double fstpExitTime = null;   

}

