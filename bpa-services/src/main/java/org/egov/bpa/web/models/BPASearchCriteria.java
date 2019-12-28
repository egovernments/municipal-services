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

	    @JsonProperty("applicationNos")
	    private List<String> applicationNos;

	    @JsonProperty("mobileNumber")
	    private String mobileNumber;

	    @JsonProperty("edcrNumbers")
	    private List<String> edcrNumbers;
	    
	   


	    @JsonProperty("offset")
	    private Integer offset;

	    @JsonProperty("limit")
	    private Integer limit;

	    @JsonIgnore
	    private List<String> ownerIds;

	    @JsonProperty("fromDate")
	    private Long fromDate;

	    @JsonProperty("toDate")
	    private Long toDate;

	    public boolean isEmpty() {
	        return (this.tenantId == null && this.status == null && this.ids == null && this.applicationNos == null
	                && this.mobileNumber == null && this.edcrNumbers == null && this.fromDate == null && this.toDate == null
	                
	        );
	    }

	    public boolean tenantIdOnly() {
	        return (this.tenantId != null &&  this.ids == null && this.applicationNos == null
	                && this.mobileNumber == null && this.edcrNumbers == null && this.fromDate == null && this.toDate == null
	        );
	    }
}
