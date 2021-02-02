package org.egov.vehiclelog.web.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

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
public class VehicleLogSearchCriteria {
    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit; 
    
    @JsonProperty("fromDate")
    private Long fromDate; 
    
    @JsonProperty("toDate")
    private Long toDate;
    
    @JsonProperty("tenantId")
    private String tenantId; 
    
    @JsonProperty("ids")
    private List<String> ids;
    
    @JsonProperty("vehicleIds")
    private List<String> vehicleIds;
    
    @JsonProperty("dsoIds")
    private List<String> dsoIds;
    
    @JsonProperty("applicationStatus")
    private List<String> applicationStatus;   
    
    @JsonProperty("fsmIds")
    private List<String> fsmIds;   
    
    @JsonProperty("sortBy")
    private SortBy sortBy;
    
    @JsonProperty("sortOrder")
    private SortOrder sortOrder;
    
    public enum SortOrder {
        ASC,
        DESC
    }

    public enum SortBy {
    	applicationStatus,
        dso,
        vehicle,
        fsm,
        createdTime
    }
    
    public boolean isEmpty() {
		// TODO Auto-generated method stub
		 return (this.tenantId == null && this.offset == null && this.limit == null
				 && CollectionUtils.isEmpty(this.applicationStatus)  && this.fromDate == null && this.toDate == null && CollectionUtils.isEmpty(this.vehicleIds) && CollectionUtils.isEmpty(this.dsoIds) && CollectionUtils.isEmpty(this.ids) && CollectionUtils.isEmpty(this.fsmIds));
	}
    
}
