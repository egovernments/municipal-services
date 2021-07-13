package org.egov.gcservice.web.models;

import java.util.Objects;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contract class to receive request. Array of Property items are used in case
 * of create . Where as single Property item is used for update
 */
@ApiModel(description = "Contract class to receive request. Array of Property items  are used in case of create . Where as single Property item is used for update")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-10-24T10:29:25.253+05:30[Asia/Kolkata]")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class GarbageConnectionRequest {
	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo = null;

	@JsonProperty("GarbageConnection")
	private GarbageConnection garbageConnection = null;

	public GarbageConnectionRequest requestInfo(RequestInfo requestInfo) {
		this.requestInfo = requestInfo;
		return this;
	}

	/**
	 * Get requestInfo
	 * 
	 * @return requestInfo
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public RequestInfo getRequestInfo() {
		return requestInfo;
	}

	public void setRequestInfo(RequestInfo requestInfo) {
		this.requestInfo = requestInfo;
	}

	public GarbageConnectionRequest garbageConnection(GarbageConnection garbageConnection) {
		this.garbageConnection = garbageConnection;
		return this;
	}

	/**
	 * Get GarbageConnection
	 * 
	 * @return GarbageConnection
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public GarbageConnection getGarbageConnection() {
		return garbageConnection;
	}

	public void setGarbageConnection(GarbageConnection garbageConnection) {
		this.garbageConnection = garbageConnection;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GarbageConnectionRequest garbageConnectionRequest = (GarbageConnectionRequest) o;
		return Objects.equals(this.requestInfo, garbageConnectionRequest.requestInfo)
				&& Objects.equals(this.garbageConnection, garbageConnectionRequest.garbageConnection);
	}

	@Override
	public int hashCode() {
		return Objects.hash(requestInfo, garbageConnection);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class GarbageConnectionRequest {\n");

		sb.append("    requestInfo: ").append(toIndentedString(requestInfo)).append("\n");
		sb.append("    GarbageConnection: ").append(toIndentedString(garbageConnection)).append("\n");
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
