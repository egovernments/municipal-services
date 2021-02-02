package org.egov.vehiclelog.web.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.fsm.web.model.AuditDetails;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.dso.Vendor;
import org.egov.fsm.web.model.vehicle.Vehicle;
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
 * Request schema of VehicleLog.  
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-12-23T12:08:13.326Z[GMT]")

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VehicleLog   {

  @JsonProperty("id")
  private String id = null;

  @JsonProperty("tenantId")
  private String tenantId = null;

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

  @JsonProperty("applicationStatus")
  private String applicationStatus = null;
  
  @JsonProperty("vehicleId")
  private String vehicleId = null;
  
  @JsonProperty("dsoId")
  private String dsoId = null;

  @JsonProperty("wasteDumped")
  private Integer wasteDumped = null;

  @JsonProperty("dumpTime")
  private Long dumpTime = null;
  
  @JsonProperty("auditDetails")
  private AuditDetails auditDetails = null;
  
  @JsonProperty("fsms")
  @Valid
  private List<FSM> fsms = new ArrayList<FSM>();
  
  @JsonProperty("dso")
  private Vendor dso;
  
  @JsonProperty("vehicle")
  private Vehicle vehicle;

}

