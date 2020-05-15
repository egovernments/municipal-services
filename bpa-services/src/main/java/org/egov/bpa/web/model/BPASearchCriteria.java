package org.egov.bpa.web.model;

import java.util.List;

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
	    private List<String> status;

	    @JsonProperty("edcrNumber")
	    private List<String> edcrNumbers;

	    @JsonProperty("applicationNo")
	    private List<String> applicationNo;
	    
	    @JsonProperty("approvalNo")
	    private List<String> approvalNo;

	    @JsonProperty("mobileNumber")
	    private String mobileNumber;

	    @JsonProperty("landId")
	    private String landId;

	    @JsonProperty("offset")
	    private Integer offset;

	    @JsonProperty("limit")
	    private Integer limit; 
	      
	    @JsonProperty("approvalDate")
	    private Long approvalDate;

		
	    public boolean isEmpty() {
	        return (this.tenantId == null && this.status == null && this.ids == null && this.applicationNo == null
	                && this.mobileNumber == null && this.landId == null && this.edcrNumbers == null && this.approvalNo == null 
	                && this.approvalDate == null
	                
	        );
	    }

	    public boolean tenantIdOnly() {
	        return (this.tenantId != null&& this.status == null  && this.ids == null && this.applicationNo == null
	                && this.mobileNumber == null && this.landId == null && this.edcrNumbers == null && this.approvalNo == null 
	                && this.approvalDate == null
	        );
	    }
}
