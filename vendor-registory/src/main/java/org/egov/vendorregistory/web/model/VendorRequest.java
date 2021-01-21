package org.egov.vendorregistory.web.model;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
/**
 * Request for Vendor registartion
 */
//@Schema(description = "Request for Vendor registartion")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-01-06T05:34:12.238Z[GMT]")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorRequest {

	 @JsonProperty("RequestInfo")
	  private RequestInfo RequestInfo = null;

	@JsonProperty("vendor")
	private Vendor vendor = null;

	public VendorRequest requestInfo(RequestInfo requestInfo) {
		this.RequestInfo = requestInfo;
		return this;
	}

	/**
	 * Get requestInfo
	 * 
	 * @return requestInfo
	 **/
	//@Schema(required = true, description = "")
	@NotNull
	@Valid
	public RequestInfo getRequestInfo() {
		return RequestInfo;
	}

	public void setRequestInfo(RequestInfo requestInfo) {
		this.RequestInfo = requestInfo;
	}

	public VendorRequest vendor(Vendor vendor) {
		this.vendor = vendor;
		return this;
	}

	/**
	 * Get vendor
	 * 
	 * @return vendor
	 **/
	//@Schema(required = true, description = "")
	@NotNull

	@Valid
	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		VendorRequest vendorRequest = (VendorRequest) o;
		return Objects.equals(this.RequestInfo, vendorRequest.RequestInfo)
				&& Objects.equals(this.vendor, vendorRequest.vendor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(RequestInfo, vendor);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class VendorRequest {\n");

		sb.append("    requestInfo: ").append(toIndentedString(RequestInfo)).append("\n");
		sb.append("    vendor: ").append(toIndentedString(vendor)).append("\n");
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
