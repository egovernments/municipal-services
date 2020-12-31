package org.egov.fsm.web.model;

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
public class FSMSearchCriteria {
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
    
    @JsonProperty("fromDate")
    private Long fromDate;
    
    @JsonProperty("toDate")
    private Long toDate;
    
    @JsonProperty("applicationNumber")
    private List<String> applicationNumber;
    
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		 return (this.tenantId == null && this.offset == null && this.limit == null && this.mobileNumber == null
                && this.ownerIds == null && this.fromDate == null && this.toDate == null && this.applicationNumber == null);
	}

	public boolean tenantIdOnly() {
		// TODO Auto-generated method stub
		return (this.tenantId != null && this.offset == null && this.limit == null && this.mobileNumber == null
                && this.ownerIds == null && this.fromDate == null && this.toDate == null && this.applicationNumber == null);
	} 
}
