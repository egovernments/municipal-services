package org.egov.wsCalculation.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;

import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
/**
 * This is lightweight meter reading object that can be used as reference by definitions needing meterreading linking.
 */
@ApiModel(description = "This is lightweight meter reading object that can be used as reference by definitions needing meterreading linking.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-12T10:46:14.653+05:30[Asia/Kolkata]")
public class MeterReading   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("billingPeriod")
  private String billingPeriod = null;

  /**
   * Gets or Sets meterStatus
   */
  public enum MeterStatusEnum {
    WORKING("Working"),
    
    LOCKED("Locked"),
    
    BREAKDOWN("Breakdown"),
    
    NO_METER("No meter"),
    
    RESET("Reset"),
    
    REPLACEMENT("Replacement");

    private String value;

    MeterStatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static MeterStatusEnum fromValue(String text) {
      for (MeterStatusEnum b : MeterStatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("meterStatus")
  private MeterStatusEnum meterStatus = null;

  @JsonProperty("lastReading")
  private Integer lastReading = null;

  @JsonProperty("lastReadingDate")
  private Long lastReadingDate = null;

  @JsonProperty("currentReading")
  private Integer currentReading = null;

  @JsonProperty("currentReadingDate")
  private Long currentReadingDate = null;

  public MeterReading id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Unique Identifier of the meterreading for internal reference.
   * @return id
  **/
  @ApiModelProperty(readOnly = true, value = "Unique Identifier of the meterreading for internal reference.")

@Size(min=1,max=64)   public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public MeterReading billingPeriod(String billingPeriod) {
    this.billingPeriod = billingPeriod;
    return this;
  }

  /**
   * Formatted billingPeriod
   * @return billingPeriod
  **/
  @ApiModelProperty(required = true, readOnly = true, value = "Formatted billingPeriod")
  @NotNull

@Size(min=1,max=64)   public String getBillingPeriod() {
    return billingPeriod;
  }

  public void setBillingPeriod(String billingPeriod) {
    this.billingPeriod = billingPeriod;
  }

  public MeterReading meterStatus(MeterStatusEnum meterStatus) {
    this.meterStatus = meterStatus;
    return this;
  }

  /**
   * Get meterStatus
   * @return meterStatus
  **/
  @ApiModelProperty(required = true, readOnly = true, value = "")
  @NotNull

@Size(min=1,max=64)   public MeterStatusEnum getMeterStatus() {
    return meterStatus;
  }

  public void setMeterStatus(MeterStatusEnum meterStatus) {
    this.meterStatus = meterStatus;
  }

  public MeterReading lastReading(Integer lastReading) {
    this.lastReading = lastReading;
    return this;
  }

  /**
   * Last Reading
   * @return lastReading
  **/
  @ApiModelProperty(required = true, value = "Last Reading")
  @NotNull

  public Integer getLastReading() {
    return lastReading;
  }

  public void setLastReading(Integer lastReading) {
    this.lastReading = lastReading;
  }

  public MeterReading lastReadingDate(Long lastReadingDate) {
    this.lastReadingDate = lastReadingDate;
    return this;
  }

  /**
   * The date of meter last reading date.
   * @return lastReadingDate
  **/
  @ApiModelProperty(required = true, value = "The date of meter last reading date.")
  @NotNull

  public Long getLastReadingDate() {
    return lastReadingDate;
  }

  public void setLastReadingDate(Long lastReadingDate) {
    this.lastReadingDate = lastReadingDate;
  }

  public MeterReading currentReading(Integer currentReading) {
    this.currentReading = currentReading;
    return this;
  }

  /**
   * Current Reading
   * @return currentReading
  **/
  @ApiModelProperty(required = true, value = "Current Reading")
  @NotNull

  public Integer getCurrentReading() {
    return currentReading;
  }

  public void setCurrentReading(Integer currentReading) {
    this.currentReading = currentReading;
  }

  public MeterReading currentReadingDate(Long currentReadingDate) {
    this.currentReadingDate = currentReadingDate;
    return this;
  }

  /**
   * The date of meter current reading date.
   * @return currentReadingDate
  **/
  @ApiModelProperty(required = true, value = "The date of meter current reading date.")
  @NotNull

  public Long getCurrentReadingDate() {
    return currentReadingDate;
  }

  public void setCurrentReadingDate(Long currentReadingDate) {
    this.currentReadingDate = currentReadingDate;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MeterReading meterReading = (MeterReading) o;
    return Objects.equals(this.id, meterReading.id) &&
        Objects.equals(this.billingPeriod, meterReading.billingPeriod) &&
        Objects.equals(this.meterStatus, meterReading.meterStatus) &&
        Objects.equals(this.lastReading, meterReading.lastReading) &&
        Objects.equals(this.lastReadingDate, meterReading.lastReadingDate) &&
        Objects.equals(this.currentReading, meterReading.currentReading) &&
        Objects.equals(this.currentReadingDate, meterReading.currentReadingDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, billingPeriod, meterStatus, lastReading, lastReadingDate, currentReading, currentReadingDate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MeterReading {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    billingPeriod: ").append(toIndentedString(billingPeriod)).append("\n");
    sb.append("    meterStatus: ").append(toIndentedString(meterStatus)).append("\n");
    sb.append("    lastReading: ").append(toIndentedString(lastReading)).append("\n");
    sb.append("    lastReadingDate: ").append(toIndentedString(lastReadingDate)).append("\n");
    sb.append("    currentReading: ").append(toIndentedString(currentReading)).append("\n");
    sb.append("    currentReadingDate: ").append(toIndentedString(currentReadingDate)).append("\n");
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
