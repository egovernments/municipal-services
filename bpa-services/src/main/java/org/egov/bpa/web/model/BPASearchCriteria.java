package org.egov.bpa.web.model;

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
public class BPASearchCriteria {

	 @JsonProperty("tenantId")
	    private String tenantId;

	    @JsonProperty("ids")
	    private List<String> ids;

	    @JsonProperty("status")
	    private String status;

	    @JsonProperty("edcrNumber")
	    private String edcrNumber;

	    @JsonProperty("applicationNo")
	    private String applicationNo;
	    	
	    @JsonProperty("approvalNo")
	    private String approvalNo;

	    @JsonProperty("mobileNumber")
	    private String mobileNumber;

	    @JsonProperty("landId")
	    private List<String> landId;

	    @JsonProperty("offset")
	    private Integer offset;

	    @JsonProperty("limit")
	    private Integer limit; 
	      
	    @JsonProperty("approvalDate")
	    private Long approvalDate;

	    @JsonProperty("applicationType")
	    private String applicationType;

	    @JsonProperty("serviceType")
	    private String serviceType;
	    
	    @JsonProperty("fromDate")
	    private Long fromDate;

	    @JsonProperty("toDate")
	    private Long toDate;
	    
	    @JsonIgnore
	    private List<String> ownerIds;

	    @JsonProperty("businessService")
	    private List<String> businessService;
	    
	    @JsonProperty("requestor")
	    private List<String> requestor;

		
	    public boolean isEmpty() {
	        return (this.tenantId == null && this.status == null && this.ids == null && this.applicationNo == null
	                && this.mobileNumber == null && this.landId == null && this.edcrNumber == null && this.approvalNo == null 
	                && this.applicationType == null && this.serviceType == null && this.approvalDate == null && this.ownerIds == null
	                && this.businessService == null && this.requestor == null
	                
	        );
	    }

	    public boolean tenantIdOnly() {
	        return (this.tenantId != null&& this.status == null  && this.ids == null && this.applicationNo == null
	                && this.mobileNumber == null && this.landId == null && this.edcrNumber == null && this.approvalNo == null 
	                && this.applicationType == null && this.serviceType == null && this.approvalDate == null && this.ownerIds == null
	                && this.businessService == null && this.requestor == null
	        );
	    }
}
