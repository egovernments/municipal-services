/*
 * eChallan System
 * ### API Specs For eChallan System ### 1. Generate the new challan. 2. Update the details of existing challan 3. Search the existing challan 4. Generate the demand and bill for the challan amount so that collection can be done in online and offline mode. 
 *
 * OpenAPI spec version: 1.0.0
 * Contact: contact@egovernments.org
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package org.egov.echallan.model;

import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representation of a address. Indiavidual APIs may choose to extend from this using allOf if more details needed to be added in their case. 
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2020-08-10T16:46:24.044+05:30[Asia/Calcutta]")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {

  @JsonProperty("tenantId")

  private String tenantId = null;

  @JsonProperty("doorNo")

  private String doorNo = null;

  @JsonProperty("plotNo")

  private String plotNo = null;

  @JsonProperty("id")

  private String id = null;

  @JsonProperty("landmark")

  private String landmark = null;
  
  @JsonProperty("latitude")
  private Double latitude = null;

  @JsonProperty("longitude")
  private Double longitude = null;

  @JsonProperty("city")

  private String city = null;

  @JsonProperty("district")

  private String district = null;

  @JsonProperty("region")

  private String region = null;

  @JsonProperty("state")

  private String state = null;

  @JsonProperty("country")

  private String country = null;

  @JsonProperty("pincode")

  private String pincode = null;

  @JsonProperty("additionDetails")

  private String additionDetails = null;

  @JsonProperty("buildingName")

  private String buildingName = null;

  @JsonProperty("street")

  private String street = null;

  @Valid
  @JsonProperty("locality")
  private Boundary locality = null;
  
  @Size(max=64)
  @JsonProperty("detail")
  private String detail = null;

  public String getTenantId() {
    return tenantId;
  }
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }
  public Address doorNo(String doorNo) {
    this.doorNo = doorNo;
    return this;
  }

  
  public String getDoorNo() {
    return doorNo;
  }
  public void setDoorNo(String doorNo) {
    this.doorNo = doorNo;
  }
  public Address plotNo(String plotNo) {
    this.plotNo = plotNo;
    return this;
  }

  
  public String getPlotNo() {
    return plotNo;
  }
  public void setPlotNo(String plotNo) {
    this.plotNo = plotNo;
  }

  public String getId() {
    return id;
  }
  public Address landmark(String landmark) {
    this.landmark = landmark;
    return this;
  }

  public String getLandmark() {
    return landmark;
  }
  public void setLandmark(String landmark) {
    this.landmark = landmark;
  }
  public Address city(String city) {
    this.city = city;
    return this;
  }

  public String getCity() {
    return city;
  }
  public void setCity(String city) {
    this.city = city;
  }
  public Address district(String district) {
    this.district = district;
    return this;
  }

  public String getDistrict() {
    return district;
  }
  public void setDistrict(String district) {
    this.district = district;
  }
  public Address region(String region) {
    this.region = region;
    return this;
  }

  public String getRegion() {
    return region;
  }
  public void setRegion(String region) {
    this.region = region;
  }
  public Address state(String state) {
    this.state = state;
    return this;
  }

  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }
  public Address country(String country) {
    this.country = country;
    return this;
  }

  public String getCountry() {
    return country;
  }
  public void setCountry(String country) {
    this.country = country;
  }
  public Address pincode(String pincode) {
    this.pincode = pincode;
    return this;
  }

  public String getPincode() {
    return pincode;
  }
  public void setPincode(String pincode) {
    this.pincode = pincode;
  }
  public Address additionDetails(String additionDetails) {
    this.additionDetails = additionDetails;
    return this;
  }

  public String getAdditionDetails() {
    return additionDetails;
  }
  public void setAdditionDetails(String additionDetails) {
    this.additionDetails = additionDetails;
  }
  public Address buildingName(String buildingName) {
    this.buildingName = buildingName;
    return this;
  }

  
  public String getBuildingName() {
    return buildingName;
  }
  public void setBuildingName(String buildingName) {
    this.buildingName = buildingName;
  }
  public Address street(String street) {
    this.street = street;
    return this;
  }

  public String getStreet() {
    return street;
  }
  public void setStreet(String street) {
    this.street = street;
  }
  

  

  
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Address address = (Address) o;
    return Objects.equals(this.tenantId, address.tenantId) &&
        Objects.equals(this.doorNo, address.doorNo) &&
        Objects.equals(this.plotNo, address.plotNo) &&
        Objects.equals(this.id, address.id) &&
        Objects.equals(this.landmark, address.landmark) &&
        Objects.equals(this.city, address.city) &&
        Objects.equals(this.district, address.district) &&
        Objects.equals(this.region, address.region) &&
        Objects.equals(this.state, address.state) &&
        Objects.equals(this.country, address.country) &&
        Objects.equals(this.pincode, address.pincode) &&
        Objects.equals(this.additionDetails, address.additionDetails) &&
        Objects.equals(this.buildingName, address.buildingName) &&
        Objects.equals(this.street, address.street) ;
       
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(tenantId, doorNo, plotNo, id, landmark, city, district, region, state, country, pincode, additionDetails, buildingName, street);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Address {\n");
    
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    doorNo: ").append(toIndentedString(doorNo)).append("\n");
    sb.append("    plotNo: ").append(toIndentedString(plotNo)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    landmark: ").append(toIndentedString(landmark)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("    district: ").append(toIndentedString(district)).append("\n");
    sb.append("    region: ").append(toIndentedString(region)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
    sb.append("    pincode: ").append(toIndentedString(pincode)).append("\n");
    sb.append("    additionDetails: ").append(toIndentedString(additionDetails)).append("\n");
    sb.append("    buildingName: ").append(toIndentedString(buildingName)).append("\n");
    sb.append("    street: ").append(toIndentedString(street)).append("\n");
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