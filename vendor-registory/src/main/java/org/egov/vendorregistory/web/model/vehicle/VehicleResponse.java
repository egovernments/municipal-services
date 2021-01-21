package org.egov.vendorregistory.web.model.vehicle;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;
import java.util.Objects;
import javax.validation.Valid;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response of vehicle detail
 */
//@Schema(description = "Response of vehicle detail")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-01-06T05:37:21.257Z[GMT]")
public class VehicleResponse {

	@JsonProperty("responseInfo")
	private ResponseInfo responseInfo = null;

	@JsonProperty("vehicle")
	private Vehicle vehicle = null;

	public VehicleResponse responseInfo(ResponseInfo responseInfo) {
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

	public VehicleResponse vehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
		return this;
	}

	/**
	 * Get vehicle
	 * 
	 * @return vehicle
	 **/
	//@Schema(description = "")
	@Valid
	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		VehicleResponse vehicleResponse = (VehicleResponse) o;
		return Objects.equals(this.responseInfo, vehicleResponse.responseInfo)
				&& Objects.equals(this.vehicle, vehicleResponse.vehicle);
	}

	@Override
	public int hashCode() {
		return Objects.hash(responseInfo, vehicle);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class VehicleResponse {\n");

		sb.append("    responseInfo: ").append(toIndentedString(responseInfo)).append("\n");
		sb.append("    vehicle: ").append(toIndentedString(vehicle)).append("\n");
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
