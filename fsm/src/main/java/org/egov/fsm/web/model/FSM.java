package org.egov.fsm.web.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Request schema of FSM application.  
 */

@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-12-09T07:13:46.742Z[GMT]")

@ApiModel(description = "BPA application object to capture the details of land, land owners, and address of the land.")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id"})
public class FSM   {
  @JsonProperty("citizen")
  private UserInfo citizen = null;

  @JsonProperty("id")
  private String id = null;

  @JsonProperty("tenantId")
  private String tenantId = null;
  @JsonProperty("owners")
  @Valid
  private List<OwnerInfo> owners = new ArrayList<OwnerInfo>();

  @JsonProperty("applicationNo")
  private String applicationNo = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("accountId")
  private String accountId = null;

  @JsonProperty("additionalDetail")
  private Object additionalDetail = null;

  @JsonProperty("applicationStatus")
  private String applicationStatus = null;

  @JsonProperty("source")
  private String source = null;

  @JsonProperty("sanitationtype")
  private String sanitationtype = null;

  @JsonProperty("propertyUsage")
  private String propertyUsage = null;

  @JsonProperty("noOfTrips")
  private Integer noOfTrips = null;
  
  @JsonProperty("distanceOfPit")
  private Integer distanceOfPit;
  
  @JsonProperty("amountPerTrip")
  private Integer amountPerTrip;
  
  @JsonProperty("totalAmountToPay")
  private Integer totalAmountToPay;
  
  @JsonProperty("applicationChannel")
  private String applicationChannel;
  
  
  
  

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
  @ApiModelProperty(required = true, value = "Property owners, these will be citizen users in system.")
  @NotNull
@Valid
public List<OwnerInfo> getOwners() {
return owners;
}

public void setOwners(List<OwnerInfo> owners) {
this.owners = owners;
}
  @JsonProperty("status")
  private StatusEnum status = null;

  @JsonProperty("vehicleId")
  private String vehicleId = null;

  @JsonProperty("pitDetail")
  private PitDetail pitDetail = null;

  @JsonProperty("address")
  private Address address = null;

  @JsonProperty("auditDetails")
  private AuditDetails auditDetails = null;

  public FSM citizen(UserInfo citizen) {
    this.citizen = citizen;
    return this;
  }

  /**
   * Get citizen
   * @return citizen
   **/
  
  
    @Valid
    public UserInfo getCitizen() {
    return citizen;
  }

  public void setCitizen(UserInfo citizen) {
    this.citizen = citizen;
  }

  public FSM id(String id) {
    this.id = id;
    return this;
  }

  /**
   * The server generated unique ID(UUID).
   * @return id
   **/
@ApiModelProperty(readOnly = true, value = "Unique Identifier(UUID) of the bpa application for internal reference.")
  
  @Size(min=1,max=64)  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public FSM tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Unique identifier of the tenant.
   * @return tenantId
   **/
  
  @ApiModelProperty(required = true, value = "Unique ULB identifier.")
  @NotNull

@Size(min=2,max=256)  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public FSM applicationNo(String applicationNo) {
    this.applicationNo = applicationNo;
    return this;
  }

  /**
   * The unique formatted id for application no.
   * @return applicationNo
   **/
  
 @ApiModelProperty(readOnly = true, value = "Generate formatted Unique Identifier of the building permit application. Keep the format in mdms")
  
  @Size(min=1,max=64)    public String getApplicationNo() {
    return applicationNo;
  }

  public void setApplicationNo(String applicationNo) {
    this.applicationNo = applicationNo;
  }

  public FSM description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Additional information or description of the application
   * @return description
   **/
  
  
  @Size(min=2,max=256)   public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public FSM accountId(String accountId) {
    this.accountId = accountId;
    return this;
  }

  /**
   * Applier(CItizen) unique id(UUID) from user service.
   * @return accountId
   **/
@ApiModelProperty(value = "Initiator User UUID")
  
  @Size(min=1,max=64)   public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public FSM additionalDetail(Object additionalDetail) {
    this.additionalDetail = additionalDetail;
    return this;
  }

  /**
   * This is the json object that will carry the actual input (whereever the metadata requries input). Structure should be same as the schema definition provided in the metadata of the service (schema compliance check to be performed at client/server)
   * @return additionalDetail
   **/
  
  
    public Object getAdditionalDetail() {
    return additionalDetail;
  }

  public void setAdditionalDetail(Object additionalDetail) {
    this.additionalDetail = additionalDetail;
  }

  public FSM applicationStatus(String applicationStatus) {
    this.applicationStatus = applicationStatus;
    return this;
  }

  /**
   * The current status of the service request.
   * @return applicationStatus
   **/
  
  
    public String getApplicationStatus() {
    return applicationStatus;
  }

  public void setApplicationStatus(String applicationStatus) {
    this.applicationStatus = applicationStatus;
  }

  public FSM source(String source) {
    this.source = source;
    return this;
  }

  /**
   * Source mdms master data. Which captures the source of the service request(ex:- whatsapp, ivr, Swachhata etc)
   * @return source
   **/
  
  
  @Size(min=2,max=64)   public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public FSM sanitationtype(String sanitationtype) {
    this.sanitationtype = sanitationtype;
    return this;
  }

  /**
   * This is the master data defined in MDMS
   * @return sanitationtype
   **/
  
  
  @Size(min=2,max=64)   public String getSanitationtype() {
    return sanitationtype;
  }

