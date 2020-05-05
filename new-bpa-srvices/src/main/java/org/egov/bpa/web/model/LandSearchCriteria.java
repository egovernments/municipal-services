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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandSearchCriteria {


	 @JsonProperty("tenantId")
	    private String tenantId;

	    @JsonProperty("ids")
	    private List<String> ids;

	    @JsonProperty("applicationNos")
	    private String landUid;

	    @JsonProperty("mobileNumber")
	    private String mobileNumber;

		
	    public boolean isEmpty() {
	        return (this.tenantId == null && this.ids == null && this.landUid == null
	                && this.mobileNumber == null
	                
	        );
	    }

	    public boolean tenantIdOnly() {
	        return (this.tenantId != null &&  this.ids == null && this.landUid == null
	                && this.mobileNumber == null
	        );
	    }
}
