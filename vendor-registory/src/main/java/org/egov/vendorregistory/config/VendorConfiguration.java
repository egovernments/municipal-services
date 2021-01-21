package org.egov.vendorregistory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Component
public class VendorConfiguration {

	public String getSaveTopic() {
		return saveTopic;
	}

	public void setSaveTopic(String saveTopic) {
		this.saveTopic = saveTopic;
	}

	public String getMdmsHost() {
		return mdmsHost;
	}

	public void setMdmsHost(String mdmsHost) {
		this.mdmsHost = mdmsHost;
	}

	public String getMdmsEndPoint() {
		return mdmsEndPoint;
	}

	public void setMdmsEndPoint(String mdmsEndPoint) {
		this.mdmsEndPoint = mdmsEndPoint;
	}

	public String getOwnerHost() {
		return ownerHost;
	}

	public void setOwnerHost(String ownerHost) {
		this.ownerHost = ownerHost;
	}

	public String getOwnerContextPath() {
		return ownerContextPath;
	}

	public void setOwnerContextPath(String ownerContextPath) {
		this.ownerContextPath = ownerContextPath;
	}

	public String getAllowedEmployeeSearchParameters() {
		return allowedEmployeeSearchParameters;
	}

	public void setAllowedEmployeeSearchParameters(String allowedEmployeeSearchParameters) {
		this.allowedEmployeeSearchParameters = allowedEmployeeSearchParameters;
	}

	public String getAllowedVendorSearchParameters() {
		return allowedVendorSearchParameters;
	}

	public void setAllowedVendorSearchParameters(String allowedVendorSearchParameters) {
		this.allowedVendorSearchParameters = allowedVendorSearchParameters;
	}

	public String getOwnerCreateEndpoint() {
		return ownerCreateEndpoint;
	}

	public void setOwnerCreateEndpoint(String ownerCreateEndpoint) {
		this.ownerCreateEndpoint = ownerCreateEndpoint;
	}

	public String getOwnerSearchEndpoint() {
		return ownerSearchEndpoint;
	}

	public void setOwnerSearchEndpoint(String ownerSearchEndpoint) {
		this.ownerSearchEndpoint = ownerSearchEndpoint;
	}

	public String getOwnerUpdateEndpoint() {
		return ownerUpdateEndpoint;
	}

	public void setOwnerUpdateEndpoint(String ownerUpdateEndpoint) {
		this.ownerUpdateEndpoint = ownerUpdateEndpoint;
	}

	public String getOwnernamePrefix() {
		return ownernamePrefix;
	}

	public void setOwnernamePrefix(String ownernamePrefix) {
		this.ownernamePrefix = ownernamePrefix;
	}

	public String getLocationHost() {
		return locationHost;
	}

	public void setLocationHost(String locationHost) {
		this.locationHost = locationHost;
	}

	public String getLocationContextPath() {
		return locationContextPath;
	}

	public void setLocationContextPath(String locationContextPath) {
		this.locationContextPath = locationContextPath;
	}

	public String getLocationEndpoint() {
		return locationEndpoint;
	}

	public void setLocationEndpoint(String locationEndpoint) {
		this.locationEndpoint = locationEndpoint;
	}

	public String getHierarchyTypeCode() {
		return hierarchyTypeCode;
	}

	public void setHierarchyTypeCode(String hierarchyTypeCode) {
		this.hierarchyTypeCode = hierarchyTypeCode;
	}

	public Integer getDefaultLimit() {
		return defaultLimit;
	}

	public void setDefaultLimit(Integer defaultLimit) {
		this.defaultLimit = defaultLimit;
	}

	public Integer getDefaultOffset() {
		return defaultOffset;
	}

	public void setDefaultOffset(Integer defaultOffset) {
		this.defaultOffset = defaultOffset;
	}

	public Integer getMaxSearchLimit() {
		return maxSearchLimit;
	}

	public void setMaxSearchLimit(Integer maxSearchLimit) {
		this.maxSearchLimit = maxSearchLimit;
	}

	// Persister Config
	@Value("${persister.save.vendor-registory.topic}")
	private String saveTopic;

	// MDMS
	//@Value("${egov.mdms.host}")
	private String mdmsHost;

	//@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndPoint;

	// Owner Configuration
	@Value("${egov.owner.host}")
	private String ownerHost;

	@Value("${egov.owner.context.path}")    
	private String ownerContextPath;
	
	
	
	//@Value("${citizen.allowed.search.params}")
	//private String allowedCitizenSearchParameters;

	// Serch Parameters and configured in appliction.prop file
	@Value("${employee.allowed.search.params}")
	private String allowedEmployeeSearchParameters;
	
	@Value("vendor.allowed.search.params")
	private String allowedVendorSearchParameters;
	
	

	@Value("${egov.owner.create.path}")
	private String ownerCreateEndpoint;

	@Value("${egov.owner.search.path}")
	private String ownerSearchEndpoint;

	@Value("${egov.owner.update.path}")
	private String ownerUpdateEndpoint;

	// @Value("${egov.owner.ownername.prefix}")
	private String ownernamePrefix;

	@Value("${egov.location.host}")
	private String locationHost;

	@Value("${egov.location.context.path}")
	private String locationContextPath;

	@Value("${egov.location.endpoint}")
	private String locationEndpoint;

	@Value("${egov.location.hierarchyTypeCode}")
	private String hierarchyTypeCode;

	@Value("${egov.owner.username.prefix}")
	private String usernamePrefix;
	
	

	@Value("${egov.vendorregistory.default.limit}")
	private Integer defaultLimit;

	@Value("${egov.vendorregistory.default.offset}")
	private Integer defaultOffset;

	@Value("${egov.vendorregistory.max.limit}")
	private Integer maxSearchLimit;
}