  public void setSanitationtype(String sanitationtype) {
    this.sanitationtype = sanitationtype;
  }

  public FSM propertyUsage(String propertyUsage) {
    this.propertyUsage = propertyUsage;
    return this;
  }

  /**
   * This is the master data defined in MDMS
   * @return propertyUsage
   **/
  
  @NotNull
  @Size(min=2,max=64)  
  public String getPropertyUsage() {
    return propertyUsage;
  }

  public void setPropertyUsage(String propertyUsage) {
    this.propertyUsage = propertyUsage;
  }

  public FSM noOfTrips(Integer noOfTrips) {
    this.noOfTrips = noOfTrips;
    return this;
  }

  /**
   * Total no of trips reqired for desludging the PIT
   * @return noOfTrips
   **/
  
  
    public Integer getNoOfTrips() {
    return noOfTrips;
  }

  public void setNoOfTrips(Integer noOfTrips) {
    this.noOfTrips = noOfTrips;
  }

  public FSM status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   **/
  
  @ApiModelProperty(value = "status of the application.")
    public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public FSM vehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
    return this;
  }

  /**
   * Unique Id of the vehicle which got assigned to the perticular application
   * @return vehicleId
   **/
  
  
  @Size(min=2,max=64)   public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public FSM pitDetail(PitDetail pitDetail) {
    this.pitDetail = pitDetail;
    return this;
  }

  /**
   * Get pitDetail
   * @return pitDetail
   **/
  
  
    @Valid
    public PitDetail getPitDetail() {
    return pitDetail;
  }

  public void setPitDetail(PitDetail pitDetail) {
    this.pitDetail = pitDetail;
  }

  public FSM address(Address address) {
    this.address = address;
    return this;
  }

  /**
   * Get address
   * @return address
   **/
  
  
    @Valid
    public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public FSM auditDetails(AuditDetails auditDetails) {
    this.auditDetails = auditDetails;
    return this;
  }

  /**
   * Get auditDetails
   * @return auditDetails
   **/
  
  
    @Valid
    public AuditDetails getAuditDetails() {
    return auditDetails;
  }

  public void setAuditDetails(AuditDetails auditDetails) {
    this.auditDetails = auditDetails;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FSM FSM = (FSM) o;
    return Objects.equals(this.citizen, FSM.citizen) &&
        Objects.equals(this.id, FSM.id) &&
        Objects.equals(this.tenantId, FSM.tenantId) &&
        Objects.equals(this.applicationNo, FSM.applicationNo) &&
        Objects.equals(this.description, FSM.description) &&
        Objects.equals(this.accountId, FSM.accountId) &&
        Objects.equals(this.additionalDetail, FSM.additionalDetail) &&
        Objects.equals(this.applicationStatus, FSM.applicationStatus) &&
        Objects.equals(this.source, FSM.source) &&
        Objects.equals(this.sanitationtype, FSM.sanitationtype) &&
        Objects.equals(this.propertyUsage, FSM.propertyUsage) &&
        Objects.equals(this.noOfTrips, FSM.noOfTrips) &&
        Objects.equals(this.status, FSM.status) &&
        Objects.equals(this.vehicleId, FSM.vehicleId) &&
        Objects.equals(this.pitDetail, FSM.pitDetail) &&
        Objects.equals(this.address, FSM.address) &&
        Objects.equals(this.auditDetails, FSM.auditDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(citizen, id, tenantId, applicationNo, description, accountId, additionalDetail, applicationStatus, source, sanitationtype, propertyUsage, noOfTrips, status, vehicleId, pitDetail, address, auditDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FSM {\n");
    
    sb.append("    citizen: ").append(toIndentedString(citizen)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    applicationNo: ").append(toIndentedString(applicationNo)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    accountId: ").append(toIndentedString(accountId)).append("\n");
    sb.append("    additionalDetail: ").append(toIndentedString(additionalDetail)).append("\n");
    sb.append("    applicationStatus: ").append(toIndentedString(applicationStatus)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    sanitationtype: ").append(toIndentedString(sanitationtype)).append("\n");
    sb.append("    propertyUsage: ").append(toIndentedString(propertyUsage)).append("\n");
    sb.append("    noOfTrips: ").append(toIndentedString(noOfTrips)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    vehicleId: ").append(toIndentedString(vehicleId)).append("\n");
    sb.append("    pitDetail: ").append(toIndentedString(pitDetail)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    auditDetails: ").append(toIndentedString(auditDetails)).append("\n");
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

public Integer getDistanceOfPit() {
	return distanceOfPit;
}

public void setDistanceOfPit(Integer distanceOfPit) {
	this.distanceOfPit = distanceOfPit;
}

public Integer getAmountPerTrip() {
	return amountPerTrip;
}

public void setAmountPerTrip(Integer amountPerTrip) {
	this.amountPerTrip = amountPerTrip;
}

public Integer getTotalAmountToPay() {
	return totalAmountToPay;
}

public void setTotalAmountToPay(Integer totalAmountToPay) {
	this.totalAmountToPay = totalAmountToPay;
}
@ApiModelProperty(required = true, value = "Application Channel Required .")
@NotNull

@Size(max = 64)
public String getApplicationChannel() {
	return applicationChannel;
}

public void setApplicationChannel(String applicationChannel) {
	this.applicationChannel = applicationChannel;
}
}
