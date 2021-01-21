package org.egov.vendorregistory.web.model.vehicle;

import org.apache.kafka.common.requests.RequestHeader; //Doute in this

import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Request for vehicle details
 */
//@Schema(description = "Request for vehicle details")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-01-06T05:37:21.257Z[GMT]")
public class VehicleRequest {

	@JsonProperty("requestInfo")
	private RequestHeader requestInfo = null;

	@JsonProperty("vehicle")
	private Vehicle vehicle = null;

	public VehicleRequest requestInfo(RequestHeader requestInfo) {
		this.requestInfo = requestInfo;
		return this;
	}

	/**
	 * Get requestInfo
	 * 
	 * @return requestInfo
	 **/
	// @Schema(description = "")
	@Valid
	public RequestHeader getRequestInfo() {
		return requestInfo;
	}

	public void setRequestInfo(RequestHeader requestInfo) {
		this.requestInfo = requestInfo;
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
		VehicleRequest vehicleRequest = (VehicleRequest) o;
		return Objects.equals(this.requestInfo, vehicleRequest.requestInfo)
				&& Objects.equals(this.vehicle, vehicleRequest.vehicle);
	}

	@Override
	public int hashCode() {
		return Objects.hash(requestInfo, vehicle);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class VehicleRequest {\n");

		sb.append("    requestInfo: ").append(toIndentedString(requestInfo)).append("\n");
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
