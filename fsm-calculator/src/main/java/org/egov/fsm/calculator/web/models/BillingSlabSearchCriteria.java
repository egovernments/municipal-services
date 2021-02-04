package org.egov.fsm.calculator.web.models;

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
public class BillingSlabSearchCriteria {
	
	@JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;
    
	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("ids")
	private List<String> ids;
	
	@JsonProperty("propertyType")
	private String propertyType;
	
	@JsonProperty("capacity")
	private Long capacity;
	
	@JsonProperty("sortBy")
    private SortBy sortBy;
    
    @JsonProperty("sortOrder")
    private SortOrder sortOrder;
    
    public enum SortOrder {
        ASC,
        DESC
    }

    public enum SortBy {
    	id,
    	propertyType,
        capacity
    }
	
}
