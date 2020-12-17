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

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean tenantIdOnly() {
		// TODO Auto-generated method stub
		return false;
	} 
}
