package org.egov.vendorregistory.web.model;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * Response of Vendor detail
 */
//@Schema(description = "Response of Vendor detail")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-01-06T05:34:12.238Z[GMT]")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorResponse {

	@JsonProperty("responseInfo")
	private ResponseInfo responseInfo = null;

	//@JsonProperty("vendor")
	//private Vendor vendor = null;
	
	 @JsonProperty("vendor")
	 private List<Vendor> vendor = null;
	 
	 

	public VendorResponse responseInfo(ResponseInfo responseInfo) {
		this.responseInfo = responseInfo;
		return this;
	}

	/**
	 * Get responseInfo
	 * 
	 * @return responseInfo
	 **/
	// @Schema(description = "")
	@Valid
	public ResponseInfo getResponseInfo() {
		return responseInfo;
	}

	public void setResponseInfo(ResponseInfo responseInfo) {
		this.responseInfo = responseInfo;
	}

	public VendorResponse vendor(List<Vendor> vendor) {
		this.vendor = vendor;
		return this;
	}

	/**
	 * Get vendor
	 * 
	 * @return vendor
	 **/
	// @Schema(description = "")
	@Valid
	public List<Vendor> getVendor() {
		return vendor;
	}

	public void setVendor(List<Vendor> vendor) {
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
		VendorResponse vendorResponse = (VendorResponse) o;
		return Objects.equals(this.responseInfo, vendorResponse.responseInfo)
				&& Objects.equals(this.vendor, vendorResponse.vendor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(responseInfo, vendor);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class VendorResponse {\n");

		sb.append("    responseInfo: ").append(toIndentedString(responseInfo)).append("\n");
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
