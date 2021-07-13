package org.egov.gcservice.web.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
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
 * Contains the ResponseHeader and the created/updated property
 */
@ApiModel(description = "Contains the ResponseHeader and the created/updated property")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-10-24T10:29:25.253+05:30[Asia/Kolkata]")

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class GarbageConnectionResponse {
	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo = null;

	@JsonProperty("GarbageConnections")
	@Valid
	private List<GarbageConnection> GarbageConnections = null;

	public GarbageConnectionResponse responseInfo(ResponseInfo responseInfo) {
		this.responseInfo = responseInfo;
		return this;
	}

	/**
	 * Get responseInfo
	 * 
	 * @return responseInfo
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public ResponseInfo getResponseInfo() {
		return responseInfo;
	}

	public void setResponseInfo(ResponseInfo responseInfo) {
		this.responseInfo = responseInfo;
	}

	public GarbageConnectionResponse GarbageConnections(List<GarbageConnection> GarbageConnections) {
		this.GarbageConnections = GarbageConnections;
		return this;
	}

	public GarbageConnectionResponse addGarbageConnectionsItem(GarbageConnection GarbageConnectionsItem) {
		if (this.GarbageConnections == null) {
			this.GarbageConnections = new ArrayList<GarbageConnection>();
		}
		this.GarbageConnections.add(GarbageConnectionsItem);
		return this;
	}

	/**
	 * Get GarbageConnections
	 * 
	 * @return GarbageConnections
	 **/
	@ApiModelProperty(value = "")
	@Valid
	public List<GarbageConnection> getGarbageConnections() {
		return GarbageConnections;
	}

	public void setGarbageConnections(List<GarbageConnection> GarbageConnections) {
		this.GarbageConnections = GarbageConnections;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GarbageConnectionResponse garbageConnectionResponse = (GarbageConnectionResponse) o;
		return Objects.equals(this.responseInfo, garbageConnectionResponse.responseInfo)
				&& Objects.equals(this.GarbageConnections, garbageConnectionResponse.GarbageConnections);
	}

	@Override
	public int hashCode() {
		return Objects.hash(responseInfo, GarbageConnections);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class GarbageConnectionResponse {\n");

		sb.append("    responseInfo: ").append(toIndentedString(responseInfo)).append("\n");
		sb.append("    GarbageConnections: ").append(toIndentedString(GarbageConnections)).append("\n");
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
