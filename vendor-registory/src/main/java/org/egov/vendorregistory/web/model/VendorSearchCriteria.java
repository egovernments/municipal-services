package org.egov.vendorregistory.web.model;

import java.util.List;
import org.springframework.util.CollectionUtils;
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
public class VendorSearchCriteria {
	@JsonProperty("offset")
	private Integer offset;

	@JsonProperty("limit")
	private Integer limit;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("mobileNumber")
	private String mobileNumber;

	@JsonProperty("ownerIds")
	private List<String> ownerIds;

	@JsonProperty("ownerName")
	private List<String> ownerName;

	@JsonProperty("applicationNumber")
	private List<String> applicationNumber;

	@JsonProperty("ids")
	private List<String> ids;

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public List<String> getOwnerIds() {
		return ownerIds;
	}

	public void setOwnerIds(List<String> ownerIds) {
		this.ownerIds = ownerIds;
	}

	public List<String> getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(List<String> ownerName) {
		this.ownerName = ownerName;
	}

	public List<String> getApplicationNumber() {
		return applicationNumber;
	}

	public void setApplicationNumber(List<String> applicationNumber) {
		this.applicationNumber = applicationNumber;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return (this.tenantId == null && this.offset == null && this.limit == null && this.mobileNumber == null
				&& this.ownerIds == null && CollectionUtils.isEmpty(this.ownerName)
				&& CollectionUtils.isEmpty(this.ids));
	}

	public boolean tenantIdOnly() {
		// TODO Auto-generated method stub
		return (this.tenantId != null && this.mobileNumber == null && this.ownerIds == null
				&& CollectionUtils.isEmpty(this.ownerName) && CollectionUtils.isEmpty(this.ids));
	}
}
