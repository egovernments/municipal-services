package org.egov.waterConnection.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaterConnectionSearchCriteria {

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("status")
	private String status;

	@JsonProperty("ids")
	private List<String> ids;

	@JsonProperty("applicationNumber")
	private String applicationNumber;

	@JsonProperty("connectionNumber")
	private String connectionNumber;

	@JsonProperty("oldConnectionNumber")
	private String oldConnectionNumber;

	@JsonProperty("mobileNumber")
	private String mobileNumber;

	@JsonIgnore
	private String accountId;

	@JsonProperty("fromDate")
	private Long fromDate = null;

	@JsonProperty("toDate")
	private Long toDate = null;

	@JsonProperty("offset")
	private Integer offset;

	@JsonProperty("limit")
	private Integer limit;

	@JsonIgnore
	private List<String> ownerIds;

	public boolean isEmpty() {
		return (this.tenantId == null && this.status == null && this.ids == null && this.applicationNumber == null
				&& this.connectionNumber == null && this.oldConnectionNumber == null && this.mobileNumber == null
				&& this.fromDate == null && this.toDate == null && this.ownerIds == null);
	}

	public boolean tenantIdOnly() {
		return (this.tenantId != null && this.status == null && this.ids == null && this.applicationNumber == null
				&& this.connectionNumber == null && this.oldConnectionNumber == null && this.mobileNumber == null
				&& this.fromDate == null && this.toDate == null && this.ownerIds == null);
	}

}
