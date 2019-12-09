package org.egov.bpa.web.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BPASearchCriteria {

	 @JsonProperty("tenantId")
	    private String tenantId;

	    @JsonProperty("status")
	    private String status;

	    @JsonProperty("ids")
	    private List<String> ids;

	    @JsonProperty("applicationNo")
	    private List<String> applicationNo;

	    @JsonProperty("mobileNumber")
	    private String mobileNumber;

	    @JsonProperty("edcrNumber")
	    private List<String> edcrNumber;
	    
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
	        return (this.tenantId == null && this.status == null && this.ids == null && this.applicationNo == null
	                && this.mobileNumber == null &&
	                this.fromDate == null && this.toDate == null && this.ownerIds == null && this.edcrNumber == null
	                
	        );
	    }

	    public boolean tenantIdOnly() {
	        return (this.tenantId != null && this.status == null && this.ids == null && this.applicationNo == null
	                && this.mobileNumber == null &&
	                this.fromDate == null && this.toDate == null && this.ownerIds == null && this.edcrNumber == null
	        );
	    }
}
